package com.example.ui.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.donations.DonationCase
import com.example.data.donations.DonationRepository
import com.example.ui.auth.AuthUiState
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
fun ProfileScreen(
  authUiState: AuthUiState,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // User Profile state
  var profileData by rememberSaveable(authUiState) {
    val name = authUiState.currentUser?.displayName
      ?: authUiState.userProfile?.displayName
      ?: if (authUiState.isGuest) "ضيف كريم" else "ذو الفقار سعدون"
    val userHandle = if (authUiState.isGuest) "@athar_guest" else "@zulfiqar_saadoun"

    mutableStateOf(
      UserProfileData(
        displayName = name,
        username = userHandle
      )
    )
  }

  var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
  var isEditProfileOpen by rememberSaveable { mutableStateOf(false) }
  var selectedTab by rememberSaveable { mutableStateOf(ProfileTab.MY_REELS) }

  AnimatedContent(
    targetState = isSettingsOpen,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "settings_transition"
  ) { showSettings ->
    if (showSettings) {
      SettingsScreen(
        profileData = profileData,
        onUpdateProfileData = { profileData = it },
        onOpenEditProfile = { isEditProfileOpen = true },
        onSignOut = onSignOut,
        onBack = { isSettingsOpen = false },
        modifier = modifier
      )
    } else {
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
          modifier = modifier
            .fillMaxSize()
            .background(AtharPitchBlack)
            .testTag("profile_screen")
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 30.dp)
          ) {

            // 1. Header with Banner & Avatar
            item {
              ProfileHeaderSection(
                profileData = profileData,
                onOpenEditProfile = { isEditProfileOpen = true },
                onOpenSettings = { isSettingsOpen = true }
              )
            }

            // 2. Name, Username, Bio & Stats
            item {
              ProfileBioAndStatsSection(profileData = profileData)
              Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Tab Selector (My Reels, My Donations, Saved)
            item {
              ProfileTabSelector(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it }
              )
              Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Tab Content Grid
            when (selectedTab) {
              ProfileTab.MY_REELS -> {
                item {
                  MyReelsGrid(reels = ProfileDataProvider.defaultReels)
                }
              }
              ProfileTab.MY_DONATIONS -> {
                item {
                  MyDonationsSection(cases = DonationRepository.initialCases)
                }
              }
              ProfileTab.SAVED -> {
                item {
                  SavedItemsSection(items = ProfileDataProvider.defaultSavedItems)
                }
              }
            }
          }

          // Edit Profile Dialog Modal
          if (isEditProfileOpen) {
            EditProfileDialog(
              currentProfileData = profileData,
              onSave = { updated -> profileData = updated },
              onDismiss = { isEditProfileOpen = false }
            )
          }
        }
      }
    }
  }
}

/**
 * 1. Banner Image with Overlapping Avatar and Transparent Top Buttons
 */
@Composable
private fun ProfileHeaderSection(
  profileData: UserProfileData,
  onOpenEditProfile: () -> Unit,
  onOpenSettings: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(210.dp)
  ) {
    // Top Banner Image
    Image(
      painter = painterResource(id = profileData.bannerRes),
      contentDescription = "بانر البروفايل",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
    )

    // Dark Gradient overlay at bottom of banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, AtharPitchBlack.copy(alpha = 0.8f))
          )
        )
    )

    // Top Header Buttons Row (Transparent & elegant, no borders)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Edit Profile Transparent Button
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(AtharPitchBlack.copy(alpha = 0.6f))
          .clickable { onOpenEditProfile() }
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = null,
          tint = AtharGold,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "تعديل البروفايل",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = AtharMilkyCream
        )
      }

      // Settings Icon Button
      IconButton(
        onClick = onOpenSettings,
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(AtharPitchBlack.copy(alpha = 0.6f))
          .testTag("profile_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "الإعدادات",
          tint = AtharGoldLight,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    // Overlapping Avatar Image (Aligned to Bottom-Start in RTL)
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 20.dp)
        .size(92.dp)
        .clip(CircleShape)
        .border(2.5.dp, AtharGold, CircleShape)
        .background(AtharDarkCardElevated)
        .testTag("profile_avatar")
    ) {
      Image(
        painter = painterResource(id = profileData.avatarRes),
        contentDescription = "الصورة الشخصية",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}

/**
 * 2. Profile Details, Bio, and 3 Stats Bar
 */
@Composable
private fun ProfileBioAndStatsSection(
  profileData: UserProfileData
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp)
  ) {
    Spacer(modifier = Modifier.height(6.dp))

    // Display Name
    Text(
      text = profileData.displayName,
      fontFamily = CairoFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      color = AtharMilkyCream
    )

    // Username
    Text(
      text = profileData.username,
      fontFamily = CairoFontFamily,
      fontSize = 13.sp,
      color = AtharOliveLight
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Bio
    Text(
      text = profileData.bio,
      fontFamily = CairoFontFamily,
      fontSize = 13.5.sp,
      lineHeight = 20.sp,
      color = AtharMilkyCreamMuted
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 3 Account Stats Bar (Followers, Following, Barakah Points)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
      border = null
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Stat 1: Followers
        StatItem(
          value = formatStatNumber(profileData.followersCount),
          label = "المُتابِعين"
        )

        Box(
          modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(AtharDividerDark)
        )

        // Stat 2: Following
        StatItem(
          value = "${profileData.followingCount}",
          label = "المُتابَعين"
        )

        Box(
          modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(AtharDividerDark)
        )

        // Stat 3: Barakah Points
        StatItem(
          value = formatStatNumber(profileData.barakahPoints),
          label = "الحسنات / الأثر",
          isGold = true
        )
      }
    }
  }
}

