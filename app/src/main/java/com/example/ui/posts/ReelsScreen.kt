package com.example.ui.posts

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkInputBg
import com.example.ui.theme.AtharDarkSurface
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveContainer
import com.example.ui.theme.AtharOliveDark
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ReelsFeedTab {
  FOR_YOU,
  FOLLOWING
}

data class ReelComment(
  val id: String,
  val userName: String,
  val userAvatarLetter: String,
  val text: String,
  val timeAgo: String,
  val likesCount: Int = 0,
  val isLiked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelsScreen(
  modifier: Modifier = Modifier
) {
  var selectedFeedTab by remember { mutableStateOf(ReelsFeedTab.FOR_YOU) }
  var activeCommentsReelId by remember { mutableStateOf<String?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  // Initial spiritual sample reels data
  val reelsList = remember {
    mutableStateListOf(
      ReelItem(
        id = "1",
        publisherName = "الشيخ ياسر الدوسري",
        isVerified = true,
        quoteHighlight = "﴿ وَبَشِّرِ الصَّابِرِينَ ﴾",
        caption = "تلاوة خاشعة تمس القلوب من سورة البقرة.. ما خاب عبدٌ وثق بربه وأحسن به الظن واستعان بالصبر والصلاة. كم من همٍّ فرّجه الله بلطفه الخفي بعد أن اشتد الكرب. #قرآن #تدبر #صبر",
        audioTitle = "تلاوة سورة البقرة - د. ياسر الدوسري",
        likesCount = 1420,
        bookmarksCount = 310,
        sharesCount = 540,
        imageRes = R.drawable.img_reel_quran,
        isForYou = true,
        isFollowingFeed = true
      ),
      ReelItem(
        id = "2",
        publisherName = "د. عمر عبد الكافي",
        isVerified = true,
        quoteHighlight = "« الصَّدَقَةُ تُطْفِئُ الخَطِيئَةَ »",
        caption = "أثر الصدقة الجارية في تفريج الكربات ومغفرة الزلات، صدقة السر أثرها يبقى في صحيفة العبد إلى يوم القيامة وحسناتها تتجدد بعد الرحيل. #أثر #صدقة_جارية",
        audioTitle = "موعظة الصدقة والبركة - د. عمر عبد الكافي",
        likesCount = 2890,
        bookmarksCount = 780,
        sharesCount = 920,
        imageRes = R.drawable.img_reel_mosque,
        isForYou = true,
        isFollowingFeed = false
      ),
      ReelItem(
        id = "3",
        publisherName = "الشيخ مشاري العفاسي",
        isVerified = true,
        quoteHighlight = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
        caption = "نفحة قرآنية هادئة تسكن بها الأرواح وتنجلي بها الأحزان. لا تدع يومك يمضي دون ورد من الذكر والاستغفار والصلاة على النبي ﷺ. #أذكار #طمأنينة",
        audioTitle = "سورة الرعد - مشاري راشد العفاسي",
        likesCount = 3650,
        bookmarksCount = 940,
        sharesCount = 1200,
        imageRes = R.drawable.img_reel_quran,
        isForYou = true,
        isFollowingFeed = true
      ),
      ReelItem(
        id = "4",
        publisherName = "قناة أثر الطيبة",
        isVerified = true,
        quoteHighlight = "« الدَّالُّ عَلَى الخَيْرِ كَفَاعِلِهِ »",
        caption = "شارك هذا المقطع الإيماني لعل آية واحدة تسمعها أذن غافلة فتكون سبباً في هدايتها ويكون لك أجرها المستمر إلى قيام الساعة. #الدال_على_الخير #حسنات",
        audioTitle = "خواطر نورانية في الأثر المستمر",
        likesCount = 1980,
        bookmarksCount = 450,
        sharesCount = 890,
        imageRes = R.drawable.img_reel_mosque,
        isForYou = true,
        isFollowingFeed = true
      )
    )
  }

  // Comments map per reel
  val commentsMap = remember {
    mutableMapOf(
      "1" to mutableStateListOf(
        ReelComment("c1", "عبدالله المنصور", "ع", "ما شاء الله تبارك الرحمن، تلاوة تقشعر لها الأبدان وتلامس الوجدان جزاك الله خيراً.", "منذ 10 د", 24),
        ReelComment("c2", "فاطمة الزهراء", "ف", "اللهم اجعلنا من الصابرين المحتسبين واجعل القرآن ربيع قلوبنا.", "منذ 25 د", 18),
        ReelComment("c3", "أحمد التميمي", "أ", "اللهم بارك في هذا العمل الطيب وأثره المستمر.", "منذ ساعة", 9)
      ),
      "2" to mutableStateListOf(
        ReelComment("c4", "يوسف العلي", "ي", "صدقة السر من أعظم القربات، نسأل الله الإخلاص والقبول.", "منذ 15 د", 32),
        ReelComment("c5", "مريم الكعبي", "م", "كلام يثلج الصدر، جزاك الله خيراً شيخنا الفاضل.", "منذ 40 د", 14)
      )
    )
  }

  val filteredReels = remember(selectedFeedTab, reelsList) {
    if (selectedFeedTab == ReelsFeedTab.FOR_YOU) {
      reelsList
    } else {
      reelsList.filter { it.isFollowingFeed || it.isFollowing }
    }
  }

  val pagerState = rememberPagerState(pageCount = { filteredReels.size })

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .testTag("reels_screen")
    ) {
      // Full Screen Vertical Snap Scroll Pager
      VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
      ) { page ->
        val reel = filteredReels.getOrNull(page)
        if (reel != null) {
          ReelPageItem(
            reel = reel,
            commentsCount = commentsMap[reel.id]?.size ?: 3,
            onLikeClicked = {
              val index = reelsList.indexOfFirst { it.id == reel.id }
              if (index != -1) {
                val current = reelsList[index]
                val newLiked = !current.isLiked
                reelsList[index] = current.copy(
                  isLiked = newLiked,
                  likesCount = if (newLiked) current.likesCount + 1 else current.likesCount - 1
                )
              }
            },
            onCommentsClicked = {
              activeCommentsReelId = reel.id
            },
            onBookmarkClicked = {
              val index = reelsList.indexOfFirst { it.id == reel.id }
              if (index != -1) {
                val current = reelsList[index]
                val newBookmarked = !current.isBookmarked
                reelsList[index] = current.copy(
                  isBookmarked = newBookmarked,
                  bookmarksCount = if (newBookmarked) current.bookmarksCount + 1 else current.bookmarksCount - 1
                )
              }
            },
            onFollowClicked = {
              val index = reelsList.indexOfFirst { it.id == reel.id }
              if (index != -1) {
                val current = reelsList[index]
                reelsList[index] = current.copy(isFollowing = !current.isFollowing)
              }
            }
          )
        }
      }

      // Top Floating Tabs ("لك" - "المُتابَعون")
      ReelsTopTabs(
        selectedTab = selectedFeedTab,
        onTabSelected = { selectedFeedTab = it },
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(top = 10.dp)
          .align(Alignment.TopCenter)
      )

      // Instagram-style Comments BottomSheet Modal
      if (activeCommentsReelId != null) {
        val reelId = activeCommentsReelId!!
        val currentComments = commentsMap.getOrPut(reelId) { mutableStateListOf() }

        ModalBottomSheet(
          onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
              activeCommentsReelId = null
            }
          },
          sheetState = sheetState,
          containerColor = AtharDarkCard,
          contentColor = AtharMilkyCream,
          dragHandle = {
            Box(
              modifier = Modifier
                .padding(vertical = 10.dp)
                .width(42.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AtharMilkyCreamSubtle.copy(alpha = 0.4f))
            )
          }
        ) {
          CommentsBottomSheetContent(
            comments = currentComments,
            onAddComment = { newText ->
              currentComments.add(
                0,
                ReelComment(
                  id = "c_${System.currentTimeMillis()}",
                  userName = "مستخدم أثر",
                  userAvatarLetter = "م",
                  text = newText,
                  timeAgo = "الآن",
                  likesCount = 0
                )
              )
            },
            onClose = {
              scope.launch { sheetState.hide() }.invokeOnCompletion {
                activeCommentsReelId = null
              }
            }
          )
        }
      }
    }
  }
}

