package com.example.carz.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun AccountScreen(
    name: String,
    avatarUrl: String?,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Đăng xuất", fontWeight = FontWeight.Black) },
            text = { Text(text = "Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng Carz không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Hủy", color = CarzTextMain, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, CarzBlue, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CarzBlue.copy(alpha = 0.1f))
                    .border(1.dp, CarzBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CarzBlue,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = CarzTextMain
        )
        
        Surface(
            color = Color(0xFFFFF9C4),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "Thành viên Carz Gold",
                color = Color(0xFFFBC02D),
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AccountMenuItem("Thông tin cá nhân", Icons.Default.PersonOutline)
        AccountMenuItem("Ví điện tử CarzPay", Icons.Default.AccountBalanceWallet)
        AccountMenuItem("Thẻ của tôi", Icons.Default.CreditCard)
        AccountMenuItem("Địa chỉ đã lưu", Icons.Default.BookmarkBorder)
        AccountMenuItem("Cài đặt tài khoản", Icons.Default.Settings)
        AccountMenuItem("Trung tâm hỗ trợ", Icons.Default.SupportAgent)
        AccountMenuItem(
            title = "Đăng xuất",
            icon = Icons.AutoMirrored.Filled.Logout,
            isLast = true,
            onClick = { showLogoutDialog = true }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun AccountMenuItem(
    title: String,
    icon: ImageVector,
    isLast: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CarzTextMain,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = if (title == "Đăng xuất") Color.Red else CarzTextMain,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (!isLast) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFFF5F5F5)
                )
            }
        }
    }
}
