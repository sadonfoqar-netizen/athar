package com.example.data.azkar

data class TasbeehDhikr(
  val id: Int,
  val text: String,
  val targetCount: Int = 33,
  val reward: String = ""
)

object TasbeehPresets {
  val defaultPresets = listOf(
    TasbeehDhikr(
      id = 1,
      text = "سبحان الله",
      targetCount = 33,
      reward = "تسبيح يملأ ما بين السماء والأرض"
    ),
    TasbeehDhikr(
      id = 2,
      text = "الحمد لله",
      targetCount = 33,
      reward = "الحمد يملأ الميزان بالخير والبركة"
    ),
    TasbeehDhikr(
      id = 3,
      text = "لا إله إلا الله",
      targetCount = 100,
      reward = "أفضل الذكر وخير ما قال النبيون"
    ),
    TasbeehDhikr(
      id = 4,
      text = "الله أكبر",
      targetCount = 34,
      reward = "تكبير وتعظيم لشأن رب العالمين"
    ),
    TasbeehDhikr(
      id = 5,
      text = "سبحان الله وبحمده، سبحان الله العظيم",
      targetCount = 100,
      reward = "كلمتان خفيفتان على اللسان، ثقيلتان في الميزان"
    ),
    TasbeehDhikr(
      id = 6,
      text = "أستغفر الله وأتوب إليه",
      targetCount = 100,
      reward = "ممحاة للذنوب ومجلبة للرزق والفرج"
    ),
    TasbeehDhikr(
      id = 7,
      text = "لا حول ولا قوة إلا بالله العلي العظيم",
      targetCount = 33,
      reward = "كنز من كنوز الجنة ودواء للهم"
    ),
    TasbeehDhikr(
      id = 8,
      text = "اللهم صلِّ وسلِّم على نبينا محمد",
      targetCount = 100,
      reward = "من صلى عليَّ صلاة صلى الله عليه بها عشراً"
    ),
    TasbeehDhikr(
      id = 9,
      text = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
      targetCount = 10,
      reward = "كانت له عدل عشر رقاب وكُتبت له مائة حسنة"
    )
  )
}
