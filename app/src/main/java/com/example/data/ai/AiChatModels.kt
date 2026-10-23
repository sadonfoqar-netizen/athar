package com.example.data.ai

/**
 * Represents an individual message in the Islamic AI Chat
 */
data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val role: String, // "user" or "model"
  val content: String,
  val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a saved chat session synchronized with Firebase Firestore
 */
data class ChatSession(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val messages: List<ChatMessage> = emptyList()
)
