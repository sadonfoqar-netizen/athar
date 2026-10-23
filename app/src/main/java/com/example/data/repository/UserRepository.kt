package com.example.data.repository

import android.util.Log
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "UserRepository"

class UserRepository(
  private val db: FirebaseFirestore
) {

  fun observeUserProfile(userId: String): Flow<UserProfile?> {
    return db.collection("users")
      .document(userId)
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
          null
        }
      }
  }

  suspend fun syncUserProfile(
    userId: String,
    displayName: String,
    email: String,
    photoUrl: String = "",
    isGuest: Boolean = false
  ) {
    val userRef = db.collection("users").document(userId)
    try {
      val existing = userRef.get().await()
      if (!existing.exists()) {
        val data = mapOf(
          "displayName" to displayName,
          "email" to email,
          "photoUrl" to photoUrl,
          "isGuest" to isGuest,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        userRef.set(data).await()
      } else {
        val updates = mutableMapOf<String, Any>(
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (displayName.isNotBlank()) updates["displayName"] = displayName
        if (email.isNotBlank()) updates["email"] = email
        if (photoUrl.isNotBlank()) updates["photoUrl"] = photoUrl
        userRef.update(updates).await()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to sync user profile: ${e.message}", e)
    }
  }
}
