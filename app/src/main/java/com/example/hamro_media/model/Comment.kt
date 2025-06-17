package com.example.hamro_media.model

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)