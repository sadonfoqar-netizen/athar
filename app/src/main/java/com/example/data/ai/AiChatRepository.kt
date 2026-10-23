package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "AiChatRepository"
private const val PREFS_AI_CHAT = "athar_ai_chat_prefs"
private const val KEY_SESSIONS_JSON = "key_ai_sessions_json"
private const val KEY_ACTIVE_SESSION_ID = "key_active_session_id"

class AiChatRepository(
  private val context: Context,
  private val db: FirebaseFirestore? = null,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
  val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

  private val _activeSession = MutableStateFlow<ChatSession?>(null)
  val activeSession: StateFlow<ChatSession?> = _activeSession.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  init {
    loadLocalSessions()
    listenToFirestoreSessions()
  }

  private fun loadLocalSessions() {
    val prefs = context.getSharedPreferences(PREFS_AI_CHAT, Context.MODE_PRIVATE)
    val rawJson = prefs.getString(KEY_SESSIONS_JSON, null)
    if (!rawJson.isNullOrBlank()) {
      try {
        val array = JSONArray(rawJson)
        val list = mutableListOf<ChatSession>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          list.add(deserializeSession(obj))
        }
        _sessions.value = list
        val savedActiveId = prefs.getString(KEY_ACTIVE_SESSION_ID, null)
        _activeSession.value = list.find { it.id == savedActiveId } ?: list.firstOrNull()
      } catch (e: Exception) {
        Log.e(TAG, "Error loading local chat sessions", e)
      }
    }

    if (_activeSession.value == null) {
      createNewSession()
    }
  }

  private fun listenToFirestoreSessions() {
    val firestore = db ?: return

    try {
      firestore.collection("chats")
        .orderBy("updatedAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Firestore chats listen failed: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val firestoreList = snapshot.documents.mapNotNull { doc ->
              try {
                val data = doc.data ?: return@mapNotNull null
                val id = doc.id
                val title = data["title"] as? String ?: "محادثة إيمانية"
                val createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                @Suppress("UNCHECKED_CAST")
                val messagesRaw = data["messages"] as? List<Map<String, Any>> ?: emptyList()

                val messages = messagesRaw.map { msgMap ->
                  ChatMessage(
                    id = msgMap["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                    role = msgMap["role"] as? String ?: "user",
                    content = msgMap["content"] as? String ?: "",
                    timestamp = (msgMap["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                  )
                }

                ChatSession(
                  id = id,
                  title = title,
                  createdAt = createdAt,
                  updatedAt = updatedAt,
                  messages = messages
                )
              } catch (e: Exception) {
                Log.e(TAG, "Error parsing firestore chat session", e)
                null
              }
            }

            if (firestoreList.isNotEmpty()) {
              _sessions.value = firestoreList
              saveSessionsLocally(firestoreList)
              val currentActive = _activeSession.value
              if (currentActive != null) {
                val updatedActive = firestoreList.find { it.id == currentActive.id }
                if (updatedActive != null) {
                  _activeSession.value = updatedActive
                }
              }
            }
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Exception initializing firestore listener", e)
    }
  }

  fun createNewSession(): ChatSession {
    val newSession = ChatSession(
      title = "محادثة إيمانية جديدة",
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis(),
      messages = emptyList()
    )

    _sessions.update { listOf(newSession) + it }
    _activeSession.value = newSession
    saveActiveSessionId(newSession.id)
    saveSessionsLocally(_sessions.value)
    syncSessionToFirestore(newSession)
    return newSession
  }

  fun selectSession(sessionId: String) {
    val found = _sessions.value.find { it.id == sessionId }
    if (found != null) {
      _activeSession.value = found
      saveActiveSessionId(found.id)
    }
  }

  fun deleteSession(sessionId: String) {
    _sessions.update { list -> list.filter { it.id != sessionId } }
    if (_activeSession.value?.id == sessionId) {
      _activeSession.value = _sessions.value.firstOrNull() ?: createNewSession()
      _activeSession.value?.let { saveActiveSessionId(it.id) }
    }
    saveSessionsLocally(_sessions.value)

    db?.let { firestore ->
      scope.launch {
        try {
          firestore.collection("chats").document(sessionId).delete().await()
        } catch (e: Exception) {
          Log.w(TAG, "Failed to delete chat from firestore: ${e.message}")
        }
      }
    }
  }

  suspend fun sendMessage(userText: String): Unit = withContext(Dispatchers.IO) {
    if (userText.isBlank()) return@withContext

    val current = _activeSession.value ?: createNewSession()
    val userMsg = ChatMessage(role = "user", content = userText.trim())

    // Generate smart title from first user query if still default
    val updatedTitle = if (current.messages.isEmpty() && current.title.contains("جديدة")) {
      userText.trim().take(36)
    } else {
      current.title
    }

    val updatedWithUser = current.copy(
      title = updatedTitle,
      updatedAt = System.currentTimeMillis(),
      messages = current.messages + userMsg
    )

    _activeSession.value = updatedWithUser
    _sessions.update { list -> list.map { if (it.id == updatedWithUser.id) updatedWithUser else it } }
    saveSessionsLocally(_sessions.value)
    syncSessionToFirestore(updatedWithUser)

    _isGenerating.value = true

    try {
      val responseText = queryAiAssistant(updatedWithUser.messages)
      val assistantMsg = ChatMessage(
        role = "model",
        content = responseText
      )

      val finalSession = updatedWithUser.copy(
        updatedAt = System.currentTimeMillis(),
        messages = updatedWithUser.messages + assistantMsg
      )

      _activeSession.value = finalSession
      _sessions.update { list -> list.map { if (it.id == finalSession.id) finalSession else it } }
      saveSessionsLocally(_sessions.value)
      syncSessionToFirestore(finalSession)
    } catch (e: Exception) {
      Log.e(TAG, "Error generating AI response", e)
      val errorMsg = ChatMessage(
        role = "model",
        content = "عذراً يا أخي الكريم، تعذر الاتصال بالمساعد الذكي في الوقت الراهن. يرجى التحقق من اتصالك بالإنترنت والمحاولة مجدداً."
      )
      val fallbackSession = updatedWithUser.copy(
        updatedAt = System.currentTimeMillis(),
        messages = updatedWithUser.messages + errorMsg
      )
      _activeSession.value = fallbackSession
      _sessions.update { list -> list.map { if (it.id == fallbackSession.id) fallbackSession else it } }
    } finally {
      _isGenerating.value = false
    }
  }

  private suspend fun queryAiAssistant(messages: List<ChatMessage>): String = withContext(Dispatchers.IO) {
    val geminiKey = try {
      BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
    } catch (_: Exception) {
      null
    }

    // 1. Try Gemini 3.5 Flash if available
    if (!geminiKey.isNullOrBlank() && geminiKey != "DEFAULT_KEY") {
      try {
        val geminiResponse = callGeminiApi(geminiKey, messages)
        if (!geminiResponse.isNullOrBlank()) {
          return@withContext geminiResponse
        }
      } catch (e: Exception) {
        Log.w(TAG, "Gemini call failed, trying secondary route", e)
      }
    }

    // 2. Try OpenRouter API as fallback
    val openRouterKey = try {
      BuildConfig::class.java.getField("OPENROUTER_API_KEY").get(null) as? String
    } catch (_: Exception) {
      null
    }

    if (!openRouterKey.isNullOrBlank() && openRouterKey != "DEFAULT_KEY") {
      try {
        val openRouterResponse = callOpenRouterApi(openRouterKey, messages)
        if (!openRouterResponse.isNullOrBlank()) {
          return@withContext openRouterResponse
        }
      } catch (e: Exception) {
        Log.w(TAG, "OpenRouter call failed", e)
      }
    }

    // 3. Authentic Islamic Knowledge Engine Fallback (Offline Guarantee)
    generateAuthenticLocalResponse(messages.lastOrNull()?.content ?: "")
  }

  private fun callGeminiApi(apiKey: String, messages: List<ChatMessage>): String? {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

    val systemInstruction = "أنت «المساعد الإيماني الذكي» لتطبيق أثر للصدقة الجارية. مهمتك تقديم إجابات إيمانية موثوقة ومبنية على القرآن الكريم وصحيح السنة النبوية المطهرة وتفاسير أهل العلم المعتمدة (كتفسير ابن كثير، السعدي، والقرطبي)، وشرح الأحاديث والمسائل الفقهية الميسرة بأسلوب هادئ، أدبي، ورحيم. تجنب التكلف والخوض في الخلافات الفقهية الحادة، واذكر أقوال العلماء بأدب ومحبة."

    val contentsArray = JSONArray()
    for (msg in messages.takeLast(10)) {
      val role = if (msg.role == "model") "model" else "user"
      val partObj = JSONObject().put("text", msg.content)
      val contentObj = JSONObject().apply {
        put("role", role)
        put("parts", JSONArray().put(partObj))
      }
      contentsArray.put(contentObj)
    }

    val requestJson = JSONObject().apply {
      put("system_instruction", JSONObject().apply {
        put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
      })
      put("contents", contentsArray)
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = requestJson.toString().toRequestBody(mediaType)

    val request = Request.Builder()
      .url(url)
      .post(requestBody)
      .build()

    val response = okHttpClient.newCall(request).execute()
    if (response.isSuccessful) {
      val bodyStr = response.body?.string() ?: return null
      val json = JSONObject(bodyStr)
      val candidates = json.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val content = candidates.getJSONObject(0).optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
          return parts.getJSONObject(0).optString("text")
        }
      }
    }
    return null
  }

  private fun callOpenRouterApi(apiKey: String, messages: List<ChatMessage>): String? {
    val systemPrompt = "أنت «المساعد الإيماني الذكي» لتطبيق أثر للصدقة الجارية. مهمتك الإجابة على استفسارات المسلم حول القرآن، الحديث، الأذكار، والسيرة النبوية بأدب وعلم موثق."

    val rootPayload = JSONObject().apply {
      put("model", "openai/gpt-4o-mini")
      val messagesArr = JSONArray().apply {
        put(JSONObject().apply {
          put("role", "system")
          put("content", systemPrompt)
        })
        for (m in messages.takeLast(8)) {
          val role = if (m.role == "model") "assistant" else "user"
          put(JSONObject().apply {
            put("role", role)
            put("content", m.content)
          })
        }
      }
      put("messages", messagesArr)
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = rootPayload.toString().toRequestBody(mediaType)

    val request = Request.Builder()
      .url("https://openrouter.ai/api/v1/chat/completions")
      .addHeader("Authorization", "Bearer $apiKey")
      .addHeader("HTTP-Referer", "https://athar.app")
      .addHeader("X-Title", "Athar AI Chat")
      .post(body)
      .build()

    val response = okHttpClient.newCall(request).execute()
    if (response.isSuccessful) {
      val respBody = response.body?.string() ?: return null
      val respObj = JSONObject(respBody)
      val choices = respObj.getJSONArray("choices")
      if (choices.length() > 0) {
        return choices.getJSONObject(0).getJSONObject("message").getString("content")
      }
    }
    return null
  }

  private fun generateAuthenticLocalResponse(query: String): String {
    val normalized = query.lowercase()
    return when {
      normalized.contains("كرسي") -> {
        "آية الكرسي هي أعظم آية في كتاب الله تعالى (سورة البقرة، الآية 255). تشتمل على عشر جمل مستقلة تؤصل لتوحيد الله تعالى وعظمته وقدرته وقيوميته التي لا تأخذها سِنةٌ ولا نوم. من قرأها دبر كل صلاة مكتوبة لم يمنعه من دخول الجنة إلا أن يموت كما صح في الحديث النبوي الشريف، وهي حصن حصين يحفظ المسلم من وساوس الشيطان."
      }
      normalized.contains("فجر") || normalized.contains("صلاة") -> {
        "صلاة الفجر ركعتان فرضهما الله، ولهما سنة راتبة قبلية قال عنها النبي ﷺ: «رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا وَمَا فِيهَا» (رواه مسلم). والمحافظة على صلاة الفجر في وقتها تجعل المسلم في ذمة الله ورعايته طوال يومه، وتورث النور التام يوم القيامة."
      }
      normalized.contains("نية") || normalized.contains("نيات") -> {
        "حديث «إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى» (رواه البخاري ومسلم) هو مدار الإسلام وأحد أركانه العظام. قال الإمام الشافعي: إنه يدخل في سبعين باباً من أبواب الفقه. ومعناه أن صلاح العمل وثوابه وقبوله عند الله متوقف على إخلاص النية لله سبحانه وتعالى وتجريدها من الرياء والسمعة."
      }
      normalized.contains("نوم") || normalized.contains("أذكار") -> {
        "من أذكار النوم المأثورة عن النبي ﷺ:\n1. قراءة آية الكرسي والمعوذتين والإخلاص ثلاثاً والنفث في الكفين ومسح ما استطاع من الجسد.\n2. قول: «بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ».\n3. التسبيح ثلاثاً وثلاثين، والتحميد ثلاثاً وثلاثين، والتكبير أربعاً وثلاثين."
      }
      else -> {
        "بارك الله فيك ونفع بك يا أخي الكريم. نسعد بإجابتك ومرافقتك في رحلتك الإيمانية عبر مدارسة القرآن الكريم وصحيح السنة النبوية العطرة. قال الله تعالى: ﴿فَاسْأَلُوا أَهْلَ الذِّكْرِ إِن كُنتُمْ لَا تَعْلَمُونَ﴾. يسعدني أن تجيبني بتحديد المسألة أو الآية الكريمة التي ترغب في استجلاء معانيها وبيان هداياتها."
      }
    }
  }

  private fun syncSessionToFirestore(session: ChatSession) {
    val firestore = db ?: return

    scope.launch {
      try {
        val messagesMap = session.messages.map { msg ->
          mapOf(
            "id" to msg.id,
            "role" to msg.role,
            "content" to msg.content,
            "timestamp" to msg.timestamp
          )
        }

        val data = hashMapOf(
          "id" to session.id,
          "title" to session.title,
          "createdAt" to session.createdAt,
          "updatedAt" to session.updatedAt,
          "messages" to messagesMap
        )

        firestore.collection("chats")
          .document(session.id)
          .set(data)
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Failed syncing chat session to firestore: ${e.message}")
      }
    }
  }

  private fun saveSessionsLocally(list: List<ChatSession>) {
    try {
      val prefs = context.getSharedPreferences(PREFS_AI_CHAT, Context.MODE_PRIVATE)
      val array = JSONArray()
      for (session in list) {
        val obj = JSONObject().apply {
          put("id", session.id)
          put("title", session.title)
          put("createdAt", session.createdAt)
          put("updatedAt", session.updatedAt)
          val msgsArr = JSONArray()
          for (m in session.messages) {
            msgsArr.put(JSONObject().apply {
              put("id", m.id)
              put("role", m.role)
              put("content", m.content)
              put("timestamp", m.timestamp)
            })
          }
          put("messages", msgsArr)
        }
        array.put(obj)
      }
      prefs.edit().putString(KEY_SESSIONS_JSON, array.toString()).apply()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving sessions locally", e)
    }
  }

  private fun saveActiveSessionId(id: String) {
    val prefs = context.getSharedPreferences(PREFS_AI_CHAT, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_ACTIVE_SESSION_ID, id).apply()
  }

  private fun deserializeSession(obj: JSONObject): ChatSession {
    val msgsList = mutableListOf<ChatMessage>()
    val msgsArr = obj.optJSONArray("messages")
    if (msgsArr != null) {
      for (i in 0 until msgsArr.length()) {
        val mObj = msgsArr.getJSONObject(i)
        msgsList.add(
          ChatMessage(
            id = mObj.optString("id", java.util.UUID.randomUUID().toString()),
            role = mObj.optString("role", "user"),
            content = mObj.optString("content", ""),
            timestamp = mObj.optLong("timestamp", System.currentTimeMillis())
          )
        )
      }
    }

    return ChatSession(
      id = obj.getString("id"),
      title = obj.getString("title"),
      createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
      updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
      messages = msgsList
    )
  }
}
