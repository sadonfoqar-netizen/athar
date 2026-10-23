package com.example.ui.azkar

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.azkar.TasbeehDhikr
import com.example.data.azkar.TasbeehPresets
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
fun TasbeehScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val presets = TasbeehPresets.defaultPresets

  var selectedPreset by remember { mutableStateOf(presets[0]) }
  var currentCount by rememberSaveable { mutableIntStateOf(0) }
  var lapCount by rememberSaveable { mutableIntStateOf(0) }
  var totalSessionCount by rememberSaveable { mutableIntStateOf(0) }
  var isVibrationEnabled by rememberSaveable { mutableStateOf(true) }

  // Target count options (33, 99, 100, 0 for infinite)
  var targetCount by rememberSaveable { mutableIntStateOf(33) }

  val vibrator = remember(context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun triggerHapticFeedback(isLapCompleted: Boolean) {
    if (!isVibrationEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (isLapCompleted) {
          // Stronger double pulse when completing a cycle
          vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1))
        } else {
          vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        }
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(if (isLapCompleted) 70 else 30)
      }
    } catch (_: Exception) {}
  }

  fun incrementCount() {
    val nextCount = currentCount + 1
    totalSessionCount++

    if (targetCount > 0 && nextCount >= targetCount) {
      currentCount = 0
      lapCount++
      triggerHapticFeedback(isLapCompleted = true)
    } else {
      currentCount = nextCount
      triggerHapticFeedback(isLapCompleted = false)
    }
  }

  fun resetCount() {
    currentCount = 0
    lapCount = 0
    totalSessionCount = 0
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
        .testTag("tasbeeh_screen")
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {

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
              modifier = Modifier.testTag("tasbeeh_back_button")
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
                text = "المسبحة الإلكترونية",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharMilkyCream
              )
              Text(
                text = "إجمالي التسبيح: $totalSessionCount",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharOliveLight
              )
            }
          }

          // Top actions: Vibration toggle & Reset
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { isVibrationEnabled = !isVibrationEnabled }
            ) {
              Icon(
                imageVector = Icons.Default.Vibration,
                contentDescription = "الاهتزاز",
                tint = if (isVibrationEnabled) AtharGold else AtharMilkyCreamSubtle,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = { resetCount() },
              modifier = Modifier.testTag("tasbeeh_reset_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "إعادة ضبط",
                tint = AtharMilkyCreamSubtle,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        // Horizontal Dhikr Picker (Free floating modern pills)
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(presets, key = { it.id }) { preset ->
            val isSelected = preset.id == selectedPreset.id
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) AtharOliveContainer else AtharDarkCard)
                .clickable {
                  selectedPreset = preset
                  targetCount = preset.targetCount
                  currentCount = 0
                }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("dhikr_preset_${preset.id}")
            ) {
              Text(
                text = preset.text,
                fontFamily = CairoFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                color = if (isSelected) AtharGoldLight else AtharMilkyCreamMuted
              )
            }
          }
        }

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        // Middle Interactive Tasbeeh Area (Full tap gesture anywhere in center)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = { incrementCount() }
            )
            .testTag("tasbeeh_tap_area"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {

            // Selected Dhikr Title & Reward
            Text(
              text = selectedPreset.text,
              fontFamily = AmiriFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp,
              color = AtharMilkyCream,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 24.dp)
            )

            if (selectedPreset.reward.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = selectedPreset.reward,
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharOliveLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
              )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Giant Circular Digital Tasbeeh Counter
            val progressFraction = if (targetCount > 0) currentCount.toFloat() / targetCount.toFloat() else 0f
            val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "progress")

            Box(
              modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(AtharDarkCardElevated)
                .testTag("tasbeeh_counter_circle"),
              contentAlignment = Alignment.Center
            ) {
              // Outer Glowing Progress Ring
              Canvas(modifier = Modifier.size(220.dp)) {
                // Background Track
                drawArc(
                  color = AtharDividerDark,
                  startAngle = -90f,
                  sweepAngle = 360f,
                  useCenter = false,
                  style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Foreground Progress Arc
                if (targetCount > 0) {
                  drawArc(
                    color = AtharGold,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                  )
                }
              }

              // Counter Display Inside
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "$currentCount",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 58.sp,
                  color = AtharMilkyCream
                )

                Text(
                  text = if (targetCount > 0) "الهدف: $targetCount" else "مستمر",
                  fontFamily = CairoFontFamily,
                  fontSize = 13.sp,
                  color = AtharGoldLight
                )

                if (lapCount > 0) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "الدورة $lapCount",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = AtharOliveLight
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
              text = "المس في أي مكان للتسبيح",
              fontFamily = CairoFontFamily,
              fontSize = 13.sp,
              color = AtharMilkyCreamSubtle
            )
          }
        }

        // Bottom Target Selector (33, 99, 100, مفتوح)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          listOf(33 to "33", 99 to "99", 100 to "100", 0 to "مفتوح").forEach { (count, label) ->
            val isSelected = targetCount == count
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) AtharGold else AtharDarkCard)
                .clickable {
                  targetCount = count
                  currentCount = 0
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
              Text(
                text = label,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isSelected) androidx.compose.ui.graphics.Color.White else AtharMilkyCreamMuted
              )
            }
          }
        }
      }
    }
  }
}
