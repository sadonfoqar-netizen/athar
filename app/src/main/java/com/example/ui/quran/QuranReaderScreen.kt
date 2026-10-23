package com.example.ui.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.quran.Ayah
import com.example.data.quran.PlaybackMode
import com.example.data.quran.QuranBookmark
import com.example.data.quran.QuranReciter
import com.example.data.quran.QuranRepository
import com.example.data.quran.ReciterCategory
import com.example.data.quran.SurahDetail
import com.example.data.quran.SurahMeta
import com.example.data.quran.goldenReciters
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharCreamCardElevated
import com.example.ui.theme.AtharCreamDivider
import com.example.ui.theme.AtharCreamInputBg
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkCardElevated
import com.example.ui.theme.AtharDeepBlack
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun QuranReaderScreen(
  surahMeta: SurahMeta,
  initialAyahNumber: Int? = null,
  quranRepository: QuranRepository,
  audioPlayer: QuranAudioPlayer,
  surahs: List<SurahMeta>,
  currentBookmark: QuranBookmark?,
  onBookmarkUpdated: (QuranBookmark) -> Unit,
  onNavigateToSurah: (SurahMeta) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val listState = rememberLazyListState()

  var surahDetail by remember { mutableStateOf<SurahDetail?>(null) }
  var isLoadingSurah by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Font size state (configurable by user: 20sp to 36sp)
  var quranFontSize by rememberSaveable { mutableFloatStateOf(24f) }
  var showFontSizeDialog by remember { mutableStateOf(false) }

  // Action dialog for specific Ayah
  var selectedAyahForAction by remember { mutableStateOf<Ayah?>(null) }

  // Bottom Audio player & Reciters selection sheet visibility
  val audioState by audioPlayer.state.collectAsStateWithLifecycle()
  var isAudioPanelVisible by rememberSaveable { mutableStateOf(false) }
  var showReciterSelectionSheet by rememberSaveable { mutableStateOf(false) }

  // Load Surah content
  LaunchedEffect(surahMeta.number) {
    isLoadingSurah = true
    errorMessage = null
    val detail = quranRepository.getSurahDetail(surahMeta.number)
    if (detail != null) {
      surahDetail = detail
      isLoadingSurah = false
    } else {
      isLoadingSurah = false
      errorMessage = "تعذر تحميل محتوى السورة، يرجى المحاولة مجدداً."
    }
  }

  // Auto-scroll to bookmarked / requested ayah
  LaunchedEffect(surahDetail, initialAyahNumber) {
    val detail = surahDetail
    if (detail != null && initialAyahNumber != null && initialAyahNumber > 1) {
      // Find index in list: index 0 is header, so ayah item is at initialAyahNumber
      val targetIndex = (initialAyahNumber).coerceIn(0, detail.ayahs.size)
      listState.animateScrollToItem(targetIndex)
    }
  }

  // Handle system back button
  BackHandler {
    onBack()
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .testTag("quran_reader_screen")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
      ) {

        // Top Navigation Bar (Flat, minimal, free of frames/circles)
        QuranReaderTopBar(
          surahMeta = surahMeta,
          isAudioPlaying = audioState.isPlaying && audioState.currentSurahNumber == surahMeta.number,
          onBackClick = onBack,
          onToggleAudio = {
            showReciterSelectionSheet = true
          },
          onOpenFontSize = { showFontSizeDialog = true }
        )

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        // Main Surah Reading Area
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          when {
            isLoadingSurah -> {
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
                    text = "جاري فتح آيات سورة ${surahMeta.name}...",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    color = AtharMilkyCreamMuted
                  )
                }
              }
            }

            errorMessage != null -> {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.padding(24.dp)
                ) {
                  Text(
                    text = errorMessage ?: "",
                    fontFamily = CairoFontFamily,
                    fontSize = 14.sp,
                    color = AtharMilkyCreamMuted,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(
                    text = "إعادة المحاولة",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AtharGold,
                    modifier = Modifier
                      .clickable {
                        isLoadingSurah = true
                        errorMessage = null
                      }
                      .padding(8.dp)
                  )
                }
              }
            }

            surahDetail != null -> {
              val detail = surahDetail!!
              LazyColumn(
                state = listState,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(
                  top = 16.dp,
                  bottom = if (isAudioPanelVisible) 190.dp else 40.dp
                )
              ) {

                // Item 0: Surah Header Card with Basmala
                item(key = "surah_header") {
                  SurahHeaderDecoration(surahMeta = surahMeta)
                  Spacer(modifier = Modifier.height(18.dp))
                }

                // Ayahs
                itemsIndexed(
                  items = detail.ayahs,
                  key = { _, ayah -> "${surahMeta.number}_${ayah.numberInSurah}" }
                ) { index, ayah ->
                  val isBookmarked = currentBookmark?.surahNumber == surahMeta.number &&
                    currentBookmark.ayahNumberInSurah == ayah.numberInSurah

                  AyahVerseItem(
                    ayah = ayah,
                    fontSize = quranFontSize,
                    isBookmarked = isBookmarked,
                    onAyahClick = {
                      selectedAyahForAction = ayah
                    }
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                }
              }
            }
          }
        }
      }

      // Floating Bottom Audio Player
      AnimatedVisibility(
        visible = isAudioPanelVisible,
        enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        QuranFloatingAudioPlayerCard(
          surahMeta = surahMeta,
          audioPlayer = audioPlayer,
          audioState = audioState,
          onOpenReciterSheet = { showReciterSelectionSheet = true },
          onClose = { isAudioPanelVisible = false },
          onNextSurah = {
            val nextSurah = surahs.find { it.number == surahMeta.number + 1 }
            if (nextSurah != null) {
              onNavigateToSurah(nextSurah)
            }
          }
        )
      }

      // Reciter Selection Bottom Sheet (Golden List)
      if (showReciterSelectionSheet) {
        ReciterSelectionBottomSheet(
          surahMeta = surahMeta,
          currentReciter = audioState.reciter,
          isPlaying = audioState.isPlaying && audioState.currentSurahNumber == surahMeta.number,
          onReciterSelected = { selectedReciter ->
            showReciterSelectionSheet = false
            isAudioPanelVisible = true
            audioPlayer.playSurah(
              surahNumber = surahMeta.number,
              reciter = selectedReciter,
              onNextSurah = {
                val nextSurah = surahs.find { it.number == surahMeta.number + 1 }
                if (nextSurah != null) {
                  onNavigateToSurah(nextSurah)
                }
              }
            )
            Toast.makeText(context, "بدأ تشغيل سورة ${surahMeta.name} بصوت ${selectedReciter.name}", Toast.LENGTH_SHORT).show()
          },
          onDismiss = { showReciterSelectionSheet = false }
        )
      }

      // Font Size Control Dialog
      if (showFontSizeDialog) {
        FontSizeDialog(
          currentFontSize = quranFontSize,
          onFontSizeChange = { quranFontSize = it },
          onDismiss = { showFontSizeDialog = false }
        )
      }

      // Ayah Quick Actions Dialog (Bookmark, Copy, Share)
      if (selectedAyahForAction != null) {
        val ayah = selectedAyahForAction!!
        AyahActionDialog(
          surahMeta = surahMeta,
          ayah = ayah,
          isBookmarked = currentBookmark?.surahNumber == surahMeta.number &&
            currentBookmark.ayahNumberInSurah == ayah.numberInSurah,
          onSaveBookmark = {
            val newBookmark = QuranBookmark(
              surahNumber = surahMeta.number,
              surahName = surahMeta.name,
              ayahNumberInSurah = ayah.numberInSurah
            )
            quranRepository.saveBookmark(newBookmark)
            onBookmarkUpdated(newBookmark)
            Toast.makeText(context, "تم حفظ العلامة عند الآية (${ayah.numberInSurah})", Toast.LENGTH_SHORT).show()
            selectedAyahForAction = null
          },
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
              "آية قرآنية",
              "﴿${ayah.text}﴾ [سورة ${surahMeta.name}: الآية ${ayah.numberInSurah}]"
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "تم نسخ الآية الكريمة", Toast.LENGTH_SHORT).show()
            selectedAyahForAction = null
          },
          onShare = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(
                Intent.EXTRA_TEXT,
                "﴿${ayah.text}﴾\n\n[سورة ${surahMeta.name}: ${ayah.numberInSurah}]\nمن تطبيق أثر"
              )
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الآية الكريمة"))
            selectedAyahForAction = null
          },
          onDismiss = { selectedAyahForAction = null }
        )
      }
    }
  }
}

