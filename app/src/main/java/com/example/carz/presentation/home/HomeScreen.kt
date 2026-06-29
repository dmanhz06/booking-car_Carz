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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carz.R
import java.util.Calendar

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
                1 -> ActivityScreen()
                2 -> ServicesScreen(onServiceSelected)
                3 -> OffersScreen()
                4 -> AccountScreen(userName, userAvatarUrl, onLogout)
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
    Box(modifier = Modifier.fillMaxSize().background(CarzBgGray)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                // Background top
                Image(
                    painter = painterResource(id = timeInfo.bgResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp), // Tăng nhẹ chiều cao để phần chữ thoáng hơn
                    contentScale = ContentScale.FillBounds
                )
                HomeHeader(name, avatarUrl, timeInfo)
            }

            Box(modifier = Modifier.offset(y = (-15).dp)) {
                SearchSection()
            }

            // Added 14.dp horizontal margin as requested
            Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                ServiceGrid(onServiceSelected)
            }

            Spacer(modifier = Modifier.height(12.dp))
            CarzOneBanner()
            Spacer(modifier = Modifier.height(20.dp))
            PromotionSection()
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HomeHeader(name: String, avatarUrl: String?, timeInfo: TimeBasedInfo) {
    // Kiểm tra xem có phải buổi tối hoặc đêm không (Ví dụ qua chuỗi greeting hoặc biến cấu hình trong timeInfo của bạn)
    val isDarkBackground = timeInfo.greeting.contains("tối", ignoreCase = true) ||
            timeInfo.greeting.contains("đêm", ignoreCase = true)

    // Xác định màu chữ động dựa trên mốc thời gian nền tối hay sáng
    val titleColor = if (isDarkBackground) Color.White else CarzTextMain
    val subtitleColor = if (isDarkBackground) Color(0xFFE0E0E0) else CarzTextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chào $name!",
                fontSize = 22.sp, // Tăng kích thước font chữ to hơn, hiện đại hơn
                fontWeight = FontWeight.Black,
                color = titleColor, // Áp dụng màu chữ động
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeInfo.greeting,
                fontSize = 13.sp, // Tăng kích thước chữ phụ
                color = subtitleColor, // Áp dụng màu chữ động
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        // Khung Avatar hình vuông bo góc nhẹ hiện đại (12.dp)
        val avatarShape = RoundedCornerShape(12.dp)

        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(52.dp)
                    .clip(avatarShape)
                    .border(1.5.dp, Color.White, avatarShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp) // Tăng nhẹ kích thước Box icon mặc định cho cân xứng với hình vuông
                    .clip(avatarShape)
                    .background(Color.White)
                    .border(1.dp, CarzBlue, avatarShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CarzBlue,
                    modifier = Modifier.size(28.dp)
                )
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
fun CarzOneBanner() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.background(Brush.horizontalGradient(listOf(CarzBlue.copy(alpha = 0.1f), Color.White))).padding(14.dp).fillMaxWidth(),
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
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = "Ưu đãi hot hôm nay",
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = CarzTextMain,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        // 1. Food Deal Discount
        PromotionRow(
            title = "Food Deal Discount",
            items = listOf(
                PromotionItem("Combo giảm giá 10%", "4.8", "Food Deal", R.drawable.food_deal_discount1),
                PromotionItem("Combo giảm giá 15%", "4.9", "Hot deal", R.drawable.food_deal_discount2),
                PromotionItem("Combo giảm giá 20%", "4.7", "Bán chạy", R.drawable.food_deal_discount3)
            )
        )

        // 2. Car Booking Discount
        PromotionRow(
            title = "Car Booking Discount",
            items = listOf(
                PromotionItem("Giảm 20k chuyến xe", "5.0", "Hôm nay", R.drawable.car_booking_discount1),
                PromotionItem("Giảm 50k chuyến xe", "Mới", "Quà tặng", R.drawable.get_start_goc),
                PromotionItem("Ưu đãi Carz mới", "4.8", "Tiết kiệm", R.drawable.get_start_goc)
            )
        )

        // 3. Drink Deal Discount
        PromotionRow(
            title = "Drink Deal Discount",
            items = listOf(
                PromotionItem("Combo giảm giá 10%", "4.5", "Giải nhiệt", R.drawable.drink_deal_discount1),
                PromotionItem("Combo giảm giá 15%", "4.6", "Mua nhiều", R.drawable.drink_deal_discount2),
                PromotionItem("Combo giảm giá 20%", "4.8", "Ưu đãi lớn", R.drawable.drink_deal_discount3)
            )
        )
    }
}

@Composable
fun PromotionRow(title: String, items: List<PromotionItem>) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CarzTextMain)
            Text(text = "Xem thêm", color = CarzBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEach { item ->
                PromotionCard(item.title, item.rating, item.time, item.imageRes)
            }
        }
    }
}

@Composable
fun PromotionCard(title: String, rating: String, time: String, imageRes: Int) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    fontSize = 12.sp,
                    color = CarzTextMain
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = CarzBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = " $rating • $time",
                        fontSize = 9.sp,
                        color = CarzTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
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
                colors = NavigationBarItemDefaults.colors(selectedIconColor = CarzBlue, selectedTextColor = CarzBlue, unselectedIconColor = Color(0xFF757575), unselectedTextColor = Color(0xFF757575), indicatorColor = CarzBlue.copy(alpha = 0.1f))
            )
        }
    }
}