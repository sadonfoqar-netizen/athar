package com.example.ui.azkar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.azkar.AiDailyStory
import com.example.data.azkar.AiStoryRepository
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
import kotlinx.coroutines.launch
import java.util.Locale

enum class AzkarSubScreen {
  NONE,
  PRAYER_TIMES,
  DAILY_AZKAR,
  QIBLA,
  TASBEEH,
  STORY_DETAIL
}

@Composable
fun AzkarMainScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val storyRepository = remember(context) { AiStoryRepository(context) }

  var currentSubScreen by rememberSaveable { mutableStateOf(AzkarSubScreen.NONE) }
  var currentStory by remember { mutableStateOf<AiDailyStory?>(null) }
  var isLoadingStory by remember { mutableStateOf(true) }
  var countdownMillis by remember { mutableLongStateOf(0L) }

  LaunchedEffect(Unit) {
    isLoadingStory = true
    currentStory = storyRepository.getActiveStory()
    countdownMillis = storyRepository.getTimeUntilNextStoryMillis()
    isLoadingStory = false
  }

  // Countdown timer ticker
  LaunchedEffect(countdownMillis) {
    while (countdownMillis > 0) {
      delay(1000L)
      countdownMillis -= 1000L
      if (countdownMillis <= 0) {
        currentStory = storyRepository.getActiveStory(forceRefresh = true)
        countdownMillis = storyRepository.getTimeUntilNextStoryMillis()
      }
    }
  }

  // Handle sub-screen routing
  AnimatedContent(
    targetState = currentSubScreen,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "azkar_subscreen_transition"
  ) { subScreen ->
    when (subScreen) {
      AzkarSubScreen.PRAYER_TIMES -> {
        PrayerTimesScreen(
          onBack = { currentSubScreen = AzkarSubScreen.NONE },
          modifier = modifier
        )
      }
      AzkarSubScreen.DAILY_AZKAR -> {
        DailyAzkarScreen(
          onBack = { currentSubScreen = AzkarSubScreen.NONE },
          modifier = modifier
        )
      }
      AzkarSubScreen.QIBLA -> {
        QiblaCompassScreen(
          onBack = { currentSubScreen = AzkarSubScreen.NONE },
          modifier = modifier
        )
      }
      AzkarSubScreen.TASBEEH -> {
        TasbeehScreen(
          onBack = { currentSubScreen = AzkarSubScreen.NONE },
          modifier = modifier
        )
      }
      AzkarSubScreen.STORY_DETAIL -> {
        if (currentStory != null) {
          AiStoryDetailScreen(
            story = currentStory!!,
            onBack = { currentSubScreen = AzkarSubScreen.NONE },
            modifier = modifier
          )
        } else {
          currentSubScreen = AzkarSubScreen.NONE
        }
      }
      AzkarSubScreen.NONE -> {
        AzkarDashboardContent(
          currentStory = currentStory,
          isLoadingStory = isLoadingStory,
          countdownMillis = countdownMillis,
          onOpenPrayerTimes = { currentSubScreen = AzkarSubScreen.PRAYER_TIMES },
          onOpenDailyAzkar = { currentSubScreen = AzkarSubScreen.DAILY_AZKAR },
          onOpenQibla = { currentSubScreen = AzkarSubScreen.QIBLA },
          onOpenTasbeeh = { currentSubScreen = AzkarSubScreen.TASBEEH },
          onOpenStoryDetail = { currentSubScreen = AzkarSubScreen.STORY_DETAIL },
          onRefreshStory = {
            scope.launch {
              isLoadingStory = true
              currentStory = storyRepository.getActiveStory(forceRefresh = true)
              countdownMillis = storyRepository.getTimeUntilNextStoryMillis()
              isLoadingStory = false
            }
          },
          modifier = modifier
        )
      }
    }
  }
}

/**
 * Main Azkar Dashboard:
 * 1. Top 4 Action Buttons (Prayer Times, Azkar, Qibla, Tasbeeh)
 * 2. AI Daily Stories container with 8-hour auto refresh
 */
