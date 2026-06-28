package com.example.carz.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carz.R
import java.util.Calendar

val CarzBlue = Color(0xFF55B3D9)
val CarzDark = Color(0xFF121212)
val CarzTextMain = Color(0xFF1A1A1A)
val CarzTextSecondary = Color(0xFF424242)
val CarzLightBlue = Color(0xFFF0F9FF)

data class CarzServiceData(val name: String, val iconRes: Int)

data class TimeBasedInfo(
    val bgResId: Int,
    val greeting: String
)

private fun getTimeBasedInfo(): TimeBasedInfo {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..10 -> TimeBasedInfo(R.drawable.bg_chao_buoi_sang, "Chào buổi sáng! Chúc bạn một ngày tốt lành.")
        in 11..12 -> TimeBasedInfo(R.drawable.bg_chao_buoi_trua, "Chào buổi trưa! Bạn đã ăn gì chưa?")
        in 13..16 -> TimeBasedInfo(R.drawable.bg_chao_buoi_chieu_sang, "Chào buổi chiều! Cùng Carz vi vu nhé.")
        in 17..17 -> TimeBasedInfo(R.drawable.bg_chao_buoi_chieu, "Buổi chiều tà thật đẹp! Bạn muốn về nhà chưa?")
        in 18..21 -> TimeBasedInfo(R.drawable.bg_chao_buoi_toi, "Chào buổi tối! Chúc bạn có một tối vui vẻ.")
        else -> TimeBasedInfo(R.drawable.bg_chao_dem_muon, "Đêm đã muộn rồi, hãy để Carz đưa bạn về an toàn nhé.")
    }
}

@Composable
fun HomeScreen(
    userName: String = "Mạnh Duy",
    userAvatarUrl: String? = null,
    onServiceSelected: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val timeInfo = remember { getTimeBasedInfo() }

    Scaffold(
        bottomBar = {
            CarzBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeTab(userName, userAvatarUrl, timeInfo, onServiceSelected)
                1 -> ActivityTab()
                2 -> ServicesTab(onServiceSelected)
                3 -> OffersTab()
                4 -> AccountTab(userName, userAvatarUrl, onLogout)
            }
        }
    }
}

@Composable
fun HomeTab(
    name: String,
    avatarUrl: String?,
    timeInfo: TimeBasedInfo,
    onServiceSelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFBFBFB))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                // Dynamic Top Background Image
                Image(
                    painter = painterResource(id = timeInfo.bgResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentScale = ContentScale.FillBounds
                )
                HomeHeader(name, avatarUrl, timeInfo.greeting)
            }

            Box(modifier = Modifier.offset(y = (-15).dp)) {
                SearchSection()
            }

            ServiceGrid(onServiceSelected)
            
            Spacer(modifier = Modifier.height(12.dp))
            CarzOneBanner()
            Spacer(modifier = Modifier.height(16.dp))
            PromotionSection()
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HomeHeader(name: String, avatarUrl: String?, greeting: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chào $name!",
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = CarzTextMain
            )
            Text(
                text = greeting,
                fontSize = 12.sp,
                color = CarzTextSecondary,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, CarzBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = CarzBlue, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
fun SearchSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF8C00), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Bạn muốn đi tới đâu?", color = CarzTextMain.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.weight(1f))
                Surface(color = CarzLightBlue, shape = RoundedCornerShape(12.dp), onClick = { }) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(12.dp), tint = CarzBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đặt hộ", fontSize = 10.sp, color = CarzBlue, fontWeight = FontWeight.Black)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = Color(0xFFF5F5F5))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDestinationItem("Vinhomes Central Park")
                QuickDestinationItem("Sân bay Tân Sơn Nhất")
                QuickDestinationItem("Chợ Bến Thành")
                QuickDestinationItem("Nhà Thờ Đức Bà")
            }
        }
    }
}

@Composable
fun QuickDestinationItem(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F8F8),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEEEEEE))
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(11.dp), tint = CarzBlue)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = label, fontSize = 11.sp, color = CarzTextMain, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun ServiceGrid(onServiceSelected: (String) -> Unit) {
    val services = listOf(
        CarzServiceData("Giao đồ ăn", R.drawable.burger),
        CarzServiceData("Xe máy", R.drawable.bycicle),
        CarzServiceData("Ô tô", R.drawable.baby_car),
        CarzServiceData("Giao hàng", R.drawable.delivery_man),
        CarzServiceData("Vé đi lại", R.drawable.take_off),
        CarzServiceData("Giúp việc", R.drawable.broom),
        CarzServiceData("Vay nhanh", R.drawable.loan),
        CarzServiceData("Tất cả", R.drawable.grid_menu)
    )
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        services.chunked(4).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                rowItems.forEach { service ->
                    ServiceItem(service.name, service.iconRes) { onServiceSelected(service.name) }
                }
            }
        }
    }
}

