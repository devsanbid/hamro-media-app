package com.example.hamro_media.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hamro_media.model.Post
import com.example.hamro_media.model.User
import com.example.hamro_media.viewmodel.AuthViewModel
import com.example.hamro_media.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileActivity(
    userId: String,
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit,
    authViewModel: AuthViewModel? = null,
    postViewModel: PostViewModel? = null
) {
    val context = LocalContext.current
    val actualAuthViewModel = authViewModel ?: viewModel()
    val actualPostViewModel = postViewModel ?: viewModel { PostViewModel(context) }
    val authState by actualAuthViewModel.authState.collectAsState()
    val currentUser by actualAuthViewModel.currentUser.collectAsState()
    val postState by actualPostViewModel.postState.collectAsState()
    
    var profileUser by remember { mutableStateOf<User?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load user profile and posts
    LaunchedEffect(userId) {
        isLoading = true
        
        // Load user profile
        actualAuthViewModel.loadOtherUserProfile(userId) { user ->
            profileUser = user
            isLoading = false
        }
        
        // Load user posts
        actualPostViewModel.loadUserPosts(userId)
        
        // Check if current user is following this user
        actualAuthViewModel.checkIfFollowing(userId) { following ->
            isFollowing = following
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = profileUser?.username ?: "Profile",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Profile Header
                profileUser?.let { user ->
                    ProfileHeader(
                        user = user,
                        isCurrentUser = user.userId == currentUser?.userId,
                        isFollowing = isFollowing,
                        onFollowClick = {
                            if (isFollowing) {
                                actualAuthViewModel.unfollowUser(userId) { success ->
                                    if (success) {
                                        isFollowing = false
                                        // Update the user's follower count locally
                                        profileUser?.let { user ->
                                            profileUser = user.copy(
                                                followersCount = (user.followersCount - 1).coerceAtLeast(0)
                                            )
                                        }
                                    }
                                }
                            } else {
                                actualAuthViewModel.followUser(userId) { success ->
                                    if (success) {
                                        isFollowing = true
                                        // Update the user's follower count locally
                                        profileUser?.let { user ->
                                            profileUser = user.copy(
                                                followersCount = user.followersCount + 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Posts Grid
                val userPosts = postState.userPosts
                
                if (userPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No posts yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "When ${profileUser?.username} shares photos, they'll appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(userPosts) { post ->
                            PostGridItem(
                                post = post,
                                onClick = { onPostClick(post.id) },
                                onLikeClick = {
                                    actualPostViewModel.likePost(post.id, currentUser?.userId ?: "")
                                },
                                onUnlikeClick = {
                                    actualPostViewModel.unlikePost(post.id, currentUser?.userId ?: "")
                                },
                                currentUser = currentUser
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: User,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            ) {
                if (user.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Stats
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    count = user.postsCount,
                    label = "Posts"
                )
                ProfileStat(
                    count = user.followersCount,
                    label = "Followers"
                )
                ProfileStat(
                    count = user.followingCount,
                    label = "Following"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Username
        Text(
            text = user.username,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Bio
        if (user.bio.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Follow/Message Button
        if (!isCurrentUser) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFollowClick,
                    modifier = Modifier.weight(1f),
                    colors = if (isFollowing) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(if (isFollowing) "Following" else "Follow")
                }
                
                OutlinedButton(
                    onClick = {
                        // TODO: Implement messaging
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Message")
                }
            }
        }
    }
}

@Composable
fun ProfileStat(
    count: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PostGridItem(
    post: Post,
    onClick: () -> Unit,
    onLikeClick: (() -> Unit)? = null,
    onUnlikeClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    currentUser: User? = null,
    isCurrentUserPost: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        Box {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Edit/Delete options for current user's posts
            if (isCurrentUserPost && onEditClick != null && onDeleteClick != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                CircleShape
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .background(
                                Color.Red.copy(alpha = 0.8f),
                                CircleShape
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Like button overlay
            if (onLikeClick != null && onUnlikeClick != null && currentUser != null) {
                val isLiked = post.isLikedByCurrentUser(currentUser.userId)
                
                IconButton(
                    onClick = {
                        if (isLiked) {
                            onUnlikeClick()
                        } else {
                            onLikeClick()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "Like",
                        tint = if (isLiked) {
                            Color.Red
                        } else {
                            Color.White
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}