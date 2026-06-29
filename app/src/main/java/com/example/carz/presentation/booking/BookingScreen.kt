package com.example.carz.presentation.booking

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.carz.R
import com.example.carz.presentation.home.*
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.*

enum class BookingStep {
    ENTER_DESTINATION,
    CONFIRM_PICKUP,
    SELECT_RIDE
}

@Composable
fun BookingFlowScreen(
    initialService: String,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(BookingStep.ENTER_DESTINATION) }

    var currentLatLng by remember { mutableStateOf(GeoPoint(10.8231, 106.6297)) }
    var pickupAddress by remember { mutableStateOf("Đang lấy vị trí hiện tại...") }
    var destinationLocation by remember { mutableStateOf("") }
    var destinationLatLng by remember { mutableStateOf(GeoPoint(10.8142, 106.6438)) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLatLng = GeoPoint(it.latitude, it.longitude)
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            pickupAddress = addresses[0].getAddressLine(0) ?: "Vị trí của tôi"
                        }
                    }
                }
            } catch (e: SecurityException) {
                pickupAddress = "Huy Trần Office"
            }
        } else {
            pickupAddress = "Huy Trần Office"
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLatLng = GeoPoint(it.latitude, it.longitude)
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        pickupAddress = addresses[0].getAddressLine(0)
                    }
                }
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val estimatedBaseFare = remember { (20..115).random() * 1000 }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentStep) {
            BookingStep.ENTER_DESTINATION -> {
                EnterDestinationScreen(
                    pickup = pickupAddress,
                    onDestinationSelected = { destName, lat, lng ->
                        destinationLocation = destName
                        destinationLatLng = GeoPoint(lat, lng)
                        currentStep = BookingStep.CONFIRM_PICKUP
                    },
                    onBack = onBack
                )
            }
            BookingStep.CONFIRM_PICKUP -> {
                ConfirmPickupScreen(
                    pickupAddress = pickupAddress,
                    pickupLatLng = currentLatLng,
                    onConfirm = { currentStep = BookingStep.SELECT_RIDE },
                    onBack = { currentStep = BookingStep.ENTER_DESTINATION }
                )
            }
            BookingStep.SELECT_RIDE -> {
                RideSelectionScreen(
                    pickup = pickupAddress,
                    destination = destinationLocation,
                    pickupPoint = currentLatLng,
                    destPoint = destinationLatLng,
                    baseFare = estimatedBaseFare,
                    initialService = initialService,
                    onBack = { currentStep = BookingStep.CONFIRM_PICKUP },
                    onBook = onFinish
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterDestinationScreen(
    pickup: String,
    onDestinationSelected: (String, Double, Double) -> Unit,
    onBack: () -> Unit
) {
    var destInput by remember { mutableStateOf("") }

    val popularDestinations = listOf(
        Triple("Vinhomes Central Park", 10.7946, 106.7218),
        Triple("Nhà Thờ Đức Bà", 10.7798, 106.6990),
        Triple("Bến Xe Miền Đông Mới", 10.8753, 106.8007),
        Triple("Bến Xe Miền Tây", 10.7517, 106.6151),
        Triple("Aeon Mall Tân Phú", 10.8016, 106.6178),
        Triple("Thảo Cầm Viên Sài Gòn", 10.7875, 106.7053)
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Bạn muốn đi đâu?", fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("Chọn từ bản đồ", color = CarzBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, null, tint = CarzBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(pickup, color = CarzTextMain, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFFFF8C00), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = destInput,
                        onValueChange = { destInput = it },
                        placeholder = { Text("Nhập điểm đến", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Điểm đến phổ biến", modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

        LazyColumn {
            items(popularDestinations.filter { it.first.contains(destInput, ignoreCase = true) }) { place ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDestinationSelected(place.first, place.second, place.third) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(place.first, fontWeight = FontWeight.Bold, color = CarzTextMain, fontSize = 15.sp)
                        Text("Hồ Chí Minh, Việt Nam", color = Color.Gray, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.BookmarkBorder, null, tint = Color.LightGray)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun ConfirmPickupScreen(
    pickupAddress: String,
    pickupLatLng: GeoPoint,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        OSMView(center = pickupLatLng, zoom = 16.5) { mapView ->
            mapView.overlays.clear()
            val centerMarker = Marker(mapView).apply {
                position = pickupLatLng
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Điểm đón của bạn"
            }
            mapView.overlays.add(centerMarker)
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 40.dp, start = 16.dp).shadow(4.dp, CircleShape).background(Color.White, CircleShape)
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black) }

        Icon(
            Icons.Default.LocationOn,
            null,
            tint = CarzBlue,
            modifier = Modifier.size(44.dp).align(Alignment.Center).offset(y = (-22).dp)
        )

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Sửa lỗi dòng cũ ở đây (fontWeight)
                Text("Chọn điểm đón", fontWeight = FontWeight.Black, fontSize = 18.sp, color = CarzTextMain)
                Text("Xác nhận hoặc thay đổi điểm đón của bạn", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, null, tint = CarzBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(pickupAddress, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CarzTextMain, maxLines = 2)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue)
                ) {
                    Text("Xác nhận điểm đón", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RideSelectionScreen(
    pickup: String,
    destination: String,
    pickupPoint: GeoPoint,
    destPoint: GeoPoint,
    baseFare: Int,
    initialService: String,
    onBack: () -> Unit,
    onBook: () -> Unit
) {
    var selectedService by remember { mutableStateOf(if (initialService == "bike") "beBike" else "beCar 4") }

    Box(modifier = Modifier.fillMaxSize()) {
        OSMView(center = pickupPoint, zoom = 13.5) { mapView ->
            mapView.overlays.clear()

            val startMarker = Marker(mapView).apply {
                position = pickupPoint
                title = "Điểm đón"
            }
            val endMarker = Marker(mapView).apply {
                position = destPoint
                title = "Điểm đến"
            }

            val routeLine = Polyline().apply {
                setPoints(listOf(pickupPoint, destPoint))
                outlinePaint.color = android.graphics.Color.parseColor("#55B3D9")
                outlinePaint.strokeWidth = 8f
            }

            mapView.overlays.add(startMarker)
            mapView.overlays.add(endMarker)
            mapView.overlays.add(routeLine)
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(CarzBlue, CircleShape))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(pickup, fontSize = 13.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF8C00)))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(destination, fontSize = 13.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Đề xuất dịch vụ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                val bikeFare = (baseFare * 0.45).toInt()
                val car4Fare = baseFare
                val carPlusFare = (baseFare * 1.15).toInt()

                LazyColumn(modifier = Modifier.height(210.dp)) {
                    item {
                        RideOptionItem("beBike", "Giá siêu tốt, nhanh chóng", bikeFare, R.drawable.bycicle, selectedService == "beBike") {
                            selectedService = "beBike"
                        }
                    }
                    item {
                        RideOptionItem("beCar 4", "Xe 4 chỗ riêng tư", car4Fare, R.drawable.baby_car, selectedService == "beCar 4") {
                            selectedService = "beCar 4"
                        }
                    }
                    item {
                        RideOptionItem("beCar Plus", "Xe rộng rãi và thoải mái hơn", carPlusFare, R.drawable.baby_car, selectedService == "beCar Plus") {
                            selectedService = "beCar Plus"
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBook,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue)
                ) {
                    Text("Đặt $selectedService ngay", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RideOptionItem(
    name: String,
    desc: String,
    price: Int,
    icon: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CarzBlue else Color(0xFFEEEEEE),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (isSelected) CarzBlue.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(icon), null, modifier = Modifier.size(36.dp), contentScale = ContentScale.Fit)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Sửa lỗi dòng cũ ở đây (fontWeight)
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CarzTextMain)
            Text(desc, color = Color.Gray, fontSize = 11.sp)
        }
        Text(
            text = String.format("%,dđ", price).replace(',', '.'),
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = CarzTextMain
        )
    }
}

@Composable
fun OSMView(
    center: GeoPoint,
    zoom: Double = 17.0,
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(zoom)
                controller.setCenter(center)
                setMultiTouchControls(true)
                onMapReady(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}