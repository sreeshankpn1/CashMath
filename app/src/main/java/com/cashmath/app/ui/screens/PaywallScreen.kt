package com.cashmath.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.subscription.SubscriptionManager
import com.cashmath.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    onDismiss: () -> Unit,
    onSubscriptionSuccess: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        PaywallContent(onDismiss = onDismiss, onSubscriptionSuccess = onSubscriptionSuccess)
    }
}

@Composable
fun PaywallContent(
    onDismiss: () -> Unit,
    onSubscriptionSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val subManager = remember { SubscriptionManager(context) }
    var selectedPlanIsLifetime by remember { mutableStateOf(true) } // true = Lifetime, false = Annual
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Icon & Title
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Text("👑", fontSize = 32.sp)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Upgrade to RupeeMath Pro",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Text(
            text = "Unlock unlimited businesses, team collaboration & cloud backup",
            fontSize = 12.sp,
            color = SlateGray
        )

        Spacer(Modifier.height(20.dp))

        // Feature Comparison Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderHairline)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProFeatureRow("Unlimited Businesses & Books", "Free: 1 Business")
                ProFeatureRow("Unlimited Team Members & Invites", "Free: 2 Members")
                ProFeatureRow("Automatic Cloud Backup & Restore", "Google Drive / Firestore")
                ProFeatureRow("Advanced PDF & Excel Exports", "Statements & Reports")
                ProFeatureRow("Priority WhatsApp & Phone Support", "Dedicated Executive")
            }
        }

        Spacer(Modifier.height(20.dp))

        // Plan Selection Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Annual Plan Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedPlanIsLifetime = false },
                colors = CardDefaults.cardColors(
                    containerColor = if (!selectedPlanIsLifetime) BrandIndigoLight else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(if (!selectedPlanIsLifetime) 2.dp else 1.dp, if (!selectedPlanIsLifetime) RoyalIndigo else BorderHairline)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(6.dp)) {
                        Text("Annual Plan", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalIndigo)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("₹ 999", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("per year", fontSize = 11.sp, color = SlateGray)
                }
            }

            // Lifetime Pro Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedPlanIsLifetime = true },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPlanIsLifetime) BrandIndigoLight else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(if (selectedPlanIsLifetime) 2.dp else 1.dp, if (selectedPlanIsLifetime) RoyalIndigo else BorderHairline)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(6.dp)) {
                        Text("BEST VALUE 🚀", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("₹ 1,999", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("one-time lifetime", fontSize = 11.sp, color = SlateGray)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action Button
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    subManager.activateSubscription(isLifetime = selectedPlanIsLifetime)
                    isLoading = false
                    Toast.makeText(context, "Welcome to RupeeMath Pro! 👑", Toast.LENGTH_SHORT).show()
                    onSubscriptionSuccess()
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2646)),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text(
                    text = if (selectedPlanIsLifetime) "Get Lifetime Access • ₹1,999" else "Subscribe Annual • ₹999/yr",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        subManager.activateSubscription(isLifetime = true)
                        Toast.makeText(context, "Purchases Restored!", Toast.LENGTH_SHORT).show()
                        onSubscriptionSuccess()
                        onDismiss()
                    }
                }
            ) {
                Text("Restore Purchase", fontSize = 12.sp, color = RoyalIndigo, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onDismiss) {
                Text("Continue Free Plan", fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProFeatureRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 10.sp, color = SlateGray)
        }
    }
}