@Composable
private fun AzkarDashboardContent(
  currentStory: AiDailyStory?,
  isLoadingStory: Boolean,
  countdownMillis: Long,
  onOpenPrayerTimes: () -> Unit,
  onOpenDailyAzkar: () -> Unit,
  onOpenQibla: () -> Unit,
  onOpenTasbeeh: () -> Unit,
  onOpenStoryDetail: () -> Unit,
  onRefreshStory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .testTag("azkar_dashboard")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
      ) {

        // Top Header Title
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "الأذكار والعبادات",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp,
              color = AtharMilkyCream
            )
            Text(
              text = "زاد المسلم اليومي ومجالس العبرة",
              fontFamily = CairoFontFamily,
              fontSize = 12.sp,
              color = AtharOliveLight
            )
          }
        }

        // 1. Top Action Bar: 4 Transparent Buttons with Gold/Olive Icons
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          TopServiceButton(
            title = "مواقيت الصلاة",
            icon = Icons.Default.Mosque,
            onClick = onOpenPrayerTimes,
            testTag = "service_prayer_times"
          )
          TopServiceButton(
            title = "أذكار اليوم",
            icon = Icons.Default.WbSunny,
            onClick = onOpenDailyAzkar,
            testTag = "service_daily_azkar"
          )
          TopServiceButton(
            title = "بوصلة القبلة",
            icon = Icons.Default.Explore,
            onClick = onOpenQibla,
            testTag = "service_qibla"
          )
          TopServiceButton(
            title = "المسبحة",
            icon = Icons.Default.AllInclusive,
            onClick = onOpenTasbeeh,
            testTag = "service_tasbeeh"
          )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // 2. Rest of Screen: "مجالس العبرة بالذكاء الاصطناعي"
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {

          // Section Header with Countdown Timer
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AtharGold,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "مجالس العبرة بالذكاء الاصطناعي",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AtharMilkyCream
              )
            }

            // Manual Refresh
            IconButton(
              onClick = onRefreshStory,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "تحديث القصة",
                tint = AtharMilkyCreamSubtle,
                modifier = Modifier.size(17.dp)
              )
            }
          }

          // Countdown Pill
          val hours = (countdownMillis / (1000 * 60 * 60)).toInt()
          val minutes = ((countdownMillis / (1000 * 60)) % 60).toInt()
          val seconds = ((countdownMillis / 1000) % 60).toInt()
          val countdownStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = AtharOliveLight,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "تتجدد القصة مرتين يومياً (1:00 ص و 1:00 م) • القادمة بعد: $countdownStr",
              fontFamily = CairoFontFamily,
              fontSize = 11.5.sp,
              color = AtharMilkyCreamMuted
            )
          }

          // Story Card
          if (isLoadingStory) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AtharDarkCard),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                  color = AtharGold,
                  strokeWidth = 2.5.dp,
                  modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "جاري تحضير القصة والعبرة...",
                  fontFamily = CairoFontFamily,
                  fontSize = 12.5.sp,
                  color = AtharMilkyCreamMuted
                )
              }
            }
          } else if (currentStory != null) {
            val story = currentStory

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onOpenStoryDetail)
                .testTag("ai_story_card"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
              border = null
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(18.dp)
              ) {

                // Category Tag
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AtharOliveContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = story.category,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = AtharGoldLight
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                  text = story.title,
                  fontFamily = AmiriFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 24.sp,
                  lineHeight = 32.sp,
                  color = AtharGold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Excerpt snippet
                val excerpt = story.content.take(180) + "..."
                Text(
                  text = excerpt,
                  fontFamily = CairoFontFamily,
                  fontSize = 13.5.sp,
                  lineHeight = 22.sp,
                  color = AtharMilkyCream,
                  textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Lesson Teaser Box
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AtharDarkCard)
                    .padding(12.dp)
                ) {
                  Row(verticalAlignment = Alignment.Top) {
                    Icon(
                      imageVector = Icons.Default.Lightbulb,
                      contentDescription = null,
                      tint = AtharGold,
                      modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = story.lesson.take(110) + "...",
                      fontFamily = CairoFontFamily,
                      fontSize = 12.sp,
                      lineHeight = 18.sp,
                      color = AtharOliveLight
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Read full story button
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "اقرأ القصة والعبرة كاملة ←",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = AtharGoldLight
                  )

                  Text(
                    text = "وقت القراءة: دقيقتان",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = AtharMilkyCreamSubtle
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

/**
 * Top Transparent Service Button without heavy borders or solid circular backgrounds
 */
@Composable
private fun TopServiceButton(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  testTag: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 8.dp)
      .testTag(testTag)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = AtharOlivePrimary,
      modifier = Modifier.size(26.dp)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      fontFamily = CairoFontFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 11.5.sp,
      color = AtharMilkyCream,
      textAlign = TextAlign.Center
    )
  }
}
