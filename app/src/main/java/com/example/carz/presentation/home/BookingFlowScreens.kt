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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.carz.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.*

// --- UTILITIES & LOGIC ---

/**
 * Calculates fare based on the specific formula:
 * Phí cơ bản: 12,000đ (cho 2km đầu)
 * Phí mỗi km tiếp theo: 10,000đ
 * Phí thời gian: 5,000đ/phút
 * Multiplier: Hệ số nhân (mặc định 1.0)
 */
fun calculateBookingFare(distanceKm: Double, durationMin: Double, multiplier: Double = 1.0): Int {
    val baseFare = 12000.0
    val perKmNext = 10000.0
    val timeFeePerMin = 5000.0
    
    val distanceFare = if (distanceKm <= 2.0) {
        baseFare
    } else {
        baseFare + (distanceKm - 2.0) * perKmNext
    }
    
    val timeFare = durationMin * timeFeePerMin
    
    return ((distanceFare + timeFare) * multiplier).toInt()
}

/**
 * Fetches real road route from OSRM API
 */
suspend fun fetchRealRoute(start: GeoPoint, end: GeoPoint): Triple<List<GeoPoint>, Double, Double> {
    return withContext(Dispatchers.IO) {
        try {
            // lon,lat format for OSRM
            val urlStr = "https://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val routes = json.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val distanceKm = route.getDouble("distance") / 1000.0
                val durationMin = route.getDouble("duration") / 60.0
                
                val geometry = route.getJSONObject("geometry")
                val coords = geometry.getJSONArray("coordinates")
                val points = mutableListOf<GeoPoint>()
                for (i in 0 until coords.length()) {
                    val point = coords.getJSONArray(i)
                    points.add(GeoPoint(point.getDouble(1), point.getDouble(0)))
                }
                Triple(points, distanceKm, durationMin)
            } else {
                throw Exception("No routes found")
            }
        } catch (e: Exception) {
            // Fallback: Haversine distance and estimated speed
            val dist = start.distanceToAsDouble(end) / 1000.0
            Triple(listOf(start, end), dist, dist * 2.5) // Assume 24km/h for city fallback
        }
    }
}

