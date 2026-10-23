package com.example.ui.azkar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.azkar.KAABA_LATITUDE
import com.example.data.azkar.KAABA_LONGITUDE
import com.example.data.azkar.QiblaSensorManager
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val sensorManager = remember(context) { QiblaSensorManager(context) }
  val qiblaState by sensorManager.qiblaState.collectAsStateWithLifecycle()

  var hasLocationPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  // Location permission request launcher
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasLocationPermission = isGranted
    if (isGranted) {
      fetchDeviceLocation(context, sensorManager)
    }
  }

  val vibrator = remember(context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  // Manage sensor listener lifecycle
  DisposableEffect(sensorManager) {
    sensorManager.startListening()
    if (hasLocationPermission) {
      fetchDeviceLocation(context, sensorManager)
    } else {
      permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    onDispose {
      sensorManager.stopListening()
    }
  }

  // Alignment haptic trigger
  var lastAlignedState by remember { mutableStateOf(false) }
  LaunchedEffect(qiblaState.isAligned) {
    if (qiblaState.isAligned && !lastAlignedState) {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(45)
        }
      } catch (_: Exception) {}
    }
    lastAlignedState = qiblaState.isAligned
  }

  BackHandler {
    onBack()
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(AtharPitchBlack)
        .statusBarsPadding()
        .testTag("qibla_compass_screen")
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {

        // Top Navigation Bar
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
              modifier = Modifier.testTag("qibla_back_button")
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
                text = "بوصلة القبلة المشرفة",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharMilkyCream
              )
              Text(
                text = if (qiblaState.isAligned) "أنت باتجاه القبلة الآن" else "زاوية القبلة: ${qiblaState.qiblaBearingDegrees.toInt()}°",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = if (qiblaState.isAligned) AtharGoldLight else AtharOliveLight
              )
            }
          }

          // Accuracy badge
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CompassCalibration,
              contentDescription = null,
              tint = if (qiblaState.isAligned) AtharGold else AtharOliveLight,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = qiblaState.accuracyStatus,
              fontFamily = CairoFontFamily,
              fontSize = 11.sp,
              color = AtharMilkyCreamSubtle
            )
          }
        }

        HorizontalDivider(color = AtharDividerDark, thickness = 0.8.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Alignment Status Banner
        val bannerBg by animateColorAsState(
          targetValue = if (qiblaState.isAligned) AtharOliveContainer else AtharDarkCard,
          label = "banner_bg"
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bannerBg)
            .padding(vertical = 12.dp, horizontal = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Mosque,
              contentDescription = null,
              tint = if (qiblaState.isAligned) AtharGold else AtharOliveLight,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (qiblaState.isAligned) "أنت تواجه الكعبة المشرفة بدقة" else "قم بتدوير الهاتف نحو السهم الذهبي",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = if (qiblaState.isAligned) AtharGoldLight else AtharMilkyCream
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Compass Dial Box
        Box(
          modifier = Modifier
            .size(290.dp)
            .testTag("compass_dial_box"),
          contentAlignment = Alignment.Center
        ) {

          // Animated rotation angle: compass card rotates opposite to azimuth
          val animatedAzimuth by animateFloatAsState(
            targetValue = -qiblaState.azimuthDegrees,
            animationSpec = spring(stiffness = 300f),
            label = "azimuth_anim"
          )

          // 1. Compass Outer Dial (Rotates with azimuth)
          Canvas(
            modifier = Modifier
              .size(280.dp)
              .rotate(animatedAzimuth)
          ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Outer Circle Ring
            drawCircle(
              color = AtharDarkCardElevated,
              radius = radius
            )
            drawCircle(
              color = AtharDividerDark,
              radius = radius,
              style = Stroke(width = 2.dp.toPx())
            )

            // Degree Ticks (every 30 degrees)
            for (deg in 0 until 360 step 30) {
              val rad = Math.toRadians(deg.toDouble())
              val isCardinal = deg % 90 == 0
              val tickLen = if (isCardinal) 14.dp.toPx() else 7.dp.toPx()
              val color = if (deg == 0) AtharGold else AtharMilkyCreamSubtle

              val start = Offset(
                (center.x + (radius - tickLen) * sin(rad)).toFloat(),
                (center.y - (radius - tickLen) * cos(rad)).toFloat()
              )
              val end = Offset(
                (center.x + radius * sin(rad)).toFloat(),
                (center.y - radius * cos(rad)).toFloat()
              )
              drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = if (isCardinal) 2.5.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
              )
            }
          }

          // 2. Cardinal Labels overlay (N, S, E, W / ش, ج, ق, غ) rotating with dial
          Box(
            modifier = Modifier
              .size(280.dp)
              .rotate(animatedAzimuth),
            contentAlignment = Alignment.Center
          ) {
            // North (شمال)
            Text(
              text = "ش",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = AtharGold,
              modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
            )
            // South (جنوب)
            Text(
              text = "ج",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = AtharMilkyCreamMuted,
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
            )
            // East (شرق)
            Text(
              text = "ق",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = AtharMilkyCreamMuted,
              modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
            )
            // West (غرب)
            Text(
              text = "غ",
              fontFamily = CairoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = AtharMilkyCreamMuted,
              modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
            )
          }

          // 3. Qibla Needle Pointing to Kaaba (Relative angle: differenceDegrees)
          val needleAngle by animateFloatAsState(
            targetValue = qiblaState.differenceDegrees,
            animationSpec = spring(stiffness = 300f),
            label = "needle_anim"
          )

          Canvas(
            modifier = Modifier
              .size(220.dp)
              .rotate(needleAngle)
          ) {
            val center = Offset(size.width / 2, size.height / 2)
            val needleLen = size.height * 0.42f
            val needleWidth = 10.dp.toPx()

            val needleColor = if (qiblaState.isAligned) AtharGoldLight else AtharGold

            // Golden Top Needle (Points to Qibla)
            val topPath = Path().apply {
              moveTo(center.x, center.y - needleLen)
              lineTo(center.x + needleWidth / 2, center.y)
              lineTo(center.x - needleWidth / 2, center.y)
              close()
            }
            drawPath(topPath, color = needleColor)

            // Bottom subtle needle
            val bottomPath = Path().apply {
              moveTo(center.x, center.y + needleLen * 0.6f)
              lineTo(center.x + needleWidth / 2, center.y)
              lineTo(center.x - needleWidth / 2, center.y)
              close()
            }
            drawPath(bottomPath, color = AtharDividerDark)

            // Center golden hub
            drawCircle(color = AtharGold, radius = 6.dp.toPx(), center = center)
          }

          // 4. Center Kaaba Symbol
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (qiblaState.isAligned) AtharGold else AtharDarkCardElevated),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Mosque,
              contentDescription = "الكعبة المشرفة",
              tint = if (qiblaState.isAligned) androidx.compose.ui.graphics.Color.White else AtharGold,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Details Info Card (Distance, Coordinates, Bearing)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = AtharDarkCardElevated),
          border = null
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Bearing
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "${qiblaState.qiblaBearingDegrees.toInt()}°",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharGold
              )
              Text(
                text = "زاوية القبلة",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharMilkyCreamSubtle
              )
            }

            // Distance to Kaaba
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (qiblaState.distanceToKaabaKm > 0) "${qiblaState.distanceToKaabaKm} كم" else "مكة المكرمة",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharMilkyCream
              )
              Text(
                text = "المسافة إلى مكة",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharMilkyCreamSubtle
              )
            }

            // Current Compass Heading
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "${qiblaState.azimuthDegrees.toInt()}°",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AtharOliveLight
              )
              Text(
                text = "اتجاه الهاتف",
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                color = AtharMilkyCreamSubtle
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "ضع الهاتف بشكل مستوٍ وأدره حتى يضيء المؤشر باللون الذهبي",
          fontFamily = CairoFontFamily,
          fontSize = 12.sp,
          color = AtharMilkyCreamSubtle,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 32.dp)
        )
      }
    }
  }
}

private fun fetchDeviceLocation(context: Context, sensorManager: QiblaSensorManager) {
  try {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager != null) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val best = lastGps ?: lastNet
        if (best != null) {
          sensorManager.updateUserLocation(best.latitude, best.longitude)
          return
        }
      }
    }
  } catch (_: Exception) {}

  // Fallback default coordinates (Mecca)
  sensorManager.updateUserLocation(KAABA_LATITUDE, KAABA_LONGITUDE)
}
