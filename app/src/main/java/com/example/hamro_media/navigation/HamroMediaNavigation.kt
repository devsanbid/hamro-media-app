package com.example.hamro_media.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hamro_media.view.HomeActivity
import com.example.hamro_media.view.LoginActivity
import com.example.hamro_media.view.RegisterActivity
import com.example.hamro_media.view.SplashActivity
import com.example.hamro_media.viewmodel.AuthViewModel

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
            HomeActivity(
                onNavigateToCreatePost = {
                    // TODO: Navigate to create post screen
                },
                onNavigateToProfile = { userId ->
                    // TODO: Navigate to profile screen
                },
                onNavigateToComments = { postId ->
                    // TODO: Navigate to comments screen
                }
            )
        }
    }
}