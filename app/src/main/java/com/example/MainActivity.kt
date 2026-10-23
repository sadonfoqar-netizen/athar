package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.UserRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.SignUpScreen
import com.example.ui.main.MainScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.AtharTheme
import com.google.firebase.firestore.FirebaseFirestore

enum class AtharScreen {
  SPLASH,
  LOGIN,
  SIGN_UP,
  MAIN
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
    val db = FirebaseFirestore.getInstance(databaseId)
    @Suppress("UNCHECKED_CAST")
    return AuthViewModel(UserRepository(db)) as T
  }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AtharTheme {
        AtharAppRoot()
      }
    }
  }
}

@Composable
fun AtharAppRoot() {
  val context = LocalContext.current
  val authViewModel: AuthViewModel = viewModel(
    factory = remember(context) { AuthViewModelFactory(context) }
  )

  val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
  var currentScreen by rememberSaveable { mutableStateOf(AtharScreen.SPLASH) }

  // Auto transition to main if user logs in while on login or sign up
  LaunchedEffect(authUiState.isLoggedIn) {
    if (authUiState.isLoggedIn && (currentScreen == AtharScreen.LOGIN || currentScreen == AtharScreen.SIGN_UP)) {
      currentScreen = AtharScreen.MAIN
    }
  }

  when (currentScreen) {
    AtharScreen.SIGN_UP -> {
      BackHandler {
        currentScreen = AtharScreen.LOGIN
      }
    }
    AtharScreen.SPLASH -> {
      BackHandler {
        currentScreen = AtharScreen.MAIN
      }
    }
    else -> {}
  }

  Crossfade(
    targetState = currentScreen,
    animationSpec = tween(durationMillis = 500),
    label = "screen_crossfade"
  ) { screen ->
    when (screen) {
      AtharScreen.SPLASH -> {
        SplashScreen(
          onSplashFinished = {
            currentScreen = AtharScreen.MAIN
          },
          modifier = Modifier.fillMaxSize()
        )
      }
      AtharScreen.LOGIN -> {
        LoginScreen(
          viewModel = authViewModel,
          onNavigateToSignUp = {
            currentScreen = AtharScreen.SIGN_UP
          },
          modifier = Modifier.fillMaxSize()
        )
      }
      AtharScreen.SIGN_UP -> {
        SignUpScreen(
          viewModel = authViewModel,
          onNavigateToLogin = {
            currentScreen = AtharScreen.LOGIN
          },
          modifier = Modifier.fillMaxSize()
        )
      }
      AtharScreen.MAIN -> {
        MainScreen(
          authUiState = authUiState,
          onSignOut = {
            authViewModel.signOut()
            currentScreen = AtharScreen.LOGIN
          },
          onReplaySplash = {
            currentScreen = AtharScreen.SPLASH
          },
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
