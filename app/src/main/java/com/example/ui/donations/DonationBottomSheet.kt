package com.example.ui.donations

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.donations.DonationCase
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkCardElevated
import com.example.ui.theme.AtharDarkInputBg
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationBottomSheet(
  donationCase: DonationCase,
  onDismiss: () -> Unit,
  onRecordContribution: (Double) -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var customAmountStr by remember { mutableStateOf("") }
  var selectedQuickAmount by remember { mutableDoubleStateOf(25.0) }

  fun copyToClipboard(text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ رقم البطاقة بنجاح", Toast.LENGTH_SHORT).show()
  }

  fun openWhatsApp() {
    val phone = donationCase.whatsappPhone.replace(" ", "").replace("-", "")
    val message = "السلام عليكم، أود تأكيد التحويل والمساهمة في حالة التبرع: ${donationCase.title}"
    val encodedMsg = try {
      URLEncoder.encode(message, "UTF-8")
    } catch (_: Exception) {
      ""
    }

    try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedMsg")
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "تعذر فتح تطبيق واتساب، رقم الهاتف: $phone", Toast.LENGTH_LONG).show()
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = sheetState,
      containerColor = AtharDarkCardElevated,
      contentColor = AtharMilkyCream,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      dragHandle = null
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 20.dp)
          .navigationBarsPadding()
          .testTag("donation_bottom_sheet")
      ) {

        // Sheet Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "طرق التبرع والمساهمة",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = AtharMilkyCream
            )
            Text(
              text = donationCase.title,
              fontFamily = CairoFontFamily,
              fontSize = 12.sp,
              color = AtharOliveLight,
              maxLines = 1
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "إغلاق",
              tint = AtharMilkyCreamSubtle,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Financial Account / Card Number Section (Click to Copy)
        Text(
          text = "1. رقم الحساب المالي / البطاقة (${donationCase.bankOrPlatformName})",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = AtharGold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AtharDarkCard)
            .border(1.dp, AtharGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { copyToClipboard(donationCase.cardNumber, "رقم البطاقة") }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("copy_card_box")
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = AtharGold,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = donationCase.cardNumber,
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  letterSpacing = 1.sp,
                  color = AtharMilkyCream
                )
                Text(
                  text = "المس لنسخ الرقم فوراً",
                  fontFamily = CairoFontFamily,
                  fontSize = 10.5.sp,
                  color = AtharOliveLight
                )
              }
            }

            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "نسخ",
              tint = AtharGoldLight,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. WhatsApp Direct Contact Section
        Text(
          text = "2. تأكيد التحويل والتواصل عبر واتساب",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = AtharGold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AtharOliveContainer)
            .clickable { openWhatsApp() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("whatsapp_contact_button")
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = AtharOliveLight,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "محادثة فورية مع المشرف: ${donationCase.whatsappPhone}",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.5.sp,
                  color = AtharMilkyCream
                )
                Text(
                  text = "إرسال إشعار التحويل لتحديث العداد",
                  fontFamily = CairoFontFamily,
                  fontSize = 11.sp,
                  color = AtharOliveLight
                )
              }
            }

            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "فتح واتساب",
              tint = AtharOliveLight,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Fast Contribution Amount Picker
        Text(
          text = "3. حدد مبلغ مساهمتك لتسجيل الأثر",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = AtharGold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(10.0 to "$10", 25.0 to "$25", 50.0 to "$50", 100.0 to "$100").forEach { (amt, label) ->
            val isSelected = selectedQuickAmount == amt && customAmountStr.isBlank()
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) AtharGold else AtharDarkCard)
                .clickable {
                  selectedQuickAmount = amt
                  customAmountStr = ""
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) androidx.compose.ui.graphics.Color.White else AtharMilkyCream
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Record Contribution Action Button
        Button(
          onClick = {
            val finalAmount = customAmountStr.toDoubleOrNull() ?: selectedQuickAmount
            if (finalAmount > 0) {
              onRecordContribution(finalAmount)
              Toast.makeText(context, "جزاك الله خيراً، تم تسجيل مساهمتك المباركة بمبلغ $finalAmount$", Toast.LENGTH_LONG).show()
              onDismiss()
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AtharOlivePrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_contribution_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = AtharGoldLight,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val displayAmt = customAmountStr.toDoubleOrNull() ?: selectedQuickAmount
            Text(
              text = "سجّل مساهمتي ($displayAmt$) وحسناتي",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.5.sp,
              color = AtharMilkyCream
            )
          }
        }
      }
    }
  }
}
