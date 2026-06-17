package com.example.proyect_final

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.proyect_final.data.local.StyleGenDatabase
import com.example.proyect_final.data.remote.ElaraApiService
import com.example.proyect_final.data.repository.StyleAdvisorRepositoryImpl
import com.example.proyect_final.domain.repository.ProductRepository
import com.example.proyect_final.domain.repository.StyleAdvisorRepository
import com.example.proyect_final.data.repository.AuthRepositoryImpl
import com.example.proyect_final.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import okhttp3.OkHttpClient

import com.example.proyect_final.data.repository.FirebaseProductRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import com.google.ai.client.generativeai.GenerativeModel
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp. OkHttpNetworkFetcherFactory

class AppContainer(private val context: Context) {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val database: StyleGenDatabase by lazy {
        Room.databaseBuilder(
            context,
            StyleGenDatabase::class.java,
            "stylegen_database"
        ).build()
    }

    private val apiService: ElaraApiService by lazy {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val client = OkHttpClient.Builder()
            .addInterceptor(com.example.proyect_final.data.remote.MeLiMockInterceptor(context))
            .build()

        Retrofit.Builder()
            .baseUrl("https://script.google.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ElaraApiService::class.java)
    }

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    val productRepository: ProductRepository by lazy {
        FirebaseProductRepositoryImpl(firestore)
    }

    val styleAdvisorRepository: StyleAdvisorRepository by lazy {
        StyleAdvisorRepositoryImpl(database.dao, context, generativeModel)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(FirebaseAuth.getInstance(), context)
    }

    val userProfileRepository: com.example.proyect_final.domain.repository.UserProfileRepository by lazy {
        com.example.proyect_final.data.repository.UserProfileRepositoryImpl(context)
    }
}

class StyleGenApplication : Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        container = AppContainer(this)
    }

    override fun newImageLoader(platformContext: coil3.PlatformContext): ImageLoader {
        return ImageLoader.Builder(platformContext)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
}
