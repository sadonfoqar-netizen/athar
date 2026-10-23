package com.example.data.quran

data class SurahMeta(
  val number: Int,
  val name: String,
  val englishName: String,
  val englishNameTranslation: String,
  val numberOfAyahs: Int,
  val revelationType: String
) {
  val isMeccan: Boolean get() = revelationType.equals("Meccan", ignoreCase = true)
  val revelationTypeArabic: String get() = if (isMeccan) "مكية" else "مدنية"
}

data class Ayah(
  val number: Int,
  val numberInSurah: Int,
  val text: String,
  val juz: Int,
  val page: Int,
  val sajda: Boolean = false
)

data class SurahDetail(
  val number: Int,
  val name: String,
  val englishName: String,
  val revelationType: String,
  val numberOfAyahs: Int,
  val ayahs: List<Ayah>
)

data class QuranBookmark(
  val surahNumber: Int,
  val surahName: String,
  val ayahNumberInSurah: Int,
  val timestamp: Long = System.currentTimeMillis()
)

enum class PlaybackMode {
  AUTO_NEXT,    // الانتقال التلقائي للسورة التالية
  LOOP_SURAH,   // تكرار السورة الحالية
  STOP_ON_END   // التوقف عند نهاية السورة
}
