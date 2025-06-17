package com.example.hamro_media.model

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)