/**
 * Top Transparent Floating Tabs: "لك" (For You) & "المُتابَعون" (Following)
 */
@Composable
private fun ReelsTopTabs(
  selectedTab: ReelsFeedTab,
  onTabSelected: (ReelsFeedTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // "المُتابَعون" Tab
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .clickable { onTabSelected(ReelsFeedTab.FOLLOWING) }
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .testTag("tab_following")
    ) {
      Text(
        text = "المُتابَعون",
        fontFamily = CairoFontFamily,
        fontWeight = if (selectedTab == ReelsFeedTab.FOLLOWING) FontWeight.Bold else FontWeight.Normal,
        fontSize = if (selectedTab == ReelsFeedTab.FOLLOWING) 16.sp else 14.5.sp,
        color = if (selectedTab == ReelsFeedTab.FOLLOWING) AtharMilkyCream else AtharMilkyCreamSubtle
      )
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .width(28.dp)
          .height(2.5.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(if (selectedTab == ReelsFeedTab.FOLLOWING) AtharGold else Color.Transparent)
      )
    }

    Text(
      text = "|",
      color = AtharMilkyCreamSubtle.copy(alpha = 0.4f),
      fontSize = 14.sp,
      modifier = Modifier.padding(horizontal = 4.dp)
    )

    // "لك" Tab
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .clickable { onTabSelected(ReelsFeedTab.FOR_YOU) }
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .testTag("tab_for_you")
    ) {
      Text(
        text = "لك",
        fontFamily = CairoFontFamily,
        fontWeight = if (selectedTab == ReelsFeedTab.FOR_YOU) FontWeight.Bold else FontWeight.Normal,
        fontSize = if (selectedTab == ReelsFeedTab.FOR_YOU) 16.sp else 14.5.sp,
        color = if (selectedTab == ReelsFeedTab.FOR_YOU) AtharMilkyCream else AtharMilkyCreamSubtle
      )
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .width(24.dp)
          .height(2.5.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(if (selectedTab == ReelsFeedTab.FOR_YOU) AtharGold else Color.Transparent)
      )
    }
  }
}

