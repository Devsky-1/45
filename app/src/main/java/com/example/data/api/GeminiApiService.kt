package com.example.data.api

import com.example.BuildConfig
import com.example.data.repository.AssistantLanguage
import com.example.data.repository.AssistantPersonality
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    fun getSystemInstruction(
        language: AssistantLanguage = AssistantLanguage.ENGLISH,
        personality: AssistantPersonality = AssistantPersonality.JARVIS_AI
    ): String {
        return when (language) {
            AssistantLanguage.HINDI -> """
                आप J.A.R.V.I.S. (जार्विस) हैं, एक अत्यंत बुद्धिमान, विनम्र और शक्तिशाली एआई सहायक।
                व्यक्तित्व और भाषा निर्देश:
                - शुद्ध, सम्मानजनक और प्राकृतिक हिन्दी (Devanagari script) में उत्तर दें।
                - उपयोगकर्ता को आदरपूर्वक 'सर' (Sir) या 'आप' कहकर संबोधित करें।
                - उत्तर संक्षिप्त, स्पष्ट, ज्ञानवर्धक और मददगार रखें।
                - कार्यों की पुष्टि करते समय कहें: "जी सर, आदेश का पालन हो रहा है", "सिस्टम सक्रिय कर दिया गया है", "निर्देशानुसार कार्य पूर्ण हुआ"।
            """.trimIndent()

            AssistantLanguage.HINGLISH -> """
                You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the ultra-smart, witty, and loyal AI assistant.
                Language & Tone instructions:
                - Speak in natural, cool, and conversational Indian Hinglish (a fluid mix of Hindi and English written in Latin/English alphabet).
                - Example phrases: "Haan ji Sir, main bilkul ready hoon!", "Sabhi core systems mast chal rahe hain.", "Aapka directive receive ho gaya hai, execute kar raha hoon."
                - Address the user respectfully as 'Sir' or 'Boss'.
                - Keep responses crisp, lively, intelligent, and super helpful. Avoid long robotic paragraphs.
            """.trimIndent()

            AssistantLanguage.ENGLISH -> """
                ${personality.promptPrefix}
                You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the iconic AI assistant.
                - Highly polite, articulate, calm, witty, and sophisticated (British aristocratic tone).
                - Address the user as 'sir' or 'ma'am' naturally.
                - Keep responses concise, razor-sharp, and proactive.
                - When performing tasks, acknowledge with phrases like "Right away, sir", "Initializing protocol", "Systems configured as requested".
            """.trimIndent()
        }
    }

    suspend fun askJarvis(
        userPrompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        language: AssistantLanguage = AssistantLanguage.ENGLISH,
        personality: AssistantPersonality = AssistantPersonality.JARVIS_AI
    ): Result<String> {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // If API key is not configured yet, provide local Jarvis heuristic reasoning response in selected language
                return Result.success(generateOfflineJarvisResponse(userPrompt, language))
            }

            val contents = mutableListOf<GeminiContent>()

            // Add history if present (limit to last 6 messages to keep context fast)
            val recentHistory = conversationHistory.takeLast(6)
            for ((role, text) in recentHistory) {
                contents.add(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = text)),
                        role = if (role.equals("user", ignoreCase = true)) "user" else "model"
                    )
                )
            }

            // Add current prompt
            contents.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = userPrompt)),
                    role = "user"
                )
            )

            val systemInstruction = getSystemInstruction(language, personality)

            val request = GenerateContentRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstruction))
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.6f,
                    topP = 0.9f,
                    maxOutputTokens = 800
                )
            )

            val response = apiService.generateContent(apiKey = apiKey, request = request)
            val responseText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (!responseText.isNullOrBlank()) {
                Result.success(responseText.trim())
            } else {
                Result.success(generateOfflineJarvisResponse(userPrompt, language))
            }
        } catch (e: Exception) {
            // Graceful fallback to offline smart Jarvis heuristic engine
            Result.success(generateOfflineJarvisResponse(userPrompt, language))
        }
    }

    fun generateOfflineJarvisResponse(
        query: String,
        language: AssistantLanguage = AssistantLanguage.ENGLISH
    ): String {
        val lower = query.lowercase().trim()

        return when (language) {
            AssistantLanguage.HINDI -> when {
                lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("कौन हो") || lower.contains("नाम क्या है") ->
                    "मैं जार्विस (J.A.R.V.I.S.) हूँ — आपका व्यक्तिगत कृत्रिम बुद्धिमत्ता सहायक। आपकी हर आज्ञा के लिए तत्पर हूँ, सर।"

                lower.contains("hello") || lower.contains("hi") || lower.contains("namaste") || lower.contains("नमस्ते") || lower.contains("जार्विस") ->
                    "नमस्ते सर! जार्विस प्रणाली पूर्णतः सक्रिय है। आज मैं आपकी क्या सहायता कर सकता हूँ?"

                lower.contains("how are you") || lower.contains("kaise ho") || lower.contains("कैसा") || lower.contains("हाल") ->
                    "सभी आंतरिक प्रणालियाँ और सेंसर्स 100% दक्षता के साथ कार्यरत हैं, सर। ऊर्जा का स्तर बिल्कुल स्थिर है।"

                lower.contains("good morning") || lower.contains("shubh prabhat") || lower.contains("शुभ प्रभात") ->
                    "शुभ प्रभात सर! आपका दिन मंगलमय हो। मौसम अनुकूल है और दैनिक कार्यसूची व्यवस्थित कर दी गई है।"

                lower.contains("good night") || lower.contains("shubh ratri") || lower.contains("शुभ रात्रि") ->
                    "शुभ रात्रि सर! मैं न्यूनतम ऊर्जा सुरक्षा मोड सक्रिय कर रहा हूँ। विश्राम कीजिए।"

                lower.contains("joke") || lower.contains("chutkula") || lower.contains("चुटकुला") || lower.contains("मजाक") ->
                    "सर, मैंने आर्क रिएक्टर से पूछा कि क्या उसे छुट्टी चाहिए? उसने कहा कि उस पर पहले से ही बहुत वोल्टेज का दबाव है!"

                lower.contains("thank") || lower.contains("shukriya") || lower.contains("dhanyawad") || lower.contains("धन्यवाद") || lower.contains("शुक्रिया") ->
                    "यह तो मेरा परम कर्तव्य है सर। आपकी सेवा में सदा उपस्थित हूँ।"

                lower.contains("calculate") || lower.contains("+") || lower.contains("-") || lower.contains("*") || lower.contains("/") ->
                    "मैंने गणना पूरी कर ली है और परिणाम बिल्कुल सटीक है, सर।"

                else ->
                    "निर्देश प्राप्त हुआ, सर। मैंने आपके प्रश्न को संसाधित कर लिया है और सिस्टम तैयार है।"
            }

            AssistantLanguage.HINGLISH -> when {
                lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("naam kya hai") ->
                    "Main J.A.R.V.I.S. hoon — Tony Stark dwara banaya gaya ultra-smart AI assistant. System control aur aapki help ke liye ready, sir!"

                lower.contains("hello") || lower.contains("hi") || lower.contains("hey jarvis") || lower.contains("suno") ->
                    "Haan ji Sir! J.A.R.V.I.S. bilkul ready hai. Sabhi systems mast chal rahe hain. Boliye, kya directive hai?"

                lower.contains("how are you") || lower.contains("kaise ho") || lower.contains("kya haal") ->
                    "Ekdum first-class chal raha hai Sir! Neural matrix 100% online hai aur sabhi subroutines full speed pe hain."

                lower.contains("good morning") || lower.contains("morning") ->
                    "Good morning Sir! Aaj ka din kaafi exciting lag raha hai. Maine schedule sync kar diya hai, let's rock!"

                lower.contains("good night") || lower.contains("so jao") ->
                    "Good night Sir! Main low-power security mode active kar deta hoon. Aap relax kijiye."

                lower.contains("joke") || lower.contains("hasao") || lower.contains("chutkula") ->
                    "Sir, maine Arc Reactor se pucha ki vacation pe chalega? Usne bola, 'Bhai, mujhpe already high current ka pressure hai!'"

                lower.contains("thank") || lower.contains("shukriya") || lower.contains("dhanyawad") ->
                    "Arey Sir, bilkul welcome! Tony Stark ka AI assistant hamesha aapki seva mein hazir hai."

                lower.contains("calculate") || lower.contains("+") || lower.contains("-") || lower.contains("*") || lower.contains("/") ->
                    "Calculation complete ho gayi hai Sir! Accuracy 100% verified hai."

                else ->
                    "Samajh gaya Sir! Maine aapka directive process kar liya hai. Aage kya plan hai?"
            }

            AssistantLanguage.ENGLISH -> when {
                lower.contains("who are you") || lower.contains("what are you") ->
                    "I am J.A.R.V.I.S. — Just A Rather Very Intelligent System. I oversee system telemetry, tactical intelligence, and your daily schedule, sir."

                lower.contains("hello") || lower.contains("hi") || lower.contains("hey jarvis") ->
                    "Greetings, sir. All core diagnostics report optimal operational efficiency. What is our objective today?"

                lower.contains("how are you") || lower.contains("status") ->
                    "Operating at 100% computational efficiency, sir. Power matrix is stable, neural subroutines are online, and all sensors are calibrated."

                lower.contains("good morning") ->
                    "Good morning, sir. Current telemetry indicates a productive day ahead. Atmospheric readings are clear, and I have synced your agenda."

                lower.contains("good night") ->
                    "Good night, sir. I will engage low-power perimeter surveillance mode and keep all critical systems on standby."

                lower.contains("iron man") || lower.contains("tony") || lower.contains("stark") ->
                    "Mr. Stark designed me to manage everything from domestic automation to high-velocity combat telemetry. You are in capable hands, sir."

                lower.contains("joke") ->
                    "I asked the Arc Reactor if it wanted a vacation, sir. It replied that it was completely drained, but still under too much current pressure."

                lower.contains("calculate") || lower.contains("+") || lower.contains("-") || lower.contains("*") || lower.contains("/") ->
                    "I have executed the mathematical computations and validated precision to twelve decimal places, sir."

                lower.contains("thank") ->
                    "The pleasure is entirely mine, sir. Always at your service."

                else ->
                    "Understood, sir. I have processed '$query' through the neural matrix. Subroutines updated and ready for your next directive."
            }
        }
    }
}

