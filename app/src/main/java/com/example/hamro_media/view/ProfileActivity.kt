package com.example.hamro_media.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hamro_media.model.Post
import com.example.hamro_media.model.User
import com.example.hamro_media.view.PostGridItem
import com.example.hamro_media.viewmodel.AuthViewModel
import com.example.hamro_media.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileActivity(
    userId: String? = null,
    authViewModel: AuthViewModel? = null,
    postViewModel: PostViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val actualAuthViewModel = authViewModel ?: viewModel()
    val actualPostViewModel = postViewModel ?: viewModel { PostViewModel(context) }
    
    val authState by actualAuthViewModel.authState.collectAsState()
    val currentUser by actualAuthViewModel.currentUser.collectAsState()
    val postState by actualPostViewModel.postState.collectAsState()
    
    // State for other user's profile
    var otherUser by remember { mutableStateOf<User?>(null) }
    var isLoadingOtherUser by remember { mutableStateOf(false) }
    
    // State for edit post dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    
    // Determine which user to display
    val isCurrentUserProfile = userId == null || userId == currentUser?.userId
    val displayUser = if (isCurrentUserProfile) {
        currentUser
    } else {
        otherUser
    }
    
    // Load other user's profile if needed
    LaunchedEffect(userId, currentUser) {
        if (!isCurrentUserProfile && userId != null) {
            isLoadingOtherUser = true
            actualAuthViewModel.loadOtherUserProfile(userId) { user ->
                otherUser = user
                isLoadingOtherUser = false
            }
        }
    }
    
    // Load user posts for the profile being viewed
    LaunchedEffect(displayUser?.userId) {
        displayUser?.userId?.let { targetUserId ->
            actualPostViewModel.loadUserPosts(targetUserId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = displayUser?.username ?: "Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (!isCurrentUserProfile) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (isCurrentUserProfile) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                        IconButton(onClick = {
                            actualAuthViewModel.logout()
                            onLogout()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (displayUser == null && (authState.isLoading || isLoadingOtherUser)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Header
                ProfileHeader(
                    user = displayUser,
                    isCurrentUser = isCurrentUserProfile,
                    onEditProfile = onNavigateToEditProfile,
                    displayUser = displayUser,
                    currentUser = currentUser,
                    authViewModel = actualAuthViewModel
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Posts Grid
                Text(
                    text = "Posts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                when {
                    postState.isLoading && postState.userPosts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    postState.userPosts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isCurrentUserProfile) "No posts yet" else "No posts",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                if (isCurrentUserProfile) {
                                    Text(
                                        text = "Share your first post!",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((postState.userPosts.size / 3 + 1) * 120).dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            items(postState.userPosts) { post ->
                                PostGridItem(
                                    post = post,
                                    onClick = { /* Handle post click */ },
                                    onLikeClick = {
                                        actualPostViewModel.likePost(post.id, currentUser?.userId ?: "")
                                    },
                                    onUnlikeClick = {
                                        actualPostViewModel.unlikePost(post.id, currentUser?.userId ?: "")
                                    },
                                    onEditClick = if (post.userId == currentUser?.userId) {
                                        {
                                            postToEdit = post
                                            showEditDialog = true
                                        }
                                    } else null,
                                    onDeleteClick = if (post.userId == currentUser?.userId) {
                                        {
                                            actualPostViewModel.deletePost(post.id)
                                        }
                                    } else null,
                                    currentUser = currentUser,
                                    isCurrentUserPost = post.userId == currentUser?.userId
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Edit Post Dialog
        if (showEditDialog && postToEdit != null) {
            Dialog(
                onDismissRequest = {
                    showEditDialog = false
                    postToEdit = null
                }
            ) {
                EditPostActivity(
                    post = postToEdit!!,
                    onNavigateBack = {
                        showEditDialog = false
                        postToEdit = null
                        // Reload posts after edit
                        displayUser?.userId?.let { targetUserId ->
                            actualPostViewModel.loadUserPosts(targetUserId)
                        }
                    },
                    authViewModel = actualAuthViewModel,
                    postViewModel = actualPostViewModel
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: User?,
    isCurrentUser: Boolean,
    onEditProfile: () -> Unit,
    displayUser: User? = null,
    currentUser: User? = null,
    authViewModel: AuthViewModel? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            AsyncImage(
                model = user?.profileImageUrl?.ifEmpty { "https://via.placeholder.com/100" },
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username
            Text(
                text = user?.username ?: "Username",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Email
            Text(
                text = user?.email ?: "email@example.com",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Bio
            if (!user?.bio.isNullOrEmpty()) {
                Text(
                    text = user?.bio ?: "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    count = user?.postsCount ?: 0,
                    label = "Posts"
                )
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                
                StatItem(
                    count = user?.followersCount ?: 0,
                    label = "Followers"
                )
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                
                StatItem(
                    count = user?.followingCount ?: 0,
                    label = "Following"
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Action Button
            if (isCurrentUser) {
                OutlinedButton(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                var isFollowing by remember { mutableStateOf(false) }
                var isFollowLoading by remember { mutableStateOf(false) }
                
                // Check follow status when user changes
                LaunchedEffect(displayUser?.userId, currentUser?.userId) {
                    displayUser?.userId?.let { targetUserId ->
                        authViewModel?.checkIfFollowing(targetUserId) { following ->
                            isFollowing = following
                        }
                    }
                }
                
                Button(
                    onClick = {
                        displayUser?.userId?.let { targetUserId ->
                            isFollowLoading = true
                            if (isFollowing) {
                                authViewModel?.unfollowUser(targetUserId) { success ->
                                    if (success) {
                                        isFollowing = false
                                    }
                                    isFollowLoading = false
                                }
                            } else {
                                authViewModel?.followUser(targetUserId) { success ->
                                    if (success) {
                                        isFollowing = true
                                    }
                                    isFollowLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isFollowing) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isFollowLoading
                ) {
                    if (isFollowLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    count: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}