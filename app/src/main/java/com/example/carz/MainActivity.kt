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

                // Lưu lại loại phương tiện được chọn (bike hoặc car) để truyền sang màn hình đặt xe
                var selectedVehicleType by remember { mutableStateOf("bike") }

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
                        onServiceSelected = { vehicleType ->
                            // Đã SỬA TẠI ĐÂY: Nhận loại dịch vụ từ HomeScreen truyền ra
                            if (vehicleType == "bike" || vehicleType == "car") {
                                selectedVehicleType = vehicleType
                                currentScreen = "booking" // Chuyển sang màn hình đặt xe của bạn
                            }
                        },
                        onLogout = {
                            // Xử lý khi bấm nút đăng xuất trong AccountScreen
                            userData = null
                            currentScreen = "get_started"
                        }
                    )
                    "booking" -> {
                        // Giả sử tên file/màn hình đặt xe của bạn là BookingScreen hoặc BookingFlowScreen
                        // Bạn cần import màn hình đó vào đây (Ví dụ bên dưới):

                        /*
                        BookingScreen(
                            vehicleType = selectedVehicleType, // Truyền "bike" hoặc "car" vào đây
                            onBackClick = {
                                currentScreen = "home" // Bấm nút quay lại thì về HomeScreen
                            }
                        )
                        */
                    }
                }
            }
        }
    }
}

data class UserData(val name: String, val avatarUrl: String?)