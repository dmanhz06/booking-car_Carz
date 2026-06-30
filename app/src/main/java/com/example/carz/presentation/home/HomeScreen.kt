package com.example.carz.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
                0 -> HomeTab(name = userName, avatarUrl = userAvatarUrl, timeInfo = timeInfo, onServiceSelected = onServiceSelected)
                1 -> TabPlaceholder(title = "Hoạt động", subtitle = "Lịch sử đặt chuyến và dịch vụ")
                2 -> TabPlaceholder(title = "Dịch vụ", subtitle = "Tất cả tiện ích của Carz")
                3 -> TabPlaceholder(title = "Ưu đãi", subtitle = "Khuyến mãi dành riêng cho bạn")
                4 -> HomeAccountScreen(userName = userName, userAvatarUrl = userAvatarUrl, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun HomeTab(name: String, avatarUrl: String?, timeInfo: TimeBasedInfo, onServiceSelected: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(CarzBgGray)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box {
                Image(
                    painter = painterResource(id = timeInfo.bgResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.FillBounds
                )
                HomeHeader(name, avatarUrl, timeInfo)
            }

            Box(modifier = Modifier.offset(y = (-15).dp)) { 
                SearchSection(onClick = { onServiceSelected("bike") }) 
            }

            ServiceGrid(onServiceSelected = { serviceName ->
                when (serviceName.trim()) {
                    "Xe máy" -> onServiceSelected("bike")
                    "Ô tô" -> onServiceSelected("car")
                    else -> onServiceSelected(serviceName)
                }
            })

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
    val isDarkBackground = timeInfo.greeting.contains("tối", ignoreCase = true) || timeInfo.greeting.contains("đêm", ignoreCase = true)
    val titleColor = if (isDarkBackground) Color.White else CarzTextMain
    val subtitleColor = if (isDarkBackground) Color(0xFFE0E0E0) else CarzTextSecondary

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 36.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Chào $name!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = titleColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = timeInfo.greeting, fontSize = 13.sp, color = subtitleColor, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.width(12.dp))

        val avatarShape = RoundedCornerShape(12.dp)
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl, contentDescription = "Avatar",
                modifier = Modifier.size(52.dp).clip(avatarShape).border(1.5.dp, Color.White, avatarShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(48.dp).clip(avatarShape).background(Color.White).border(1.dp, CarzBlue, avatarShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = CarzBlue, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun SearchSection(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF8C00), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Bạn muốn đi tới đâu?", color = CarzTextMain.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.weight(1f))
                Surface(color = CarzLightBlue, shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(12.dp), tint = CarzBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đặt hộ", fontSize = 10.sp, color = CarzBlue, fontWeight = FontWeight.Black)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = Color(0xFFF5F5F5))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDestinationItem("Vinhomes Central Park")
                QuickDestinationItem("Sân bay Tân Sơn Nhất")
                QuickDestinationItem("Chợ Bến Thành")
            }
        }
    }
}

@Composable
fun QuickDestinationItem(label: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF8F8F8), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEEEEEE))) {
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
                Text(text = "Ưu đãi đặc quyền dành cho bạn ➔", fontSize = 11.sp, color = CarzTextSecondary, fontWeight = FontWeight.Bold)
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
            text = "Ưu đãi hot hôm nay", fontWeight = FontWeight.Black, fontSize = 17.sp, color = CarzTextMain,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        PromotionRow(
            title = "Food Deal Discount",
            items = listOf(
                PromotionItem("Combo Gà Rán Giòn Rúm", "4.8", "Giảm 15k", R.drawable.food_deal_discount1),
                PromotionItem("Pizza Gấp Đôi Phô Mai", "4.7", "Freeship", R.drawable.food_deal_discount2),
                PromotionItem("Cơm Tấm Sườn Bì Chả", "4.9", "Mua 1 Tặng 1", R.drawable.food_deal_discount3)
            )
        )

        PromotionRow(
            title = "Drink Deal Discount",
            items = listOf(
                PromotionItem("Trà Sữa Khoai Môn Kem Cheese", "4.9", "Ưu đãi hot", R.drawable.drink_deal_discount1),
                PromotionItem("Cà Phê Muối Đậm Vị", "4.6", "Giảm 30%", R.drawable.drink_deal_discount2),
                PromotionItem("Trà Đào Đột Phá Năng Lượng", "4.8", "Đồng giá 19k", R.drawable.drink_deal_discount3)
            )
        )

        PromotionRow(
            title = "Car Booking Discount",
            items = listOf(
                PromotionItem("Giảm ngay 20k đặt xe", "5.0", "Hôm nay", R.drawable.car_booking_discount1),
                PromotionItem("Đường xa không lo giá chát", "4.9", "Đặc quyền", R.drawable.get_start_goc),
                PromotionItem("Trải nghiệm Carz 5 sao", "4.8", "Mã: CARZNEW", R.drawable.get_start_goc)
            )
        )
    }
}

@Composable
fun PromotionRow(title: String, items: List<PromotionItem>) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CarzTextMain)
            Text(text = "Xem thêm", color = CarzBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                PromotionCard(item.title, item.rating, item.time, item.imageRes)
            }
        }
    }
}

@Composable
fun PromotionCard(title: String, rating: String, time: String, imageRes: Int) {
    Card(
        modifier = Modifier.width(160.dp), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes), contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.4f), contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = title, fontWeight = FontWeight.Black, maxLines = 1, fontSize = 12.sp, color = CarzTextMain)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = CarzBlue, modifier = Modifier.size(12.dp))
                    Text(text = " $rating • $time", fontSize = 9.sp, color = CarzTextSecondary, fontWeight = FontWeight.Bold)
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

@Composable
fun HomeAccountScreen(userName: String, userAvatarUrl: String?, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(CarzBgGray), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
            val avatarShape = RoundedCornerShape(16.dp)
            if (userAvatarUrl != null) {
                AsyncImage(model = userAvatarUrl, contentDescription = null, modifier = Modifier.size(80.dp).clip(avatarShape), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.size(80.dp).clip(avatarShape).background(Color.White).border(1.dp, CarzBlue, avatarShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = CarzBlue, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = userName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = CarzTextMain)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))) {
                Text("Đăng xuất", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}