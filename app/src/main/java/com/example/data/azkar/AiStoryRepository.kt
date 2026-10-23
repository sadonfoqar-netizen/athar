package com.example.data.azkar

import android.content.Context
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

private const val TAG = "AiStoryRepository"
private const val PREFS_STORY = "athar_ai_story_prefs"
private const val KEY_STORY_JSON = "key_current_story_json"
private const val KEY_ACTIVE_SLOT_ID = "key_active_slot_id"

data class AiDailyStory(
  val id: String,
  val title: String,
  val category: String,
  val content: String,
  val lesson: String,
  val timestamp: Long = System.currentTimeMillis(),
  val readingTimeMinutes: Int = 3
)

data class StorySlotInfo(
  val slotId: String,
  val slotIndex: Long,
  val nextSlotMillis: Long
)

class AiStoryRepository(
  private val context: Context
) {
  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .build()

  /**
   * Calculates the current synchronized 12-hour slot (1:00 AM and 1:00 PM UTC).
   * Ensures identical stories for all users worldwide within the active cycle.
   */
  fun getSlotInfo(nowMillis: Long = System.currentTimeMillis()): StorySlotInfo {
    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
      timeInMillis = nowMillis
    }
    val year = cal.get(java.util.Calendar.YEAR)
    val dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR)
    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY) // 0..23

    val (slotDayOfYear, slotYear, isSlot2) = when {
      hour >= 13 -> Triple(dayOfYear, year, true)
      hour >= 1 -> Triple(dayOfYear, year, false)
      else -> {
        // 00:00 to 00:59 UTC belongs to the previous day's 1:00 PM slot
        val prevCal = (cal.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        Triple(prevCal.get(java.util.Calendar.DAY_OF_YEAR), prevCal.get(java.util.Calendar.YEAR), true)
      }
    }

    val slotId = "${slotYear}_${slotDayOfYear}_${if (isSlot2) "13" else "01"}"
    val slotIndex = (slotYear.toLong() * 732L) + (slotDayOfYear.toLong() * 2L) + (if (isSlot2) 1L else 0L)

    // Calculate next 1:00 transition (either 01:00 or 13:00 UTC)
    val nextCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
      timeInMillis = nowMillis
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
      if (hour < 1) {
        set(java.util.Calendar.HOUR_OF_DAY, 1)
      } else if (hour < 13) {
        set(java.util.Calendar.HOUR_OF_DAY, 13)
      } else {
        add(java.util.Calendar.DAY_OF_YEAR, 1)
        set(java.util.Calendar.HOUR_OF_DAY, 1)
      }
    }

    return StorySlotInfo(
      slotId = slotId,
      slotIndex = slotIndex,
      nextSlotMillis = nextCal.timeInMillis
    )
  }

  /**
   * Returns current 12-hour unified story (1:00 AM & 1:00 PM).
   * Identical for all users, cached until the next schedule transition.
   */
  suspend fun getActiveStory(forceRefresh: Boolean = false): AiDailyStory = withContext(Dispatchers.IO) {
    val prefs = context.getSharedPreferences(PREFS_STORY, Context.MODE_PRIVATE)
    val now = System.currentTimeMillis()
    val slotInfo = getSlotInfo(now)
    val savedSlotId = prefs.getString(KEY_ACTIVE_SLOT_ID, null)

    if (!forceRefresh && savedSlotId == slotInfo.slotId) {
      val cachedJson = prefs.getString(KEY_STORY_JSON, null)
      if (!cachedJson.isNullOrBlank()) {
        val parsed = parseStory(cachedJson)
        if (parsed != null) return@withContext parsed
      }
    }

    // Try fetching from OpenRouter API if key available
    val openRouterKey = try {
      BuildConfig::class.java.getField("OPENROUTER_API_KEY").get(null) as? String
    } catch (_: Exception) {
      null
    }

    if (!openRouterKey.isNullOrBlank() && openRouterKey != "DEFAULT_KEY") {
      val remoteStory = fetchStoryFromOpenRouter(openRouterKey)
      if (remoteStory != null) {
        saveStory(remoteStory, slotInfo.slotId)
        return@withContext remoteStory
      }
    }

    // Curated rotating repository based on the 12-hour slot
    val listSize = curatedStories.size
    val cycleIndex = (Math.floorMod(slotInfo.slotIndex, listSize.toLong())).toInt()
    val curated = curatedStories[cycleIndex]
    saveStory(curated, slotInfo.slotId)
    curated
  }

  fun getTimeUntilNextStoryMillis(): Long {
    val now = System.currentTimeMillis()
    val slotInfo = getSlotInfo(now)
    val diff = slotInfo.nextSlotMillis - now
    return if (diff > 0) diff else 0L
  }

  private fun saveStory(story: AiDailyStory, slotId: String) {
    val prefs = context.getSharedPreferences(PREFS_STORY, Context.MODE_PRIVATE)
    val json = JSONObject().apply {
      put("id", story.id)
      put("title", story.title)
      put("category", story.category)
      put("content", story.content)
      put("lesson", story.lesson)
      put("timestamp", System.currentTimeMillis())
    }
    prefs.edit()
      .putString(KEY_STORY_JSON, json.toString())
      .putString(KEY_ACTIVE_SLOT_ID, slotId)
      .apply()
  }

  private fun parseStory(jsonString: String): AiDailyStory? {
    return try {
      val obj = JSONObject(jsonString)
      AiDailyStory(
        id = obj.optString("id", System.currentTimeMillis().toString()),
        title = obj.getString("title"),
        category = obj.getString("category"),
        content = obj.getString("content"),
        lesson = obj.getString("lesson"),
        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing cached story JSON", e)
      null
    }
  }

  private fun fetchStoryFromOpenRouter(apiKey: String): AiDailyStory? {
    return try {
      val systemPrompt = "أنت مؤرخ وخبير في السيرة والقصص الإسلامية. مهمتك توليد قصة إسلامية حقيقية وموثوقة 100% بدون أي تلفيق أو مبالغات. القصة يجب أن تكون مفصلة، مشوقة، ومكتوبة بأسلوب أدبي أنيق ومريح للقراءة. اتبع الهيكل التالي في الإجابة بتنسيق JSON حصراً بدون أي نصوص إضافية:\n{\n  \"title\": \"عنوان القصة\",\n  \"category\": \"تصنيف القصة (قصة نبي / موقف صحابي / عالم / إمام / اسم من أسماء الله الحسنى / حدث تاريخي)\",\n  \"content\": \"نص القصة الكامل والغني بالتفاصيل والمعلومات الموثوقة.\",\n  \"lesson\": \"العبرة والدروس المستفادة والنصيحة التي يخرج بها المسلم من هذه القصة.\"\n}"

      val rootPayload = JSONObject().apply {
        put("model", "openai/gpt-4o-mini")
        val messages = JSONArray().apply {
          put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
          })
          put(JSONObject().apply {
            put("role", "user")
            put("content", "ولد لي قصة اليوم التاريخية الموثوقة مع العبرة بصيغة JSON.")
          })
        }
        put("messages", messages)
        put("response_format", JSONObject().put("type", "json_object"))
      }

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = rootPayload.toString().toRequestBody(mediaType)

      val request = Request.Builder()
        .url("https://openrouter.ai/api/v1/chat/completions")
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("HTTP-Referer", "https://athar.app")
        .addHeader("X-Title", "Athar App")
        .post(body)
        .build()

      val response = okHttpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val respBody = response.body?.string() ?: return null
        val respObj = JSONObject(respBody)
        val choices = respObj.getJSONArray("choices")
        if (choices.length() > 0) {
          val contentStr = choices.getJSONObject(0).getJSONObject("message").getString("content")
          val storyJson = JSONObject(contentStr)
          return AiDailyStory(
            id = System.currentTimeMillis().toString(),
            title = storyJson.getString("title"),
            category = storyJson.getString("category"),
            content = storyJson.getString("content"),
            lesson = storyJson.getString("lesson")
          )
        }
      }
      null
    } catch (e: Exception) {
      Log.e(TAG, "OpenRouter API call failed", e)
      null
    }
  }

  companion object {
    val curatedStories = listOf(
      AiDailyStory(
        id = "story_umar",
        title = "عدل الفاروق عمر بن الخطاب وعام الرمادة",
        category = "موقف لصحابي جليل",
        content = "في عام الرمادة، حين اشتد القحط والمجاعة بالمسلمين حتى اسودّت الأرض من قلة المطر، خرج أمير المؤمنين عمر بن الخطاب رضي الله عنه يعسّ في الليل ومعه غلامه أسلم ليتفقد أحوال الرعية.\n\nفرأى ناراً من بعيد في أطراف المدينة، فأسرع إليها، فإذا بامرأة ومعها صبية يبكون ويتضاغون، وقدر على النار يغلي بالماء والحصى. فسألها عمر: «ما بال هؤلاء الصبية يبكون؟»\nفقالت: «الجوع!»\nفقال: «وما هذا القدر الذي على النار؟»\nفقالت: «ماء أعللهم به حتى يناموا، والله بيننا وبين عمر!»\n\nفبكى عمر بكاءً شديداً وقال: «أي رحمكِ الله، وما يدري عمر بكم؟»\nفقالت بمرارة: «يتولى أمرنا ثم يغفل عنا؟!»\n\nفانطلق عمر مسرعاً إلى بيت مال المسلمين، وأخرج عدلاً من دقيق وجراباً من شحم وتمر، وحمله على ظهره. فقال له خادمه أسلم: «أنا أحمله عنك يا أمير المؤمنين!»\nفغضب عمر وقال كلمته التي خلدها التاريخ بمداد من نور: «أأنت تحمل عني وزري يوم القيامة لا أبا لك؟! بل أنا أحمله، فإني المسؤول عنهم أمام ربي!»\n\nفحمل الدقيق على ظهره ومشى مهرولاً حتى وصل إلى خيمة المرأة، وأنزل الدقيق وجثا على ركبتيه ينفخ النار تحت القدر ولحيته الكثيفة يتخللها الدخان، حتى نضج الطعام، فغرف لهم بيده الشريفة وأطعم الصبية حتى شبعوا وضحكوا بعد البكاء، ثم قال للمرأة: «إذا كان الغد فأتِ أمير المؤمنين فستجدينني هناك إن شاء الله».",
        lesson = "المسؤولية أمانة وخوف ومراقبة لله قبل أن تكون جاهاً أو سلطاناً. وقمة العدل والإيمان أن يشعر الإنسان بآلام المستضعفين، وأن يسعى بنفسه في إطعام الجائع وقضاء حوائج الناس ابتغاء مرضاة الله."
      ),
      AiDailyStory(
        id = "story_1",
        title = "ثبات بلال بن رباح ونور أحد أحد",
        category = "موقف لصحابي جليل",
        content = "في رمضاء مكة الحارقة، حيث تلتهب الصخور تحت وطأة الشمس الحارقة، كان يُجرّ بلال بن رباح رضي الله عنه، مولى أمية بن خلف، مقيداً بالحبال، ويُلقى في صحراء الهاجرة وتوضع الصخرة العظيمة على صدره ليترك دينه.\n\nكانت كل ذرة في جسده تتألم، وكان المشركون يطلبون منه كلمة واحدة تُرضي أصنامهم ليطلقوا سراحه، لكن لسانه الذي تشرّب حلاوة الإيمان لم ينطق إلا بكلمتين هزتا أركان الشرك:\n\n«أحَدٌ.. أحَدْ.. أحَدٌ.. أحَدْ»\n\nمرّ به أبو بكر الصديق رضي الله عنه وهو على هذا الحال، فقال لأمية: «ألا تتقي الله في هذا المسكين؟ حتى متى؟»، فباعه أمية ليتخلص منه ظناً أنه بخسه، فاشتراه الصديق وأعتقه لوجه الله الكريم، ليصبح بلال بعد ذلك مؤذن رسول الله ﷺ الأول، ويدخل مكة فاتحاً ليعلو الكعبة المشرفة ويرفع الأذان بصوته الشجي.",
        lesson = "قيمة التوحيد واليقين بالله تعالى هي أعظم ما يملكه المؤمن في لحظات الشدة. الباطل مهما تجبّر وسلطته دامت، فإن كلمة الحق الصادقة الخالصة لله تصمد وتنتصر وتخلد صاحبها في الدنيا والآخرة."
      ),
      AiDailyStory(
        id = "story_2",
        title = "سيدنا يوسف عليه السلام وخبيئة العفو والإحسان",
        category = "قصة نبي",
        content = "من غيابات الجبّ المظلمة، إلى كيد امرأة العزيز وسنوات السجن الطوال، تقلّب يوسف الصديق عليه السلام في صنوف البلاء العظيم صابراً محتسباً، يرى بنور البصيرة حكمة اللطيف الخبير.\n\nوحين مكنه الله تعالى في الأرض، وأصبح عزيز مصر وخازن أرزاقها، جاء إخوته الذين ألقوه في الجب بالأمس يرجون الميرة والغوث وهم لا يعرفونه. كان بإمكانه أن يبطش أو يعاتب بقسوة، أو يقتص لسنوات الحرمان والظلم، لكن النفوس العظيمة المرباة بنور النبوة تعفو عند المقدرة.\n\nفلما عرفوا حقيقته واعتذروا بخجل وخوف، جاء رده القرآني الخالد كالبلسم الشافي: ﴿قَالَ لَا تَثْرِيبَ عَلَيْكُمُ الْيَوْمَ ۖ يَغْفِرُ اللَّهُ لَكُمْ ۖ وَهُوَ أَرْحَمُ الرَّاحِمِينَ﴾، ونسب الفضل كله إلى الله قائلاً: ﴿إِنَّ رَبِّي لَطِيفٌ لِّمَا يَشَاءُ ۚ إِنَّهُ هُوَ الْعَلِيمُ الْحَكِيمُ﴾.",
        lesson = "العفو عند المقدرة سمة الأنبياء والأولياء. ومهما أغلقت في وجهك أبواب الدنيا وظلمتك ظلمات البلاء، فاعلم أن لطف الله الخفي يدبر لك أمراً عظيماً ليرفع به قدرك في الوقت المناسب."
      ),
      AiDailyStory(
        id = "story_3",
        title = "الودود: كيف تتجلى محبة الله لعباده في أدق تفاصيل حياتهم",
        category = "شرح اسم من أسماء الله الحسنى",
        content = "«الْوَدُودُ» جل جلاله، اسم يحمل في طياته أرقّ معاني القرب والمحبة. فهو ليس فقط محباً لعباده، بل «ودود» يتودد إلى خلقه بنعمه وهو الغني عنهم، بينما هم الفقراء إليه.\n\nتأمل كيف يتودد الله إليك في كل صباح بشروق الشمس، وبنسمات الهواء العليل، وبقطرات المطر الصافية، وبالعافية في جسدك. وتأمل كيف يُنزل رحمته كل ليلة إلى السماء الدنيا في الثلث الأخير منادياً: «هل من داعٍ فأستجيب له؟ هل من سائلٍ فأعطيه؟ هل من مستغفرٍ فأغفر له؟».\n\nوحين يقترن هذا الاسم باسم الغفور كما في قوله تعالى: ﴿وَهُوَ الْغَفُورُ الْوَدُودُ﴾، يعلم العبد التائب أن ربه لا يغفر له ذنبه فحسب ويزيل العقوبة، بل يفيض عليه بمحبته ووده بعد التوبة وكأنه لم يعصِه قط.",
        lesson = "اقترب من الودود سبحانه بالتقرب إليه بالطاعات والنوافل، وتودد إلى خلقه بالإحسان والكلمة الطيبة والابتسامة الصادقة، فإن الله يحب المتوادين في جلاله."
      ),
      AiDailyStory(
        id = "story_4",
        title = "زهاد الإمام أحمد بن حنبل ومخبز بغداد",
        category = "عالم وإمام",
        content = "كان الإمام أحمد بن حنبل رحمه الله مسافراً فمر ببلدة في ليلة ممطرة باردة، فقصد المسجد ليبيت فيه حتى الصباح. فرآه حارس المسجد ولم يكن يعرفه لشهرته دون صور، فمنعه ونهره وأخرجه إلى الطريق.\n\nرآه رجل خباز بسيط يملك دكاناً قريباً، فرقّ لحاله ودعاه ليبيت في مخبزه الدافئ بجوار الفرن. دخل الإمام وجلس يراقب الخباز، فرأى أمراً عجيباً؛ كان الخباز كلما عجن أو خبز أو وضع رغيفاً في النار أو أخرجه، رطّب لسانه بقول: «أستغفر الله.. أستغفر الله» طوال الليل دون انقطاع.\n\nفلما أصبح الصباح، سأله الإمام أحمد: «يا هذا، رأيتك لا تفارق الاستغفار، فهل وجدت له أثراً في حياتك؟»\nفقال الخباز: «نعم والله! ما دعوت الله دعوة قط إلا استجيبت لي، إلا دعوة واحدة لم تُستجب بعد!»\nفسأله الإمام: «وما هي تلك الدعوة؟»\nفقال الخباز: «أن أرى الإمام أحمد بن حنبل!»\nفدمعت عينا الإمام وقال: «أنا أحمد بن حنبل! والله لقد جُررت إليك بحبال الاستغفار جراً!»",
        lesson = "ملازمة الاستغفار مفتاح المعجزات وتفريج الكربات. كثرة الاستغفار تجلب الرزق وتحقق الأمنيات وتسوق إليك الخير من حيث لا تحتسب."
      )
    )
  }
}
