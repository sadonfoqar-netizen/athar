package com.example.data.quran

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private const val TAG = "QuranRepository"
private const val PREFS_NAME = "athar_quran_prefs"
private const val KEY_BOOKMARK_SURAH_NUM = "bm_surah_num"
private const val KEY_BOOKMARK_SURAH_NAME = "bm_surah_name"
private const val KEY_BOOKMARK_AYAH_NUM = "bm_ayah_num"
private const val KEY_BOOKMARK_TIME = "bm_time"

class QuranRepository(
  private val context: Context
) {

  private val memoryCache = ConcurrentHashMap<Int, SurahDetail>()
  private var cachedSurahList: List<SurahMeta>? = null

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  /**
   * Returns list of all 114 Surahs from assets, with instantaneous 0ms loading
   */
  suspend fun getSurahList(): List<SurahMeta> = withContext(Dispatchers.IO) {
    cachedSurahList?.let { return@withContext it }

    try {
      val jsonString = context.assets.open("surahs.json").use { stream ->
        InputStreamReader(stream).readText()
      }
      val jsonObject = JSONObject(jsonString)
      val dataArray = jsonObject.getJSONArray("data")
      val list = ArrayList<SurahMeta>(dataArray.length())

      for (i in 0 until dataArray.length()) {
        val item = dataArray.getJSONObject(i)
        list.add(
          SurahMeta(
            number = item.getInt("number"),
            name = item.getString("name"),
            englishName = item.getString("englishName"),
            englishNameTranslation = item.optString("englishNameTranslation", ""),
            numberOfAyahs = item.getInt("numberOfAyahs"),
            revelationType = item.getString("revelationType")
          )
        )
      }
      cachedSurahList = list
      list
    } catch (e: Exception) {
      Log.e(TAG, "Failed to load surahs list from assets", e)
      emptyList()
    }
  }

  /**
   * Fetches or loads Surah details with Uthmani text.
   * Priority: Memory -> Cache Dir -> Bundled Asset -> Remote API
   */
  suspend fun getSurahDetail(surahNumber: Int): SurahDetail? = withContext(Dispatchers.IO) {
    // 1. Memory cache
    memoryCache[surahNumber]?.let { return@withContext it }

    // 2. Local disk cache
    val cacheFile = File(context.cacheDir, "surah_$surahNumber.json")
    if (cacheFile.exists() && cacheFile.length() > 0) {
      try {
        val parsed = parseSurahDetailJson(cacheFile.readText())
        if (parsed != null) {
          memoryCache[surahNumber] = parsed
          return@withContext parsed
        }
      } catch (e: Exception) {
        Log.w(TAG, "Cache read failed for surah $surahNumber", e)
      }
    }

    // 3. Bundled Asset (popular surahs: 1, 18, 36, 67, 112, 113, 114)
    try {
      val assetName = "surah_$surahNumber.json"
      val jsonString = context.assets.open(assetName).use { stream ->
        InputStreamReader(stream).readText()
      }
      val parsed = parseSurahDetailJson(jsonString)
      if (parsed != null) {
        memoryCache[surahNumber] = parsed
        // Write to cache file for consistency
        cacheFile.writeText(jsonString)
        return@withContext parsed
      }
    } catch (_: Exception) {
      // Not in bundled assets, proceed to remote API
    }

    // 4. Remote API
    try {
      val url = "https://api.alquran.cloud/v1/surah/$surahNumber/quran-uthmani"
      val request = Request.Builder().url(url).build()
      val response = okHttpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val body = response.body?.string()
        if (!body.isNullOrBlank()) {
          val parsed = parseSurahDetailJson(body)
          if (parsed != null) {
            memoryCache[surahNumber] = parsed
            cacheFile.writeText(body)
            return@withContext parsed
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to fetch surah $surahNumber from API", e)
    }

    null
  }

  private fun parseSurahDetailJson(jsonString: String): SurahDetail? {
    return try {
      val root = JSONObject(jsonString)
      val data = root.getJSONObject("data")
      val number = data.getInt("number")
      val name = data.getString("name")
      val englishName = data.getString("englishName")
      val revelationType = data.getString("revelationType")
      val numberOfAyahs = data.getInt("numberOfAyahs")

      val ayahsArray = data.getJSONArray("ayahs")
      val ayahsList = ArrayList<Ayah>(ayahsArray.length())

      for (i in 0 until ayahsArray.length()) {
        val ayahObj = ayahsArray.getJSONObject(i)
        ayahsList.add(
          Ayah(
            number = ayahObj.getInt("number"),
            numberInSurah = ayahObj.getInt("numberInSurah"),
            text = ayahObj.getString("text"),
            juz = ayahObj.optInt("juz", 1),
            page = ayahObj.optInt("page", 1),
            sajda = ayahObj.optBoolean("sajda", false)
          )
        )
      }

      SurahDetail(
        number = number,
        name = name,
        englishName = englishName,
        revelationType = revelationType,
        numberOfAyahs = numberOfAyahs,
        ayahs = ayahsList
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing Surah detail JSON", e)
      null
    }
  }

  // --- Bookmark Management ---

  fun saveBookmark(bookmark: QuranBookmark) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putInt(KEY_BOOKMARK_SURAH_NUM, bookmark.surahNumber)
      .putString(KEY_BOOKMARK_SURAH_NAME, bookmark.surahName)
      .putInt(KEY_BOOKMARK_AYAH_NUM, bookmark.ayahNumberInSurah)
      .putLong(KEY_BOOKMARK_TIME, bookmark.timestamp)
      .apply()
  }

  fun getBookmark(): QuranBookmark? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val surahNum = prefs.getInt(KEY_BOOKMARK_SURAH_NUM, -1)
    if (surahNum == -1) return null
    return QuranBookmark(
      surahNumber = surahNum,
      surahName = prefs.getString(KEY_BOOKMARK_SURAH_NAME, "") ?: "",
      ayahNumberInSurah = prefs.getInt(KEY_BOOKMARK_AYAH_NUM, 1),
      timestamp = prefs.getLong(KEY_BOOKMARK_TIME, 0L)
    )
  }
}
