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

        // Khởi tạo cấu hình bộ nhớ đệm cho OpenStreetMap (OSMDroid) hoạt động chính xác
        Configuration.getInstance().load(applicationContext, getSharedPreferences("carz_osm_pref", MODE_PRIVATE))

        enableEdgeToEdge()
        setContent {
            CarzTheme {
                var currentScreen by remember { mutableStateOf("splash") }
                var userData by remember { mutableStateOf<UserData?>(null) }

                // Các biến lưu trữ dữ liệu luồng hành trình đặt xe thực tế
                var targetVehicleType by remember { mutableStateOf("bike") } // "bike" hoặc "car"
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
                            // Giải quyết lỗi màn hình trắng: Nhận diện loại xe và đưa vào luồng Hình 1
                            if (vehicleType == "bike" || vehicleType == "car") {
                                targetVehicleType = vehicleType
                                currentScreen = "search_destination"
                            }
                        },
                        onLogout = {
                            userData = null
                            currentScreen = "get_started"
                        }
                    )
                    // HÌNH 1: Màn hình nhập điểm đến
                    "search_destination" -> SearchDestinationScreen(
                        vehicleType = targetVehicleType,
                        onDestinationConfirmed = { destination ->
                            selectedDestinationAddress = destination
                            currentScreen = "confirm_pickup" // Chuyển tiếp sang màn Hình 2
                        },
                        onBack = { currentScreen = "home" }
                    )
                    // HÌNH 2: Màn hình xác nhận điểm đón tích hợp Map định vị thực tế
                    "confirm_pickup" -> ConfirmPickupScreen(
                        destination = selectedDestinationAddress,
                        onPickupConfirmed = { pickup ->
                            selectedPickupAddress = pickup
                            currentScreen = "booking_summary" // Chuyển tiếp sang màn Hình 3
                        },
                        onBack = { currentScreen = "search_destination" }
                    )
                    // HÌNH 3 & 4: Bản đồ lộ trình, tính toán khoảng giá tiền từ 20k - 115k và xác nhận đặt xe
                    "booking_summary" -> BookingSummaryScreen(
                        vehicleType = targetVehicleType,
                        pickup = selectedPickupAddress,
                        destination = selectedDestinationAddress,
                        onBookingDone = {
                            // Sau khi hoàn thành quy trình đặt xe, điều hướng an toàn về màn hình chính
                            currentScreen = "home"
                        },
                        onBack = { currentScreen = "confirm_pickup" }
                    )
                }
            }
        }
    }
}

data class UserData(val name: String, val avatarUrl: String?)