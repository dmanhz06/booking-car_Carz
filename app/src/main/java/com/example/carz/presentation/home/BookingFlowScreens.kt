package com.example.carz.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.random.Random

// --- MÀN HÌNH 1: NHẬP ĐIỂM ĐẾN (Hình 1) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDestinationScreen(
    vehicleType: String,
    onDestinationConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    var destinationText by remember { mutableStateOf("") }

    val popularDestinations = listOf(
        Pair("Vinhomes Central Park", "208 Nguyễn Hữu Cảnh, P.22, Q.Bình Thạnh, Hồ Chí Minh"),
        Pair("Nhà Thờ Đức Bà", "Công Xã Paris, P.Bến Nghé, Q.1, Hồ Chí Minh"),
        Pair("Bến Xe Miền Đông Mới", "Xa Lộ Hà Nội, P.Long Bình, TP.Thủ Đức, Hồ Chí Minh"),
        Pair("Bến Xe Miền Tây", "395 Kinh Dương Vương, P.An Lạc, Q.Bình Tân, Hồ Chí Minh"),
        Pair("Aeon Mall Tân Phú", "30 Tân Thắng, P.Sơn Kỳ, Q.Tân Phú, Hồ Chí Minh")
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Bạn muốn đi đâu?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                TextButton(onClick = { onDestinationConfirmed("Vị trí tùy chọn trên bản đồ") }) {
                    Text("Chọn từ bản đồ", color = CarzBlue, fontWeight = FontWeight.Bold)
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Điểm đi cố định giả lập định vị hiện tại
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = CarzBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Huy Trần Office", fontWeight = FontWeight.Medium, color = CarzTextMain)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ô Nhập điểm đến
            OutlinedTextField(
                value = destinationText,
                onValueChange = { destinationText = it },
                placeholder = { Text("Nhập điểm đến") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFBC02D)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CarzBlue),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Điểm đến phổ biến", fontWeight = FontWeight.Black, color = CarzTextMain, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                popularDestinations.forEach { dest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDestinationConfirmed(dest.first) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = CarzTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dest.first, fontWeight = FontWeight.Bold, color = CarzTextMain, fontSize = 15.sp)
                            Text(dest.second, color = CarzTextSecondary, fontSize = 12.sp, maxLines = 1)
                        }
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.Gray)
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                }
            }
        }
    }
}

// --- MÀN HÌNH 2: XÁC NHẬN ĐIỂM ĐÓN TRÊN OPENSTREETMAP (Hình 2) ---
@Composable
fun ConfirmPickupScreen(
    destination: String,
    onPickupConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val pickupGeoPoint = remember { GeoPoint(10.8456, 106.7533) }
    var selectedAddress by remember { mutableStateOf("75/22/37/24 Đường Số 48, P.Hiệp Bình Chánh, TP.Thủ Đức") }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.5)
                    controller.setCenter(pickupGeoPoint)

                    val marker = Marker(this)
                    marker.position = pickupGeoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Điểm đón của bạn"
                    overlays.add(marker)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.White, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Chọn điểm đón", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Xác nhận hoặc thay đổi điểm đón của bạn", color = Color.Gray, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(CarzLightBlue, RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = CarzBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(selectedAddress, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Hồ Chí Minh, Việt Nam", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onPickupConfirmed(selectedAddress) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận điểm đón", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

// --- MÀN HÌNH 3: CHỌN LOẠI XE VÀ TÍNH TIỀN (Hình 3 & 4) ---
@Composable
fun BookingSummaryScreen(
    vehicleType: String,
    pickup: String,
    destination: String,
    onBookingDone: () -> Unit,
    onBack: () -> Unit
) {
    val priceBike = remember { Random.nextInt(20, 45) * 1000 }
    val priceCar = remember { Random.nextInt(65, 115) * 1000 }

    var selectedServicePrice by remember { mutableStateOf(if (vehicleType == "bike") priceBike else priceCar) }
    var selectedServiceName by remember { mutableStateOf(if (vehicleType == "bike") "carzBike" else "carzCar 4") }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(10.8231, 106.6297))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("Từ: $pickup", maxLines = 1, fontSize = 13.sp, color = Color.Blue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Đến: $destination", maxLines = 1, fontSize = 13.sp, color = Color(0xFFFF8C00), fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = CarzLightBlue, shape = RoundedCornerShape(16.dp)) {
                        Text("Đề xuất", color = CarzBlue, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                    Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp)) {
                        Text("carzBike", color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                    Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp)) {
                        Text("carzCar", color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                }

                // ĐÃ SỬA LỖI TẠI ĐÂY: verticalScroll đã được chuyển vào Modifier chuẩn cấu trúc Jetpack Compose
                Column(
                    modifier = Modifier
                        .height(180.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ServiceOptionItem("carzCar 4 chỗ", priceCar, selectedServiceName == "carzCar 4") {
                        selectedServiceName = "carzCar 4"
                        selectedServicePrice = priceCar
                    }
                    ServiceOptionItem("carzCar Plus (Rộng rãi)", priceCar + 20000, selectedServiceName == "carzCar Plus") {
                        selectedServiceName = "carzCar Plus"
                        selectedServicePrice = priceCar + 20000
                    }
                    ServiceOptionItem("carzBike (Giá siêu tốt)", priceBike, selectedServiceName == "carzBike") {
                        selectedServiceName = "carzBike"
                        selectedServicePrice = priceBike
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBookingDone,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đặt $selectedServiceName - ${String.format("%,d", selectedServicePrice)}đ", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ServiceOptionItem(name: String, price: Int, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .background(if (isSelected) CarzLightBlue else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (name.contains("Car")) Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
            contentDescription = null,
            tint = CarzBlue,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = CarzTextMain)
        Text("${String.format("%,d", price)}đ", fontWeight = FontWeight.Black, fontSize = 16.sp, color = CarzTextMain)
    }
}