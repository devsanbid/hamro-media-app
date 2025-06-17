package com.example.hamro_media.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hamro_media.model.Comment
import com.example.hamro_media.model.Post
import com.example.hamro_media.repository.PostRepository
import com.example.hamro_media.repository.PostRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PostViewModel(
    context: Context,
    private val postRepository: PostRepository = com.example.hamro_media.di.AppModule.providePostRepository(context)
) : ViewModel() {
    
    private val _postState = MutableStateFlow(PostState())
    val postState: StateFlow<PostState> = _postState.asStateFlow()
    
    private val _commentsState = MutableStateFlow(CommentsState())
    val commentsState: StateFlow<CommentsState> = _commentsState.asStateFlow()

    fun loadPosts() {
        viewModelScope.launch {
            _postState.value = _postState.value.copy(isLoading = true, error = null)
            
            postRepository.getAllPosts()
                .onSuccess { posts ->
                    _postState.value = _postState.value.copy(
                        isLoading = false,
                        posts = posts
                    )
                }
                .onFailure { exception ->
                    _postState.value = _postState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            _postState.value = _postState.value.copy(isLoading = true, error = null)
            
            postRepository.getUserPosts(userId)
                .onSuccess { posts ->
                    _postState.value = _postState.value.copy(
                        isLoading = false,
                        userPosts = posts
                    )
                }
                .onFailure { exception ->
                    _postState.value = _postState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun createPost(post: Post, imageUri: String) {
        viewModelScope.launch {
            _postState.value = _postState.value.copy(isLoading = true, error = null)
            
            val postWithId = post.copy(id = UUID.randomUUID().toString())
            
            postRepository.createPost(postWithId, imageUri)
                .onSuccess {
                    _postState.value = _postState.value.copy(
                        isLoading = false,
                        isPostCreated = true
                    )
                    loadPosts()
                }
                .onFailure { exception ->
                    _postState.value = _postState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun updatePost(post: Post) {
        viewModelScope.launch {
            postRepository.updatePost(post)
                .onSuccess {
                    loadPosts()
                }
                .onFailure { exception ->
                    _postState.value = _postState.value.copy(error = exception.message)
                }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId)
                .onSuccess {
                    loadPosts()
                }
                .onFailure { exception ->
                    _postState.value = _postState.value.copy(error = exception.message)
                }
        }
    }

    fun likePost(postId: String, userId: String) {
        viewModelScope.launch {
            postRepository.likePost(postId, userId)
                .onSuccess {
                    loadPosts()
                }
        }
    }

    fun unlikePost(postId: String, userId: String) {
        viewModelScope.launch {
            postRepository.unlikePost(postId, userId)
                .onSuccess {
                    loadPosts()
                }
        }
    }

    fun addComment(comment: Comment) {
        viewModelScope.launch {
            _commentsState.value = _commentsState.value.copy(isLoading = true, error = null)
            
            postRepository.addComment(comment)
                .onSuccess {
                    loadComments(comment.postId)
                }
                .onFailure { exception ->
                    _commentsState.value = _commentsState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _commentsState.value = _commentsState.value.copy(isLoading = true, error = null)
            
            postRepository.getComments(postId)
                .onSuccess { comments ->
                    _commentsState.value = _commentsState.value.copy(
                        isLoading = false,
                        comments = comments
                    )
                }
                .onFailure { exception ->
                    _commentsState.value = _commentsState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun clearPostCreatedFlag() {
        _postState.value = _postState.value.copy(isPostCreated = false)
    }

    fun clearError() {
        _postState.value = _postState.value.copy(error = null)
        _commentsState.value = _commentsState.value.copy(error = null)
    }
}

data class PostState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val userPosts: List<Post> = emptyList(),
    val isPostCreated: Boolean = false,
    val error: String? = null
)

data class CommentsState(
    val isLoading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val error: String? = null
)