package com.example.carz.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.carz.R
import com.example.carz.data.SearchHistory
import com.example.carz.data.SearchHistoryDatabase
import com.example.carz.utils.NetworkUtils
import com.example.carz.utils.RoutingService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

// --- UTILITIES & LOGIC ---

/**
 * Tạo Marker Icon từ Resource Image với kích thước tùy chỉnh (dp)
 */
fun getCustomMarkerIcon(context: Context, resId: Int, sizeDp: Int = 32): Drawable? {
    val drawable = ContextCompat.getDrawable(context, resId) ?: return null
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return BitmapDrawable(context.resources, bitmap)
}

fun calculateFare(distanceKm: Double): Int {
    val baseFare = 15000.0
    val perKmNext = 8000.0
    val total = if (distanceKm <= 2.0) baseFare else baseFare + (distanceKm - 2.0) * perKmNext
    return (Math.round(total / 1000.0) * 1000).toInt()
}

/**
 * Rút gọn địa chỉ: Lấy số nhà và tên đường.
 */
fun formatShortAddress(fullAddress: String): String {
    if (fullAddress.isBlank()) return ""
    val parts = fullAddress.split(",")
    return if (parts.isNotEmpty()) {
        val streetPart = parts[0].trim()
        // Nếu phần đầu tiên quá ngắn (ví dụ chỉ là số nhà), lấy thêm phần tiếp theo
        if (streetPart.length < 5 && parts.size > 1) {
            "$streetPart, ${parts[1].trim()}"
        } else {
            streetPart
        }
    } else {
        fullAddress
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
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (isSelected) CarzBlue else Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF212121),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${String.format(Locale.getDefault(), "%,d", price)}đ",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF212121)
            )
        }
    }
}

