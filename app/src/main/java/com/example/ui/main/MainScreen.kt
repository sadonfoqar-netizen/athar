package com.example.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiChatRepository
import com.example.data.quran.QuranBookmark
import com.example.data.quran.QuranRepository
import com.example.data.quran.SurahMeta
import com.example.ui.ai.AiChatScreen
import com.example.ui.auth.AuthUiState
import com.example.ui.azkar.AzkarMainScreen
import com.example.ui.quran.QuranAudioPlayer
import com.example.ui.quran.QuranReaderScreen
import com.example.ui.quran.SurahListScreen
import com.example.ui.theme.AtharDeepBlack
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCreamBg
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily
import com.google.firebase.firestore.FirebaseFirestore

/**
 * The 3 Bottom Navigation Tabs for Athar:
 * 1. Right: Azkar & Worship (الأذكار والعبادات)
 * 2. Center: AI Assistant (الذكاء الاصطناعي - مركز التطبيق الذكي)
 * 3. Left: Holy Quran (القرآن الكريم)
 */
enum class MainTab(val title: String) {
  AZKAR("الأذكار والعبادات"),
  AI_CHAT("الذكاء الاصطناعي"),
  QURAN("القرآن الكريم")
}

@Composable
fun MainScreen(
  authUiState: AuthUiState,
  onSignOut: () -> Unit = {},
  onReplaySplash: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var selectedTab by rememberSaveable { mutableStateOf(MainTab.QURAN) }
  var lastNonAiTab by rememberSaveable { mutableStateOf(MainTab.QURAN) }

  val aiChatRepository = remember(context) {
    val databaseId = try {
      context.getString(com.example.R.string.firestore_database_id)
    } catch (_: Exception) {
      null
    }
    val firestore = if (!databaseId.isNullOrBlank()) {
      try {
        FirebaseFirestore.getInstance(databaseId)
      } catch (_: Exception) {
        null
      }
    } else {
      null
    }
    AiChatRepository(context = context, db = firestore, scope = scope)
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Scaffold(
      modifier = modifier
        .fillMaxSize()
        .background(AtharMilkyCreamBg)
        .testTag("main_screen"),
      containerColor = AtharMilkyCreamBg,
      bottomBar = {
        // Show bottom navigation bar on all tabs except during Full Screen AI Chat
        if (selectedTab != MainTab.AI_CHAT) {
          AtharBottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
              if (tab != MainTab.AI_CHAT) {
                lastNonAiTab = tab
              }
              selectedTab = tab
            }
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(if (selectedTab == MainTab.AI_CHAT) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding),
        contentAlignment = Alignment.Center
      ) {
        // Active Container Screen
        AnimatedContent(
          targetState = selectedTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "container_transition"
        ) { currentTab ->
          when (currentTab) {
            MainTab.AZKAR -> {
              AzkarMainScreen(
                modifier = Modifier.fillMaxSize()
              )
            }
            MainTab.AI_CHAT -> {
              AiChatScreen(
                repository = aiChatRepository,
                onBackClick = { selectedTab = lastNonAiTab },
                modifier = Modifier.fillMaxSize()
              )
            }
            MainTab.QURAN -> {
              QuranContainerScreen(
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Host Composable for the Holy Quran Container
 * Seamlessly manages Surahs list, Reader screen, and active Bookmarks
 */
@Composable
fun QuranContainerScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val quranRepository = remember(context) { QuranRepository(context) }
  val audioPlayer = remember(scope) { QuranAudioPlayer(scope) }

  var surahs by remember { mutableStateOf<List<SurahMeta>>(emptyList()) }
  var currentBookmark by remember { mutableStateOf<QuranBookmark?>(null) }
  var selectedSurahNumber by rememberSaveable { mutableStateOf<Int?>(null) }
  var targetAyahNumber by rememberSaveable { mutableStateOf<Int?>(null) }

  LaunchedEffect(Unit) {
    surahs = quranRepository.getSurahList()
    currentBookmark = quranRepository.getBookmark()
  }

  val selectedSurahMeta = remember(selectedSurahNumber, surahs) {
    selectedSurahNumber?.let { num ->
      surahs.find { it.number == num }
    }
  }

  if (selectedSurahMeta != null) {
    QuranReaderScreen(
      surahMeta = selectedSurahMeta,
      initialAyahNumber = targetAyahNumber,
      quranRepository = quranRepository,
      audioPlayer = audioPlayer,
      surahs = surahs,
      currentBookmark = currentBookmark,
      onBookmarkUpdated = { newBookmark ->
        currentBookmark = newBookmark
      },
      onNavigateToSurah = { nextMeta ->
        selectedSurahNumber = nextMeta.number
        targetAyahNumber = null
      },
      onBack = {
        selectedSurahNumber = null
        targetAyahNumber = null
      },
      modifier = modifier
    )
  } else {
    SurahListScreen(
      surahs = surahs,
      currentBookmark = currentBookmark,
      onSurahClick = { surah, targetAyah ->
        selectedSurahNumber = surah.number
        targetAyahNumber = targetAyah
      },
      modifier = modifier
    )
  }
}

/**
 * Minimal Flat Bottom Navigation Bar:
 * - Pure milky cream background with subtle divider line.
 * - Exactly 3 free-floating icons without any boxes, circles, or borders.
 * - Center Sparkles (AutoAwesome) icon in pure radiant gold, prominently elevated as the AI hub.
 */
@Composable
private fun AtharBottomNavigationBar(
  selectedTab: MainTab,
  onTabSelected: (MainTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .drawBehind {
        drawLine(
          color = AtharDividerDark,
          start = Offset(0f, 0f),
          end = Offset(size.width, 0f),
          strokeWidth = 1.dp.toPx()
        )
      }
      .background(AtharMilkyCreamBg)
      .navigationBarsPadding()
      .height(64.dp)
      .testTag("bottom_nav_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {

      // 1. Right (RTL): Azkar & Worship (الأذكار والعبادات)
      BottomNavItem(
        icon = Icons.Default.AllInclusive,
        contentDescription = "الأذكار والعبادات",
        selected = selectedTab == MainTab.AZKAR,
        onClick = { onTabSelected(MainTab.AZKAR) },
        testTag = "nav_azkar"
      )

      // 2. Center: AI Assistant (أيقونة النجمات الثلاث - Sparkles باللون الذهبي الخالص)
      BottomNavItem(
        icon = Icons.Default.AutoAwesome,
        contentDescription = "المساعد الإيماني الذكي",
        selected = selectedTab == MainTab.AI_CHAT,
        isCenter = true,
        onClick = { onTabSelected(MainTab.AI_CHAT) },
        testTag = "nav_ai_chat"
      )

      // 3. Left (RTL): Holy Quran (القرآن الكريم)
      BottomNavItem(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        contentDescription = "القرآن الكريم",
        selected = selectedTab == MainTab.QURAN,
        onClick = { onTabSelected(MainTab.QURAN) },
        testTag = "nav_quran"
      )
    }
  }
}

/**
 * Free-floating Bottom Nav Item:
 * ZERO borders, ZERO background circles, ZERO boxes.
 * Elevates upward and grows larger in elegant Olive Green / Pure Gold when active.
 */
@Composable
private fun BottomNavItem(
  icon: ImageVector,
  contentDescription: String,
  selected: Boolean,
  isCenter: Boolean = false,
  onClick: () -> Unit,
  testTag: String
) {
  // Animated vertical pop-up offset (center sparkles pops up more prominently)
  val targetOffset = when {
    selected && isCenter -> (-10).dp
    selected -> (-7).dp
    isCenter -> (-3).dp
    else -> 0.dp
  }

  val offsetY by animateDpAsState(
    targetValue = targetOffset,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "nav_offset"
  )

  // Animated icon size (center sparkles is larger and clearly distinguished)
  val baseSelectedSize = if (isCenter) 36.dp else 28.dp
  val baseUnselectedSize = if (isCenter) 30.dp else 24.dp

  val iconSize by animateDpAsState(
    targetValue = if (selected) baseSelectedSize else baseUnselectedSize,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "nav_icon_size"
  )

  val interactionSource = remember { MutableInteractionSource() }

  // Pure Radiant Gold for center sparkles icon, Olive Green for others
  val activeTint = if (isCenter) AtharGoldLight else AtharOlivePrimary
  val inactiveTint = if (isCenter) AtharGold else AtharMilkyCreamSubtle.copy(alpha = 0.50f)

  Box(
    modifier = Modifier
      .offset(y = offsetY)
      .size(62.dp)
      .testTag(testTag)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = if (selected) activeTint else inactiveTint,
      modifier = Modifier.size(iconSize)
    )
  }
}
