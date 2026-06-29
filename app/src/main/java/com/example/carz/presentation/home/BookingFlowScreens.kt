package com.example.carz.presentation.home

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.carz.R
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Locale
import kotlin.math.*

// --- COMPONENT 1: LỰA CHỌN PHƯƠNG TIỆN (FIX LỖI UNRESOLVED REFERENCE) ---
@Composable
fun ServiceOptionItem(
    name: String,
    price: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF9F9F9), // CarzLightBlue
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) Color(0xFF2196F3) else Color(0xFFEEEEEE) // CarzBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (name.contains("Bike", ignoreCase = true)) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF2196F3) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )
            }
            Text(
                text = "${String.format("%,d", price)}đ",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = if (isSelected) Color(0xFF2196F3) else Color(0xFF212121)
            )
        }
    }
}

// --- COMPONENT 2: Ô ƯU ĐÃI NHỎ Ở PHẦN ĐẶT XE (FIX LỖI ĐỎ DISCOUNT_HORIZONTAL_ITEM) ---
@Composable
fun DiscountHorizontalItem(imgRes: Int, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imgRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121), maxLines = 1)
    }
}

// --- SCREEN 1: BẠN MUỐN ĐI ĐÂU ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDestinationScreen(
    vehicleType: String,
    onDestinationConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var startLocationText by remember { mutableStateOf("Huy Trần Office") }
    var destinationText by remember { mutableStateOf("") }

    val popularDestinations = listOf(
        Pair("Vinhomes Central Park", "208 Nguyễn Hữu Cảnh, P.22, Q.Bình Thạnh, Hồ Chí Minh"),
        Pair("Nhà Thờ Đức Bà", "Công Xã Paris, P.Bến Nghé, Q.1, Hồ Chí Minh"),
        Pair("Bến Xe Miền Đông Mới", "Xa Lộ Hà Nội, P.Long Bình, TP.Thủ Đức, Hồ Chí Minh"),
        Pair("Bến Xe Miền Tây", "395 Kinh Dương Vương, P.An Lạc, Q.Bình Tân, Hồ Chí Minh")
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Bạn muốn đi đâu?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF2196F3))
                Spacer(modifier = Modifier.width(12.dp))
                Text(startLocationText, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = destinationText,
                onValueChange = { destinationText = it },
                placeholder = { Text("Nhập điểm đến cụ thể") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFBC02D)) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (destinationText.isNotBlank()) {
                                onDestinationConfirmed("10.7798,106.6990|$destinationText")
                            }
                        }
                    ) { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2196F3)) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Điểm đến phổ biến", fontWeight = FontWeight.Black, color = Color(0xFF212121), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                popularDestinations.forEach { dest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDestinationConfirmed("10.7731,106.7020|${dest.first}") }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(dest.first, fontWeight = FontWeight.Bold, color = Color(0xFF212121), fontSize = 14.sp)
                            Text(dest.second, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

// --- SCREEN 2: CHỌN ĐIỂM ĐÓN (REVERSE GEOCODING KHI DI CHUYỂN BẢN ĐỒ) ---
@Composable
fun ConfirmPickupScreen(
    destination: String,
    onPickupConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var centerAddress by remember { mutableStateOf("Đang xác định vị trí...") }
    var currentLat by remember { mutableDoubleStateOf(10.8456) }
    var currentLon by remember { mutableDoubleStateOf(10.7533) }
    val geocoder = remember { Geocoder(context, Locale("vi", "VN")) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.5)
                    controller.setCenter(GeoPoint(10.8456, 106.7533))

                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val mapCenter = mapCenter as GeoPoint
                            currentLat = mapCenter.latitude
                            currentLon = mapCenter.longitude

                            try {
                                val addresses = geocoder.getFromLocation(mapCenter.latitude, mapCenter.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    val street = addr.thoroughfare ?: ""
                                    val num = addr.subThoroughfare ?: ""
                                    centerAddress = if (street.isNotEmpty()) {
                                        if (num.isNotEmpty()) "$num $street" else street
                                    } else addr.getAddressLine(0) ?: "Vị trí không tên"
                                }
                            } catch (e: Exception) {
                                centerAddress = "75/22 Đường Số 48, Thủ Đức"
                            }
                            return true
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean = true
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Ghim ở tâm màn hình
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp).offset(y = (-20).dp))
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Địa chỉ điểm đón thực tế:", fontWeight = FontWeight.Bold)
                Text(centerAddress, color = Color(0xFF2196F3), fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    onClick = { onPickupConfirmed("$currentLat,$currentLon|$centerAddress") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) { Text("Xác nhận điểm đón", color = Color.White) }
            }
        }
    }
}

// --- SCREEN 3: TỔNG HỢP CHUYẾN ĐI (FIX LỖI CÚ PHÁP VERTICAL SCROLL) ---
@Composable
fun BookingSummaryScreen(
    vehicleType: String,
    pickup: String,
    destination: String,
    onBookingDone: () -> Unit,
    onBack: () -> Unit
) {
    val pickupParts = pickup.split("|")
    val destParts = destination.split("|")
    val pickupLabel = if (pickupParts.size > 1) pickupParts[1] else pickup
    val destLabel = if (destParts.size > 1) destParts[1] else destination

    val priceBike = 35000
    val priceCar = 115000

    var selectedServiceName by remember { mutableStateOf(if (vehicleType == "bike") "carzBike" else "carzCar 4") }
    var selectedServicePrice by remember { mutableStateOf(if (vehicleType == "bike") priceBike else priceCar) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx -> MapView(ctx).apply { setTileSource(TileSourceFactory.MAPNIK) } },
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Phương thức dịch vụ chuyên nghiệp", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Sửa lỗi cú pháp verticalScroll tại đây: Đưa modifier .verticalScroll vào bên trong hàm của Modifier!
                Column(
                    modifier = Modifier
                        .height(110.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ServiceOptionItem("carzBike (Giá siêu tiết kiệm)", priceBike, selectedServiceName == "carzBike") {
                        selectedServiceName = "carzBike"
                        selectedServicePrice = priceBike
                    }
                    ServiceOptionItem("carzCar 4 chỗ sang trọng", priceCar, selectedServiceName == "carzCar 4") {
                        selectedServiceName = "carzCar 4"
                        selectedServicePrice = priceCar
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Ưu đãi áp dụng tự động:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DiscountHorizontalItem(imgRes = R.drawable.car_booking_discount1, title = "Giảm Car", modifier = Modifier.weight(1f))
                    DiscountHorizontalItem(imgRes = R.drawable.food_deal_discount1, title = "Deal Food", modifier = Modifier.weight(1f))
                    DiscountHorizontalItem(imgRes = R.drawable.drink_deal_discount1, title = "Drink Hot", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBookingDone,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) { Text("Đặt $selectedServiceName - ${String.format("%,d", selectedServicePrice)}đ", color = Color.White) }
            }
        }
    }
}