package com.example.data.quran

/**
 * Categories of Quran Reciters
 */
enum class ReciterCategory(val title: String) {
  HARAMAIN("أئمة الحرمين الشريفين وقراء المملكة"),
  EGYPT_HERITAGE("العمالقة وقراء المدرسة المصرية التراثية"),
  MODERN_YOUTH("أصوات الشباب الخاشعة والتلاوات الحديثة")
}

/**
 * Model representing a Quran Reciter with direct high-speed audio streaming server
 */
data class QuranReciter(
  val id: String,
  val name: String,
  val category: ReciterCategory,
  val serverUrl: String,
  val narration: String = "حفص عن عاصم"
) {
  /**
   * Formats the surah number to 3 digits (e.g., 1 -> "001", 18 -> "018", 114 -> "114")
   * and returns the direct MP3 URL.
   */
  fun getAudioUrl(surahNumber: Int): String {
    val paddedNumber = surahNumber.toString().padStart(3, '0')
    val normalizedBase = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
    return "$normalizedBase$paddedNumber.mp3"
  }
}

/**
 * The Golden List of the most famous and beloved Quran Reciters in the Islamic World
 * with verified 100% direct high-speed audio servers.
 */
val goldenReciters: List<QuranReciter> = listOf(
  // -------------------------------------------------------------
  // أ. أئمة الحرمين الشريفين وقراء المملكة العربية السعودية
  // -------------------------------------------------------------
  QuranReciter(
    id = "afs",
    name = "مشاري راشد العفاسي",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server8.mp3quran.net/afs/"
  ),
  QuranReciter(
    id = "maher",
    name = "ماهر المعيقلي",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server12.mp3quran.net/maher/"
  ),
  QuranReciter(
    id = "sds",
    name = "عبد الرحمن السديس",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server11.mp3quran.net/sds/"
  ),
  QuranReciter(
    id = "shur",
    name = "سعود الشريم",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server7.mp3quran.net/shur/"
  ),
  QuranReciter(
    id = "yasser",
    name = "ياسر الدوسري",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server11.mp3quran.net/yasser/"
  ),
  QuranReciter(
    id = "shatri",
    name = "أبو بكر الشاطري",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server11.mp3quran.net/shatri/"
  ),
  QuranReciter(
    id = "s_gmd",
    name = "سعد الغامدي",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server7.mp3quran.net/s_gmd/"
  ),
  QuranReciter(
    id = "hthfi",
    name = "علي الحذيفي",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server9.mp3quran.net/hthfi/"
  ),
  QuranReciter(
    id = "jhn",
    name = "عبد الله الجهني",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server13.mp3quran.net/jhn/"
  ),
  QuranReciter(
    id = "ayyub",
    name = "محمد أيوب",
    category = ReciterCategory.HARAMAIN,
    serverUrl = "https://server8.mp3quran.net/ayyub/"
  ),

  // -------------------------------------------------------------
  // ب. العمالقة وقراء المدرسة المصرية التراثية
  // -------------------------------------------------------------
  QuranReciter(
    id = "basit",
    name = "عبد الباسط عبد الصمد (مرتل)",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server7.mp3quran.net/basit/"
  ),
  QuranReciter(
    id = "basit_mjwd",
    name = "عبد الباسط عبد الصمد (مجود)",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server13.mp3quran.net/basit_mjwd/",
    narration = "المصحف المجود"
  ),
  QuranReciter(
    id = "minsh",
    name = "محمد صديق المنشاوي (مرتل)",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server10.mp3quran.net/minsh/"
  ),
  QuranReciter(
    id = "minsh_mjwd",
    name = "محمد صديق المنشاوي (مجود)",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server11.mp3quran.net/minsh_mjwd/",
    narration = "المصحف المجود"
  ),
  QuranReciter(
    id = "hsr",
    name = "محمود خليل الحصري",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server13.mp3quran.net/hsr/"
  ),
  QuranReciter(
    id = "mustafa",
    name = "مصطفى إسماعيل",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server8.mp3quran.net/mustafa/"
  ),
  QuranReciter(
    id = "bna",
    name = "محمود علي البنا",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server8.mp3quran.net/bna/"
  ),
  QuranReciter(
    id = "tblwi",
    name = "محمد الطبلاوي",
    category = ReciterCategory.EGYPT_HERITAGE,
    serverUrl = "https://server10.mp3quran.net/tblwi/"
  ),

  // -------------------------------------------------------------
  // ج. أصوات الشباب الخاشعة والتلاوات الحديثة
  // -------------------------------------------------------------
  QuranReciter(
    id = "ajm",
    name = "أحمد بن علي العجمي",
    category = ReciterCategory.MODERN_YOUTH,
    serverUrl = "https://server10.mp3quran.net/ajm/"
  ),
  QuranReciter(
    id = "frs_a",
    name = "فارس عباد",
    category = ReciterCategory.MODERN_YOUTH,
    serverUrl = "https://server8.mp3quran.net/frs_a/"
  ),
  QuranReciter(
    id = "islam",
    name = "إسلام صبحي",
    category = ReciterCategory.MODERN_YOUTH,
    serverUrl = "https://server14.mp3quran.net/islam/Rewayat-Hafs-A'n-Asim/"
  ),
  QuranReciter(
    id = "qtm",
    name = "ناصر القطامي",
    category = ReciterCategory.MODERN_YOUTH,
    serverUrl = "https://server6.mp3quran.net/qtm/"
  ),
  QuranReciter(
    id = "abkar",
    name = "إدريس أبكر",
    category = ReciterCategory.MODERN_YOUTH,
    serverUrl = "https://server6.mp3quran.net/abkar/"
  ),
  QuranReciter(
    id = "hrefe",
    name = "هاني الرفاعي",
    category = ReciterCategory.MODERN_YOUTH,
    serverUrl = "https://server8.mp3quran.net/hrefe/"
  )
)