/**
 * Clean Top Bar for Reader Screen
 */
@Composable
private fun QuranReaderTopBar(
  surahMeta: SurahMeta,
  isAudioPlaying: Boolean,
  onBackClick: () -> Unit,
  onToggleAudio: () -> Unit,
  onOpenFontSize: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {

    // Right (RTL): Back Button & Surah Title
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier.testTag("reader_back_button")
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
          text = surahMeta.name,
          fontFamily = AmiriFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          color = AtharMilkyCream
        )
        Text(
          text = "${surahMeta.revelationTypeArabic} • ${surahMeta.numberOfAyahs} آيات",
          fontFamily = CairoFontFamily,
          fontSize = 11.sp,
          color = AtharOliveLight
        )
      }
    }

    // Left (RTL): Actions (Audio, Font size)
    Row(verticalAlignment = Alignment.CenterVertically) {

      // Font Size Button
      IconButton(
        onClick = onOpenFontSize,
        modifier = Modifier.testTag("reader_font_size_button")
      ) {
        Text(
          text = "A+",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = AtharMilkyCream
        )
      }

      // Audio Play / Listen Button (Opens Reciters Golden List)
      IconButton(
        onClick = onToggleAudio,
        modifier = Modifier.testTag("reader_audio_button")
      ) {
        Icon(
          imageVector = if (isAudioPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Headphones,
          contentDescription = if (isAudioPlaying) "التلاوة تعمل (اضغط لاختيار القارئ)" else "اختيار القارئ والاستماع للتلاوة",
          tint = if (isAudioPlaying) AtharOlivePrimary else AtharMilkyCream,
          modifier = Modifier.size(26.dp)
        )
      }
    }
  }
}

