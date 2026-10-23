package com.example.data.games

enum class GameType(
  val title: String,
  val description: String,
  val testTag: String
) {
  FILL_BLANK(
    title = "لعبة املأ الفراغ",
    description = "اكتب أو اختر الكلمة المفقودة لإكمال معنى الآية أو الحديث الشريف.",
    testTag = "game_card_fill_blank"
  ),
  TRUE_FALSE(
    title = "لعبة صح أم خطأ",
    description = "اختبر معلوماتك الدينية والفقهية والسيرة النبوية بإجابة سريعة مع الشرح.",
    testTag = "game_card_true_false"
  ),
  MULTIPLE_CHOICE(
    title = "لعبة الاختيارات من متعدد",
    description = "سؤال إسلامي مفصل مع 4 خيارات لاكتشاف المعلومة الصحيحة والموثقة.",
    testTag = "game_card_multiple_choice"
  ),
  ORDERING(
    title = "ترتيب الأحداث والآيات",
    description = "رتّب الأحداث التاريخية أو كلمات الآيات الكريمة بالتسلسل الزمني والصحيح.",
    testTag = "game_card_ordering"
  )
}

data class GameQuestion(
  val id: String,
  val gameType: GameType,
  val prompt: String,
  val options: List<String> = emptyList(),
  val correctAnswer: String,
  val correctOrder: List<String> = emptyList(),
  val explanation: String,
  val source: String,
  val isFromAi: Boolean = false
)

data class EvaluationResult(
  val isCorrect: Boolean,
  val feedbackTitle: String,
  val explanation: String,
  val recognizedAnswer: String? = null
)
