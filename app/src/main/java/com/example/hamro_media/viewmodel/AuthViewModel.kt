package com.example.hamro_media.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hamro_media.model.User
import com.example.hamro_media.repository.AuthRepository
import com.example.hamro_media.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = com.example.hamro_media.di.AppModule.provideAuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.login(email, password)
                .onSuccess { firebaseUser ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    loadUserProfile(firebaseUser.uid)
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.register(email, password, username)
                .onSuccess { firebaseUser ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    loadUserProfile(firebaseUser.uid)
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.resetPassword(email)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        passwordResetSent = true
                    )
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _authState.value = AuthState()
                    _currentUser.value = null
                }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.updateProfile(user)
                .onSuccess {
                    _currentUser.value = user
                    _authState.value = _authState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser != null) {
                _authState.value = _authState.value.copy(isAuthenticated = true)
                loadUserProfile(firebaseUser.uid)
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            authRepository.getUserProfile(userId)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = _authState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    // If user profile doesn't exist in Firestore, create a basic user object
                    val firebaseUser = authRepository.getCurrentUser()
                    if (firebaseUser != null) {
                        val basicUser = User(
                            userId = firebaseUser.uid,
                            username = firebaseUser.email?.substringBefore("@") ?: "user",
                            email = firebaseUser.email ?: "",
                            profileImageUrl = "",
                            bio = "",
                            followersCount = 0,
                            followingCount = 0,
                            postsCount = 0
                        )
                        _currentUser.value = basicUser
                        _authState.value = _authState.value.copy(isLoading = false)
                        // Optionally save this basic user to Firestore
                        updateProfile(basicUser)
                    }
                }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun clearPasswordResetFlag() {
        _authState.value = _authState.value.copy(passwordResetSent = false)
    }

    fun loadOtherUserProfile(userId: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            authRepository.getUserProfile(userId)
                .onSuccess { user ->
                    onResult(user)
                }
                .onFailure {
                    onResult(null)
                }
        }
    }

    fun followUser(targetUserId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUserId = _currentUser.value?.userId
            if (currentUserId != null) {
                authRepository.followUser(currentUserId, targetUserId)
                    .onSuccess {
                        onResult(true)
                    }
                    .onFailure {
                        onResult(false)
                    }
            } else {
                onResult(false)
            }
        }
    }

    fun unfollowUser(targetUserId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUserId = _currentUser.value?.userId
            if (currentUserId != null) {
                authRepository.unfollowUser(currentUserId, targetUserId)
                    .onSuccess {
                        onResult(true)
                    }
                    .onFailure {
                        onResult(false)
                    }
            } else {
                onResult(false)
            }
        }
    }

    fun checkIfFollowing(targetUserId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUserId = _currentUser.value?.userId
            if (currentUserId != null) {
                authRepository.isFollowing(currentUserId, targetUserId)
                    .onSuccess { isFollowing ->
                        onResult(isFollowing)
                    }
                    .onFailure {
                        onResult(false)
                    }
            } else {
                onResult(false)
            }
        }
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val passwordResetSent: Boolean = false
)