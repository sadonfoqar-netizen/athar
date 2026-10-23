package com.example.ui.donations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DonationsScreen(
  authUiState: AuthUiState,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val repository = remember(context) {
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
    DonationRepository(context = context, db = firestore, scope = scope)
  }

  val donationsList by repository.donations.collectAsStateWithLifecycle()

  var isAddingDonation by rememberSaveable { mutableStateOf(false) }
  var selectedCaseForDonation by remember { mutableStateOf<DonationCase?>(null) }

  AnimatedContent(
    targetState = isAddingDonation,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "add_donation_transition"
  ) { showAddScreen ->
    if (showAddScreen) {
      AddDonationScreen(
        authUiState = authUiState,
        onPublish = { newCase ->
          scope.launch {
            repository.addDonation(newCase)
          }
        },
        onBack = { isAddingDonation = false },
        modifier = modifier
      )
    } else {
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
          modifier = modifier
            .fillMaxSize()
            .background(AtharPitchBlack)
            .statusBarsPadding()
            .testTag("donations_screen")
        ) {
          Column(modifier = Modifier.fillMaxSize()) {

            // Top Action Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "حالات التبرع والصدقات",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp,
                  color = AtharMilkyCream
                )
                Text(
                  text = "صنائع المعروف تقي مصارع السوء",
                  fontFamily = CairoFontFamily,
                  fontSize = 11.5.sp,
                  color = AtharOliveLight
                )
              }

              // Add Case Button (+) (Free floating transparent gold icon)
              IconButton(
                onClick = { isAddingDonation = true },
                modifier = Modifier
                  .size(42.dp)
                  .testTag("add_donation_top_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "إضافة حالة تبرع",
                  tint = AtharGold,
                  modifier = Modifier.size(28.dp)
                )
              }
            }

            HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

            // Donations Stream
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              items(donationsList, key = { it.id }) { donationCase ->
                DonationCardItem(
                  donationCase = donationCase,
                  onDonateClick = {
                    selectedCaseForDonation = donationCase
                  }
                )
              }

              item {
                Spacer(modifier = Modifier.height(30.dp))
              }
            }
          }

          // Donation Modal BottomSheet
          if (selectedCaseForDonation != null) {
            DonationBottomSheet(
              donationCase = selectedCaseForDonation!!,
              onDismiss = { selectedCaseForDonation = null },
              onRecordContribution = { amount ->
                scope.launch {
                  repository.recordContribution(selectedCaseForDonation!!.id, amount)
                }
              }
            )
          }
        }
      }
    }
  }
}

/**
 * Individual Donation Card Item with Image Slider, Progress Bar, and Donate Action
 */
@Composable
private fun DonationCardItem(
  donationCase: DonationCase,
  onDonateClick: () -> Unit
) {
  val images = donationCase.imageUrls.ifEmpty {
    listOf("https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?q=80&w=800&auto=format&fit=crop")
  }
  val pagerState = rememberPagerState(pageCount = { images.size })

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("donation_card_${donationCase.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
    border = null
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {

      // 1. Image Slider (1 to 4 images with page dots indicator)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
          .background(AtharDarkCard)
      ) {
        HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxSize()
        ) { page ->
          AsyncImage(
            model = images[page],
            contentDescription = "صورة حالة التبرع",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Category Tag overlay on top-right
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AtharPitchBlack.copy(alpha = 0.75f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = donationCase.category,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = AtharGoldLight
          )
        }

        // Page Dots Indicator (if more than 1 image)
        if (images.size > 1) {
          Row(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            repeat(images.size) { index ->
              val isSelected = pagerState.currentPage == index
              Box(
                modifier = Modifier
                  .size(if (isSelected) 8.dp else 6.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) AtharGold else AtharMilkyCreamSubtle.copy(alpha = 0.6f))
              )
            }
          }
        }
      }

      // 2. Card Content & Details
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {

        // Title
        Text(
          text = donationCase.title,
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          lineHeight = 24.sp,
          color = AtharMilkyCream
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Author / Organization Tag
        if (donationCase.authorName.isNotBlank()) {
          Text(
            text = "بواسطة: ${donationCase.authorName}",
            fontFamily = CairoFontFamily,
            fontSize = 11.sp,
            color = AtharOliveLight
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        // Description
        Text(
          text = donationCase.description,
          fontFamily = CairoFontFamily,
          fontSize = 13.sp,
          lineHeight = 21.sp,
          color = AtharMilkyCreamMuted,
          textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Progress Bar & Amounts
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Column {
              Text(
                text = "المبلغ المجمّع",
                fontFamily = CairoFontFamily,
                fontSize = 11.sp,
                color = AtharMilkyCreamSubtle
              )
              Text(
                text = "${donationCase.collectedAmount.toInt()} ${donationCase.currency}",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AtharGold
              )
            }

            // Percentage in bold gold
            Text(
              text = "${donationCase.percentage}%",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = AtharGoldLight
            )

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "المطلوب الكلي",
                fontFamily = CairoFontFamily,
                fontSize = 11.sp,
                color = AtharMilkyCreamSubtle
              )
              Text(
                text = "${donationCase.targetAmount.toInt()} ${donationCase.currency}",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AtharMilkyCream
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Linear Progress Indicator
          LinearProgressIndicator(
            progress = { donationCase.progressFraction },
            color = AtharOliveLight,
            trackColor = AtharDividerDark,
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp))
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "المتبقي للاكتمال: ${donationCase.remainingAmount.toInt()} ${donationCase.currency}",
            fontFamily = CairoFontFamily,
            fontSize = 11.sp,
            color = AtharOliveLight,
            textAlign = TextAlign.Start
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. [ تبرّع الآن ] Action Button
        Button(
          onClick = onDonateClick,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AtharOlivePrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("donate_button_${donationCase.id}")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.VolunteerActivism,
              contentDescription = null,
              tint = AtharGold,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "تبرّع الآن",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = AtharMilkyCream
            )
          }
        }
      }
    }
  }
}
