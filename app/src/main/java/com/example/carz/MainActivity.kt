package com.example.carz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.carz.presentation.auth.GetStartedScreen
import com.example.carz.presentation.auth.RegistrationScreen
import com.example.carz.presentation.auth.SplashScreen
import com.example.carz.presentation.home.*
import com.example.carz.ui.theme.CarzTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("carz_osm_pref", MODE_PRIVATE))

        enableEdgeToEdge()
        setContent {
            CarzTheme {
                var currentScreen by remember { mutableStateOf("splash") }
                var userData by remember { mutableStateOf<UserData?>(null) }

                var targetVehicleType by remember { mutableStateOf("bike") }
                var selectedDestinationAddress by remember { mutableStateOf("") }
                var selectedPickupAddress by remember { mutableStateOf("") }

                when (currentScreen) {
                    "splash" -> SplashScreen(
                        onTimeout = { currentScreen = "get_started" }
                    )
                    "get_started" -> GetStartedScreen(
                        onGetStartedClick = { currentScreen = "registration" }
                    )
                    "registration" -> RegistrationScreen(
                        onLoginSuccess = { name, avatar ->
                            userData = UserData(name, avatar)
                            currentScreen = "home"
                        }
                    )
                    "home" -> HomeScreen(
                        userName = userData?.name ?: "Mạnh Duy",
                        userAvatarUrl = userData?.avatarUrl,
                        onServiceSelected = { vehicleType ->
                            targetVehicleType = vehicleType
                            currentScreen = "search_destination"
                        },
                        onLogout = {
                            userData = null
                            currentScreen = "get_started"
                        }
                    )
                    "search_destination" -> SearchDestinationScreen(
                        vehicleType = targetVehicleType,
                        onDestinationConfirmed = { destination ->
                            selectedDestinationAddress = destination
                            currentScreen = "confirm_pickup"
                        },
                        onBack = { currentScreen = "home" }
                    )
                    "confirm_pickup" -> ConfirmPickupScreen(
                        destination = selectedDestinationAddress,
                        onPickupConfirmed = { pickup ->
                            selectedPickupAddress = pickup
                            currentScreen = "booking_summary"
                        },
                        onBack = { currentScreen = "search_destination" }
                    )
                    "booking_summary" -> BookingSummaryScreen(
                        vehicleType = targetVehicleType,
                        pickup = selectedPickupAddress,
                        destination = selectedDestinationAddress,
                        onBookingDone = {
                            currentScreen = "home"
                        },
                        onBack = { currentScreen = "confirm_pickup" },
                        onEditPickup = { currentScreen = "confirm_pickup" },
                        onEditDestination = { currentScreen = "search_destination" }
                    )
                }
            }
        }
    }
}

data class UserData(val name: String, val avatarUrl: String?)