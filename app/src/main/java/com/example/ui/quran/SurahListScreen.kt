package com.example.ui.quran

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.quran.QuranBookmark
import com.example.data.quran.SurahMeta
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkInputBg
import com.example.ui.theme.AtharDividerDark
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharMilkyCream
import com.example.ui.theme.AtharMilkyCreamMuted
import com.example.ui.theme.AtharMilkyCreamSubtle
import com.example.ui.theme.AtharOliveLight
import com.example.ui.theme.AtharOlivePrimary
import com.example.ui.theme.AtharPitchBlack
import com.example.ui.theme.CairoFontFamily

@Composable
fun SurahListScreen(
  surahs: List<SurahMeta>,
  currentBookmark: QuranBookmark?,
  onSurahClick: (SurahMeta, Int?) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }

  // Filter surahs by name, englishName, or number
  val filteredSurahs = remember(searchQuery, surahs) {
    if (searchQuery.isBlank()) {
      surahs
    } else {
      val query = searchQuery.trim().lowercase()
      surahs.filter { surah ->
        surah.number.toString() == query ||
          surah.name.contains(query, ignoreCase = true) ||
          surah.name.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").contains(query, ignoreCase = true) ||
          surah.englishName.lowercase().contains(query) ||
          surah.englishNameTranslation.lowercase().contains(query)
      }
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .testTag("surah_list_screen")
    ) {

      // Top Header & Search Bar Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {

        // Transparent Elegant Search Field
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("surah_search_input"),
          shape = RoundedCornerShape(16.dp),
          placeholder = {
            Text(
              text = "ابحث عن سورة بالاسم أو الرقم...",
              fontFamily = CairoFontFamily,
              fontSize = 13.sp,
              color = AtharMilkyCreamSubtle
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = AtharGold,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "مسح البحث",
                  tint = AtharMilkyCreamSubtle,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
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

        Spacer(modifier = Modifier.width(10.dp))

        // Quick Bookmark Jump Button (Free floating icon, no circles or boxes)
        IconButton(
          onClick = {
            if (currentBookmark != null) {
              val targetSurah = surahs.find { it.number == currentBookmark.surahNumber }
              if (targetSurah != null) {
                onSurahClick(targetSurah, currentBookmark.ayahNumberInSurah)
              }
            } else {
              Toast.makeText(context, "لم تقم بحفظ أي علامة مرجعية بعد", Toast.LENGTH_SHORT).show()
            }
          },
          modifier = Modifier.testTag("quick_bookmark_button")
        ) {
          Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = "الانتقال للعلامة المرجعية",
            tint = if (currentBookmark != null) AtharGoldLight else AtharMilkyCreamSubtle,
            modifier = Modifier.size(28.dp)
          )
        }
      }

      // Bookmark Banner if present
      if (currentBookmark != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
              val targetSurah = surahs.find { it.number == currentBookmark.surahNumber }
              if (targetSurah != null) {
                onSurahClick(targetSurah, currentBookmark.ayahNumberInSurah)
              }
            }
            .testTag("bookmark_resume_banner"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = AtharDarkCard),
          border = null
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = AtharGold,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "مواصلة القراءة من آخر علامة",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = AtharMilkyCream
                )
                Text(
                  text = "${currentBookmark.surahName} - الآية ${currentBookmark.ayahNumberInSurah}",
                  fontFamily = CairoFontFamily,
                  fontSize = 11.5.sp,
                  color = AtharOliveLight
                )
              }
            }

            Text(
              text = "متابعة ←",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = AtharGoldLight
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
      }

      // Surahs Count Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "سور القرآن الكريم (${filteredSurahs.size})",
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = AtharMilkyCreamMuted
        )
      }

      // 114 Surahs List
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filteredSurahs, key = { it.number }) { surah ->
          SurahCardItem(
            surah = surah,
            isBookmarked = currentBookmark?.surahNumber == surah.number,
            onClick = { onSurahClick(surah, null) }
          )
        }
      }
    }
  }
}

/**
 * Individual Surah Item Card
 * Clean, dark styling with gold & milky cream typography, free of heavy circles.
 */
@Composable
private fun SurahCardItem(
  surah: SurahMeta,
  isBookmarked: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("surah_item_${surah.number}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = AtharDarkCard),
    border = null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {

      // Right side: Surah Number & Details (In RTL, this is start)
      Row(verticalAlignment = Alignment.CenterVertically) {

        // Surah Number (Styled without heavy circular background)
        Box(
          modifier = Modifier.size(36.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${surah.number}",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = AtharGold
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Surah Names & Verse Count
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = surah.name,
              fontFamily = AmiriFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 19.sp,
              color = AtharMilkyCream
            )

            if (isBookmarked) {
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "تحتوي على علامة",
                tint = AtharGoldLight,
                modifier = Modifier.size(15.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${surah.revelationTypeArabic} • ",
              fontFamily = CairoFontFamily,
              fontSize = 12.sp,
              color = AtharOliveLight
            )
            Text(
              text = "${surah.numberOfAyahs} آيات",
              fontFamily = CairoFontFamily,
              fontSize = 12.sp,
              color = AtharMilkyCreamSubtle
            )
          }
        }
      }

      // Left side: English transliteration
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = surah.englishName,
          fontFamily = CairoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = AtharMilkyCreamMuted
        )
        Text(
          text = surah.englishNameTranslation,
          fontFamily = CairoFontFamily,
          fontSize = 11.sp,
          color = AtharMilkyCreamSubtle
        )
      }
    }
  }
}
