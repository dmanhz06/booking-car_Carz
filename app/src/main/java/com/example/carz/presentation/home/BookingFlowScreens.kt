package com.example.carz.presentation.home

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.carz.R
import kotlinx.coroutines.delay
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

// --- COMPONENT 1: SERVICE OPTION ITEM ---
@Composable
fun ServiceOptionItem(
    name: String,
    price: Int,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFFFFDE7) else Color(0xFFF9F9F9),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) Color(0xFFFFD54F) else Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isSelected) Color(0xFFFFD54F) else Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.Black else Color.Gray, modifier = Modifier.size(22.dp))
                }
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
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF212121)
            )
        }
    }
}

// --- COMPONENT 2: DISCOUNT HORIZONTAL ITEM ---
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
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// --- SCREEN 1: BẠN MUỐN ĐI ĐÂU? ---
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
    var showGPSDialog by remember { mutableStateOf(false) }
    var currentPickupCoords by remember { mutableStateOf("10.8456,106.7533") }

    val popularDestinations = listOf(
        Pair("Vinhomes Central Park", "208 Nguyễn Hữu Cảnh, P.22, Q.Bình Thạnh, Hồ Chí Minh"),
        Pair("Nhà Thờ Đức Bà", "Công Xã Paris, P.Bến Nghé, Q.1, Hồ Chí Minh"),
        Pair("Bến Xe Miền Đông Mới", "Xa Lộ Hà Nội, P.Long Bình, TP.Thủ Đức, Hồ Chí Minh"),
        Pair("Bến Xe Miền Tây", "395 Kinh Dương Vương, P.An Lạc, Q.Bình Tân, Hồ Chí Minh")
    )

    if (showGPSDialog) {
        AlertDialog(
            onDismissRequest = { showGPSDialog = false },
            title = { Text("Định vị vị trí hiện tại", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có muốn chọn vị trí GPS hiện tại làm điểm đón không?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                    onClick = {
                        startLocationText = "75/22 Đường Số 48, Hiệp Bình Chánh, Thủ Đức"
                        currentPickupCoords = "10.8465,106.7541"
                        showGPSDialog = false
                    }
                ) { Text("Đồng ý") }
            },
            dismissButton = {
                TextButton(onClick = { showGPSDialog = false }) { Text("Hủy", color = Color.Gray) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Bạn muốn đi đâu?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .clickable { showGPSDialog = true }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = startLocationText, color = Color(0xFF212121), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = destinationText,
                onValueChange = { destinationText = it },
                placeholder = { Text("Nhập điểm đến...") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (destinationText.isNotBlank()) {
                                onDestinationConfirmed("$currentPickupCoords|$startLocationText|$destinationText")
                            } else {
                                Toast.makeText(context, "Vui lòng nhập điểm đến", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Điểm đến phổ biến", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                popularDestinations.forEach { dest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDestinationConfirmed("$currentPickupCoords|$startLocationText|${dest.first}") }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).background(Color(0xFFF5F5F5), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dest.first, fontWeight = FontWeight.Bold, color = Color(0xFF212121), fontSize = 14.sp)
                            Text(dest.second, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

// --- SCREEN 2: CHỌN ĐIỂM ĐÓN (ĐÃ THIẾT KẾ ĐÓNG GÓI AN TOÀN CHỐNG VĂNG APP) ---
@Composable
fun ConfirmPickupScreen(
    destination: String,
    onPickupConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var textInputPickup by remember { mutableStateOf("") }
    var centerAddress by remember { mutableStateOf("Đang xác định vị trí...") }
    var currentLat by remember { mutableDoubleStateOf(10.8456) }
    var currentLon by remember { mutableDoubleStateOf(106.7533) }
    val geocoder = remember { Geocoder(context, Locale("vi", "VN")) }

    // Phân tách an toàn tránh crash dữ liệu đầu vào
    val extractedDestText = remember(destination) {
        val parts = destination.split("|")
        if (parts.size > 2) parts[2] else if (parts.size > 1) parts[1] else destination
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(18.0)
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
                                    centerAddress = addr.getAddressLine(0) ?: "Vị trí không xác định"
                                }
                            } catch (e: Exception) {
                                centerAddress = "75 Đường số 48, Hiệp Bình Chánh, Thủ Đức"
                            }
                            return true
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean = true
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.White, CircleShape)
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp).offset(y = (-20).dp))
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Xác nhận điểm đón chính xác", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF212121))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = textInputPickup,
                    onValueChange = { textInputPickup = it },
                    placeholder = { Text("Nhập số nhà, tên ngõ...") },
                    leadingIcon = { Icon(Icons.Default.EditLocation, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF81C784))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = centerAddress, fontSize = 13.sp, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        // SỬA LỖI VĂNG APP: Làm sạch chuỗi, loại bỏ ký tự gạch đứng | phát sinh trong địa chỉ thực tế
                        val cleanPickupLabel = (if(textInputPickup.isNotBlank()) textInputPickup else centerAddress).replace("|", "-")
                        val cleanDestLabel = extractedDestText.replace("|", "-")

                        onPickupConfirmed("$currentLat,$currentLon|$cleanPickupLabel|$cleanDestLabel")
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận điểm đón này", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SCREEN 3: TỔNG HỢP ĐẶT XE (ĐÃ SỬA TOÀN BỘ LOGIC PARSE TRÁNH LỖI INDEX) ---
@Composable
fun BookingSummaryScreen(
    vehicleType: String,
    pickup: String,
    destination: String,
    onBookingDone: () -> Unit,
    onBack: () -> Unit
) {
    var isBookingSuccessShow by remember { mutableStateOf(false) }

    // GIẢI PHÁP CHỐNG CRASH: Trích xuất chuỗi có kiểm tra độ dài mảng (Bounds Checking)
    val pickupParts = pickup.split("|")
    val pickupLabel = if (pickupParts.size > 1) pickupParts[1] else "Vị trí đón của bạn"
    val destLabel = if (pickupParts.size > 2) pickupParts[2] else destination.split("|").last()

    val startPoint = remember {
        try {
            if (pickupParts.isNotEmpty()) {
                val coords = pickupParts[0].split(",")
                if (coords.size >= 2) {
                    GeoPoint(coords[0].toDouble(), coords[1].toDouble())
                } else {
                    GeoPoint(10.8456, 106.7533)
                }
            } else {
                GeoPoint(10.8456, 106.7533)
            }
        } catch(e: Exception) {
            GeoPoint(10.8456, 106.7533) // Tọa độ cứu cánh mặc định nếu có lỗi
        }
    }
    val endPoint = GeoPoint(10.7798, 106.6990)

    val calculatedKm = remember {
        try {
            val radius = 6371.0
            val dLat = Math.toRadians(endPoint.latitude - startPoint.latitude)
            val dLon = Math.toRadians(endPoint.longitude - startPoint.longitude)
            val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(startPoint.latitude)) * cos(Math.toRadians(endPoint.latitude)) * sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val dist = radius * c
            String.format("%.1f", if (dist <= 0.1) 5.2 else dist)
        } catch (e: Exception) {
            "4.5"
        }
    }

    val kmDouble = calculatedKm.toDoubleOrNull() ?: 4.5
    val priceBike = (kmDouble * 9000).toInt()
    val priceCar = (kmDouble * 18000).toInt()

    var selectedServiceName by remember { mutableStateOf("beBike") }
    var selectedServicePrice by remember { mutableStateOf(priceBike) }

    if (isBookingSuccessShow) {
        LaunchedEffect(Unit) {
            delay(4000)
            isBookingSuccessShow = false
            onBookingDone()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    val markerStart = Marker(this).apply {
                        position = startPoint
                        title = "Đón: $pickupLabel"
                    }
                    val markerEnd = Marker(this).apply {
                        position = endPoint
                        title = "Đến: $destLabel"
                    }
                    overlays.add(markerStart)
                    overlays.add(markerEnd)

                    val polyline = Polyline().apply {
                        addPoint(startPoint)
                        addPoint(endPoint)
                        outlinePaint.color = android.graphics.Color.parseColor("#FFD54F")
                        outlinePaint.strokeWidth = 10f
                    }
                    overlays.add(polyline)

                    post {
                        val bounds = BoundingBox.fromGeoPoints(listOf(startPoint, endPoint))
                        zoomToBoundingBox(bounds, true, 150)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 14.dp, end = 14.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Color.Green, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Đón: $pickupLabel", fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Đến: $destLabel", fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Chọn dịch vụ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .height(120.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ServiceOptionItem("beBike (Giá siêu tốt)", priceBike, selectedServiceName == "beBike", Icons.Default.TwoWheeler) {
                        selectedServiceName = "beBike"
                        selectedServicePrice = priceBike
                    }
                    ServiceOptionItem("beCar 4 chỗ", priceCar, selectedServiceName == "beCar 4 chỗ", Icons.Default.DirectionsCar) {
                        selectedServiceName = "beCar 4 chỗ"
                        selectedServicePrice = priceCar
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DiscountHorizontalItem(imgRes = R.drawable.car_booking_discount1, title = "Giảm 15%", modifier = Modifier.weight(1f))
                    DiscountHorizontalItem(imgRes = R.drawable.food_deal_discount1, title = "Ưu đãi Khủng", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { isBookingSuccessShow = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đặt $selectedServiceName - ${String.format("%,d", selectedServicePrice)}đ", fontWeight = FontWeight.Bold)
                }
            }
        }

        AnimatedVisibility(
            visible = isBookingSuccessShow,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.82f).padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đặt Xe Thành Công!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tài xế đang đến đón bạn. Vui lòng chuẩn bị di chuyển hành lý.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = Color(0xFFFFD54F), strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}