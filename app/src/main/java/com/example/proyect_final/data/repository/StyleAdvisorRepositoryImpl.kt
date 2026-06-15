package com.example.proyect_final.data.repository

import android.graphics.Bitmap
import com.example.proyect_final.BuildConfig
import com.example.proyect_final.data.local.StyleAdviceEntity
import com.example.proyect_final.data.local.StyleGenDao
import com.example.proyect_final.domain.repository.StyleAdvisorRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.content.Context
import com.example.proyect_final.util.ImageUtils
import com.example.proyect_final.domain.repository.StyleAdviceResult
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.google.ai.client.generativeai.type.generationConfig

class StyleAdvisorRepositoryImpl(
    private val dao: StyleGenDao,
    private val context: Context,
    private val generativeModel: GenerativeModel
) : StyleAdvisorRepository {

    override suspend fun getStyleAdvice(
        shopProductImage: String,
        userProductImage: Bitmap,
        isAccessory: Boolean
    ): Result<StyleAdviceResult> = withContext(Dispatchers.IO) {
        try {
            if (BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY.contains("tu_clave")) {
                throw Exception("La API Key de Gemini no está configurada. Por favor, añádela en local.properties.")
            }

            // ⚡ Reducir la imagen del usuario a 512px para que se suba y analice ultra rápido
            val scaledUserBitmap = ImageUtils.scaleBitmap(userProductImage, 512)

            // Guardar la imagen localmente antes de consultar a Gemini
            val savedPath = ImageUtils.saveBitmap(context, scaledUserBitmap) ?: ""

            // ⚡ Cargar la imagen del producto de la tienda usando Coil 3
            val shopBitmap = try {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(shopProductImage)
                    .build()
                val result = imageLoader.execute(request)
                // Escalar también la imagen de la tienda para optimizar memoria y tiempo de red
                result.image?.toBitmap()?.let { ImageUtils.scaleBitmap(it, 512) }
            } catch (e: Exception) {
                android.util.Log.e("StyleAdvisor", "No se pudo cargar la imagen de la tienda desde Coil", e)
                null
            }

            // 🧠 Prompt detallado para ropa o prompt especializado para accesorios (complementos)
            val prompt = if (isAccessory) {
                "Eres un asesor de imagen amigable, experto en complementos y accesorios de moda.\n" +
                "Analiza de forma detallada y sincera cómo este accesorio de la tienda complementa la prenda/outfit del usuario:\n" +
                "1. El accesorio de la tienda ($shopProductImage).\n" +
                "2. La prenda u outfit del usuario.\n\n" +
                "Instrucciones de análisis:\n" +
                "- Explica en detalle y con palabras sencillas cómo este accesorio aporta estilo, contraste o armonía visual al outfit del usuario.\n" +
                "- Sé honesto: si el accesorio no encaja con los colores o el estilo del outfit, dilo con naturalidad y califica como [Regular] o [Mala]. Solo califica como [Buena] si realmente realza el outfit.\n\n" +
                "Escribe en español de forma directa, estructurando exactamente así:\n" +
                "Análisis: [Veredicto claro de cómo complementa el outfit en palabras sencillas, máx 15 palabras]\n" +
                "• Colores: [Cómo contrastan o armonizan sus colores con el outfit en el día a día, máx 15 palabras]\n" +
                "• Estilo: [Para qué ocasión es ideal este complemento con ese outfit, máx 15 palabras]\n" +
                "[Calificación]\n\n" +
                "Donde [Calificación] sea estrictamente [Buena], [Regular] o [Mala] (sin corchetes)."
            } else {
                "Eres un asesor de imagen amigable, honesto y muy detallista.\n" +
                "Analiza de forma detallada y sincera cómo combinan estas dos prendas:\n" +
                "1. La prenda de la tienda ($shopProductImage).\n" +
                "2. La prenda del usuario.\n\n" +
                "Instrucciones de análisis:\n" +
                "- Explica en detalle y con palabras sencillas (sin tecnicismos aburridos) cómo se ven juntas en la vida real en cuanto a colores y estilo.\n" +
                "- Sé honesto: si la combinación no se ve bien o los estilos chocan, dilo con naturalidad y califica como [Regular] o [Mala]. Solo califica como [Buena] si se ven geniales juntas.\n\n" +
                "Escribe en español de forma directa, estructurando exactamente así:\n" +
                "Análisis: [Veredicto claro y detallado en palabras sencillas, máx 15 palabras]\n" +
                "• Colores: [Cómo contrastan o combinan sus colores en el día a día, máx 15 palabras]\n" +
                "• Estilo: [Para qué ocasión sirve esta combinación y si encaja su estilo, máx 15 palabras]\n" +
                "[Calificación]\n\n" +
                "Donde [Calificación] sea estrictamente [Buena], [Regular] o [Mala] (sin corchetes)."
            }

            // Configuración de la generación de la IA para limitar tokens (aumenta 2x la velocidad de respuesta)
            val genConfig = generationConfig {
                maxOutputTokens = 120
                temperature = 0.4f
            }

            var retryCount = 0
            val maxRetries = 3
            var delayMs = 1000L
            var responseText: String? = null
            var lastException: Exception? = null

            while (retryCount < maxRetries && responseText == null) {
                try {
                    val currentModel = if (retryCount >= 2) {
                        GenerativeModel(
                            modelName = "gemini-3.1-flash-lite",
                            apiKey = BuildConfig.GEMINI_API_KEY,
                            generationConfig = genConfig
                        )
                    } else {
                        // Creamos una copia del modelo primario con nuestro generationConfig optimizado
                        GenerativeModel(
                            modelName = generativeModel.modelName,
                            apiKey = BuildConfig.GEMINI_API_KEY,
                            generationConfig = genConfig
                        )
                    }

                    android.util.Log.d("StyleAdvisor", "Consultando modelo ${currentModel.modelName} (Intento ${retryCount + 1}/$maxRetries)")
                    val response = currentModel.generateContent(
                        content {
                            if (shopBitmap != null) {
                                image(shopBitmap)
                            }
                            image(scaledUserBitmap)
                            text(prompt)
                        }
                    )
                    responseText = response.text
                } catch (e: Exception) {
                    lastException = e
                    retryCount++
                    if (retryCount < maxRetries) {
                        android.util.Log.w("StyleAdvisor", "Intento ${retryCount} falló. Reintentando en ${delayMs}ms...", e)
                        kotlinx.coroutines.delay(delayMs)
                        delayMs *= 2
                    }
                }
            }

            if (responseText == null) {
                throw lastException ?: Exception("Error al obtener respuesta de Gemini tras varios intentos.")
            }

            Result.success(StyleAdviceResult(responseText, savedPath))
        } catch (e: Exception) {
            android.util.Log.e("StyleAdvisor", "Error en getStyleAdvice al consultar Gemini", e)
            Result.failure(e)
        }
    }

    // Método auxiliar para obtener el path de la última imagen guardada (opcional, o podemos cambiar el retorno de getStyleAdvice)
    // Para ser más limpios, vamos a cambiar el retorno de getStyleAdvice para que incluya el path.


    override fun getAllAdvice(): Flow<List<StyleAdviceEntity>> = dao.getAllAdvice()

    override suspend fun saveAdvice(advice: StyleAdviceEntity) {
        dao.insertAdvice(advice)
    }
}