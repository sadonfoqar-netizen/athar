package com.example.ui.posts

data class ReelItem(
  val id: String,
  val publisherName: String,
  val isVerified: Boolean = true,
  val quoteHighlight: String,
  val caption: String,
  val audioTitle: String,
  val likesCount: Int,
  val bookmarksCount: Int,
  val sharesCount: Int,
  val isLiked: Boolean = false,
  val isBookmarked: Boolean = false,
  val isFollowing: Boolean = false,
  val imageRes: Int,
  val isForYou: Boolean = true,
  val isFollowingFeed: Boolean = false
)
