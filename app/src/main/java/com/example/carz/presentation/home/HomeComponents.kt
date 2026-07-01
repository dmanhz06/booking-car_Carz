package com.example.carz.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carz.R
import com.example.carz.data.SearchHistory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

// --- KHAI BÁO MÀU SẮC DUY NHẤT TẠI ĐÂY ---
val CarzBlue = Color(0xFF55B3D9)
val CarzDark = Color(0xFF121212)
val CarzTextMain = Color(0xFF1A1A1A)
val CarzTextSecondary = Color(0xFF424242)
val CarzLightBlue = Color(0xFFEBF7FC)
val CarzBgGray = Color(0xFFF7F9FA)

// --- DATA MODELS ---
data class CarzServiceData(val name: String, val iconRes: Int)
data class PromotionItem(val title: String, val rating: String, val time: String, val imageRes: Int)
data class TimeBasedInfo(val bgResId: Int, val greeting: String)

// --- CHức năng lấy thông điệp thời gian ---
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

// --- SHARED UI COMPONENTS ---

@Composable
fun SwipeableHistoryItem(
    item: SearchHistory,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val actionWidth = 120.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    
    // Tự động đóng sau 3.5 giây khi menu được mở hoàn toàn
    LaunchedEffect(offsetX.value) {
        if (offsetX.value <= -actionWidthPx) {
            delay(3500)
            offsetX.animateTo(0f, tween(300))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.White)
    ) {
        // Nền chứa các Action (Pin/Xóa)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(actionWidth),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(if (item.isPinned) Color(0xFFFFB74D) else Color(0xFF81C784))
                    .clickable { 
                        onPin()
                        coroutineScope.launch { offsetX.animateTo(0f) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isPinned) Icons.Default.PushPin else Icons.Default.Favorite,
                    contentDescription = "Pin",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFFE57373))
                    .clickable { 
                        onDelete()
                        coroutineScope.launch { offsetX.animateTo(0f) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Nội dung hiển thị ở trên (có thể vuốt)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .background(Color.White)
                .clickable { onClick() }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val newOffset = (offsetX.value + delta).coerceIn(-actionWidthPx * 1.5f, 0f)
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragStopped = {
                        if (offsetX.value < -actionWidthPx / 2) {
                            offsetX.animateTo(-actionWidthPx, tween(300))
                        } else {
                            offsetX.animateTo(0f, tween(300))
                        }
                    }
                )
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF5F5F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isPinned) Icons.Default.PushPin else Icons.Default.History,
                        contentDescription = null,
                        tint = if (item.isPinned) Color(0xFFFBC02D) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold, color = Color(0xFF212121), fontSize = 14.sp)
                    Text(item.address, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
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