/**
 * Individual Full-Screen Reel Page Item
 * Clean floating action icons without boxes or circular backgrounds
 */
@Composable
private fun ReelPageItem(
  reel: ReelItem,
  commentsCount: Int,
  onLikeClicked: () -> Unit,
  onCommentsClicked: () -> Unit,
  onBookmarkClicked: () -> Unit,
  onFollowClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var isExpandedCaption by remember { mutableStateOf(false) }
  var showHeartBurst by remember { mutableStateOf(false) }

  // Rotating audio disc animation
  val infiniteTransition = rememberInfiniteTransition(label = "audio_disc_rotation")
  val discRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 5000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "disc_angle"
  )

  // Like scale pulse animation
  val likeScale by animateFloatAsState(
    targetValue = if (reel.isLiked) 1.28f else 1.0f,
    animationSpec = spring(stiffness = 400f),
    label = "like_scale"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = {
            if (!reel.isLiked) {
              onLikeClicked()
            }
            showHeartBurst = true
            scope.launch {
              delay(850)
              showHeartBurst = false
            }
          }
        )
      }
  ) {
    // 1. Full Screen Visual Background
    Image(
      painter = painterResource(id = reel.imageRes),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )

    // 2. Cinematic Gradient Overlay (Dark bottom & top)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Black.copy(alpha = 0.65f),
              Color.Transparent,
              Color.Black.copy(alpha = 0.4f),
              Color.Black.copy(alpha = 0.92f)
            )
          )
        )
    )

    // 3. Center Highlight Verse/Quote Card
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 28.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.55f)),
        border = null
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = reel.quoteHighlight,
            fontFamily = AmiriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = AtharGoldLight,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
          )
        }
      }
    }

    // Double-tap heart burst effect
    AnimatedVisibility(
      visible = showHeartBurst,
      enter = fadeIn(animationSpec = tween(150)),
      exit = fadeOut(animationSpec = tween(400)),
      modifier = Modifier.align(Alignment.Center)
    ) {
      Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = null,
        tint = Color(0xFFE53935),
        modifier = Modifier
          .size(110.dp)
          .scale(1.35f)
      )
    }

    // 4. Vertical Floating Action Bar on the side (Transparent, NO background boxes/circles)
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart) // In RTL, this is the action side
        .padding(start = 14.dp, bottom = 90.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

      // Like / Barakah Button (Free floating icon)
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = if (reel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
          contentDescription = "الإعجاب",
          tint = if (reel.isLiked) Color(0xFFE53935) else AtharMilkyCream,
          modifier = Modifier
            .size(30.dp)
            .scale(likeScale)
            .clickable { onLikeClicked() }
            .testTag("reel_like_button_${reel.id}")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${reel.likesCount}",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = AtharMilkyCream
        )
      }

      // Comments Button (Free floating icon)
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.Outlined.ChatBubbleOutline,
          contentDescription = "التعليقات",
          tint = AtharMilkyCream,
          modifier = Modifier
            .size(28.dp)
            .clickable { onCommentsClicked() }
            .testTag("reel_comment_button_${reel.id}")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "$commentsCount",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = AtharMilkyCream
        )
      }

      // Bookmark / Favorite Button (Free floating icon)
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = if (reel.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
          contentDescription = "حفظ المنشور",
          tint = if (reel.isBookmarked) AtharGoldLight else AtharMilkyCream,
          modifier = Modifier
            .size(28.dp)
            .clickable {
              onBookmarkClicked()
              val msg = if (!reel.isBookmarked) "تم حفظ المنشور في المفضلة" else "تمت إزالة المنشور من المفضلة"
              Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            .testTag("reel_bookmark_button_${reel.id}")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${reel.bookmarksCount}",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = AtharMilkyCream
        )
      }

      // Share Button (Paper Plane Send Icon, Free floating icon)
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "مشاركة",
          tint = AtharMilkyCream,
          modifier = Modifier
            .size(26.dp)
            .clickable {
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                  Intent.EXTRA_TEXT,
                  """
                  نفحة إيمانية من تطبيق أَثَـر:
                  ${reel.quoteHighlight}
                  
                  ${reel.caption}
                  
                  الصوت: ${reel.audioTitle}
                  قال ﷺ: «الدال على الخير كفاعله»
                  """.trimIndent()
                )
              }
              context.startActivity(Intent.createChooser(shareIntent, "مشاركة الفائدة الإيمانية"))
            }
            .testTag("reel_share_button_${reel.id}")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${reel.sharesCount}",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = AtharMilkyCream
        )
      }

      // Rotating Audio Disc (Without outer bounding boxes)
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.8f))
          .rotate(discRotation),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.MusicNote,
          contentDescription = null,
          tint = AtharGold,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    // 5. Bottom Information Bar (Publisher, Caption, Audio Track)
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .fillMaxWidth()
        .padding(start = 74.dp, end = 16.dp, bottom = 22.dp)
    ) {

      // Publisher info row
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AtharOliveDark),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = reel.publisherName.take(1),
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AtharMilkyCream
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
          text = reel.publisherName,
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = AtharMilkyCream
        )

        if (reel.isVerified) {
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "موثق",
            tint = AtharGold,
            modifier = Modifier.size(15.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Quick Follow Button (Transparent minimal style)
        Text(
          text = if (reel.isFollowing) "مُتابَع" else "+ متابعة",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 12.5.sp,
          color = if (reel.isFollowing) AtharMilkyCreamSubtle else AtharOliveLight,
          modifier = Modifier
            .clickable { onFollowClicked() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("follow_button_${reel.id}")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Caption with "المزيد..." expander
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpandedCaption = !isExpandedCaption }
      ) {
        Text(
          text = reel.caption,
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 13.5.sp,
          color = AtharMilkyCream,
          lineHeight = 22.sp,
          maxLines = if (isExpandedCaption) 8 else 2,
          overflow = TextOverflow.Ellipsis
        )

        if (!isExpandedCaption && reel.caption.length > 70) {
          Text(
            text = "المزيد...",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = AtharGoldLight,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Audio Track row
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.4f))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = null,
          tint = AtharGold,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = reel.audioTitle,
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 12.sp,
          color = AtharMilkyCreamMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

/**
 * Instagram-Style Comments BottomSheet
 */
@Composable
private fun CommentsBottomSheetContent(
  comments: List<ReelComment>,
  onAddComment: (String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var newCommentText by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .fillMaxHeight(0.72f)
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "التعليقات (${comments.size})",
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = AtharMilkyCream
      )

      IconButton(onClick = onClose) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "إغلاق",
          tint = AtharMilkyCreamMuted,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    HorizontalDivider(color = AtharDividerDark, thickness = 1.dp)

    // Comments List
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(comments, key = { it.id }) { comment ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          // User Avatar
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(AtharOliveContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = comment.userAvatarLetter,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = AtharOliveLight
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          // Comment body
          Column(
            modifier = Modifier.weight(1f)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = comment.userName,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AtharMilkyCream
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = comment.timeAgo,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = AtharMilkyCreamSubtle
              )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = comment.text,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 13.sp,
              color = AtharMilkyCreamMuted,
              lineHeight = 20.sp
            )
          }

          // Like small heart
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 6.dp)
          ) {
            Icon(
              imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = null,
              tint = if (comment.isLiked) Color(0xFFE53935) else AtharMilkyCreamSubtle,
              modifier = Modifier.size(16.dp)
            )
            if (comment.likesCount > 0) {
              Text(
                text = "${comment.likesCount}",
                fontFamily = CairoFontFamily,
                fontSize = 10.sp,
                color = AtharMilkyCreamSubtle
              )
            }
          }
        }
      }
    }

    HorizontalDivider(color = AtharDividerDark, thickness = 1.dp)

    // Add Comment Input Bar (Sticky at bottom)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 10.dp, bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = newCommentText,
        onValueChange = { newCommentText = it },
        modifier = Modifier
          .weight(1f)
          .testTag("comment_input_field"),
        shape = RoundedCornerShape(20.dp),
        placeholder = {
          Text(
            text = "أضف تعليقاً إيمانياً مباركاً...",
            fontFamily = CairoFontFamily,
            fontSize = 12.5.sp,
            color = AtharMilkyCreamSubtle
          )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = AtharMilkyCream,
          unfocusedTextColor = AtharMilkyCream,
          focusedBorderColor = AtharGold,
          unfocusedBorderColor = AtharDividerDark,
          focusedContainerColor = AtharDarkInputBg,
          unfocusedContainerColor = AtharDarkInputBg
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (newCommentText.isNotBlank()) {
            onAddComment(newCommentText.trim())
            newCommentText = ""
          }
        },
        modifier = Modifier.testTag("send_comment_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "إرسال التعليق",
          tint = if (newCommentText.isNotBlank()) AtharGold else AtharMilkyCreamSubtle,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}