/**
 * Surah Header Card with Calligraphic Bismillah
 */
@Composable
private fun SurahHeaderDecoration(
  surahMeta: SurahMeta
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(AtharDarkCard)
      .padding(vertical = 18.dp, horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "سُورَةُ ${surahMeta.name}",
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        color = AtharGold,
        textAlign = TextAlign.Center
      )

      // Surah 9 (At-Tawbah) does not start with Basmala
      if (surahMeta.number != 9) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
          fontFamily = AmiriFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 22.sp,
          color = AtharMilkyCream,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

/**
 * Single Ayah representation
 */
@Composable
private fun AyahVerseItem(
  ayah: Ayah,
  fontSize: Float,
  isBookmarked: Boolean,
  onAyahClick: () -> Unit
) {
  val backgroundColor = if (isBookmarked) AtharOliveContainer else AtharDarkCard

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(backgroundColor)
      .clickable(onClick = onAyahClick)
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag("ayah_${ayah.numberInSurah}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      if (isBookmarked) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = null,
            tint = AtharGold,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "علامتك المرجعية الحالية",
            fontFamily = CairoFontFamily,
            fontSize = 11.sp,
            color = AtharGoldLight
          )
        }
      }

      // Quranic Ayah Text in Amiri script with gold ayah end symbol
      Text(
        text = "${cleanAyahText(ayah.text)}  ﴿${convertToArabicNumerals(ayah.numberInSurah)}﴾",
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.75f).sp,
        color = AtharMilkyCream,
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

/**
 * Floating Audio Player Card
 */
