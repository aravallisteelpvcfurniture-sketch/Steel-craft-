package com.example.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResult<out T> {
    data class Success<T>(val data: T) : GeminiResult<T>()
    data class Error(val message: String) : GeminiResult<Nothing>()
}

data class GeneratedImageResult(
    val bitmap: Bitmap?,
    val textDescription: String,
    val size: String
)

data class MapsGroundingResult(
    val replyText: String,
    val placesFound: List<String> = emptyList()
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private var inMemoryApiKey: String? = null

    fun setCustomApiKey(context: android.content.Context, key: String) {
        val trimmed = key.trim()
        inMemoryApiKey = trimmed
        val prefs = context.getSharedPreferences("steelcraft_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("gemini_api_key", trimmed).apply()
    }

    fun getApiKey(context: android.content.Context? = null): String {
        if (!inMemoryApiKey.isNullOrBlank()) return inMemoryApiKey!!
        if (context != null) {
            val prefs = context.getSharedPreferences("steelcraft_prefs", android.content.Context.MODE_PRIVATE)
            val saved = prefs.getString("gemini_api_key", "")
            if (!saved.isNullOrBlank()) {
                inMemoryApiKey = saved
                return saved
            }
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        if (context != null) {
            try {
                val resId = context.resources.getIdentifier("google_api_key", "string", context.packageName)
                if (resId != 0) {
                    val key = context.getString(resId)
                    if (key.isNotBlank()) return key
                }
            } catch (_: Exception) {}
        }
        return ""
    }

    fun isKeyConfigured(context: android.content.Context? = null): Boolean {
        return getApiKey(context).isNotBlank()
    }

    /**
     * Maps Grounding query using gemini-3.8-flash with googleMaps tool
     */
    suspend fun queryWithMapsGrounding(
        prompt: String,
        locationContext: String = ""
    ): GeminiResult<MapsGroundingResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext GeminiResult.Error("Gemini API key is not configured. Please set GEMINI_API_KEY in AI Studio Secrets or enter it in AI Key Settings.")
        }

        val model = "gemini-3.8-flash"
        val fullPrompt = if (locationContext.isNotBlank()) {
            "$prompt (Location context: $locationContext)"
        } else {
            prompt
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", fullPrompt))
                        })
                    })
                })
                put("tools", JSONArray().apply {
                    put(JSONObject().put("googleMaps", JSONObject()))
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$model:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Maps Grounding failed HTTP ${response.code}: $responseBody")
                return@withContext GeminiResult.Error("API Error ${response.code}: $responseBody")
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiResult.Error("No response received from Gemini.")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        textBuilder.append(p.getString("text"))
                    }
                }
            }

            val places = mutableListOf<String>()
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val webOrMaps = chunk.optJSONObject("maps") ?: chunk.optJSONObject("web")
                        val title = webOrMaps?.optString("title") ?: chunk.optString("title")
                        if (title.isNotBlank()) {
                            places.add(title)
                        }
                    }
                }
            }

            GeminiResult.Success(
                MapsGroundingResult(
                    replyText = textBuilder.toString().ifBlank { "Results found based on Google Maps data." },
                    placesFound = places
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in queryWithMapsGrounding", e)
            GeminiResult.Error(e.localizedMessage ?: "Network error querying Gemini")
        }
    }

    /**
     * Image Generation using gemini-3-pro-image-preview with 1K, 2K, 4K size affordance
     */
    suspend fun generateDesignImage(
        prompt: String,
        imageSize: String = "1K" // "1K", "2K", "4K"
    ): GeminiResult<GeneratedImageResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext GeminiResult.Error("Gemini API key is not configured. Please set GEMINI_API_KEY in AI Studio Secrets or enter it in AI Key Settings.")
        }

        val model = "gemini-3-pro-image-preview"
        val normalizedSize = when (imageSize.uppercase()) {
            "4K" -> "4K"
            "2K" -> "2K"
            else -> "1K"
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", "1:1")
                        put("imageSize", normalizedSize)
                    })
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$model:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Image Gen failed HTTP ${response.code}: $responseBody")
                return@withContext GeminiResult.Error("Failed to generate image (${response.code}): $responseBody")
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiResult.Error("No candidates returned for image generation.")
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var bitmap: Bitmap? = null
            var description = ""

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("text")) {
                        description += part.getString("text") + "\n"
                    }
                    if (part.has("inlineData")) {
                        val inlineData = part.getJSONObject("inlineData")
                        val base64Data = inlineData.optString("data", "")
                        if (base64Data.isNotBlank()) {
                            val decoded = Base64.decode(base64Data, Base64.DEFAULT)
                            bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                        }
                    }
                }
            }

            GeminiResult.Success(
                GeneratedImageResult(
                    bitmap = bitmap,
                    textDescription = description.trim().ifBlank { "Design rendered successfully in $normalizedSize resolution." },
                    size = normalizedSize
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateDesignImage", e)
            GeminiResult.Error(e.localizedMessage ?: "Failed to generate design image")
        }
    }

    /**
     * Fast assistant for calculation tips, quotation suggestions, and fabrication estimation
     * Uses gemini-3.8-flash or gemini-3.1-flash-lite-preview
     */
    suspend fun getFabricationAdvice(
        userQuery: String,
        isFast: Boolean = true
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext GeminiResult.Error("Gemini API key is not configured. Please set GEMINI_API_KEY in AI Studio Secrets or enter it in AI Key Settings.")
        }

        val model = if (isFast) "gemini-3.1-flash-lite-preview" else "gemini-3.8-flash"
        val systemPrompt = "You are an expert fabrication engineer, contractor and interior consultant specializing in Indian construction, stainless steel 304/316 railings, Jindal aluminium sections, UPVC doors & windows, wooden balcony decks, and modular furniture. Provide precise measurements, cost estimates in INR (₹), material grade recommendations, and clear Hindi/English advice."

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userQuery))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$model:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext GeminiResult.Error("API Error ${response.code}: $responseBody")
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            GeminiResult.Success(text ?: "No response generated.")
        } catch (e: Exception) {
            Log.e(TAG, "Error in getFabricationAdvice", e)
            GeminiResult.Error(e.localizedMessage ?: "Failed to fetch AI advice")
        }
    }
}
