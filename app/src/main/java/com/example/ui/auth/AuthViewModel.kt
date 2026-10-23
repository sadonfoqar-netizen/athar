package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthViewModel"

data class AuthUiState(
  val isLoading: Boolean = false,
  val isGuest: Boolean = false,
  val currentUser: FirebaseUser? = null,
  val userProfile: UserProfile? = null,
  val errorMessage: String? = null,
  val infoMessage: String? = null
) {
  val isLoggedIn: Boolean get() = currentUser != null || isGuest
}

class AuthViewModel(
  private val userRepository: UserRepository
) : ViewModel() {

  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

  private val _uiState = MutableStateFlow(
    AuthUiState(currentUser = auth.currentUser)
  )
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    val user = firebaseAuth.currentUser
    _uiState.update { it.copy(currentUser = user, isLoading = false) }
    if (user != null) {
      viewModelScope.launch {
        userRepository.syncUserProfile(
          userId = user.uid,
          displayName = user.displayName ?: "مستخدم أثر",
          email = user.email ?: "",
          photoUrl = user.photoUrl?.toString() ?: ""
        )
        userRepository.observeUserProfile(user.uid).collect { profile ->
          _uiState.update { it.copy(userProfile = profile) }
        }
      }
    }
  }

  init {
    auth.addAuthStateListener(authStateListener)
  }

  fun signInWithGoogle(context: Context, webClientId: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
      try {
        val credentialManager = CredentialManager.create(context)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
          .build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(signInWithGoogleOption)
          .build()

        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
          val idToken = googleIdTokenCredential.idToken
          val authCredential = GoogleAuthProvider.getCredential(idToken, null)
          auth.signInWithCredential(authCredential).await()
          _uiState.update { it.copy(isLoading = false, isGuest = false) }
        } else {
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = "نوع المصادقة غير مدعوم حالياً"
            )
          }
        }
      } catch (e: GetCredentialCancellationException) {
        Log.d(TAG, "Google sign-in was cancelled by user")
        _uiState.update { it.copy(isLoading = false) }
      } catch (e: Exception) {
        Log.e(TAG, "Failed Google Sign-In", e)
        _uiState.update {
          it.copy(
            isLoading = false,
            errorMessage = e.localizedMessage ?: "فشل تسجيل الدخول عبر Google، يرجى المحاولة لاحقاً"
          )
        }
      }
    }
  }

  fun continueAsGuest() {
    _uiState.update {
      it.copy(
        isGuest = true,
        errorMessage = null,
        userProfile = UserProfile(
          id = "guest_user",
          displayName = "ضيف كريم",
          email = "",
          isGuest = true
        )
      )
    }
  }

  fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      onResult(false, "يرجى إدخال بريد إلكتروني صالح")
      return
    }
    viewModelScope.launch {
      try {
        auth.sendPasswordResetEmail(email).await()
        onResult(true, "تم إرسال رابط استعادة كلمة المرور إلى بريدك الإلكتروني بنجاح.")
      } catch (e: Exception) {
        // Provide friendly message even if provider is not configured
        onResult(true, "إذا كان البريد مسجلاً في النظام، فستصلك رسالة الاستعادة قريباً.")
      }
    }
  }

  fun clearMessages() {
    _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
  }

  fun signOut() {
    auth.signOut()
    _uiState.update {
      AuthUiState(
        currentUser = null,
        userProfile = null,
        isGuest = false
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    auth.removeAuthStateListener(authStateListener)
  }
}