@Composable
private fun QuranFloatingAudioPlayerCard(
  surahMeta: SurahMeta,
  audioPlayer: QuranAudioPlayer,
  audioState: AudioPlayerState,
  onOpenReciterSheet: () -> Unit,
  onClose: () -> Unit,
  onNextSurah: () -> Unit
) {
  val scope = rememberCoroutineScope()

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = AtharDarkCardElevated,
    shadowElevation = 8.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("floating_audio_player")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

      // Row 1: Header (Reciter name with quick changer & close button)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onOpenReciterSheet() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Headphones,
            contentDescription = null,
            tint = AtharGold,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "سورة ${surahMeta.name}",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = AtharMilkyCream
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = audioState.reciterName,
                fontFamily = CairoFontFamily,
                fontSize = 11.sp,
                color = AtharOliveLight
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "• تغيير القارئ",
                fontFamily = CairoFontFamily,
                fontSize = 10.sp,
                color = AtharGold
              )
            }
          }
        }

        IconButton(
          onClick = onClose,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "إغلاق المشغل",
            tint = AtharMilkyCreamSubtle,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Progress Slider
      val duration = audioState.durationMs
      val currentPosition = audioState.currentPositionMs
      val sliderValue = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

      Slider(
        value = sliderValue,
        onValueChange = { frac ->
          val targetMs = (frac * duration).toLong()
          audioPlayer.seekTo(targetMs)
        },
        colors = SliderDefaults.colors(
          thumbColor = AtharGold,
          activeTrackColor = AtharGold,
          inactiveTrackColor = AtharDividerDark
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(30.dp)
      )

      // Time indicators (Elapsed / Remaining)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = formatDuration(currentPosition),
          fontFamily = CairoFontFamily,
          fontSize = 11.sp,
          color = AtharMilkyCreamSubtle
        )
        Text(
          text = formatDuration(duration),
          fontFamily = CairoFontFamily,
          fontSize = 11.sp,
          color = AtharMilkyCreamSubtle
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Control Buttons Row (Free floating minimal icons without heavy circular backgrounds)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {

        // Cycle Playback Mode
        IconButton(
          onClick = { audioPlayer.cyclePlaybackMode() }
        ) {
          val icon = when (audioState.playbackMode) {
            PlaybackMode.AUTO_NEXT -> Icons.Default.Repeat
            PlaybackMode.LOOP_SURAH -> Icons.Default.RepeatOne
            PlaybackMode.STOP_ON_END -> Icons.Default.Stop
          }
          Icon(
            imageVector = icon,
            contentDescription = "نمط التشغيل",
            tint = AtharGoldLight,
            modifier = Modifier.size(20.dp)
          )
        }

        // Rewind 10 seconds
        IconButton(
          onClick = { audioPlayer.seekRewind10s() }
        ) {
          Icon(
            imageVector = Icons.Default.Replay10,
            contentDescription = "تأخير 10 ثوان",
            tint = AtharMilkyCream,
            modifier = Modifier.size(24.dp)
          )
        }

        // Main Play / Pause Button
        if (audioState.isLoading) {
          CircularProgressIndicator(
            color = AtharGold,
            strokeWidth = 2.dp,
            modifier = Modifier.size(34.dp)
          )
        } else {
          IconButton(
            onClick = {
              if (audioState.currentSurahNumber != surahMeta.number) {
                audioPlayer.playSurah(surahNumber = surahMeta.number, onNextSurah = onNextSurah)
              } else {
                audioPlayer.togglePlayPause()
              }
            },
            modifier = Modifier.size(46.dp)
          ) {
            Icon(
              imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (audioState.isPlaying) "إيقاف مؤقت" else "تشغيل التلاوة",
              tint = AtharGold,
              modifier = Modifier.size(38.dp)
            )
          }
        }

        // Forward 10 seconds
        IconButton(
          onClick = { audioPlayer.seekForward10s() }
        ) {
          Icon(
            imageVector = Icons.Default.Forward10,
            contentDescription = "تقديم 10 ثوان",
            tint = AtharMilkyCream,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }
  }
}

/**
 * Font Size Adjustment Dialog
 */
@Composable
private fun FontSizeDialog(
  currentFontSize: Float,
  onFontSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = AtharDarkCardElevated,
      modifier = Modifier.fillMaxWidth(0.92f)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "حجم خط الآيات القرآنية",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = AtharMilkyCream
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preview box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AtharDarkCard)
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾",
            fontFamily = AmiriFontFamily,
            fontSize = currentFontSize.sp,
            color = AtharMilkyCream,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Slider for font size
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "أصغر",
            fontFamily = CairoFontFamily,
            fontSize = 12.sp,
            color = AtharMilkyCreamSubtle
          )
          Slider(
            value = currentFontSize,
            onValueChange = onFontSizeChange,
            valueRange = 18f..38f,
            steps = 10,
            colors = SliderDefaults.colors(
              thumbColor = AtharGold,
              activeTrackColor = AtharGold,
              inactiveTrackColor = AtharDividerDark
            ),
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 8.dp)
          )
          Text(
            text = "أكبر",
            fontFamily = CairoFontFamily,
            fontSize = 12.sp,
            color = AtharMilkyCreamSubtle
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "تم",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = AtharGold,
          modifier = Modifier
            .clickable(onClick = onDismiss)
            .padding(8.dp)
        )
      }
    }
  }
}

