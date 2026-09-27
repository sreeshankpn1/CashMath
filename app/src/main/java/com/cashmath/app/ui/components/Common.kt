package com.cashmath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.ui.theme.*

@Composable
fun AppLockedScreen(onUnlock: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(RoyalIndigo.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(40.dp), tint = RoyalIndigo)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("CashMath is Locked", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Verify identity to access your books", style = MaterialTheme.typography.bodyMedium, color = SlateGray, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onUnlock,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo)
        ) {
            Text("Unlock App", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PremiumBalanceCard(netBalance: Double, income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Net Balance", fontSize = 14.sp, color = SlateGray)
            Text(
                "₹${"%.2f".format(netBalance)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (netBalance >= 0) EmeraldGreen else CardinalRed
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total In (+)", fontSize = 12.sp, color = EmeraldGreen)
                    Text("₹${"%.2f".format(income)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Out (-)", fontSize = 12.sp, color = CardinalRed)
                    Text("₹${"%.2f".format(expense)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PremiumPermissionCard(onRequestPermission: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF3CD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Enable Automatic Tracking", fontWeight = FontWeight.Bold, color = Color(0xFF856404))
                Text("Allow CashMath to record payments.", fontSize = 11.sp, color = Color(0xFF856404).copy(alpha = 0.7f))
            }
            Button(
                onClick = onRequestPermission, 
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF856404), contentColor = Color.White)
            ) {
                Text("Enable", fontSize = 12.sp)
            }
        }
    }
}
