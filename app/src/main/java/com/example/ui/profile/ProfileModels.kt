package com.example.ui.profile

import com.example.R

data class UserProfileData(
  val displayName: String = "ذو الفقار سعدون",
  val username: String = "@zulfiqar_saadoun",
  val bio: String = "مصمم | نسعى لنشر الخير والأثر الطيب وكسب الحسنات الدائمة",
  val followersCount: Int = 1240,
  val followingCount: Int = 184,
  val barakahPoints: Int = 24890,
  val bannerRes: Int = R.drawable.profile_banner_art,
  val avatarRes: Int = R.drawable.profile_avatar_art,
  val isPrivateAccount: Boolean = false,
  val dailyAyahNotification: Boolean = true,
  val prayerTimesNotification: Boolean = true,
  val azkarNotification: Boolean = true,
  val aiStoryNotification: Boolean = true,
  val tasbeehHaptic: Boolean = true,
  val autoNextSurah: Boolean = true
)

enum class ProfileTab(val title: String, val iconEmoji: String) {
  MY_REELS("منشوراتي", ""),
  MY_DONATIONS("حالات التبرع", ""),
  SAVED("المفضلة", "")
}

data class ProfileReelSnippet(
  val id: String,
  val title: String,
  val quote: String,
  val likesCount: Int,
  val viewsCount: Int,
  val imageRes: Int
)

data class SavedItem(
  val id: String,
  val type: String, // "آية كريمة", "ريل إيماني", "ذكر مبارك"
  val title: String,
  val subtitle: String,
  val quoteOrSnippet: String,
  val iconEmoji: String
)

object ProfileDataProvider {
  val defaultReels = listOf(
    ProfileReelSnippet(
      id = "p_reel_1",
      title = "سورة البقرة وتفريج الهموم",
      quote = "﴿ وَبَشِّرِ الصَّابِرِينَ ﴾",
      likesCount = 1420,
      viewsCount = 8900,
      imageRes = R.drawable.img_reel_quran
    ),
    ProfileReelSnippet(
      id = "p_reel_2",
      title = "فضل الصدقة الجارية في صحيفة العبد",
      quote = "« الصَّدَقَةُ تُطْفِئُ الخَطِيئَةَ »",
      likesCount = 2890,
      viewsCount = 14200,
      imageRes = R.drawable.img_reel_mosque
    ),
    ProfileReelSnippet(
      id = "p_reel_3",
      title = "عظمة سورة الرحمن ونعم الله",
      quote = "﴿ فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ ﴾",
      likesCount = 3740,
      viewsCount = 19500,
      imageRes = R.drawable.img_reel_quran
    ),
    ProfileReelSnippet(
      id = "p_reel_4",
      title = "ركعتا الفجر خير من الدنيا وما فيها",
      quote = "« رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا »",
      likesCount = 2150,
      viewsCount = 11300,
      imageRes = R.drawable.img_reel_mosque
    )
  )

  val defaultSavedItems = listOf(
    SavedItem(
      id = "saved_1",
      type = "آية كريمة",
      title = "سورة الكهف - الآية 10",
      subtitle = "إشارة مرجعية محفوظة للقراءة",
      quoteOrSnippet = "﴿ إِذْ أَوَى الْفِتْيَةُ إِلَى الْكَهْفِ فَقَالُوا رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا ﴾",
      iconEmoji = ""
    ),
    SavedItem(
      id = "saved_2",
      type = "ذكر مبارك",
      title = "سيد الاستغفار",
      subtitle = "أذكار الصباح والمساء",
      quoteOrSnippet = "اللّهُـمَّ أَنْتَ رَبِّـي لا إلهَ إلاّ أَنْتَ، خَلَقْتَنـي وَأَنا عَبْـدُك...",
      iconEmoji = ""
    ),
    SavedItem(
      id = "saved_3",
      type = "ريل إيماني",
      title = "أثر الصدقة في السر",
      subtitle = "د. عمر عبد الكافي",
      quoteOrSnippet = "«الصَّدَقَةُ تُطْفِئُ الخَطِيئَةَ كَمَا يُطْفِئُ المَاءُ النَّارَ»",
      iconEmoji = ""
    ),
    SavedItem(
      id = "saved_4",
      type = "مجلس عبرة",
      title = "ثبات بلال بن رباح ونور أحد أحد",
      subtitle = "قصة وعبرة موثوقة",
      quoteOrSnippet = "«أحَدٌ.. أحَدْ.. أحَدٌ.. أحَدْ» صمود الإيمان في رمضاء مكة.",
      iconEmoji = ""
    )
  )
}