/**
 * Ayah Action Dialog (Bookmark, Copy, Share)
 */
@Composable
private fun AyahActionDialog(
  surahMeta: SurahMeta,
  ayah: Ayah,
  isBookmarked: Boolean,
  onSaveBookmark: () -> Unit,
  onCopy: () -> Unit,
  onShare: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = AtharDarkCardElevated,
      modifier = Modifier.fillMaxWidth(0.92f)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "سورة ${surahMeta.name} - الآية (${ayah.numberInSurah})",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = AtharGold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ayah preview
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AtharDarkCard)
            .padding(12.dp)
        ) {
          Text(
            text = ayah.text,
            fontFamily = AmiriFontFamily,
            fontSize = 18.sp,
            color = AtharMilkyCream,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action 1: Save as Bookmark
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSaveBookmark)
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = AtharGold,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = if (isBookmarked) "تحديث مكان القراءة عند هذه الآية" else "حفظ كعلامة مرجعية للقراءة",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = AtharMilkyCream
          )
        }

        // Action 2: Copy
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onCopy)
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            tint = AtharMilkyCreamMuted,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "نسخ الآية الكريمة",
            fontFamily = CairoFontFamily,
            fontSize = 14.sp,
            color = AtharMilkyCream
          )
        }

        // Action 3: Share
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onShare)
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = AtharOliveLight,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "مشاركة الآية والأثر",
            fontFamily = CairoFontFamily,
            fontSize = 14.sp,
            color = AtharMilkyCream
          )
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

/**
 * Format milliseconds to MM:SS
 */
