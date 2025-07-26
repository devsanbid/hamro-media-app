package com.example.hamro_media.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isLikedByCurrentUser(currentUserId: String?): Boolean {
        return currentUserId != null && likedBy.contains(currentUserId)
    }
    
    val isLikedByCurrentUser: Boolean
        get() = false
}