package com.example.data.donations

import android.content.Context
import android.util.Log
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

private const val TAG = "DonationRepository"

class DonationRepository(
  private val context: Context,
  private val db: FirebaseFirestore? = null,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

  private val _donations = MutableStateFlow<List<DonationCase>>(initialCases)
  val donations: StateFlow<List<DonationCase>> = _donations.asStateFlow()

  init {
    listenToDonations()
  }

  private fun listenToDonations() {
    val firestore = db ?: return

    try {
      firestore.collection("donations")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Listen failed: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val list = snapshot.documents.mapNotNull { doc ->
              try {
                val data = doc.data ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                DonationCase(
                  id = doc.id,
                  title = data["title"] as? String ?: "",
                  description = data["description"] as? String ?: "",
                  targetAmount = (data["targetAmount"] as? Number)?.toDouble() ?: 0.0,
                  collectedAmount = (data["collectedAmount"] as? Number)?.toDouble() ?: 0.0,
                  currency = data["currency"] as? String ?: "$",
                  imageUrls = (data["imageUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                  cardNumber = data["cardNumber"] as? String ?: "",
                  bankOrPlatformName = data["bankOrPlatformName"] as? String ?: "MasterCard / Visa",
                  whatsappPhone = data["whatsappPhone"] as? String ?: "",
                  authorId = data["authorId"] as? String ?: "",
                  authorName = data["authorName"] as? String ?: "",
                  category = data["category"] as? String ?: "إغاثة إنسانية",
                  createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
              } catch (e: Exception) {
                Log.e(TAG, "Error parsing doc ${doc.id}", e)
                null
              }
            }
            if (list.isNotEmpty()) {
              _donations.value = list
            }
          } else {
            // Seed initial cases to Firestore
            seedInitialCases(firestore)
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error initiating Firestore listener", e)
    }
  }

  private fun seedInitialCases(firestore: FirebaseFirestore) {
    scope.launch {
      try {
        for (item in initialCases) {
          firestore.collection("donations").document(item.id).set(item).await()
        }
      } catch (e: Exception) {
        Log.w(TAG, "Could not seed initial cases to Firestore", e)
      }
    }
  }

  suspend fun addDonation(case: DonationCase): Result<Unit> {
    val newId = if (case.id.isNotBlank()) case.id else "donation_${System.currentTimeMillis()}"
    val readyCase = case.copy(id = newId, createdAt = System.currentTimeMillis())

    // Update local state immediately
    _donations.update { current ->
      listOf(readyCase) + current.filter { it.id != newId }
    }

    // Persist to Firestore if available
    db?.let { firestore ->
      try {
        firestore.collection("donations").document(newId).set(readyCase).await()
      } catch (e: Exception) {
        Log.w(TAG, "Firestore write failed, kept in local state", e)
      }
    }

    return Result.success(Unit)
  }

  suspend fun recordContribution(donationId: String, amount: Double): Result<Unit> {
    if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))

    var updatedCase: DonationCase? = null

    _donations.update { list ->
      list.map { item ->
        if (item.id == donationId) {
          val next = item.copy(collectedAmount = (item.collectedAmount + amount).coerceAtMost(item.targetAmount * 1.5))
          updatedCase = next
          next
        } else {
          item
        }
      }
    }

    val target = updatedCase ?: return Result.failure(IllegalStateException("Case not found"))

    db?.let { firestore ->
      try {
        firestore.collection("donations").document(donationId)
          .update("collectedAmount", target.collectedAmount)
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Failed to update contribution in Firestore", e)
      }
    }

    return Result.success(Unit)
  }

  companion object {
    val initialCases = listOf(
      DonationCase(
        id = "case_orphans_1",
        title = "كفالة ورعاية تعليمية لـ 15 طفلاً يتيماً",
        description = "مشروع مبارك لتأمين الرعاية الشاملة من مأوى وغذاء وكسوة وحقائب دراسية لأطفال أيتام فقدوا معيلهم، لنصنع لهم مستقبلاً كريماً وننال مرافقة النبي ﷺ في الجنة.",
        targetAmount = 4500.0,
        collectedAmount = 3150.0,
        currency = "$",
        imageUrls = listOf(
          "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?q=80&w=800&auto=format&fit=crop",
          "https://images.unsplash.com/photo-1509099836639-18ba1795216d?q=80&w=800&auto=format&fit=crop"
        ),
        cardNumber = "5324 8812 9043 1187",
        bankOrPlatformName = "MasterCard / حساب الأيتام المعتمد",
        whatsappPhone = "+966501234567",
        authorId = "athar_official",
        authorName = "جمعية الأثر الخيرية",
        category = "كفالة أيتام"
      ),
      DonationCase(
        id = "case_water_well_2",
        title = "حفر بئر ماء ارتوازي وبناء محطة تنقية لقرية كاملة",
        description = "سقيا الماء أعظم الصدقات الجارية؛ نوفر مياهاً صالحة للشرب النقية لأكثر من 600 نسمة في منطقة تعاني الجفاف وقطع المسافات الطويلة بحثاً عن الماء العذب.",
        targetAmount = 3200.0,
        collectedAmount = 2100.0,
        currency = "$",
        imageUrls = listOf(
          "https://images.unsplash.com/photo-1541888946425-d0fbb186c5f7?q=80&w=800&auto=format&fit=crop",
          "https://images.unsplash.com/photo-1500382017468-9049fed747ef?q=80&w=800&auto=format&fit=crop"
        ),
        cardNumber = "4102 7745 2231 9901",
        bankOrPlatformName = "Visa / حساب سقيا الماء",
        whatsappPhone = "+966509876543",
        authorId = "athar_official",
        authorName = "فريق الأثر الإغاثي",
        category = "سقيا ماء"
      ),
      DonationCase(
        id = "case_medical_3",
        title = "إجراء عمليات جراحية عاجلة وأدوية لأطفال مرضى",
        description = "مساعدة أسر متعففة عاجزة عن تحمل تكاليف عمليات قسطرة قلب وفحوصات تخصصية لأطفال رضع في مشافي الأطفال التخصصية لإعادة البسمة إلى قلوبهم.",
        targetAmount = 5800.0,
        collectedAmount = 4350.0,
        currency = "$",
        imageUrls = listOf(
          "https://images.unsplash.com/photo-1579684385127-1ef15d508118?q=80&w=800&auto=format&fit=crop",
          "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?q=80&w=800&auto=format&fit=crop"
        ),
        cardNumber = "5200 4410 8823 6644",
        bankOrPlatformName = "محفظة الإغاثة الطبية / ZainCash",
        whatsappPhone = "+966551122334",
        authorId = "athar_official",
        authorName = "لجنة الرعاية الصحية",
        category = "علاج مريض"
      ),
      DonationCase(
        id = "case_house_4",
        title = "ترميم وتأثيث سقف منزل أرملة وأطفالها قبل الشتاء",
        description = "تأهيل سقف متهالك يغرق في الأمطار الشتوية وتوفير التدفئة وأفرشة نوم كريمة لأم أرملة وأطفالها الأربعة ليقيهم قسوة البرد والمطر.",
        targetAmount = 2400.0,
        collectedAmount = 950.0,
        currency = "$",
        imageUrls = listOf(
          "https://images.unsplash.com/photo-1513694203232-719a280e022f?q=80&w=800&auto=format&fit=crop"
        ),
        cardNumber = "4532 9900 1122 3344",
        bankOrPlatformName = "حساب إعمار المساكن / Visa",
        whatsappPhone = "+966543210987",
        authorId = "athar_official",
        authorName = "صندوق التكافل المجتمعي",
        category = "ترميم منازل"
      )
    )
  }
}