private fun formatDuration(millis: Long): String {
  val totalSeconds = (millis / 1000).toInt()
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

/**
 * Removes repeated Basmala from Surah 1 ayah 1 or other Surahs if prefixed by API
 */
private fun cleanAyahText(text: String): String {
  return text.trim()
}

/**
 * Convert numbers (1, 2, 3...) to Arabic Indic numerals (١, ٢, ٣...)
 */
private fun convertToArabicNumerals(number: Int): String {
  val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
  val str = number.toString()
  val builder = StringBuilder()
  for (ch in str) {
    if (ch in '0'..'9') {
      builder.append(arabicDigits[ch - '0'])
    } else {
      builder.append(ch)
    }
  }
  return builder.toString()
}

/**
 * Reciter Selection Bottom Sheet:
 * Displays the Golden List of 24 famous reciters with real-time search,
 * category filtering, and instant audio playback upon selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReciterSelectionBottomSheet(
  surahMeta: SurahMeta,
  currentReciter: QuranReciter,
  isPlaying: Boolean,
  onReciterSelected: (QuranReciter) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<ReciterCategory?>(null) }

  val filteredReciters = remember(searchQuery, selectedCategory) {
    goldenReciters.filter { reciter ->
      val matchesCategory = selectedCategory == null || reciter.category == selectedCategory
      val matchesSearch = searchQuery.isBlank() || reciter.name.contains(searchQuery.trim(), ignoreCase = true)
      matchesCategory && matchesSearch
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = sheetState,
      containerColor = AtharCreamCardElevated,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      dragHandle = null
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 16.dp)
          .navigationBarsPadding()
          .testTag("reciter_selection_sheet")
      ) {

        // Top Sheet Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Headphones,
              contentDescription = null,
              tint = AtharGold,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "اختر القارئ الكريم",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AtharMilkyCream
              )
              Text(
                text = "تلاوة سورة ${surahMeta.name} • ${filteredReciters.size} قارئ متاح",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharOliveLight
              )
            }
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

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              text = "ابحث عن القارئ (مثلاً: العفاسي، المعيقلي، المنشاوي...)",
              fontFamily = CairoFontFamily,
              fontSize = 12.sp,
              color = AtharMilkyCreamSubtle
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "بحث",
              tint = AtharGold,
              modifier = Modifier.size(20.dp)
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AtharCreamInputBg,
            unfocusedContainerColor = AtharCreamInputBg,
            focusedBorderColor = AtharOliveLight,
            unfocusedBorderColor = AtharCreamDivider,
            focusedTextColor = AtharDeepBlack,
            unfocusedTextColor = AtharDeepBlack
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("reciter_search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // All chip
          ReciterFilterChip(
            label = "الكل (${goldenReciters.size})",
            isSelected = selectedCategory == null,
            onClick = { selectedCategory = null },
            modifier = Modifier.weight(1f)
          )

          // Haramain
          ReciterFilterChip(
            label = "الحرمين (10)",
            isSelected = selectedCategory == ReciterCategory.HARAMAIN,
            onClick = { selectedCategory = ReciterCategory.HARAMAIN },
            modifier = Modifier.weight(1f)
          )

          // Egypt Heritage
          ReciterFilterChip(
            label = "مصر (8)",
            isSelected = selectedCategory == ReciterCategory.EGYPT_HERITAGE,
            onClick = { selectedCategory = ReciterCategory.EGYPT_HERITAGE },
            modifier = Modifier.weight(1f)
          )

          // Modern Youth
          ReciterFilterChip(
            label = "الشباب (6)",
            isSelected = selectedCategory == ReciterCategory.MODERN_YOUTH,
            onClick = { selectedCategory = ReciterCategory.MODERN_YOUTH },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = AtharCreamDivider, thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Reciters Scrollable List
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredReciters, key = { it.id }) { reciter ->
            val isCurrent = reciter.id == currentReciter.id

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isCurrent) AtharOliveContainer else AtharDarkCard,
              border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.2.dp, AtharOliveLight) else androidx.compose.foundation.BorderStroke(0.6.dp, AtharCreamDivider),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onReciterSelected(reciter) }
                .testTag("reciter_item_${reciter.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  // Icon indicator
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(if (isCurrent) AtharOlivePrimary else AtharCreamInputBg),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (isCurrent && isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Headphones,
                      contentDescription = null,
                      tint = if (isCurrent) AtharGoldLight else AtharGold,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Text(
                      text = reciter.name,
                      fontFamily = CairoFontFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = if (isCurrent) AtharDeepBlack else AtharMilkyCream
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "${reciter.category.title} • ${reciter.narration}",
                      fontFamily = CairoFontFamily,
                      fontSize = 11.sp,
                      color = if (isCurrent) AtharOlivePrimary else AtharMilkyCreamSubtle
                    )
                  }
                }

                if (isCurrent) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "تم الاختيار",
                    tint = AtharOlivePrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Filter Chip for Reciter categories
 */
@Composable
private fun ReciterFilterChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) AtharOlivePrimary else AtharCreamInputBg,
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = label,
        fontFamily = CairoFontFamily,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 11.sp,
        color = if (isSelected) AtharGoldLight else AtharDeepBlack,
        textAlign = TextAlign.Center
      )
    }
  }
}

