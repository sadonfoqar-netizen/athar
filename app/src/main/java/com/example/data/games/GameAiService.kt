package com.example.data.games

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
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "GameAiService"

class GameAiService {
  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .build()

  /**
   * Intelligently evaluate a player's text answer for Fill-in-the-blank or open-ended questions.
   * Leverages smart Arabic normalization + Levenshtein distance offline,
   * with Gemini AI semantic validation when online.
   */
  suspend fun evaluateAnswer(
    userAnswer: String,
    expectedAnswer: String,
    prompt: String
  ): EvaluationResult = withContext(Dispatchers.IO) {
    val cleanUser = normalizeArabic(userAnswer)
    val cleanExpected = normalizeArabic(expectedAnswer)

    // 1. Direct or normalized equality check (instant offline success)
    if (cleanUser.equals(cleanExpected, ignoreCase = true)) {
      return@withContext EvaluationResult(
        isCorrect = true,
        feedbackTitle = "إجابة صحيحة ومطابقة!",
        explanation = "أحسنت! إجابتك دقيقة ومطابقة تماماً.",
        recognizedAnswer = expectedAnswer
      )
    }

    // 2. Fuzzy / close distance check (offline resilience against typos or definite article "ال")
    val userWithoutAl = if (cleanUser.startsWith("ال") && cleanUser.length > 3) cleanUser.substring(2) else cleanUser
    val expWithoutAl = if (cleanExpected.startsWith("ال") && cleanExpected.length > 3) cleanExpected.substring(2) else cleanExpected
    if (userWithoutAl == expWithoutAl || levenshteinDistance(cleanUser, cleanExpected) <= 1) {
      return@withContext EvaluationResult(
        isCorrect = true,
        feedbackTitle = "إجابة صحيحة مقبولة معنوياً!",
        explanation = "أحسنت! تم قبول الإجابة نظراً لتطابق المعنى والصياغة المقاربة.",
        recognizedAnswer = expectedAnswer
      )
    }

    // 3. Online Gemini API semantic evaluation if API key is present
    val geminiKey = getGeminiKey()
    if (!geminiKey.isNullOrBlank() && geminiKey != "DEFAULT_KEY") {
      try {
        val aiEvaluation = callGeminiForEvaluation(geminiKey, userAnswer, expectedAnswer, prompt)
        if (aiEvaluation != null) {
          return@withContext aiEvaluation
        }
      } catch (e: Exception) {
        Log.w(TAG, "Gemini evaluation fallback to offline logic: ${e.message}")
      }
    }

    // Default incorrect if not matching
    EvaluationResult(
      isCorrect = false,
      feedbackTitle = "إجابة غير صحيحة",
      explanation = "الإجابة الصحيحة هي: $expectedAnswer.",
      recognizedAnswer = expectedAnswer
    )
  }

