package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AtharPitchBlack
import kotlinx.coroutines.delay

/**
 * Minimalist Pitch Black Splash Screen:
 * Displays the emblem in the center against the deep pitch black background,
 * free of any surrounding circles, borders, or loading indicators.
 */
@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  val contentAlpha = remember { Animatable(0f) }
  val contentScale = remember { Animatable(0.94f) }

  LaunchedEffect(Unit) {
    contentAlpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(Unit) {
    contentScale.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(Unit) {
    delay(2000)
    onSplashFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AtharPitchBlack)
      .testTag("splash_screen")
      .statusBarsPadding()
      .navigationBarsPadding(),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_app_logo),
      contentDescription = null,
      modifier = Modifier
        .size(190.dp)
        .scale(contentScale.value)
        .alpha(contentAlpha.value)
        .clip(RoundedCornerShape(24.dp))
        .testTag("splash_logo"),
      contentScale = ContentScale.Fit
    )
  }
}
