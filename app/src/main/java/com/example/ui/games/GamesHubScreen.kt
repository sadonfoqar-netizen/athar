package com.example.ui.games

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.games.GameRepository
import com.example.data.games.GameType
import com.example.ui.theme.AtharDeepBlack
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharMilkyCreamBg
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharTextMuted
import com.example.ui.theme.CairoFontFamily

@Composable
fun GamesHubScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repository = remember(context) { GameRepository(context, scope) }

  var activeGame by rememberSaveable { mutableStateOf<String?>(null) }
  val activeGameType = activeGame?.let { name ->
    try { GameType.valueOf(name) } catch (_: Exception) { null }
  }

  AnimatedContent(
    targetState = activeGameType,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "games_screen_transition"
  ) { selectedGame ->
    if (selectedGame != null) {
      GamePlayScreen(
        gameType = selectedGame,
        repository = repository,
        onBack = { activeGame = null },
        modifier = modifier
      )
    } else {
      GamesHubMainView(
        repository = repository,
        onSelectGame = { type -> activeGame = type.name },
        modifier = modifier
      )
    }
  }
}

@Composable
private fun GamesHubMainView(
  repository: GameRepository,
  onSelectGame: (GameType) -> Unit,
  modifier: Modifier = Modifier
) {
  val totalScore by repository.score.collectAsState()
  val availableCounts by repository.availableCounts.collectAsState()
  val isReplenishing by repository.isReplenishing.collectAsState()
  val isOnline = remember { repository.isOnline() }
  val scrollState = rememberScrollState()

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharMilkyCreamBg)
        .statusBarsPadding()
        .testTag("games_hub_screen")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 18.dp, vertical = 14.dp)
      ) {

        // Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "المسابقات والألعاب الإيمانية",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp,
              color = AtharDeepBlack
            )
            Text(
              text = "زاد معرفي وثواب متصل بإذن الله",
              fontFamily = CairoFontFamily,
              fontSize = 12.sp,
              color = AtharOlivePrimary
            )
          }

          // Total Score Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White)
              .border(1.dp, AtharGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("games_total_score_badge")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AtharGold,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$totalScore نقطة",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = AtharGoldDark
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cache & Connectivity Status Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isOnline) AtharOliveContainer.copy(alpha = 0.7f) else Color(0xFFF4ECE1))
            .border(
              1.dp,
              if (isOnline) AtharOlivePrimary.copy(alpha = 0.3f) else AtharGold.copy(alpha = 0.3f),
              RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = null,
                tint = if (isOnline) AtharOlivePrimary else AtharTextMuted,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (isOnline) "نظام التحديث الذكي متصل ومفعّل" else "وضع عدم الاتصال (أوفلاين)",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.5.sp,
                  color = AtharDeepBlack
                )
                Text(
                  text = if (isOnline) "يتم تعويض أي سؤال تستهلكه بالذكاء الاصطناعي ليبقى الرصيد 100 سؤال دائماً" else "تعمل الألعاب بالكامل من مخزون الـ 100 سؤال المحلي",
                  fontFamily = CairoFontFamily,
                  fontSize = 11.sp,
                  color = AtharTextMuted
                )
              }
            }

            if (isReplenishing) {
              CircularProgressIndicator(
                color = AtharOlivePrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4 Game Cards Grid/Stream
        GameCardItem(
          title = "لعبة املأ الفراغ",
          description = "اكتب أو اختر الكلمة المفقودة لإكمال معنى الآية أو الحديث الشريف.",
          icon = Icons.Default.EditNote,
          availableCount = availableCounts[GameType.FILL_BLANK] ?: 100,
          onClick = { onSelectGame(GameType.FILL_BLANK) },
          testTag = "game_card_fill_blank"
        )

        Spacer(modifier = Modifier.height(14.dp))

        GameCardItem(
          title = "لعبة صح أم خطأ",
          description = "اختبر معلوماتك الدينية والفقهية والسيرة النبوية بإجابة سريعة مع الشرح الفوري.",
          icon = Icons.AutoMirrored.Filled.Rule,
          availableCount = availableCounts[GameType.TRUE_FALSE] ?: 100,
          onClick = { onSelectGame(GameType.TRUE_FALSE) },
          testTag = "game_card_true_false"
        )

        Spacer(modifier = Modifier.height(14.dp))

        GameCardItem(
          title = "لعبة الاختيارات من متعدد",
          description = "سؤال إسلامي مفصل مع 4 خيارات لاكتشاف وتأكيد المعلومة الصحيحة والموثقة.",
          icon = Icons.AutoMirrored.Filled.FormatListBulleted,
          availableCount = availableCounts[GameType.MULTIPLE_CHOICE] ?: 100,
          onClick = { onSelectGame(GameType.MULTIPLE_CHOICE) },
          testTag = "game_card_multiple_choice"
        )

        Spacer(modifier = Modifier.height(14.dp))

        GameCardItem(
          title = "ترتيب الأحداث والآيات",
          description = "رتّب الأحداث التاريخية والغزوات أو كلمات الآيات الكريمة بالتسلسل الزمني والصحيح.",
          icon = Icons.Default.LowPriority,
          availableCount = availableCounts[GameType.ORDERING] ?: 100,
          onClick = { onSelectGame(GameType.ORDERING) },
          testTag = "game_card_ordering"
        )

        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

@Composable
private fun GameCardItem(
  title: String,
  description: String,
  icon: ImageVector,
  availableCount: Int,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.2.dp, AtharGold.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(AtharOliveContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = AtharOlivePrimary,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = title,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 17.5.sp,
              color = AtharDeepBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Questions Available Badge
            Text(
              text = "المخزون المتوفر: $availableCount / 100 سؤال",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Medium,
              fontSize = 11.5.sp,
              color = AtharOlivePrimary
            )
          }
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "دخول اللعبة",
          tint = AtharGold,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = description,
        fontFamily = CairoFontFamily,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = AtharTextMuted
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Progress bar for the 100 pool
      LinearProgressIndicator(
        progress = { availableCount / 100f },
        color = AtharOlivePrimary,
        trackColor = AtharOliveContainer,
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp))
      )
    }
  }
}