  /**
   * Replenishes questions for a game type via Gemini AI in the background.
   */
  suspend fun generateReplenishmentQuestion(type: GameType): GameQuestion? = withContext(Dispatchers.IO) {
    val geminiKey = getGeminiKey()
    if (geminiKey.isNullOrBlank() || geminiKey == "DEFAULT_KEY") {
      return@withContext null
    }

    val typeInstructions = when (type) {
      GameType.FILL_BLANK -> "املأ الفراغ من آية أو حديث شريف، مع وضع ___ مكان الكلمة المفقودة وتقديم 4 خيارات أحدها الكلمة الصحيحة."
      GameType.TRUE_FALSE -> "معلومة من السيرة النبوية أو الفقه أو القرآن بصيغة صح أو خطأ مع تحديد الإجابة (صح أو خطأ)."
      GameType.MULTIPLE_CHOICE -> "سؤال إسلامي مفصل مع 4 خيارات وخيار واحد فقط صحيح."
      GameType.ORDERING -> "سؤال ترتيب 4 أحداث تاريخية أو كلمات آية، مع تحديد القائمة المرتبة الصحيحة."
    }

    val promptText = """
      أنت نظام ألعاب إسلامية موثوق ودقيق. قم بتوليد سؤال واحد فقط من نوع: (${type.title}) - $typeInstructions.
      أجب فقط بكائن JSON صالح بالشكل التالي بدون أي نصوص إضافية:
      {
        "prompt": "نص السؤال أو الآية هنا",
        "options": ["خيار1", "خيار2", "خيار3", "خيار4"],
        "correctAnswer": "الإجابة الصحيحة",
        "correctOrder": ["عنصر1", "عنصر2", "عنصر3", "عنصر4"],
        "explanation": "شرح علمي وموثق للمعلومة والعبرة منها",
        "source": "المصدر مثل صحيح البخاري أو اسم السورة"
      }
    """.trimIndent()

    try {
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$geminiKey"
      val requestJson = JSONObject().apply {
        put("contents", JSONArray().put(JSONObject().apply {
          put("parts", JSONArray().put(JSONObject().apply {
            put("text", promptText)
          }))
        }))
        put("generationConfig", JSONObject().apply {
          put("responseMimeType", "application/json")
          put("temperature", 0.6)
        })
      }

      val request = Request.Builder()
        .url(url)
        .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
        .build()

      okHttpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val bodyStr = response.body?.string() ?: return@withContext null
        val root = JSONObject(bodyStr)
        val text = root.optJSONArray("candidates")
          ?.optJSONObject(0)
          ?.optJSONObject("content")
          ?.optJSONArray("parts")
          ?.optJSONObject(0)
          ?.optString("text") ?: return@withContext null

        val qJson = JSONObject(text.trim())
        val optionsList = mutableListOf<String>()
        val optArr = qJson.optJSONArray("options")
        if (optArr != null) {
          for (i in 0 until optArr.length()) {
            optionsList.add(optArr.getString(i))
          }
        }
        val orderList = mutableListOf<String>()
        val ordArr = qJson.optJSONArray("correctOrder")
        if (ordArr != null) {
          for (i in 0 until ordArr.length()) {
            orderList.add(ordArr.getString(i))
          }
        }

        GameQuestion(
          id = "ai_${UUID.randomUUID().toString().take(8)}",
          gameType = type,
          prompt = qJson.getString("prompt"),
          options = optionsList,
          correctAnswer = qJson.getString("correctAnswer"),
          correctOrder = orderList,
          explanation = qJson.optString("explanation", "معلومة إسلامية موثقة ومولدة بالذكاء الاصطناعي."),
          source = qJson.optString("source", "الذكاء الاصطناعي الإيماني"),
          isFromAi = true
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error generating replenishment question: ${e.message}")
      null
    }
  }

  private fun callGeminiForEvaluation(
    apiKey: String,
    userAnswer: String,
    expectedAnswer: String,
    prompt: String
  ): EvaluationResult? {
    val promptMsg = """
      قيم إجابة اللاعب التالية في مسابقة إسلامية:
      السياق أو السؤال: "$prompt"
      الإجابة النموذجية: "$expectedAnswer"
      إجابة اللاعب: "$userAnswer"

      هل إجابة اللاعب صحيحة أو مقبولة معنوياً وتفي بالمعنى المطلوب؟
      أجب حصراً بكائن JSON:
      {
        "isCorrect": true/false,
        "feedbackTitle": "عنوان التقييم",
        "explanation": "شرح ميسر ومختصر"
      }
    """.trimIndent()

    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
    val reqJson = JSONObject().apply {
      put("contents", JSONArray().put(JSONObject().apply {
        put("parts", JSONArray().put(JSONObject().apply {
          put("text", promptMsg)
        }))
      }))
      put("generationConfig", JSONObject().apply {
        put("responseMimeType", "application/json")
        put("temperature", 0.1)
      })
    }

    val request = Request.Builder()
      .url(url)
      .post(reqJson.toString().toRequestBody("application/json".toMediaType()))
      .build()

    okHttpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val body = response.body?.string() ?: return null
      val root = JSONObject(body)
      val text = root.optJSONArray("candidates")
        ?.optJSONObject(0)
        ?.optJSONObject("content")
        ?.optJSONArray("parts")
        ?.optJSONObject(0)
        ?.optString("text") ?: return null

      val resJson = JSONObject(text.trim())
      return EvaluationResult(
        isCorrect = resJson.optBoolean("isCorrect", false),
        feedbackTitle = resJson.optString("feedbackTitle", if (resJson.optBoolean("isCorrect")) "إجابة مقبولة ذكياً!" else "إجابة غير صحيحة"),
        explanation = resJson.optString("explanation", "الإجابة النموذجية: $expectedAnswer."),
        recognizedAnswer = expectedAnswer
      )
    }
  }

  private fun getGeminiKey(): String? {
    return try {
      BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
    } catch (_: Exception) {
      null
    }
  }

  private fun normalizeArabic(text: String): String {
    return text.trim()
      .replace(Regex("[\\u064B-\\u065F\\u0670]"), "") // Remove tashkeel/diacritics
      .replace('أ', 'ا')
      .replace('إ', 'ا')
      .replace('آ', 'ا')
      .replace('ة', 'ه')
      .replace('ى', 'ي')
      .replace(Regex("[.,،!؟؛:]"), "")
      .trim()
  }

  private fun levenshteinDistance(s1: String, s2: String): Int {
    val m = s1.length
    val n = s2.length
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 0..m) dp[i][0] = i
    for (j in 0..n) dp[0][j] = j
    for (i in 1..m) {
      for (j in 1..n) {
        val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
        dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
      }
    }
    return dp[m][n]
  }
}