@Composable
fun ServiceItem(name: String, iconRes: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Surface(
            onClick = onClick, modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                Image(painter = painterResource(id = iconRes), contentDescription = name, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = name, fontSize = 10.sp, textAlign = TextAlign.Center, color = CarzTextMain, fontWeight = FontWeight.Black, lineHeight = 12.sp)
    }
}

@Composable
fun CarzOneBanner() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.background(Brush.horizontalGradient(listOf(CarzBlue.copy(0.1f), Color.White))).padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "carzOne Plus", fontWeight = FontWeight.Black, fontSize = 16.sp, color = CarzBlue)
                Text(text = "Ưu đãi đặc quyền cho bạn \u279D", fontSize = 11.sp, color = CarzTextSecondary, fontWeight = FontWeight.Bold)
            }
            Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(10.dp)) {
                Text(text = "1,250 XU", fontWeight = FontWeight.Black, color = Color(0xFFF57C00), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PromotionSection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Ưu đãi hot hôm nay", fontWeight = FontWeight.Black, fontSize = 15.sp, color = CarzTextMain)
            Text(text = "Xem thêm", color = CarzBlue, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            PromotionCard("Giảm 50k chuyến đầu", "Mới", "Còn 2 ngày", R.drawable.get_start_goc)
            Spacer(modifier = Modifier.width(10.dp))
            PromotionCard("Combo ăn sáng rẻ", "Hot", "Đang diễn ra", R.drawable.get_start_goc)
        }
    }
}

@Composable
fun PromotionCard(title: String, rating: String, time: String, imageRes: Int) {
    Card(
        modifier = Modifier.width(200.dp), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = title, fontWeight = FontWeight.Black, maxLines = 1, fontSize = 14.sp, color = CarzTextMain)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = CarzBlue, modifier = Modifier.size(14.dp))
                    Text(text = " $rating \u2022 $time", fontSize = 11.sp, color = CarzTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CarzBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp, modifier = Modifier.shadow(20.dp)) {
        val items = listOf(
            Triple("Trang chủ", Icons.Default.Home, Icons.Outlined.Home),
            Triple("Hoạt động", Icons.Default.History, Icons.Outlined.History),
            Triple("Dịch vụ", Icons.Default.GridView, Icons.Outlined.GridView),
            Triple("Ưu đãi", Icons.Default.LocalOffer, Icons.Outlined.LocalOffer),
            Triple("Tài khoản", Icons.Default.Person, Icons.Outlined.Person)
        )
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(imageVector = if (selectedTab == index) item.second else item.third, contentDescription = item.first, modifier = Modifier.size(22.dp)) },
                label = { Text(text = item.first, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp) },
                selected = selectedTab == index, onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = CarzBlue, selectedTextColor = CarzBlue, unselectedIconColor = Color(0xFF757575), unselectedTextColor = Color(0xFF757575), indicatorColor = CarzBlue.copy(0.1f))
            )
        }
    }
}

@Composable fun ActivityTab() { TabPlaceholder("Hoạt động", "Bạn chưa có hoạt động nào gần đây.") }
@Composable fun ServicesTab(onServiceSelected: (String) -> Unit) { Column(modifier = Modifier.fillMaxSize().padding(20.dp)) { Text("Dịch vụ Carz", fontSize = 22.sp, fontWeight = FontWeight.Black); Spacer(modifier = Modifier.height(16.dp)); ServiceGrid(onServiceSelected) } }
@Composable fun OffersTab() { TabPlaceholder("Ưu đãi", "Hàng trăm voucher đang chờ bạn.") }

@Composable
fun AccountTab(name: String, avatarUrl: String?, onLogout: () -> Unit) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này không?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text(text = "Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Hủy", color = CarzTextMain)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        if (avatarUrl != null) {
            AsyncImage(model = avatarUrl, contentDescription = "Avatar", modifier = Modifier.size(70.dp).clip(CircleShape).border(2.dp, CarzBlue, CircleShape), contentScale = ContentScale.Crop)
        } else {
            Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(CarzBlue), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp)) }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CarzTextMain)
        
        Spacer(modifier = Modifier.height(24.dp))
        AccountMenuItem("Cài đặt tài khoản", Icons.Default.Settings)
        AccountMenuItem("Đăng xuất", Icons.AutoMirrored.Filled.Logout, isLast = true, onClick = { showLogoutDialog = true })
    }
}

@Composable
fun AccountMenuItem(title: String, icon: ImageVector, isLast: Boolean = false, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = CarzTextMain, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 14.sp, color = CarzTextMain, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
            }
            if (!isLast) HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF5F5F5))
        }
    }
}

@Composable
fun TabPlaceholder(title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = CarzBlue.copy(0.2f), modifier = Modifier.size(60.dp))
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = CarzTextSecondary, textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
