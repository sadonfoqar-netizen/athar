package com.example.ui.azkar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.azkar.PrayerSchedule
import com.example.data.azkar.PrayerTime
import com.example.data.azkar.PrayerTimesService
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
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PrayerTimesScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val service = remember(context) { PrayerTimesService(context) }

  var schedule by remember { mutableStateOf<PrayerSchedule?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var selectedCity by remember { mutableStateOf("مكة المكرمة") }
  var selectedCoords by remember { mutableStateOf(21.4225 to 39.8262) }
  var showCityDialog by remember { mutableStateOf(false) }

  // Countdown timer in seconds
  var countdownSeconds by remember { mutableLongStateOf(0L) }

  LaunchedEffect(selectedCity, selectedCoords) {
    isLoading = true
    val result = service.getPrayerTimes(
      latitude = selectedCoords.first,
      longitude = selectedCoords.second,
      cityName = selectedCity
    )
    schedule = result
    countdownSeconds = result.timeUntilNextMillis / 1000L
    isLoading = false
  }

  // Live countdown ticker
  LaunchedEffect(countdownSeconds) {
    while (countdownSeconds > 0) {
      delay(1000L)
      countdownSeconds--
    }
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
        .testTag("prayer_times_screen")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {

        // Top Header
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
              modifier = Modifier.testTag("prayer_back_button")
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
                text = "مواقيت الصلاة",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharMilkyCream
              )
              Text(
                text = schedule?.hijriDate ?: "مواقيت اليوم المبارك",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharOliveLight
              )
            }
          }

          // City Selector Button
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { showCityDialog = true }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = AtharGold,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = selectedCity,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp,
              color = AtharMilkyCream
            )
          }
        }

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        if (isLoading) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(
                color = AtharGold,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "جاري تحميل مواقيت الصلاة بدقة...",
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                color = AtharMilkyCreamMuted
              )
            }
          }
        } else if (schedule != null) {
          val data = schedule!!

          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {

            // Top Countdown Hero Card
            item {
              Spacer(modifier = Modifier.height(12.dp))
              NextPrayerHeroCard(
                nextPrayer = data.nextPrayer,
                countdownSeconds = countdownSeconds
              )
              Spacer(modifier = Modifier.height(6.dp))
            }

            // Prayer Items List
            items(data.prayersList, key = { it.id }) { prayer ->
              PrayerItemCard(prayer = prayer)
            }

            item {
              Spacer(modifier = Modifier.height(16.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "المواقيت بحسب تقويم أم القرى وموقع ${selectedCity}",
                  fontFamily = CairoFontFamily,
                  fontSize = 11.5.sp,
                  color = AtharMilkyCreamSubtle,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }

      // City Selection Modal Dialog
      if (showCityDialog) {
        CitySelectionDialog(
          currentCity = selectedCity,
          onSelectCity = { city, coords ->
            selectedCity = city
            selectedCoords = coords
            showCityDialog = false
          },
          onDismiss = { showCityDialog = false }
        )
      }
    }
  }
}

@Composable
private fun NextPrayerHeroCard(
  nextPrayer: PrayerTime?,
  countdownSeconds: Long
) {
  val hours = countdownSeconds / 3600
  val minutes = (countdownSeconds % 3600) / 60
  val seconds = countdownSeconds % 60
  val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("next_prayer_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
    border = null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Mosque,
          contentDescription = null,
          tint = AtharGold,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (nextPrayer != null) "الصلاة القادمة: صلاة ${nextPrayer.nameArabic}" else "موعد الصلاة",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = AtharMilkyCream
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = timeFormatted,
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        color = AtharGoldLight,
        letterSpacing = 2.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (nextPrayer != null) "موعد الأذان: ${nextPrayer.timeFormatted}" else "",
        fontFamily = CairoFontFamily,
        fontSize = 13.sp,
        color = AtharMilkyCreamMuted
      )
    }
  }
}

@Composable
private fun PrayerItemCard(
  prayer: PrayerTime
) {
  val icon: ImageVector = when (prayer.id) {
    "fajr" -> Icons.Default.Bedtime
    "sunrise" -> Icons.Default.WbTwilight
    "dhuhr" -> Icons.Default.WbSunny
    "asr" -> Icons.Default.WbSunny
    "maghrib" -> Icons.Default.WbTwilight
    else -> Icons.Default.Bedtime
  }

  val backgroundColor = when {
    prayer.isNext -> AtharOliveContainer
    else -> AtharDarkCard
  }

  val textColor = when {
    prayer.isNext -> AtharGoldLight
    prayer.isPassed -> AtharMilkyCreamSubtle
    else -> AtharMilkyCream
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("prayer_item_${prayer.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (prayer.isNext) AtharGold else AtharOliveLight,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = prayer.nameArabic,
              fontFamily = CairoFontFamily,
              fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.SemiBold,
              fontSize = 15.sp,
              color = textColor
            )
            if (prayer.isNext) {
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "الصلاة القادمة",
                fontFamily = CairoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AtharGold,
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(AtharDarkCard)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = prayer.timeFormatted,
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = textColor
        )
      }
    }
  }
}

@Composable
private fun CitySelectionDialog(
  currentCity: String,
  onSelectCity: (String, Pair<Double, Double>) -> Unit,
  onDismiss: () -> Unit
) {
  val cities = listOf(
    "مكة المكرمة" to (21.4225 to 39.8262),
    "المدينة المنورة" to (24.4672 to 39.6111),
    "الرياض" to (24.7136 to 46.6753),
    "جدة" to (21.4858 to 39.1925),
    "القاهرة" to (30.0444 to 31.2357),
    "القدس الشريف" to (31.7683 to 35.2137),
    "دبي" to (25.2048 to 55.2708),
    "بغداد" to (33.3152 to 44.3661),
    "إسطنبول" to (41.0082 to 28.9784)
  )

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = AtharDarkCardElevated,
      modifier = Modifier.fillMaxWidth(0.9f)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "اختر المدينة لمواقيت الصلاة",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = AtharMilkyCream
        )

        Spacer(modifier = Modifier.height(14.dp))

        cities.forEach { (name, coords) ->
          val isSelected = name == currentCity
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSelectCity(name, coords) }
              .background(if (isSelected) AtharOliveContainer else AtharDarkCard)
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = name,
              fontFamily = CairoFontFamily,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontSize = 14.sp,
              color = if (isSelected) AtharGoldLight else AtharMilkyCream
            )
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AtharGold,
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "إلغاء",
          fontFamily = CairoFontFamily,
          fontSize = 13.sp,
          color = AtharMilkyCreamSubtle,
          modifier = Modifier
            .clickable(onClick = onDismiss)
            .padding(6.dp)
        )
      }
    }
  }
}