// --- SCREEN 1: NHẬP ĐIỂM ĐẾN ---
@SuppressLint("MissingPermission")
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
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

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
            text = { Text("Bạn có muốn lấy địa chỉ vị trí hiện tại không?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue, contentColor = Color.White),
                    onClick = {
                        coroutineScope.launch {
                            try {
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { location ->
                                        if (location != null) {
                                            currentPickupCoords = "${location.latitude},${location.longitude}"
                                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                            if (!addresses.isNullOrEmpty()) {
                                                val addr = addresses[0]
                                                val houseNumber = addr.subThoroughfare ?: ""
                                                val street = addr.thoroughfare ?: ""
                                                val district = addr.subAdminArea ?: ""
                                                val city = addr.locality ?: addr.adminArea ?: ""

                                                val components = mutableListOf<String>()
                                                if (houseNumber.isNotEmpty()) components.add(houseNumber)
                                                if (street.isNotEmpty()) components.add(street)
                                                if (district.isNotEmpty()) components.add(district)
                                                if (city.isNotEmpty()) components.add(city)

                                                startLocationText = components.joinToString(", ")
                                            }
                                        }
                                    }
                            } catch (e: SecurityException) {
                                Toast.makeText(context, "Vui lòng cấp quyền vị trí", Toast.LENGTH_SHORT).show()
                            }
                        }
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
@SuppressLint("MissingPermission")
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
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    val destParts = remember(destination) { destination.split("|") }
    val extractedDestText = if (destParts.size > 2) destParts[2] else destination
    val destCoords = remember(destParts) {
        if (destParts.size > 3) {
            val coords = destParts[3].split(",")
            if (coords.size >= 2) GeoPoint(coords[0].toDouble(), coords[1].toDouble()) else null
        } else null
    }

    var showConfirmGPSDialog by remember { mutableStateOf(false) }

    fun triggerSearch(address: String) {
        if (address.isNotBlank()) {
            try {
                val addresses = geocoder.getFromLocationName(address, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    currentLat = addr.latitude
                    currentLon = addr.longitude
                    centerAddress = addr.getAddressLine(0) ?: address
                    mapViewInstance?.controller?.animateTo(GeoPoint(currentLat, currentLon))
                } else {
                    Toast.makeText(context, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show()
                }
            } catch(e: Exception) {
                Toast.makeText(context, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showConfirmGPSDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmGPSDialog = false },
            title = { Text("Lấy vị trí hiện tại", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có muốn lấy vị trí chính xác hiện tại?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue, contentColor = Color.White),
                    onClick = {
                        coroutineScope.launch {
                            try {
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { location ->
                                        if (location != null) {
                                            currentLat = location.latitude
                                            currentLon = location.longitude
                                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                            if (!addresses.isNullOrEmpty()) {
                                                val addr = addresses[0]
                                                // Định dạng: Số đường, tên đường, tỉnh, thành phố
                                                val houseNumber = addr.subThoroughfare ?: ""
                                                val street = addr.thoroughfare ?: ""
                                                val district = addr.subAdminArea ?: ""
                                                val city = addr.locality ?: addr.adminArea ?: ""

                                                val components = mutableListOf<String>()
                                                if (houseNumber.isNotEmpty()) components.add(houseNumber)
                                                if (street.isNotEmpty()) components.add(street)
                                                if (district.isNotEmpty()) components.add(district)
                                                if (city.isNotEmpty()) components.add(city)

                                                val formatted = components.joinToString(", ")
                                                centerAddress = formatted
                                                textInputPickup = formatted

                                                mapViewInstance?.controller?.animateTo(GeoPoint(currentLat, currentLon))
                                            }
                                        }
                                    }
                            } catch (e: SecurityException) {
                                Toast.makeText(context, "Vui lòng cấp quyền vị trí", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showConfirmGPSDialog = false
                    }
                ) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmGPSDialog = false }) { Text("Hủy", color = Color.Gray) }
            }
        )
    }

    LaunchedEffect(Unit) {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLon = location.longitude
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            centerAddress = addresses[0].getAddressLine(0) ?: "Vị trí hiện tại"
                        }
                        mapViewInstance?.controller?.animateTo(GeoPoint(currentLat, currentLon))
                    }
                }
        } catch (e: SecurityException) { }
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
                            snippet = formatShortAddress(extractedDestText)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = getCustomMarkerIcon(ctx, R.drawable.diem_den, 36)
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

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text("ĐIỂM ĐÓN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.diem_don),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).offset(y = (-4).dp)
                )
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Xác nhận điểm đón chính xác", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = textInputPickup,
                    onValueChange = { textInputPickup = it },
                    placeholder = { Text("Nhập địa chỉ điểm đón chính xác...") },
                    leadingIcon = {
                        IconButton(onClick = { showConfirmGPSDialog = true }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Lấy vị trí chính xác hiện tại", tint = CarzBlue)
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = { triggerSearch(textInputPickup) }) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F8FF), RoundedCornerShape(10.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = CarzBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = centerAddress, fontSize = 14.sp, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val finalLabel = if(textInputPickup.isNotBlank() && textInputPickup.length > 5) textInputPickup else centerAddress
                        val cleanPickupLabel = finalLabel.replace("|", "-")
                        val cleanDestLabel = extractedDestText.replace("|", "-")
                        val destCoordStr = if (destCoords != null) "${destCoords.latitude},${destCoords.longitude}" else "10.7798,106.6990"
                        onPickupConfirmed("$currentLat,$currentLon|$cleanPickupLabel|$cleanDestLabel|$destCoordStr")
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Xác nhận điểm đón này", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

    var routePoints by remember { mutableStateOf<List<GeoPoint>>(listOf(startPoint, endPoint)) }
    var calculatedKm by remember { mutableDoubleStateOf(0.0) }
    var isLoadingRoute by remember { mutableStateOf(true) }

    LaunchedEffect(startPoint, endPoint) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Toast.makeText(context, "Không có kết nối mạng!", Toast.LENGTH_SHORT).show()
            isLoadingRoute = false
            return@LaunchedEffect
        }

        isLoadingRoute = true
        val routingService = RoutingService()
        val result = routingService.fetchRoute(startPoint, endPoint)

        if (result != null) {
            routePoints = result.points
            calculatedKm = result.distanceKm
        }
        isLoadingRoute = false
    }

    val finalFare = calculateFare(calculatedKm)

    val priceBike = finalFare
    val priceBikePlus = (finalFare * 1.3).toInt()
    val priceCar = (finalFare * 1.6).toInt()
    val priceCarPlus = (finalFare * 2.0).toInt()

    var selectedServiceName by remember { mutableStateOf(if(vehicleType == "car") "CarzCar" else "CarzBike") }
    var currentPrice by remember { mutableIntStateOf(if(vehicleType == "car") priceCar else priceBike) }

    LaunchedEffect(calculatedKm, selectedServiceName) {
        currentPrice = when (selectedServiceName) {
            "CarzBike" -> priceBike
            "CarzBike (Plus)" -> priceBikePlus
            "CarzCar" -> priceCar
            "CarzCar (Plus)" -> priceCarPlus
            else -> priceBike
        }
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
                    mapViewInstance = this

                    val markerStart = Marker(this).apply {
                        position = startPoint
                        title = "ĐIỂM ĐÓN"
                        snippet = formatShortAddress(pickupLabel)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = getCustomMarkerIcon(ctx, R.drawable.diem_don, 36)
                    }
                    val markerEnd = Marker(this).apply {
                        position = endPoint
                        title = "ĐIỂM ĐẾN"
                        snippet = formatShortAddress(destLabel)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = getCustomMarkerIcon(ctx, R.drawable.diem_den, 36)
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

        // Khung thông tin điểm đi/đến thu gọn lại chiều dài (width) và gọn hơn
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 90.dp) // Gộp padding nếu muốn
                .shadow(10.dp, RoundedCornerShape(20.dp))
                .animateContentSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
        ) {
            // Giảm padding tổng thể của Box từ 12.dp xuống 8.dp
            Box(modifier = Modifier.padding(8.dp)) {

                // Trang trí: Đường nối giữa 2 điểm (Giảm padding top/bottom để sát hơn)
                Column(
                    modifier = Modifier.padding(start = 10.dp, top = 26.dp, bottom = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) {
                        Box(modifier = Modifier.size(2.dp).background(Color.LightGray, CircleShape))
                        Spacer(modifier = Modifier.height(2.dp)) // Giảm khoảng cách giữa các chấm
                    }
                }

                Column {
                    // Hàng Đón - Giảm padding vertical
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onEditPickup() }
                            .padding(horizontal = 4.dp, vertical = 2.dp) // Giảm vertical padding
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.diem_don),
                            contentDescription = null,
                            modifier = Modifier.size(21.dp) // Giảm nhẹ size icon
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Đón", fontSize = 13.sp, color = Color.Gray)
                            Text(pickupLabel, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                        }
                    }

                    // Xóa Spacer(height = 1.dp) hoặc để 0.dp để sát hơn nữa

                    // Hàng Đến - Giảm padding vertical
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onEditDestination() }
                            .padding(horizontal = 4.dp, vertical = 2.dp) // Giảm vertical padding
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.diem_den),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Đến", fontSize = 13.sp, color = CarzBlue, fontWeight = FontWeight.Bold)
                            Text(
                                text = formatShortAddress(destLabel),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Phần hiển thị KM - Đưa vào sát lề hơn hoặc giảm padding cha
            if (!isLoadingRoute) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 6.dp, bottom = 6.dp), // Giảm padding bottom tổng thể
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val distDisplay = if (calculatedKm < 1.0) "${(calculatedKm * 1000).toInt()} m" else "${String.format(Locale.getDefault(), "%.1f", calculatedKm)} km"
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            distDisplay,
                            fontSize = 10.sp,
                            color = CarzBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dịch vụ Carz", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(10.dp))

                Column(modifier = Modifier.height(145.dp).verticalScroll(rememberScrollState())) {
                    ServiceOptionItem("CarzBike (Tiết kiệm)", priceBike, selectedServiceName == "CarzBike", Icons.Default.TwoWheeler) {
                        selectedServiceName = "CarzBike"
                    }
                    ServiceOptionItem("CarzBike (Plus)", priceBikePlus, selectedServiceName == "CarzBike (Plus)", Icons.Default.TwoWheeler) {
                        selectedServiceName = "CarzBike (Plus)"
                    }
                    ServiceOptionItem("CarzCar (Thoải mái)", priceCar, selectedServiceName == "CarzCar", Icons.Default.DirectionsCar) {
                        selectedServiceName = "CarzCar"
                    }
                    ServiceOptionItem("CarzCar (Plus)", priceCarPlus, selectedServiceName == "CarzCar (Plus)", Icons.Default.DirectionsCar) {
                        selectedServiceName = "CarzCar (Plus)"
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { isBookingSuccessShow = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoadingRoute
                ) {
                    if (isLoadingRoute) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Đặt $selectedServiceName - ${String.format(Locale.getDefault(), "%,d", currentPrice)}đ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        AnimatedVisibility(visible = isBookingSuccessShow, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.fillMaxWidth(0.88f).padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Đã Gửi Yêu Cầu!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Tài xế Carz đang được kết nối với bạn. Vui lòng giữ liên lạc.", fontSize = 15.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(color = CarzBlue, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}