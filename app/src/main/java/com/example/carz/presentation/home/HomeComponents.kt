package com.example.carz.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carz.R
import java.util.Calendar

// --- Global Theme Constants ---
val CarzBlue = Color(0xFF55B3D9)
val CarzDark = Color(0xFF121212)
val CarzTextMain = Color(0xFF1A1A1A)
val CarzTextSecondary = Color(0xFF424242)
val CarzLightBlue = Color(0xFFF0F9FF)
val CarzBgGray = Color(0xFFFBFBFB)

// --- Data Models ---
data class CarzServiceData(val name: String, val iconRes: Int)
data class PromotionItem(val title: String, val rating: String, val time: String, val imageRes: Int)
data class TimeBasedInfo(val bgResId: Int, val greeting: String)

// --- Shared Functions ---
fun getTimeBasedInfo(): TimeBasedInfo {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..10 -> TimeBasedInfo(R.drawable.bg_chao_buoi_sang, "Chào buổi sáng! Chúc bạn ngày mới tốt lành.")
        in 11..12 -> TimeBasedInfo(R.drawable.bg_chao_buoi_trua, "Chào buổi trưa! Bạn đã ăn gì chưa?")
        in 13..16 -> TimeBasedInfo(R.drawable.bg_chao_buoi_chieu_sang, "Chào buổi chiều! Cùng Carz vi vu nhé.")
        in 17..17 -> TimeBasedInfo(R.drawable.bg_chao_buoi_chieu, "Chiều tà rồi! Bạn muốn đi đâu không?")
        in 18..21 -> TimeBasedInfo(R.drawable.bg_chao_buoi_toi, "Chào buổi tối! Chúc bạn có một tối vui vẻ.")
        else -> TimeBasedInfo(R.drawable.bg_chao_dem_muon, "Đêm muộn rồi, để Carz đưa bạn về an toàn nhé.")
    }
}

// --- Shared UI Components ---
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
    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
        services.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { service ->
                    ServiceItem(service.name, service.iconRes) { onServiceSelected(service.name) }
                }
            }
        }
    }
}

@Composable
fun ServiceItem(name: String, iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(11.dp)) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            color = CarzTextMain,
            fontWeight = FontWeight.Black,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun TabPlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = CarzTextMain
            )
            Text(
                text = subtitle,
                color = CarzTextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
