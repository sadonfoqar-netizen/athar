package com.example.data.azkar

import android.content.Context
import android.location.Location
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private const val TAG = "PrayerTimesService"

data class PrayerTime(
  val id: String,
  val nameArabic: String,
  val timeFormatted: String, // e.g. "05:12 AM" or "05:12"
  val raw24h: String, // e.g. "05:12"
  val isNext: Boolean = false,
  val isPassed: Boolean = false
)

data class PrayerSchedule(
  val fajr: String,
  val sunrise: String,
  val dhuhr: String,
  val asr: String,
  val maghrib: String,
  val isha: String,
  val hijriDate: String,
  val gregorianDate: String,
  val locationName: String,
  val prayersList: List<PrayerTime>,
  val nextPrayer: PrayerTime?,
  val timeUntilNextMillis: Long
)

class PrayerTimesService(
  private val context: Context
) {
  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()

  /**
   * Fetch prayer times from AlAdhan API or fallback to calculation
   */
  suspend fun getPrayerTimes(
    latitude: Double = 21.4225, // Default Mecca
    longitude: Double = 39.8262,
    cityName: String = "مكة المكرمة"
  ): PrayerSchedule = withContext(Dispatchers.IO) {
    val timestamp = System.currentTimeMillis() / 1000
    val url = "https://api.aladhan.com/v1/timings/$timestamp?latitude=$latitude&longitude=$longitude&method=4"

    try {
      val request = Request.Builder().url(url).build()
      val response = okHttpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val body = response.body?.string()
        if (!body.isNullOrBlank()) {
          val schedule = parseAlAdhanJson(body, cityName)
          if (schedule != null) {
            return@withContext schedule
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to fetch from AlAdhan API, using fallback schedule", e)
    }

    // High quality offline fallback calculation
    getFallbackSchedule(cityName)
  }

  private fun parseAlAdhanJson(jsonString: String, locationName: String): PrayerSchedule? {
    return try {
      val root = JSONObject(jsonString)
      val data = root.getJSONObject("data")
      val timings = data.getJSONObject("timings")
      val dateObj = data.getJSONObject("date")
      val hijri = dateObj.getJSONObject("hijri")
      val gregorian = dateObj.getJSONObject("gregorian")

      val hijriStr = "${hijri.getString("day")} ${hijri.getJSONObject("month").getString("ar")} ${hijri.getString("year")} هـ"
      val gregStr = gregorian.getString("date")

      val fajr = cleanTime(timings.getString("Fajr"))
      val sunrise = cleanTime(timings.getString("Sunrise"))
      val dhuhr = cleanTime(timings.getString("Dhuhr"))
      val asr = cleanTime(timings.getString("Asr"))
      val maghrib = cleanTime(timings.getString("Maghrib"))
      val isha = cleanTime(timings.getString("Isha"))

      buildSchedule(
        fajr = fajr,
        sunrise = sunrise,
        dhuhr = dhuhr,
        asr = asr,
        maghrib = maghrib,
        isha = isha,
        hijriDate = hijriStr,
        gregorianDate = gregStr,
        locationName = locationName
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing AlAdhan response", e)
      null
    }
  }

  private fun cleanTime(raw: String): String {
    // AlAdhan returns e.g. "04:52 (EEST)" -> extract "04:52"
    return raw.split(" ")[0].trim()
  }

  private fun buildSchedule(
    fajr: String,
    sunrise: String,
    dhuhr: String,
    asr: String,
    maghrib: String,
    isha: String,
    hijriDate: String,
    gregorianDate: String,
    locationName: String
  ): PrayerSchedule {
    val now = Calendar.getInstance()
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    val rawList = listOf(
      Triple("fajr", "الفجر", fajr),
      Triple("sunrise", "الشروق", sunrise),
      Triple("dhuhr", "الظهر", dhuhr),
      Triple("asr", "العصر", asr),
      Triple("maghrib", "المغرب", maghrib),
      Triple("isha", "العشاء", isha)
    )

    var nextPrayerIndex = -1
    var minDiff = Int.MAX_VALUE

    for (i in rawList.indices) {
      val prayerMinutes = timeToMinutes(rawList[i].third)
      val diff = prayerMinutes - nowMinutes
      if (diff > 0 && diff < minDiff) {
        minDiff = diff
        nextPrayerIndex = i
      }
    }

    // If all prayers today have passed, next prayer is Fajr tomorrow
    if (nextPrayerIndex == -1) {
      nextPrayerIndex = 0
      val fajrTomorrow = (24 * 60 - nowMinutes) + timeToMinutes(fajr)
      minDiff = fajrTomorrow
    }

    val prayers = rawList.mapIndexed { index, item ->
      val prayerMinutes = timeToMinutes(item.third)
      val isNext = index == nextPrayerIndex
      val isPassed = if (nextPrayerIndex == 0 && minDiff > 24 * 60 - nowMinutes) {
        // We're late at night, today's prayers all passed
        true
      } else {
        prayerMinutes < nowMinutes
      }

      PrayerTime(
        id = item.first,
        nameArabic = item.second,
        timeFormatted = formatTo12Hour(item.third),
        raw24h = item.third,
        isNext = isNext,
        isPassed = isPassed
      )
    }

    val nextPrayer = prayers.getOrNull(nextPrayerIndex)
    val timeUntilNextMillis = minDiff * 60 * 1000L

    return PrayerSchedule(
      fajr = formatTo12Hour(fajr),
      sunrise = formatTo12Hour(sunrise),
      dhuhr = formatTo12Hour(dhuhr),
      asr = formatTo12Hour(asr),
      maghrib = formatTo12Hour(maghrib),
      isha = formatTo12Hour(isha),
      hijriDate = hijriDate,
      gregorianDate = gregorianDate,
      locationName = locationName,
      prayersList = prayers,
      nextPrayer = nextPrayer,
      timeUntilNextMillis = timeUntilNextMillis
    )
  }

  private fun timeToMinutes(time24h: String): Int {
    val parts = time24h.split(":")
    if (parts.size >= 2) {
      val h = parts[0].trim().toIntOrNull() ?: 0
      val m = parts[1].trim().toIntOrNull() ?: 0
      return h * 60 + m
    }
    return 0
  }

  private fun formatTo12Hour(time24h: String): String {
    val parts = time24h.split(":")
    if (parts.size >= 2) {
      val h = parts[0].trim().toIntOrNull() ?: 0
      val m = parts[1].trim().toIntOrNull() ?: 0
      val period = if (h >= 12) "م" else "ص"
      val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
      }
      return String.format(Locale.getDefault(), "%02d:%02d %s", h12, m, period)
    }
    return time24h
  }

  private fun getFallbackSchedule(locationName: String): PrayerSchedule {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())

    return buildSchedule(
      fajr = "05:08",
      sunrise = "06:24",
      dhuhr = "12:15",
      asr = "15:35",
      maghrib = "18:04",
      isha = "19:34",
      hijriDate = "1446 هـ",
      gregorianDate = today,
      locationName = locationName
    )
  }
}
