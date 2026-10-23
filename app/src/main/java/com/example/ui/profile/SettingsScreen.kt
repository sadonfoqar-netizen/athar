package com.example.ui.profile

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
fun SettingsScreen(
  profileData: UserProfileData,
  onUpdateProfileData: (UserProfileData) -> Unit,
  onOpenEditProfile: () -> Unit,
  onSignOut: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  BackHandler {
    onBack()
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .testTag("settings_screen")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {

        // Top Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("settings_back_button")
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
            text = "الإعدادات العامة",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AtharMilkyCream
          )
        }

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        // Settings Content Scroll
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {

          // ==========================================
          // أ. حاوية الحساب والخصوصية (Account & Privacy)
          // ==========================================
          SettingsSectionHeader(title = "الحساب والخصوصية", icon = Icons.Default.Lock)
          SettingsContainer {
            // Edit Profile Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenEditProfile() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AtharGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(text = "تعديل الملف الشخصي", fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AtharMilkyCream)
                  Text(text = "تغيير الاسم، الصورة، البانر، والبايو", fontFamily = CairoFontFamily, fontSize = 11.sp, color = AtharMilkyCreamSubtle)
                }
              }
              Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = AtharMilkyCreamSubtle, modifier = Modifier.size(20.dp))
            }

            HorizontalDivider(color = AtharDividerDark, thickness = 0.5.dp)

            // Private Account Switch
            SettingsSwitchItem(
              title = "الحساب الخاص (Private Account)",
              explanation = "عند تفعيله، لن يتمكن أحد من رؤية منشوراتك أو متابعتك إلا بعد موافقتك، للحفاظ على الخصوصية التامة.",
              checked = profileData.isPrivateAccount,
              onCheckedChange = { onUpdateProfileData(profileData.copy(isPrivateAccount = it)) }
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // ==========================================
          // ب. حاوية التنبيهات والإشعارات (Notifications)
          // ==========================================
          SettingsSectionHeader(title = "التنبيهات والإشعارات الإيمانية", icon = Icons.Default.Notifications)
          SettingsContainer {
            SettingsSwitchItem(
              title = "إشعار الآية اليومية المباركة",
              explanation = "لتشغيل أو إيقاف ظهور الآية الكريمة عند فتح قفل الهاتف للتذكير بختم القرآن الكريم وتدبر معانيه.",
              checked = profileData.dailyAyahNotification,
              onCheckedChange = { onUpdateProfileData(profileData.copy(dailyAyahNotification = it)) }
            )
            HorizontalDivider(color = AtharDividerDark, thickness = 0.5.dp)

            SettingsSwitchItem(
              title = "تنبيهات أوقات الصلاة والأذان",
              explanation = "لإرسال إشعار وتنبيه الأذان عند دخول وقت كل صلاة من الصلوات الخمس بناءً على موقعك الجغرافي.",
              checked = profileData.prayerTimesNotification,
              onCheckedChange = { onUpdateProfileData(profileData.copy(prayerTimesNotification = it)) }
            )
            HorizontalDivider(color = AtharDividerDark, thickness = 0.5.dp)

            SettingsSwitchItem(
              title = "تنبيه أذكار الصباح والمساء",
              explanation = "لتذكيرك بوقت قراءة الأذكار صباحاً ومساءً لكسب الحسنات وحفظ النفس بذكر الله.",
              checked = profileData.azkarNotification,
              onCheckedChange = { onUpdateProfileData(profileData.copy(azkarNotification = it)) }
            )
            HorizontalDivider(color = AtharDividerDark, thickness = 0.5.dp)

            SettingsSwitchItem(
              title = "تنبيه قصص الذكاء الاصطناعي (كل 8 ساعات)",
              explanation = "لإعلامك فور نزول قصة وعبرة تاريخية جديدة موثوقة ومحققة في التطبيق للتزود بالإيمان.",
              checked = profileData.aiStoryNotification,
              onCheckedChange = { onUpdateProfileData(profileData.copy(aiStoryNotification = it)) }
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // ==========================================
          // ج. حاوية الصوت والاهتزاز (Audio & Haptics)
          // ==========================================
          SettingsSectionHeader(title = "الصوت والاهتزاز التفاعلي", icon = Icons.AutoMirrored.Filled.VolumeUp)
          SettingsContainer {
            SettingsSwitchItem(
              title = "اهتزاز المسبحة الإلكترونية (Haptic Feedback)",
              explanation = "لتشغيل اهتزاز خفيف جداً مع كل ضغطة تسبيح، لتقوم بالتسبيح دون الحاجة للنظر للشاشة.",
              checked = profileData.tasbeehHaptic,
              onCheckedChange = { onUpdateProfileData(profileData.copy(tasbeehHaptic = it)) }
            )
            HorizontalDivider(color = AtharDividerDark, thickness = 0.5.dp)

            SettingsSwitchItem(
              title = "التشغيل التلقائي للسورة التالية",
              explanation = "للانتقال التلقائي للسورة التالية بصوت الشيخ مشاري العفاسي عند انتهاء السورة الحالية دون انقطاع.",
              checked = profileData.autoNextSurah,
              onCheckedChange = { onUpdateProfileData(profileData.copy(autoNextSurah = it)) }
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // ==========================================
          // د. حاوية من نحن ورؤيتنا (About & Vision)
          // ==========================================
          SettingsSectionHeader(title = "من نحن ورؤيتنا", icon = Icons.Default.Info)
          SettingsContainer {
            // About Creator
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Text(
                text = "عن المصمم والمنشئ",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AtharGold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "ذو الفقار سعدون - مصمم بعمر 15 سنة من العراق",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = AtharMilkyCream
              )
            }

            HorizontalDivider(color = AtharDividerDark, thickness = 0.5.dp)

            // Vision
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Text(
                text = "رؤيتنا ولماذا أنشأنا تطبيق «أَثَـر»؟",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AtharGold
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "تم إنشاء هذا التطبيق ليكون صدقة جارية وعلم يُنتفع به، يهدف لجمع الحسنات للمستخدم والمصمم في كل ثانية (عبر القراءة، التسبيح، الصدقات، ونشر الخير)، وليكون رفيقاً إيمانياً كاملاً لكل مسلم بدون إعلانات أو شات قد يشتت العبادة.",
                fontFamily = CairoFontFamily,
                fontSize = 12.5.sp,
                lineHeight = 22.sp,
                color = AtharMilkyCreamMuted
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // ==========================================
          // هـ. حاوية الدعم والعمليات (Actions & Support)
          // ==========================================
          SettingsSectionHeader(title = "الدعم والعمليات", icon = Icons.Default.Share)
          SettingsContainer {
            // Share App
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                      Intent.EXTRA_TEXT,
                      "تطبيق «أَثَـر» الإيماني - رفيقك اليومي لقراءة القرآن الكريم، الأذكار، التبرعات والصدقات، ومجالس العبرة بالذكاء الاصطناعي\nأثرٌ يبقى.. وحسنات لا تنقطع."
                    )
                  }
                  context.startActivity(Intent.createChooser(sendIntent, "مشاركة تطبيق أثر"))
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = AtharOliveLight, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(text = "مشاركة التطبيق مع أحبابك", fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AtharMilkyCream)
                  Text(text = "الدال على الخير كفاعله، انشر الأثر لتكسب الحسنات", fontFamily = CairoFontFamily, fontSize = 11.sp, color = AtharMilkyCreamSubtle)
                }
              }
              Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = AtharMilkyCreamSubtle, modifier = Modifier.size(20.dp))
            }

            HorizontalDivider(color = AtharDividerDark, thickness = 0.5.dp)

            // Log Out Button (Transparent Subtle Red)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSignOut() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("settings_logout_button"),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = Color(0xFFE57373),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "تسجيل الخروج من الحساب",
                  fontFamily = CairoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color(0xFFEF9A9A)
                )
                Text(
                  text = "يمكنك العودة في أي وقت أو الاستمرار كضيف",
                  fontFamily = CairoFontFamily,
                  fontSize = 11.sp,
                  color = AtharMilkyCreamSubtle
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(30.dp))
        }
      }
    }
  }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = AtharGold,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = title,
      fontFamily = CairoFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = AtharGoldLight
    )
  }
}

@Composable
private fun SettingsContainer(content: @Composable () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
    border = null
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      content()
    }
  }
}

@Composable
private fun SettingsSwitchItem(
  title: String,
  explanation: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        color = AtharMilkyCream
      )
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = explanation,
        fontFamily = CairoFontFamily,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = AtharMilkyCreamSubtle
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = AtharGold,
        checkedTrackColor = AtharOliveContainer,
        uncheckedThumbColor = AtharMilkyCreamSubtle,
        uncheckedTrackColor = AtharDarkCard
      )
    )
  }
}
