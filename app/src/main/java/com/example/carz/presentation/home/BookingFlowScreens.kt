package com.example.carz.presentation.home

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
import kotlin.math.*

// --- COMPONENT LỰA CHỌN PHƯƠNG TIỆN ---
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
        color = if (isSelected) CarzLightBlue else Color(0xFFF9F9F9),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) CarzBlue else Color(0xFFEEEEEE)
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
                    tint = if (isSelected) CarzBlue else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = CarzTextMain
                )
            }
            Text(
                text = "${String.format("%,d", price)}đ",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = if (isSelected) CarzBlue else CarzTextMain
            )
        }
    }
}

// --- MÀN HÌNH 1: NHẬP ĐIỂM ĐẾN (Đã sửa lỗi IconButton Trailing Icon) ---
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
    var showLocationDialog by remember { mutableStateOf(false) }

    val popularDestinations = listOf(
        Pair("Vinhomes Central Park", "208 Nguyễn Hữu Cảnh, P.22, Q.Bình Thạnh, Hồ Chí Minh"),
        Pair("Nhà Thờ Đức Bà", "Công Xã Paris, P.Bến Nghé, Q.1, Hồ Chí Minh"),
        Pair("Bến Xe Miền Đông Mới", "Xa Lộ Hà Nội, P.Long Bình, TP.Thủ Đức, Hồ Chí Minh"),
        Pair("Bến Xe Miền Tây", "395 Kinh Dương Vương, P.An Lạc, Q.Bình Tân, Hồ Chí Minh"),
        Pair("Aeon Mall Tân Phú", "30 Tân Thắng, P.Sơn Kỳ, Q.Tân Phú, Hồ Chí Minh")
    )

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Định vị", fontWeight = FontWeight.Bold) },
            text = { Text("Chọn vị trí hiện tại của bạn?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue),
                    onClick = {
                        startLocationText = "75/22/37/24 Đường Số 48, TP.Thủ Đức"
                        showLocationDialog = false
                        Toast.makeText(context, "Đã cập nhật vị trí hiện tại!", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Xác nhận", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("Hủy", color = Color.Gray) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Bạn muốn đi đâu?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
            },
            actions = {
                TextButton(onClick = { onDestinationConfirmed("Vị trí tùy chọn trên bản đồ") }) {
                    Text("Chọn từ bản đồ", color = CarzBlue, fontWeight = FontWeight.Bold)
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .clickable { showLocationDialog = true }
                    .padding(14.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = CarzBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Text(startLocationText, fontWeight = FontWeight.Medium, color = CarzTextMain, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
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
                                onDestinationConfirmed(destinationText)
                            } else {
                                Toast.makeText(context, "Vui lòng nhập địa chỉ cần tìm kiếm", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Tìm kiếm", tint = CarzBlue)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CarzBlue),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Điểm đến phổ biến", fontWeight = FontWeight.Black, color = CarzTextMain, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                popularDestinations.forEach { dest ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onDestinationConfirmed(dest.first) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
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

// --- MÀN HÌNH 2: XÁC NHẬN ĐIỂM ĐÓN ---
@Composable
fun ConfirmPickupScreen(
    destination: String,
    onPickupConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    var centerAddress by remember { mutableStateOf("Đang xác định vị trí...") }
    val initGeoPoint = remember { GeoPoint(10.8456, 106.7533) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.5)
                    controller.setCenter(initGeoPoint)

                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val mapCenter = mapCenter as GeoPoint
                            centerAddress = "Đường số 48, Tọa độ: (${String.format("%.4f", mapCenter.latitude)}, ${String.format("%.4f", mapCenter.longitude)})"
                            return true
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean = true
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-18).dp)) {
                Box(modifier = Modifier.background(Color.Black, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Đón tại đây", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(40.dp))
            }
        }

        IconButton(onClick = onBack, modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.White, CircleShape)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Chọn điểm đón", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Di chuyển bản đồ để căn chỉnh tâm đón chính xác", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(CarzLightBlue, RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = CarzBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(centerAddress, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        Text("Hồ Chí Minh, Việt Nam", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onPickupConfirmed(centerAddress) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Xác nhận điểm đón", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }
            }
        }
    }
}

// --- MÀN HÌNH 3: TỔNG HỢP ĐẶT CHUYẾN ---
@Composable
fun BookingSummaryScreen(
    vehicleType: String,
    pickup: String,
    destination: String,
    onBookingDone: () -> Unit,
    onBack: () -> Unit
) {
    val startPoint = remember { GeoPoint(10.8456, 106.7533) }
    val endPoint = remember { GeoPoint(10.7798, 106.6990) }

    val calculatedKm = remember {
        val radius = 6371.0
        val dLat = Math.toRadians(endPoint.latitude - startPoint.latitude)
        val dLon = Math.toRadians(endPoint.longitude - startPoint.longitude)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(startPoint.latitude)) * cos(Math.toRadians(endPoint.latitude)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val rawDistance = radius * c
        String.format("%.1f", if (rawDistance < 1.0) 4.2 else rawDistance)
    }

    val priceBike = remember { (calculatedKm.toDouble() * 11000).toInt() }
    val priceCar = remember { (calculatedKm.toDouble() * 22000).toInt() }

    var selectedServicePrice by remember { mutableStateOf(if (vehicleType == "bike") priceBike else priceCar) }
    var selectedServiceName by remember { mutableStateOf(if (vehicleType == "bike") "carzBike" else "carzCar 4") }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    val startMarker = Marker(this).apply {
                        position = startPoint
                        title = "Đón: $pickup"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(startMarker)

                    val endMarker = Marker(this).apply {
                        position = endPoint
                        title = "Đến: $destination"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(endMarker)

                    val line = Polyline().apply {
                        addPoint(startPoint)
                        addPoint(GeoPoint((startPoint.latitude + endPoint.latitude)/2 + 0.003, (startPoint.longitude + endPoint.longitude)/2 - 0.003))
                        addPoint(endPoint)
                        outlinePaint.color = android.graphics.Color.parseColor("#55B3D9")
                        outlinePaint.strokeWidth = 9f
                    }
                    overlays.add(line)

                    post {
                        val box = BoundingBox.fromGeoPoints(listOf(startPoint, endPoint))
                        zoomToBoundingBox(box, true, 150)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("Khoảng cách: $calculatedKm km", color = Color(0xFFF57C00), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Từ: $pickup", maxLines = 1, fontSize = 12.sp, color = Color.Blue)
                    Text("Đến: $destination", maxLines = 1, fontSize = 12.sp, color = Color(0xFF388E3C))
                }
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = CarzLightBlue, shape = RoundedCornerShape(16.dp)) {
                        Text("Đề xuất", color = CarzBlue, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                    }
                }

                Column(
                    modifier = Modifier
                        .height(110.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ServiceOptionItem("carzBike (Giá siêu tốt)", priceBike, selectedServiceName == "carzBike") {
                        selectedServiceName = "carzBike"
                        selectedServicePrice = priceBike
                    }
                    ServiceOptionItem("carzCar 4 chỗ", priceCar, selectedServiceName == "carzCar 4") {
                        selectedServiceName = "carzCar 4"
                        selectedServicePrice = priceCar
                    }
                }

                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

                Text("Ưu đãi Hot cho chuyến đi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CarzTextMain)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiscountHorizontalItem(imgRes = R.drawable.car_booking_discount1, title = "Giảm Carz", modifier = Modifier.weight(1f))
                    DiscountHorizontalItem(imgRes = R.drawable.food_deal_discount1, title = "Deal Food", modifier = Modifier.weight(1f))
                    DiscountHorizontalItem(imgRes = R.drawable.drink_deal_discount1, title = "Drink Hot", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBookingDone, modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue), shape = RoundedCornerShape(12.dp)
                ) { Text("Đặt $selectedServiceName - ${String.format("%,d", selectedServicePrice)}đ", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White) }
            }
        }
    }
}

@Composable
fun DiscountHorizontalItem(imgRes: Int, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = imgRes), contentDescription = title, modifier = Modifier.size(30.dp).clip(RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = CarzTextMain, maxLines = 1)
    }
}