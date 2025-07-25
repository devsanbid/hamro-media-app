package com.example.hamro_media.repository

import com.example.hamro_media.model.Post

interface PostRepository {
    suspend fun createPost(post: Post, imageUri: String): Result<Unit>
    suspend fun getAllPosts(): Result<List<Post>>
    suspend fun getUserPosts(userId: String): Result<List<Post>>
    suspend fun updatePost(post: Post): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun likePost(postId: String, userId: String): Result<Unit>
    suspend fun unlikePost(postId: String, userId: String): Result<Unit>
    suspend fun uploadImageToCloudinary(imageUri: String): Result<String>
}