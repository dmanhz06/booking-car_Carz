package com.example.carz.presentation.home

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.example.carz.data.SearchHistory
import com.example.carz.data.SearchHistoryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

// --- UTILITIES & LOGIC ---

/**
 * Tính giá cước (Đã giảm 30%):
 * Phí cơ bản: 8,400đ (2km đầu)
 * Phí mỗi km tiếp theo: 7,000đ
 * Phí thời gian: 3,500đ/phút
 */
fun calculateBookingFare(distanceKm: Double, durationMin: Double, multiplier: Double = 1.0): Int {
    val baseFare = 8400.0
    val perKmNext = 7000.0
    val timeFeePerMin = 3500.0
    
    val distanceFare = if (distanceKm <= 2.0) {
        baseFare
    } else {
        baseFare + (distanceKm - 2.0) * perKmNext
    }
    
    val timeFare = durationMin * timeFeePerMin
    
    return ((distanceFare + timeFare) * multiplier).toInt()
}

/**
 * Lấy lộ trình đường đi thực tế từ OSRM
 */
suspend fun fetchRealRoute(start: GeoPoint, end: GeoPoint): Triple<List<GeoPoint>, Double, Double> {
    return withContext(Dispatchers.IO) {
        try {
            val urlStr = "https://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            
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
            val dist = start.distanceToAsDouble(end) / 1000.0
            Triple(listOf(start, end), dist, dist * 2.5) 
        }
    }
}

// --- COMPONENTS ---

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
        color = if (isSelected) CarzLightBlue else Color(0xFFF9F9F9),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) CarzBlue else Color(0xFFE0E0E0)
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
                        .background(if (isSelected) CarzBlue else Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(22.dp))
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
                text = "${String.format(Locale.getDefault(), "%,d", price)}đ",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF212121)
            )
        }
    }
}

