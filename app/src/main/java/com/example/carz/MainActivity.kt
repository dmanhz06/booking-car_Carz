package com.example.carz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.carz.presentation.auth.GetStartedScreen
import com.example.carz.presentation.auth.RegistrationScreen
import com.example.carz.presentation.auth.SplashScreen
import com.example.carz.presentation.home.HomeScreen
import com.example.carz.ui.theme.CarzTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarzTheme {
                var currentScreen by remember { mutableStateOf("splash") }
                var userData by remember { mutableStateOf<UserData?>(null) }

                when (currentScreen) {
                    "splash" -> SplashScreen(
                        onTimeout = {
                            currentScreen = "get_started"
                        }
                    )
                    "get_started" -> GetStartedScreen(
                        onGetStartedClick = {
                            currentScreen = "registration"
                        }
                    )
                    "registration" -> RegistrationScreen(
                        onLoginSuccess = { name, avatar ->
                            userData = UserData(name, avatar)
                            currentScreen = "home"
                        }
                    )
                    "home" -> HomeScreen(
                        userName = userData?.name ?: "Khách",
                        userAvatarUrl = userData?.avatarUrl,
                        onServiceSelected = { /* Handle service selection */ }
                    )
                }
            }
        }
    }
}

data class UserData(val name: String, val avatarUrl: String?)
