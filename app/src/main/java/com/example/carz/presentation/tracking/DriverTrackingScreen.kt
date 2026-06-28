package com.example.carz.presentation.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverTrackingScreen(
    driverName: String = "John Doe",
    carModel: String = "Toyota Camry",
    plateNumber: String = "ABC-1234",
    onCallClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Mock Map Placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Tracking Driver on Map...", color = Color.DarkGray)
        }

        // Driver Info Bottom Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Your driver is arriving in 5 mins",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JD", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = driverName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "$carModel • $plateNumber", fontSize = 14.sp, color = Color.Gray)
                    }

                    Row {
                        IconButton(
                            onClick = onChatClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Chat", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onCallClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