// --- SCREEN 1: NHẬP ĐIỂM ĐẾN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDestinationScreen(
    vehicleType: String,
    onDestinationConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { SearchHistoryDatabase.getDatabase(context) }
    val history by db.searchHistoryDao().getAllHistory().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    val geocoder = remember { Geocoder(context, Locale("vi", "VN")) }
    var startLocationText by remember { mutableStateOf("Vị trí của tôi") }
    var destinationText by remember { mutableStateOf("") }
    var showGPSDialog by remember { mutableStateOf(false) }
    var currentPickupCoords by remember { mutableStateOf("10.8456,106.7533") }
    
    var showDeleteConfirm by remember { mutableStateOf<SearchHistory?>(null) }

    if (showGPSDialog) {
        AlertDialog(
            onDismissRequest = { showGPSDialog = false },
            title = { Text("Định vị vị trí hiện tại", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có muốn chọn vị trí GPS hiện tại làm điểm đón không?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue, contentColor = Color.White),
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

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa địa chỉ này khỏi lịch sử?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        showDeleteConfirm?.let { db.searchHistoryDao().delete(it) }
                        showDeleteConfirm = null
                    }
                }) { Text("Xóa", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Hủy") }
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
            // Điểm đón hiện tại
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
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = CarzBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = startLocationText, color = Color(0xFF212121), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ô nhập điểm đến chính xác với icon tìm kiếm
            OutlinedTextField(
                value = destinationText,
                onValueChange = { destinationText = it },
                placeholder = { Text("Nhập địa chỉ điểm đến chính xác...") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (destinationText.isNotBlank()) {
                                try {
                                    val addresses = geocoder.getFromLocationName(destinationText, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val addr = addresses[0]
                                        val label = addr.getAddressLine(0) ?: destinationText
                                        coroutineScope.launch {
                                            db.searchHistoryDao().insert(SearchHistory(name = destinationText, address = label))
                                        }
                                        onDestinationConfirmed("$currentPickupCoords|$startLocationText|$label|${addr.latitude},${addr.longitude}")
                                    } else {
                                        Toast.makeText(context, "Không tìm thấy địa chỉ này", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Lịch sử tìm kiếm (Vuốt trái để xóa/ghim)", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (history.isEmpty()) {
                    Text("Chưa có lịch sử tìm kiếm", fontSize = 13.sp, color = Color.LightGray, modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp))
                }
                history.forEach { item ->
                    SwipeableHistoryItem(
                        item = item,
                        onPin = {
                            coroutineScope.launch {
                                db.searchHistoryDao().updatePin(item.id, !item.isPinned)
                            }
                        },
                        onDelete = { showDeleteConfirm = item },
                        onClick = {
                            try {
                                val addresses = geocoder.getFromLocationName(item.name, 1)
                                val coords = if (!addresses.isNullOrEmpty()) "${addresses[0].latitude},${addresses[0].longitude}" else "10.7798,106.6990"
                                onDestinationConfirmed("$currentPickupCoords|$startLocationText|${item.address}|$coords")
                            } catch(e: Exception) {
                                onDestinationConfirmed("$currentPickupCoords|$startLocationText|${item.address}|10.7798,106.6990")
                            }
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

// --- SCREEN 2: XÁC NHẬN ĐIỂM ĐÓN ---
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
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

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
                    controller.setZoom(18.0)
                    controller.setCenter(GeoPoint(currentLat, currentLon))
                    mapViewInstance = this

                    destCoords?.let {
                        val marker = Marker(this).apply {
                            position = it
                            title = "ĐIỂM ĐẾN"
                            snippet = extractedDestText
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        overlays.add(marker)
                        marker.showInfoWindow()
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
                                centerAddress = "Vị trí đang chọn..."
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

        // Nhãn ĐIỂM ĐÓN cố định ở giữa
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text("ĐIỂM ĐÓN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = CarzBlue, modifier = Modifier.size(40.dp).offset(y = (-4).dp))
            }
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

                // Ô nhập địa chỉ điểm đón chính xác với icon tìm kiếm
                OutlinedTextField(
                    value = textInputPickup,
                    onValueChange = { textInputPickup = it },
                    placeholder = { Text("Nhập địa chỉ điểm đón chính xác...") },
                    leadingIcon = { Icon(Icons.Default.EditLocation, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (textInputPickup.isNotBlank()) {
                                try {
                                    val addresses = geocoder.getFromLocationName(textInputPickup, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val addr = addresses[0]
                                        currentLat = addr.latitude
                                        currentLon = addr.longitude
                                        centerAddress = addr.getAddressLine(0) ?: textInputPickup
                                        mapViewInstance?.controller?.animateTo(GeoPoint(currentLat, currentLon))
                                    } else {
                                        Toast.makeText(context, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show()
                                    }
                                } catch(e: Exception) {
                                    Toast.makeText(context, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(CarzLightBlue, RoundedCornerShape(8.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = CarzBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = centerAddress, fontSize = 13.sp, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val finalLabel = if(textInputPickup.isNotBlank() && textInputPickup.length > 5) textInputPickup else centerAddress
                        val cleanPickupLabel = finalLabel.replace("|", "-")
                        val cleanDestLabel = extractedDestText.replace("|", "-")
                        val destCoordStr = if (destCoords != null) "${destCoords.latitude},${destCoords.longitude}" else "10.7798,106.6990"
                        onPickupConfirmed("$currentLat,$currentLon|$cleanPickupLabel|$cleanDestLabel|$destCoordStr")
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận điểm đón này", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SCREEN 3: TỔNG HỢP ĐẶT XE ---
@Composable
fun BookingSummaryScreen(
    vehicleType: String,
    pickup: String,
    destination: String,
    onBookingDone: () -> Unit,
    onBack: () -> Unit,
    onEditPickup: () -> Unit,
    onEditDestination: () -> Unit
) {
    var isBookingSuccessShow by remember { mutableStateOf(false) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

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
            val coordStr = if (pickupParts.size > 3) pickupParts[3] else "10.7798,106.6990"
            val coords = coordStr.split(",")
            GeoPoint(coords[0].toDouble(), coords[1].toDouble())
        } catch(e: Exception) { GeoPoint(10.7798, 106.6990) }
    }

    var routePoints by remember { mutableStateOf(listOf(startPoint, endPoint)) }
    var calculatedKm by remember { mutableDoubleStateOf(0.0) }
    var durationMin by remember { mutableDoubleStateOf(0.0) }
    var isLoadingRoute by remember { mutableStateOf(true) }

    LaunchedEffect(startPoint, endPoint) {
        isLoadingRoute = true
        val result = fetchRealRoute(startPoint, endPoint)
        routePoints = result.first
        calculatedKm = result.second
        durationMin = result.third
        isLoadingRoute = false
    }

    val priceBike = calculateBookingFare(calculatedKm, durationMin)
    val priceCar = (calculateBookingFare(calculatedKm, durationMin) * 1.8).toInt() 

    var selectedServiceName by remember { mutableStateOf(if(vehicleType == "car") "CarzCar" else "CarzBike") }
    var selectedServicePrice by remember { mutableIntStateOf(if(vehicleType == "car") priceCar else priceBike) }

    LaunchedEffect(calculatedKm, durationMin, selectedServiceName) {
        selectedServicePrice = if (selectedServiceName == "CarzBike") priceBike else priceCar
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    mapViewInstance = this

                    val markerStart = Marker(this).apply {
                        position = startPoint
                        title = "ĐIỂM ĐÓN"
                        snippet = pickupLabel
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    val markerEnd = Marker(this).apply {
                        position = endPoint
                        title = "ĐIỂM ĐẾN"
                        snippet = destLabel
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(markerStart)
                    overlays.add(markerEnd)
                    markerStart.showInfoWindow()
                    markerEnd.showInfoWindow()

                    val polyline = Polyline().apply {
                        setPoints(routePoints)
                        outlinePaint.color = "#55B3D9".toColorInt()
                        outlinePaint.strokeWidth = 14f
                    }
                    overlays.add(polyline)

                    post {
                        if (routePoints.isNotEmpty()) {
                            val bounds = BoundingBox.fromGeoPoints(routePoints)
                            zoomToBoundingBox(bounds, true, 250)
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

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.White, CircleShape)
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }

        Box(modifier = Modifier.fillMaxSize().padding(end = 16.dp), contentAlignment = Alignment.CenterEnd) {
            FloatingActionButton(
                onClick = {
                    mapViewInstance?.let { map ->
                        map.controller.animateTo(startPoint)
                        map.controller.setZoom(18.0)
                    }
                },
                containerColor = Color.White,
                contentColor = CarzBlue,
                modifier = Modifier.size(48.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center Pickup")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 100.dp, start = 14.dp, end = 14.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onEditPickup() }
                ) {
                    Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Color.Green, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Đón: $pickupLabel", fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onEditDestination() }
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Đến: $destLabel", fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                }
                if (!isLoadingRoute) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val distDisplay = if (calculatedKm < 1.0) "${(calculatedKm * 1000).toInt()} m" else "${String.format(Locale.getDefault(), "%.1f", calculatedKm)} km"
                    Text(
                        "Lộ trình: $distDisplay (~${durationMin.toInt()} phút)",
                        fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold
                    )
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
                Text("Dịch vụ Carz", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.height(130.dp).verticalScroll(rememberScrollState())) {
                    ServiceOptionItem("CarzBike (Tiết kiệm)", priceBike, selectedServiceName == "CarzBike", Icons.Default.TwoWheeler) {
                        selectedServiceName = "CarzBike"
                    }
                    ServiceOptionItem("CarzCar (Thoải mái)", priceCar, selectedServiceName == "CarzCar", Icons.Default.DirectionsCar) {
                        selectedServiceName = "CarzCar"
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { isBookingSuccessShow = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoadingRoute
                ) {
                    if (isLoadingRoute) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        val currentPrice = if(selectedServiceName == "CarzBike") priceBike else priceCar
                        Text("Đặt $selectedServiceName - ${String.format(Locale.getDefault(), "%,d", currentPrice)}đ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        AnimatedVisibility(visible = isBookingSuccessShow, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đã Gửi Yêu Cầu!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tài xế Carz đang được kết nối với bạn. Vui lòng giữ liên lạc.", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        CircularProgressIndicator(color = CarzBlue, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}
