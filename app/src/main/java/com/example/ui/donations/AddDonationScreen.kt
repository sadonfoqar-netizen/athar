package com.example.ui.donations

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.donations.DonationCase
import com.example.ui.auth.AuthUiState
import com.example.ui.theme.AtharDarkCard
import com.example.ui.theme.AtharDarkCardElevated
import com.example.ui.theme.AtharDarkInputBg
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
fun AddDonationScreen(
  authUiState: AuthUiState,
  onPublish: (DonationCase) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var targetAmountStr by remember { mutableStateOf("") }
  var cardNumber by remember { mutableStateOf("") }
  var whatsappPhone by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("كفالة أيتام") }

  val selectedImageUris = remember { mutableStateListOf<Uri>() }

  // Visual Media Picker (up to 4 images, zero-permission)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
  ) { uris ->
    if (uris.isNotEmpty()) {
      selectedImageUris.clear()
      val toAdd = uris.take(4)
      selectedImageUris.addAll(toAdd)
    }
  }

  val categories = listOf("كفالة أيتام", "سقيا ماء", "علاج مريض", "ترميم منازل", "إغاثة عامة")

  BackHandler {
    onBack()
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .testTag("add_donation_screen")
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
              modifier = Modifier.testTag("add_donation_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = AtharMilkyCream,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "إضافة حالة تبرع جديدة",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = AtharMilkyCream
            )
          }
        }

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        // Form Fields Container
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {

          // 1. Upload Images Section (1 to 4 images)
          Text(
            text = "صور الحالة (من 1 إلى 4 صور):",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = AtharGold
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Image preview row + Add button
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            // Pick Images Button
            if (selectedImageUris.size < 4) {
              item {
                Box(
                  modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AtharDarkCardElevated)
                    .border(1.dp, AtharGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable {
                      photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                      )
                    }
                    .testTag("pick_images_button"),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                      imageVector = Icons.Default.AddPhotoAlternate,
                      contentDescription = "إضافة صور",
                      tint = AtharGold,
                      modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "${selectedImageUris.size}/4 صور",
                      fontFamily = CairoFontFamily,
                      fontSize = 10.sp,
                      color = AtharMilkyCreamSubtle
                    )
                  }
                }
              }
            }

            // Image Thumbnails
            items(selectedImageUris) { uri ->
              Box(
                modifier = Modifier
                  .size(90.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(AtharDarkCard)
              ) {
                AsyncImage(
                  model = uri,
                  contentDescription = "صورة الحالة",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )

                // Delete Button on thumbnail
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AtharPitchBlack.copy(alpha = 0.7f))
                    .clickable { selectedImageUris.remove(uri) },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "حذف الصورة",
                    tint = AtharMilkyCream,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Category Selector Chips
          Text(
            text = "تصنيف الحالة:",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = AtharGold
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
              val isSelected = cat == selectedCategory
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) AtharOliveContainer else AtharDarkCard)
                  .clickable { selectedCategory = cat }
                  .padding(horizontal = 12.dp, vertical = 7.dp)
              ) {
                Text(
                  text = cat,
                  fontFamily = CairoFontFamily,
                  fontSize = 12.sp,
                  color = if (isSelected) AtharGoldLight else AtharMilkyCreamMuted
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Title
          Text(
            text = "عنوان التبرع:",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = AtharGold
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("مثال: كفالة 10 أيتام في منطقة ريفية", color = AtharMilkyCreamSubtle, fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("donation_title_input"),
            shape = RoundedCornerShape(14.dp),
            colors = formFieldColors()
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Description (Multi-line)
          Text(
            text = "شرح تفاصيل واحتياج الحالة:",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = AtharGold
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("اكتب شرحاً وافياً عن احتياج الحالة وظروفها لتشجيع أهل الخير على العطاء...", color = AtharMilkyCreamSubtle, fontSize = 13.sp) },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("donation_desc_input"),
            shape = RoundedCornerShape(14.dp),
            colors = formFieldColors()
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Target Amount ($)
          Text(
            text = "المبلغ المطلوب للتبرع ($):",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = AtharGold
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = targetAmountStr,
            onValueChange = { targetAmountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
            placeholder = { Text("مثال: 3500", color = AtharMilkyCreamSubtle, fontSize = 13.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            leadingIcon = {
              Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = AtharGold)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("donation_amount_input"),
            shape = RoundedCornerShape(14.dp),
            colors = formFieldColors()
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Card / Account Number
          Text(
            text = "رقم الكارت / الحساب المالي لتلقي الدفعات:",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = AtharGold
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            placeholder = { Text("مثال: 5324 8812 9043 1187", color = AtharMilkyCreamSubtle, fontSize = 13.sp) },
            singleLine = true,
            leadingIcon = {
              Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = AtharGold)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("donation_card_input"),
            shape = RoundedCornerShape(14.dp),
            colors = formFieldColors()
          )

          Spacer(modifier = Modifier.height(16.dp))

          // WhatsApp Phone Number
          Text(
            text = "رقم هاتف الواتساب للتواصل وتأكيد التحويل:",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = AtharGold
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = whatsappPhone,
            onValueChange = { whatsappPhone = it },
            placeholder = { Text("مثال: +966501234567", color = AtharMilkyCreamSubtle, fontSize = 13.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            leadingIcon = {
              Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = AtharOliveLight)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("donation_phone_input"),
            shape = RoundedCornerShape(14.dp),
            colors = formFieldColors()
          )

          Spacer(modifier = Modifier.height(28.dp))

          // Publish Button
          Button(
            onClick = {
              val amt = targetAmountStr.toDoubleOrNull() ?: 0.0
              if (title.isBlank()) {
                Toast.makeText(context, "يرجى كتابة عنوان حالة التبرع", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (description.isBlank()) {
                Toast.makeText(context, "يرجى كتابة شرح الحالة", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (amt <= 0) {
                Toast.makeText(context, "يرجى إدخال مبلغ تبرع صالح", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (cardNumber.isBlank()) {
                Toast.makeText(context, "يرجى إدخال رقم البطاقة أو الحساب البنكي", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (whatsappPhone.isBlank()) {
                Toast.makeText(context, "يرجى إدخال رقم هاتف الواتساب", Toast.LENGTH_SHORT).show()
                return@Button
              }

              // Fallback images if user didn't pick any
              val finalImages = if (selectedImageUris.isNotEmpty()) {
                selectedImageUris.map { it.toString() }
              } else {
                listOf(
                  "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?q=80&w=800&auto=format&fit=crop",
                  "https://images.unsplash.com/photo-1541888946425-d0fbb186c5f7?q=80&w=800&auto=format&fit=crop"
                )
              }

              val authorName = authUiState.currentUser?.displayName
                ?: authUiState.userProfile?.displayName
                ?: if (authUiState.isGuest) "ضيف كريم" else "فاعل خير"

              val newCase = DonationCase(
                id = "case_${System.currentTimeMillis()}",
                title = title.trim(),
                description = description.trim(),
                targetAmount = amt,
                collectedAmount = 0.0,
                currency = "$",
                imageUrls = finalImages,
                cardNumber = cardNumber.trim(),
                bankOrPlatformName = "حساب معتمد / Visa & MasterCard",
                whatsappPhone = whatsappPhone.trim(),
                authorId = authUiState.currentUser?.uid ?: "user_anon",
                authorName = authorName,
                category = selectedCategory,
                createdAt = System.currentTimeMillis()
              )

              onPublish(newCase)
              Toast.makeText(context, "تم نشر حالة التبرع بنجاح!", Toast.LENGTH_LONG).show()
              onBack()
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AtharOlivePrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("publish_donation_button")
          ) {
            Text(
              text = "نشر حالة التبرع الآن",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = AtharMilkyCream
            )
          }

          Spacer(modifier = Modifier.height(30.dp))
        }
      }
    }
  }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = AtharMilkyCream,
  unfocusedTextColor = AtharMilkyCream,
  focusedBorderColor = AtharGold,
  unfocusedBorderColor = AtharDividerDark,
  focusedContainerColor = AtharDarkInputBg,
  unfocusedContainerColor = AtharDarkInputBg
)
