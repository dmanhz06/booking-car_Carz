package com.example.carz.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActivityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "Hoạt động",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = CarzTextMain
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.LightGray.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chưa có chuyến đi nào",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CarzTextMain
                )
                Text(
                    text = "Các chuyến đi của bạn sẽ xuất hiện tại đây sau khi hoàn thành.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = CarzTextSecondary,
                    modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đặt chuyến ngay", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
