package com.example.carz.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carz.R

@Composable
fun OffersScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "Ưu đãi Carz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = CarzTextMain
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                OfferCard(
                    title = "Giảm 50% cho chuyến đi đầu tiên",
                    description = "Áp dụng cho khách hàng mới sử dụng dịch vụ Carz.",
                    imageRes = R.drawable.get_start_goc
                )
            }
            item {
                OfferCard(
                    title = "Hoàn tiền 20k khi đặt đồ ăn",
                    description = "Dành cho đơn hàng từ 100k trở lên qua Carz Food.",
                    imageRes = R.drawable.get_start_goc
                )
            }
            item {
                OfferCard(
                    title = "Ưu đãi cuối tuần cùng Carz Bike",
                    description = "Đồng giá 10k cho mọi chuyến đi dưới 5km.",
                    imageRes = R.drawable.get_start_goc
                )
            }
        }
    }
}

@Composable
fun OfferCard(title: String, description: String, imageRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CarzTextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = CarzTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Collect offer */ },
                    colors = ButtonDefaults.buttonColors(containerColor = CarzBlue),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Nhận ngay", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
