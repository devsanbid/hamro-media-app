package com.example.hamro_media.repository

import com.example.hamro_media.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(
                    userId = firebaseUser.uid,
                    username = username,
                    email = email
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.userId)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val user = document.toObject(User::class.java)
            user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun followUser(currentUserId: String, targetUserId: String): Result<Unit> {
        return try {
            // Add to current user's following list
            firestore.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(targetUserId)
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .await()
            
            // Add to target user's followers list
            firestore.collection("users")
                .document(targetUserId)
                .collection("followers")
                .document(currentUserId)
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .await()
            
            // Update follower/following counts
            updateFollowCounts(currentUserId, targetUserId, true)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit> {
        return try {
            // Remove from current user's following list
            firestore.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(targetUserId)
                .delete()
                .await()
            
            // Remove from target user's followers list
            firestore.collection("users")
                .document(targetUserId)
                .collection("followers")
                .document(currentUserId)
                .delete()
                .await()
            
            // Update follower/following counts
            updateFollowCounts(currentUserId, targetUserId, false)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isFollowing(currentUserId: String, targetUserId: String): Result<Boolean> {
        return try {
            val document = firestore.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(targetUserId)
                .get()
                .await()
            Result.success(document.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserStats(userId: String, postsCount: Int?, followersCount: Int?, followingCount: Int?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            postsCount?.let { updates["postsCount"] = it }
            followersCount?.let { updates["followersCount"] = it }
            followingCount?.let { updates["followingCount"] = it }
            
            if (updates.isNotEmpty()) {
                firestore.collection("users")
                    .document(userId)
                    .update(updates)
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateFollowCounts(currentUserId: String, targetUserId: String, isFollowing: Boolean) {
        try {
            // Get current counts
            val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
            val targetUserDoc = firestore.collection("users").document(targetUserId).get().await()
            
            val currentUser = currentUserDoc.toObject(User::class.java)
            val targetUser = targetUserDoc.toObject(User::class.java)
            
            if (currentUser != null && targetUser != null) {
                val followingDelta = if (isFollowing) 1 else -1
                val followersDelta = if (isFollowing) 1 else -1
                
                // Update current user's following count
                firestore.collection("users")
                    .document(currentUserId)
                    .update("followingCount", (currentUser.followingCount + followingDelta).coerceAtLeast(0))
                    .await()
                
                // Update target user's followers count
                firestore.collection("users")
                    .document(targetUserId)
                    .update("followersCount", (targetUser.followersCount + followersDelta).coerceAtLeast(0))
                    .await()
            }
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }
}