private fun getPopularDestCoords(name: String): String {
    return when (name) {
        "Vinhomes Central Park" -> "10.7950,106.7218"
        "Nhà Thờ Đức Bà" -> "10.7798,106.6990"
        "Bến Xe Miền Đông Mới" -> "10.8825,106.8122"
        "Bến Xe Miền Tây" -> "10.7516,106.6190"
        else -> "10.7769,106.7009"
    }
}

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
    val geocoder = remember { Geocoder(context, Locale("vi", "VN")) }
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
                                try {
                                    val addresses = geocoder.getFromLocationName(destinationText, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val lat = addresses[0].latitude
                                        val lon = addresses[0].longitude
                                        // Pass coordinates along with labels
                                        onDestinationConfirmed("$currentPickupCoords|$startLocationText|$destinationText|$lat,$lon")
                                    } else {
                                        Toast.makeText(context, "Không tìm thấy địa chỉ này", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
                                }
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
                            .clickable { 
                                val coords = getPopularDestCoords(dest.first)
                                onDestinationConfirmed("$currentPickupCoords|$startLocationText|${dest.first}|$coords") 
                            }
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

// --- SCREEN 2: CHỌN ĐIỂM ĐÓN ---
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

    val destParts = remember(destination) { destination.split("|") }
    val extractedDestText = if (destParts.size > 2) destParts[2] else destination
    val destCoords = remember(destParts) {
        if (destParts.size > 3) {
            val coords = destParts[3].split(",")
            if (coords.size >= 2) GeoPoint(coords[0].toDouble(), coords[1].toDouble()) else null
        } else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    controller.setCenter(GeoPoint(10.8456, 106.7533))

                    // Hiển thị marker điểm đến đã chọn (trỏ đúng vào địa điểm đó)
                    destCoords?.let {
                        val marker = Marker(this).apply {
                            position = it
                            title = "Đến: $extractedDestText"
                        }
                        overlays.add(marker)
                    }

                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val mapCenter = mapCenter as GeoPoint
                            currentLat = mapCenter.latitude
                            currentLon = mapCenter.longitude

                            try {
                                val addresses = geocoder.getFromLocation(mapCenter.latitude, mapCenter.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    centerAddress = addresses[0].getAddressLine(0) ?: "Vị trí không xác định"
                                }
                            } catch (e: Exception) {
                                centerAddress = "Đang tìm vị trí..."
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

        // Marker điểm đón ở giữa màn hình
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(40.dp).offset(y = (-20).dp))
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Xác nhận điểm đón chính xác", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = centerAddress, fontSize = 13.sp, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val cleanPickupLabel = (if(textInputPickup.isNotBlank()) textInputPickup else centerAddress).replace("|", "-")
                        val cleanDestLabel = extractedDestText.replace("|", "-")
                        // Forward full info
                        val destCoordStr = if (destCoords != null) "${destCoords.latitude},${destCoords.longitude}" else "10.7798,106.6990"
                        onPickupConfirmed("$currentLat,$currentLon|$cleanPickupLabel|$cleanDestLabel|$destCoordStr")
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

// --- SCREEN 3: TỔNG HỢP ĐẶT XE (VỚI TÍNH TOÁN THỰC TẾ) ---
@Composable
fun BookingSummaryScreen(
    vehicleType: String,
    pickup: String,
    destination: String,
    onBookingDone: () -> Unit,
    onBack: () -> Unit
) {
    var isBookingSuccessShow by remember { mutableStateOf(false) }
    var multiplierText by remember { mutableStateOf("1.0") }
    val multiplier = multiplierText.toDoubleOrNull() ?: 1.0

    val pickupParts = pickup.split("|")
    val pickupLabel = if (pickupParts.size > 1) pickupParts[1] else "Vị trí đón"
    val destLabel = if (pickupParts.size > 2) pickupParts[2] else "Điểm đến"

    val startPoint = remember {
        try {
            val coords = pickupParts[0].split(",")
            GeoPoint(coords[0].toDouble(), coords[1].toDouble())
        } catch(e: Exception) { GeoPoint(10.8456, 106.7533) }
    }
    
    val endPoint = remember {
        try {
            // Check if destination string or pickupParts[3] contains coords
            val coordStr = if (pickupParts.size > 3) pickupParts[3] else "10.7798,106.6990"
            val coords = coordStr.split(",")
            GeoPoint(coords[0].toDouble(), coords[1].toDouble())
        } catch(e: Exception) { GeoPoint(10.7798, 106.6990) }
    }

    var routePoints by remember { mutableStateOf<List<GeoPoint>>(listOf(startPoint, endPoint)) }
    var calculatedKm by remember { mutableDoubleStateOf(0.0) }
    var durationMin by remember { mutableDoubleStateOf(0.0) }
    var isLoadingRoute by remember { mutableStateOf(true) }

    // Fetch real road route and distance
    LaunchedEffect(startPoint, endPoint) {
        isLoadingRoute = true
        val result = fetchRealRoute(startPoint, endPoint)
        routePoints = result.first
        calculatedKm = result.second
        durationMin = result.third
        isLoadingRoute = false
    }

    // Fare calculation
    val priceBike = calculateBookingFare(calculatedKm, durationMin, multiplier)
    val priceCar = (calculateBookingFare(calculatedKm, durationMin, multiplier) * 1.6).toInt() // Car is slightly more expensive

    var selectedServiceName by remember { mutableStateOf(if(vehicleType == "car") "beCar 4 chỗ" else "beBike") }
    var selectedServicePrice by remember { mutableIntStateOf(if(vehicleType == "car") priceCar else priceBike) }

    LaunchedEffect(calculatedKm, durationMin, multiplier, selectedServiceName) {
        selectedServicePrice = if (selectedServiceName == "beBike") priceBike else priceCar
    }

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
                        setPoints(routePoints)
                        outlinePaint.color = android.graphics.Color.parseColor("#FFD54F")
                        outlinePaint.strokeWidth = 12f
                    }
                    overlays.add(polyline)

                    post {
                        if (routePoints.isNotEmpty()) {
                            val bounds = BoundingBox.fromGeoPoints(routePoints)
                            zoomToBoundingBox(bounds, true, 200)
                        }
                    }
                }
            },
            update = { map ->
                map.overlays.filterIsInstance<Polyline>().forEach { it.setPoints(routePoints) }
                map.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top info card
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
                    Text(pickupLabel, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(destLabel, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                }
                if (!isLoadingRoute) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Khoảng cách: ${String.format("%.2f", calculatedKm)}km - Ước tính: ${durationMin.toInt()} phút",
                        fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom selection card
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Chọn dịch vụ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Multiplier: ", fontSize = 12.sp, color = Color.Gray)
                        TextField(
                            value = multiplierText,
                            onValueChange = { multiplierText = it },
                            modifier = Modifier.width(60.dp).height(48.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.height(120.dp).verticalScroll(rememberScrollState())) {
                    ServiceOptionItem("beBike", priceBike, selectedServiceName == "beBike", Icons.Default.TwoWheeler) {
                        selectedServiceName = "beBike"
                    }
                    ServiceOptionItem("beCar 4 chỗ", priceCar, selectedServiceName == "beCar 4 chỗ", Icons.Default.DirectionsCar) {
                        selectedServiceName = "beCar 4 chỗ"
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoadingRoute
                ) {
                    if (isLoadingRoute) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text("Đặt $selectedServiceName - ${String.format("%,d", selectedServicePrice)}đ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AnimatedVisibility(visible = isBookingSuccessShow, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.fillMaxWidth(0.82f).padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đặt Xe Thành Công!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tài xế đang đến đón bạn.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = Color(0xFFFFD54F), strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
