package com.example.hamro_media.repository

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.hamro_media.model.Comment
import com.example.hamro_media.model.Post
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PostRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val context: Context
) : PostRepository {

    override suspend fun createPost(post: Post, imageUri: String): Result<Unit> {
        return try {
            val imageUrl = uploadImageToCloudinary(imageUri).getOrThrow()
            val postWithImage = post.copy(imageUrl = imageUrl)
            
            firestore.collection("posts")
                .document(post.id)
                .set(postWithImage)
                .await()
            
            firestore.collection("users")
                .document(post.userId)
                .update("postsCount", FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val querySnapshot = firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Post::class.java)
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPosts(userId: String): Result<List<Post>> {
        return try {
            val querySnapshot = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Post::class.java)
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(post: Post): Result<Unit> {
        return try {
            firestore.collection("posts")
                .document(post.id)
                .set(post.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val postDoc = firestore.collection("posts").document(postId).get().await()
            val post = postDoc.toObject(Post::class.java)
            
            firestore.collection("posts")
                .document(postId)
                .delete()
                .await()
            
            post?.let {
                firestore.collection("users")
                    .document(it.userId)
                    .update("postsCount", FieldValue.increment(-1))
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likePost(postId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("posts")
                .document(postId)
                .update(
                    "likedBy", FieldValue.arrayUnion(userId),
                    "likeCount", FieldValue.increment(1)
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikePost(postId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("posts")
                .document(postId)
                .update(
                    "likedBy", FieldValue.arrayRemove(userId),
                    "likeCount", FieldValue.increment(-1)
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addComment(comment: Comment): Result<Unit> {
        return try {
            firestore.collection("comments")
                .document(comment.commentId)
                .set(comment)
                .await()
            
            firestore.collection("posts")
                .document(comment.postId)
                .update("commentCount", FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getComments(postId: String): Result<List<Comment>> {
        return try {
            val querySnapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val comments = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)
            }
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImageToCloudinary(imageUri: String): Result<String> {
        return suspendCoroutine { continuation ->
            MediaManager.get().upload(imageUri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as? String
                        if (imageUrl != null) {
                            continuation.resume(Result.success(imageUrl))
                        } else {
                            continuation.resume(Result.failure(Exception("Failed to get image URL")))
                        }
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.failure(Exception(error.description)))
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resume(Result.failure(Exception(error.description)))
                    }
                })
                .dispatch()
        }
    }
}