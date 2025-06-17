package com.example.hamro_media

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.hamro_media.navigation.HamroMediaNavigation
import com.example.hamro_media.viewmodel.AuthViewModel
import com.example.hamro_media.ui.theme.HamromediaTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Cloudinary
        val config = hashMapOf(
            "cloud_name" to "dktnwa5xl",
            "api_key" to "183898239154347",
            "api_secret" to "-Uzdd7aSn5zPjL1Uehy3CR3oc_o"
        )
        MediaManager.init(this, config)
        
        enableEdgeToEdge()
        setContent {
            HamromediaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    
                    HamroMediaNavigation(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}