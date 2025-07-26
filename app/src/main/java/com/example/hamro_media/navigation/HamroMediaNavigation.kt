package com.example.hamro_media.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hamro_media.view.CreatePostActivity
import com.example.hamro_media.view.EditPostActivity
import com.example.hamro_media.view.HomeActivity
import com.example.hamro_media.view.LoginActivity
import com.example.hamro_media.view.MainLayoutActivity

import com.example.hamro_media.view.MyLikesScreen
import com.example.hamro_media.view.MyPostsScreen
import com.example.hamro_media.view.NotificationsPlaceholder
import com.example.hamro_media.view.ProfileScreen
import com.example.hamro_media.view.RegisterActivity
import com.example.hamro_media.view.SettingsPlaceholder
import com.example.hamro_media.view.SplashActivity
import com.example.hamro_media.viewmodel.AuthViewModel
import com.example.hamro_media.viewmodel.PostViewModel

@Composable
fun HamroMediaNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashActivity(
                onSplashFinished = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("login") {
            LoginActivity(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToForgotPassword = {
                    // TODO: Navigate to forgot password screen
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable("register") {
            RegisterActivity(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable("home") {
            MainLayoutActivity(
                currentRoute = "home",
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
                },
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToMyPosts = {
                    navController.navigate("my_posts")
                },

                onNavigateToMyLikes = {
                    navController.navigate("my_likes")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            ) {
                HomeActivity(
                    onNavigateToCreatePost = {
                        navController.navigate("create_post")
                    },
                    onNavigateToProfile = { userId ->
                        navController.navigate("profile")
                    }
                )
            }
        }
        
        composable("create_post") {
            CreatePostActivity(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPostCreated = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("profile") {
            MainLayoutActivity(
                currentRoute = "profile",
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
                },
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToProfile = {
                    
                },
                onNavigateToMyPosts = {
                    navController.navigate("my_posts")
                },

                onNavigateToMyLikes = {
                    navController.navigate("my_likes")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            ) {
                ProfileScreen()
            }
        }
        
        composable("settings") {
            MainLayoutActivity(
                currentRoute = "settings",
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
                },
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToMyPosts = {
                    navController.navigate("my_posts")
                },

                onNavigateToMyLikes = {
                    navController.navigate("my_likes")
                },
                onNavigateToSettings = {
                    
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            ) {
                SettingsPlaceholder()
            }
        }
        
        composable("notifications") {
            MainLayoutActivity(
                currentRoute = "notifications",
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
                },
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToMyPosts = {
                    navController.navigate("my_posts")
                },

                onNavigateToMyLikes = {
                    navController.navigate("my_likes")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToNotifications = {
                    
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("notifications") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            ) {
                NotificationsPlaceholder()
            }
        }
        
        composable("my_posts") {
            val context = LocalContext.current
            val postViewModel: PostViewModel = viewModel { PostViewModel(context) }
            
            MainLayoutActivity(
                currentRoute = "my_posts",
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
                },
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToMyPosts = {
                    
                },

                onNavigateToMyLikes = {
                    navController.navigate("my_likes")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("my_posts") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            ) {
                MyPostsScreen(
                    authViewModel = authViewModel,
                    postViewModel = postViewModel
                )
            }
        }
        

        
        composable("my_likes") {
            val context = LocalContext.current
            val postViewModel: PostViewModel = viewModel { PostViewModel(context) }
            
            MainLayoutActivity(
                currentRoute = "my_likes",
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
                },
                onNavigateToHome = {
                    navController.navigate("home")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToMyPosts = {
                    navController.navigate("my_posts")
                },

                onNavigateToMyLikes = {
                    
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("my_likes") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            ) {
                MyLikesScreen(
                    authViewModel = authViewModel,
                    postViewModel = postViewModel
                )
            }
        }
    }
}