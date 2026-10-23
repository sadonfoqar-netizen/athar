package com.example.data.donations

data class DonationCase(
  val id: String = "",
  val title: String = "",
  val description: String = "",
  val targetAmount: Double = 0.0,
  val collectedAmount: Double = 0.0,
  val currency: String = "$",
  val imageUrls: List<String> = emptyList(),
  val cardNumber: String = "",
  val bankOrPlatformName: String = "MasterCard / Visa",
  val whatsappPhone: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val category: String = "إغاثة إنسانية",
  val createdAt: Long = System.currentTimeMillis()
) {
  val progressFraction: Float
    get() = if (targetAmount > 0) (collectedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

  val percentage: Int
    get() = (progressFraction * 100).toInt()

  val remainingAmount: Double
    get() = (targetAmount - collectedAmount).coerceAtLeast(0.0)

  val isCompleted: Boolean
    get() = collectedAmount >= targetAmount && targetAmount > 0
}
