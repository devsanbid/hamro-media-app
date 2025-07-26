package com.example.hamro_media.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hamro_media.model.Post
import com.example.hamro_media.model.User
import com.example.hamro_media.viewmodel.AuthViewModel
import com.example.hamro_media.viewmodel.PostViewModel

@Composable
fun MyPostsScreen(
    authViewModel: AuthViewModel? = null,
    postViewModel: PostViewModel? = null
) {
    val context = LocalContext.current
    val actualAuthViewModel = authViewModel ?: viewModel()
    val actualPostViewModel = postViewModel ?: viewModel { PostViewModel(context) }
    
    val currentUser by actualAuthViewModel.currentUser.collectAsState()
    val postState by actualPostViewModel.postState.collectAsState()
    
    // Load user posts when screen is displayed
    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { userId ->
            actualPostViewModel.loadUserPosts(userId)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Posts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = "My Posts",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            postState.isLoading && postState.userPosts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            postState.userPosts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No posts yet",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Create your first post!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                            currentUser = currentUser
                        )
                    }
                }
            }
        }
        
        postState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun PostGridItem(
    post: Post,
    onClick: () -> Unit,
    onLikeClick: (() -> Unit)? = null,
    onUnlikeClick: (() -> Unit)? = null,
    currentUser: User? = null
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
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun MyLikesScreen(
    authViewModel: AuthViewModel? = null,
    postViewModel: PostViewModel? = null
) {
    val context = LocalContext.current
    val actualAuthViewModel = authViewModel ?: viewModel()
    val actualPostViewModel = postViewModel ?: viewModel { PostViewModel(context) }
    
    val currentUser by actualAuthViewModel.currentUser.collectAsState()
    val postState by actualPostViewModel.postState.collectAsState()
    
    // Load liked posts when screen is displayed
    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { userId ->
            actualPostViewModel.loadLikedPosts(userId)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Likes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "My Likes",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            postState.isLoading && postState.likedPosts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            postState.likedPosts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No liked posts yet",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Start liking posts to see them here!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(postState.likedPosts) { post ->
                        PostGridItem(
                            post = post,
                            onClick = { /* Handle post click */ },
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
        
        postState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MyPostItem(
    postTitle: String,
    postContent: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = postTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = postContent,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}



@Composable
fun LikedPostItem(
    postTitle: String,
    postContent: String,
    authorName: String,
    onUnlike: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = postTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "by $authorName",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = postContent,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onUnlike) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Unlike",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}