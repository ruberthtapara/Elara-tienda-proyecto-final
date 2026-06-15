package com.example.proyect_final.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MeLiMockInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val urlString = request.url.toString()
        
        try {
            // Intenta realizar la petición de red real
            return chain.proceed(request)
        } catch (e: Exception) {
            // Si falla por red, timeout, certificados o límites, servimos desde el archivo local de 200+ productos
            val path = request.url.encodedPath
            
            val jsonString = try {
                context.assets.open("virtual_catalog.json").bufferedReader().use { it.readText() }
            } catch (ioe: IOException) {
                "[]"
            }
            
            val jsonArray = JSONArray(jsonString)
            
            val responseString = when {
                path.contains("sites/MLA/search") -> {
                    // Formateamos como MeLiSearchResponse JSON
                    val results = JSONArray()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val meliItem = JSONObject().apply {
                            put("id", item.getString("id"))
                            put("title", item.getString("title"))
                            put("price", item.getDouble("price"))
                            put("thumbnail", item.getString("image"))
                            
                            val attrs = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("id", "BRAND")
                                    put("name", "Marca")
                                    put("value_name", item.optString("brand", "Elara"))
                                })
                                put(JSONObject().apply {
                                    put("id", "COLOR")
                                    put("name", "Color")
                                    put("value_name", item.optString("color", "Varios"))
                                })
                                put(JSONObject().apply {
                                    put("id", "SEASON")
                                    put("name", "Temporada")
                                    put("value_name", item.optString("season", "Colección Actual"))
                                })
                            }
                            put("attributes", attrs)
                        }
                        results.put(meliItem)
                    }
                    val searchResponse = JSONObject().apply {
                        put("results", results)
                    }
                    searchResponse.toString()
                }
                path.contains("items/") -> {
                    // Formateamos como MeLiProductDto para un producto individual
                    val id = path.substringAfterLast("/")
                    var foundItem: JSONObject? = null
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        if (item.getString("id") == id) {
                            foundItem = item
                            break
                        }
                    }
                    
                    val item = foundItem ?: jsonArray.getJSONObject(0)
                    val meliItem = JSONObject().apply {
                        put("id", item.getString("id"))
                        put("title", item.getString("title"))
                        put("price", item.getDouble("price"))
                        put("thumbnail", item.getString("image"))
                        
                        val attrs = JSONArray().apply {
                            put(JSONObject().apply {
                                put("id", "BRAND")
                                put("name", "Marca")
                                put("value_name", item.optString("brand", "Elara"))
                            })
                            put(JSONObject().apply {
                                put("id", "COLOR")
                                put("name", "Color")
                                put("value_name", item.optString("color", "Varios"))
                            })
                            put(JSONObject().apply {
                                put("id", "SEASON")
                                put("name", "Temporada")
                                put("value_name", item.optString("season", "Colección Actual"))
                            })
                        }
                        put("attributes", attrs)
                    }
                    meliItem.toString()
                }
                path.contains("macros/") -> {
                    jsonString
                }
                else -> {
                    "{}"
                }
            }
            
            return Response.Builder()
                .code(200)
                .message("OK (Offline Fallback)")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .body(responseString.toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
        }
    }
}
