package com.example.ui.azkar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.azkar.AzkarCategory
import com.example.data.azkar.AzkarDataProvider
import com.example.data.azkar.ZikrItem
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkCardElevated
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

@Composable
fun DailyAzkarScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedCategory by remember { mutableStateOf(AzkarCategory.MORNING) }

  // State map to hold remaining counts for current category: id -> remaining count
  val remainingCounts = remember { mutableStateMapOf<Int, Int>() }

  // Initialize or change category
  val currentAzkarList = remember(selectedCategory) {
    val list = AzkarDataProvider.getAzkar(selectedCategory)
    list.forEach { item ->
      if (!remainingCounts.containsKey(item.id)) {
        remainingCounts[item.id] = item.initialCount
      }
    }
    list
  }

  // Calculate progress
  val totalZikrs = currentAzkarList.size
  val completedZikrs = currentAzkarList.count { (remainingCounts[it.id] ?: it.initialCount) == 0 }
  val progress = if (totalZikrs > 0) completedZikrs.toFloat() / totalZikrs.toFloat() else 0f

  val vibrator = remember(context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun triggerHapticFeedback() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(35)
      }
    } catch (_: Exception) {}
  }

  BackHandler {
    onBack()
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .testTag("daily_azkar_screen")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {

        // Top Navigation Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("azkar_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = AtharMilkyCream,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "أذكار اليوم والليلة",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharMilkyCream
              )
              Text(
                text = "${selectedCategory.title} (${completedZikrs}/${totalZikrs})",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharOliveLight
              )
            }
          }

          // Reset category button
          IconButton(
            onClick = {
              currentAzkarList.forEach { remainingCounts[it.id] = it.initialCount }
              Toast.makeText(context, "تمت إعادة ضبط العدادات", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("azkar_reset_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "إعادة ضبط",
              tint = AtharMilkyCreamSubtle,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Horizontal Category Switcher Chips
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(AzkarCategory.values()) { category ->
            val isSelected = category == selectedCategory
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) AtharOliveContainer else AtharDarkCard)
                .clickable { selectedCategory = category }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("category_chip_${category.name}")
            ) {
              Text(
                text = category.title,
                fontFamily = CairoFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.5.sp,
                color = if (isSelected) AtharOlivePrimary else AtharMilkyCreamMuted
              )
            }
          }
        }

        // Progress indicator
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          LinearProgressIndicator(
            progress = { progress },
            color = AtharGold,
            trackColor = AtharDividerDark,
            modifier = Modifier
              .fillMaxWidth()
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
          )
        }

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        // List of Azkar
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
          contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(currentAzkarList, key = { it.id }) { item ->
            val remaining = remainingCounts[item.id] ?: item.initialCount
            val isFinished = remaining == 0

            ZikrCardItem(
              item = item,
              remainingCount = remaining,
              isFinished = isFinished,
              onDecrement = {
                if (remaining > 0) {
                  triggerHapticFeedback()
                  remainingCounts[item.id] = remaining - 1
                }
              },
              onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ذكر", "${item.text}\n[${item.reward}]")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "تم نسخ الذكر المبارك", Toast.LENGTH_SHORT).show()
              },
              onShare = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, "${item.text}\n\nفضل الذكر: ${item.reward}\nمن تطبيق أثر")
                }
                context.startActivity(Intent.createChooser(sendIntent, "مشاركة الذكر"))
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ZikrCardItem(
  item: ZikrItem,
  remainingCount: Int,
  isFinished: Boolean,
  onDecrement: () -> Unit,
  onCopy: () -> Unit,
  onShare: () -> Unit
) {
  val cardBg by animateColorAsState(
    targetValue = if (isFinished) AtharOliveContainer.copy(alpha = 0.5f) else AtharDarkCard,
    label = "card_color"
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("zikr_card_${item.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {

      // Zikr Text
      Text(
        text = item.text,
        fontFamily = AmiriFontFamily,
        fontSize = 19.sp,
        lineHeight = 30.sp,
        color = if (isFinished) AtharMilkyCreamMuted else AtharMilkyCream,
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Reward & Reference
      if (item.reward.isNotBlank()) {
        Text(
          text = "الفضل: ${item.reward}",
          fontFamily = CairoFontFamily,
          fontSize = 11.5.sp,
          color = AtharOliveLight,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
      }

      Text(
        text = "المصدر: ${item.reference}",
        fontFamily = CairoFontFamily,
        fontSize = 10.5.sp,
        color = AtharMilkyCreamSubtle,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Action Row: Counter button + Copy & Share icons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {

        // Counter button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFinished) AtharOlivePrimary else AtharGold)
            .clickable(onClick = onDecrement)
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .testTag("zikr_counter_button_${item.id}"),
          contentAlignment = Alignment.Center
        ) {
          if (isFinished) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "اكتمل",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = androidx.compose.ui.graphics.Color.White
              )
            }
          } else {
            Text(
              text = "$remainingCount / ${item.initialCount}",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = androidx.compose.ui.graphics.Color.White
            )
          }
        }

        // Action Icons
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onCopy,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "نسخ",
              tint = AtharMilkyCreamSubtle,
              modifier = Modifier.size(17.dp)
            )
          }

          IconButton(
            onClick = onShare,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "مشاركة",
              tint = AtharMilkyCreamSubtle,
              modifier = Modifier.size(17.dp)
            )
          }
        }
      }
    }
  }
}