@Composable
private fun StatItem(
  value: String,
  label: String,
  isGold: Boolean = false
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      fontFamily = CairoFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 17.sp,
      color = if (isGold) AtharGold else AtharMilkyCream
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontFamily = CairoFontFamily,
      fontSize = 11.5.sp,
      color = AtharMilkyCreamSubtle
    )
  }
}

/**
 * 3 Profile Tabs (My Reels, My Donations, Saved)
 */
@Composable
private fun ProfileTabSelector(
  selectedTab: ProfileTab,
  onTabSelect: (ProfileTab) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    ProfileTab.values().forEach { tab ->
      val isSelected = selectedTab == tab
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(14.dp))
          .background(if (isSelected) AtharOliveContainer else AtharDarkCard)
          .clickable { onTabSelect(tab) }
          .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = tab.title,
            fontFamily = CairoFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.5.sp,
            color = if (isSelected) AtharGoldLight else AtharMilkyCreamMuted
          )
        }
      }
    }
  }
}

/**
 * Tab 1: My Reels Grid
 */
@Composable
private fun MyReelsGrid(
  reels: List<ProfileReelSnippet>
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {
    // 2x2 grid layout
    val rows = reels.chunked(2)
    rows.forEach { rowItems ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        rowItems.forEach { reel ->
          Box(
            modifier = Modifier
              .weight(1f)
              .height(210.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(AtharDarkCardElevated)
              .testTag("my_reel_${reel.id}")
          ) {
            Image(
              painter = painterResource(id = reel.imageRes),
              contentDescription = reel.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, AtharPitchBlack.copy(alpha = 0.85f))
                  )
                )
            )

            // Quote & metrics overlay
            Column(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
            ) {
              Text(
                text = reel.quote,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AtharGoldLight,
                maxLines = 1
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = AtharMilkyCreamSubtle, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(text = "${reel.likesCount}", fontFamily = CairoFontFamily, fontSize = 10.sp, color = AtharMilkyCreamMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = AtharMilkyCreamSubtle, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(text = "${reel.viewsCount}", fontFamily = CairoFontFamily, fontSize = 10.sp, color = AtharMilkyCreamMuted)
                }
              }
            }
          }
        }
        if (rowItems.size == 1) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

/**
 * Tab 2: My Donations List
 */
@Composable
private fun MyDonationsSection(
  cases: List<DonationCase>
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    cases.take(2).forEach { donationCase ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
        border = null
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = donationCase.title,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = AtharMilkyCream,
              modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${donationCase.percentage}%",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = AtharGold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LinearProgressIndicator(
            progress = { donationCase.progressFraction },
            color = AtharOliveLight,
            trackColor = AtharDividerDark,
            modifier = Modifier
              .fillMaxWidth()
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "المجمع: ${donationCase.collectedAmount.toInt()} $",
              fontFamily = CairoFontFamily,
              fontSize = 11.5.sp,
              color = AtharOliveLight
            )
            Text(
              text = "المطلوب: ${donationCase.targetAmount.toInt()} $",
              fontFamily = CairoFontFamily,
              fontSize = 11.5.sp,
              color = AtharMilkyCreamSubtle
            )
          }
        }
      }
    }
  }
}

/**
 * Tab 3: Saved Items Section (Quran Ayahs, Azkar, Reels)
 */
@Composable
private fun SavedItemsSection(
  items: List<SavedItem>
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items.forEach { item ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
        border = null
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = item.iconEmoji, fontSize = 22.sp)
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.title,
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = AtharMilkyCream
            )
            Text(
              text = item.subtitle,
              fontFamily = CairoFontFamily,
              fontSize = 11.sp,
              color = AtharOliveLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = item.quoteOrSnippet,
              fontFamily = AmiriFontFamily,
              fontSize = 12.5.sp,
              lineHeight = 18.sp,
              color = AtharMilkyCreamMuted,
              maxLines = 2
            )
          }
          Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = null,
            tint = AtharGold,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

private fun formatStatNumber(count: Int): String {
  return when {
    count >= 1000 -> String.format("%.1fK", count / 1000.0)
    else -> count.toString()
  }
}
