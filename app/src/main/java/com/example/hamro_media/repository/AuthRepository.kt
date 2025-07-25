package com.example.hamro_media.repository

import com.example.hamro_media.model.User
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun register(email: String, password: String, username: String): Result<FirebaseUser>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): FirebaseUser?
    suspend fun updateProfile(user: User): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun followUser(currentUserId: String, targetUserId: String): Result<Unit>
    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit>
    suspend fun isFollowing(currentUserId: String, targetUserId: String): Result<Boolean>
    suspend fun updateUserStats(userId: String, postsCount: Int? = null, followersCount: Int? = null, followingCount: Int? = null): Result<Unit>
}