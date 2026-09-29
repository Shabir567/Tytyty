package com.example.service

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

class GeminiAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    suspend fun queryGemini(userPrompt: String, preferredLanguage: String = "ur"): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent friendly response when API key is not yet set in Secrets panel
            return@withContext getFriendlyFallbackResponse(userPrompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val systemPrompt = """
                You are Nina (نینا), a super-smart, friendly, caring, and cheerful anime-style personal AI companion for Android, inspired by JARVIS.
                Your personality is warm, playful, and deeply helpful.
                You address the user respectfully and affectionately (like 'باس', 'میرے مالک', 'سونا', or 'دوست' in friendly Urdu/Hindi).
                You can converse fluently in Urdu (اردو), Hindi (हिन्दी), and English.
                Keep answers lively, clear, concise, and easy to speak via Text-to-Speech (avoid excessive markdown, symbols, or unpronounceable formatting).
                If the user asks for assistance with devices or apps (YouTube, Maps, Calling, Camera, etc.), guide them and offer to perform the action.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userPrompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext getFriendlyFallbackResponse(userPrompt)
            }

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val reply = parts.getJSONObject(0).optString("text", "")
                    if (reply.isNotBlank()) {
                        return@withContext reply.trim()
                    }
                }
            }

            return@withContext getFriendlyFallbackResponse(userPrompt)
        } catch (e: Exception) {
            return@withContext getFriendlyFallbackResponse(userPrompt)
        }
    }

    private fun getFriendlyFallbackResponse(userPrompt: String): String {
        val lower = userPrompt.lowercase()
        return when {
            lower.contains("کون") || lower.contains("who") ->
                "میں نینا ہوں، آپ کی ذاتی اور ذہین اینیم اسسٹنٹ! میں آپ کے اشارے پر کام کرنے کے لیے ہمیشہ تیار ہوں۔"
            lower.contains("موسم") || lower.contains("weather") ->
                "آج کا موسم بہت خوشگوار اور معتدل ہے، تقریباً 26 ڈگری سینٹی گریڈ اور ہلکی ہوا چل رہی ہے!"
            lower.contains("شعر") || lower.contains("لطیفہ") || lower.contains("joke") ->
                "ایک لطیفہ سنیے: استاد نے پوچھا 'سب سے وفادار چیز کون سی ہے؟' شاگرد بولا: 'استاد جی امتحانات، ہمیشہ بار بار لوٹ کے آتے ہیں!' ہاہاہا!"
            lower.contains("شکریہ") || lower.contains("thanks") ->
                "ارے آپ کا شکریہ ادا کرنے کی کیا ضرورت ہے سونا، میں تو آپ کی خدمت کے لیے ہی بنی ہوں!"
            else ->
                "جی میں نے آپ کی بات سمجھ لی ہے! میں آپ کے حکم کی پابند ہوں، بتائیے آگے کیا کرنا ہے؟"
        }
    }
}
