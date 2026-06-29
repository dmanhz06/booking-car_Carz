package com.example.carz.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ServicesScreen(onServiceSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Dịch vụ Carz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = CarzTextMain
        )
        
        Text(
            text = "Khám phá mọi tiện ích cho cuộc sống của bạn",
            fontSize = 14.sp,
            color = CarzTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ServiceGrid(onServiceSelected = onServiceSelected)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // You could add "More Services" or categorized services here
        Text(
            text = "Dịch vụ khác",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CarzTextMain
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Placeholder for more categories
        Text(
            text = "Đang cập nhật thêm nhiều dịch vụ mới...",
            fontSize = 14.sp,
            color = CarzTextSecondary
        )
    }
}
