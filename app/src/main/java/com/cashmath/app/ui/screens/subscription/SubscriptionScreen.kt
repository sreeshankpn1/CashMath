package com.cashmath.app.ui.screens.subscription

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.subscription.SubscriptionManager
import kotlinx.coroutines.launch

private val DeepObsidian = Color(0xFF0A0E17)
private val SurfaceCard = Color(0xFF141A28)
private val CardBorder = Color(0xFF222C42)
private val NeonEmerald = Color(0xFF00E676)
private val GoldAccent = Color(0xFFFFD700)
private val MutedLabel = Color(0xFF8E9BB0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onDismiss: () -> Unit,
    onSubscriptionSuccess: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val subManager = remember { SubscriptionManager(context) }

    var selectedPlanIsLifetime by remember { mutableStateOf(false) } // false = ₹499/yr, true = ₹1,999 Lifetime
    var showSandboxDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidian,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 22.sp)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(SurfaceCard, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Upgrade to CashMath Pro",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Unlock unlimited books, team collaboration, and cloud sync",
                fontSize = 13.sp,
                color = MutedLabel
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. COMPETITOR PRICE ANCHOR CARD (CRED STYLE)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Other Software (Vyapar / Khatabook)",
                            fontSize = 11.sp,
                            color = MutedLabel
                        )
                        Text(
                            text = "₹ 3,999 / yr",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedLabel,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(CardBorder)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "CashMath Pro",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                        Text(
                            text = "From ₹499 / yr",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonEmerald
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonEmerald.copy(alpha = 0.12f))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ Save over 85% with zero lock-in",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. INTERACTIVE PLAN SELECTOR CARDS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Plan A: Annual Pro
                val isAnnualSelected = !selectedPlanIsLifetime
                val annualInteraction = remember { MutableInteractionSource() }
                val annualPressed by annualInteraction.collectIsPressedAsState()
                val annualScale by animateFloatAsState(
                    targetValue = if (annualPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "annualScale"
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .scale(annualScale)
                        .clickable(interactionSource = annualInteraction, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedPlanIsLifetime = false
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.5.dp, if (isAnnualSelected) NeonEmerald else CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonEmerald.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "MOST POPULAR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (isAnnualSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Annual Pro", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("₹ 499 / year", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Only ₹41 / month", fontSize = 11.sp, color = MutedLabel)
                    }
                }

                // Plan B: Lifetime Founder's Pass
                val isLifetimeSelected = selectedPlanIsLifetime
                val lifetimeInteraction = remember { MutableInteractionSource() }
                val lifetimePressed by lifetimeInteraction.collectIsPressedAsState()
                val lifetimeScale by animateFloatAsState(
                    targetValue = if (lifetimePressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "lifetimeScale"
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .scale(lifetimeScale)
                        .clickable(interactionSource = lifetimeInteraction, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedPlanIsLifetime = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.5.dp, if (isLifetimeSelected) GoldAccent else CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "BEST VALUE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (isLifetimeSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Lifetime Pass", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("₹ 1,999 One-Time", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = GoldAccent)
                        Text("Pay once, free forever", fontSize = 11.sp, color = MutedLabel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. FEATURE COMPARISON LIST
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProFeatureRow("Unlimited Businesses & Books (Free limited to 1)")
                    ProFeatureRow("Unlimited Team Members & Role Permissions")
                    ProFeatureRow("Real-Time Cloud Sync & Multi-Device Access")
                    ProFeatureRow("Professional PDF & Excel Reports with Logo")
                    ProFeatureRow("1-Tap WhatsApp Payment Reminders")
                    ProFeatureRow("100% Private, On-Device DPDP Act 2023 Compliant")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. BOTTOM ACTION BUTTON
            val buttonColor = if (selectedPlanIsLifetime) GoldAccent else NeonEmerald
            val buttonText = if (selectedPlanIsLifetime) "Get Lifetime Access (₹ 1,999)" else "Activate CashMath Pro (₹ 499/yr)"

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showSandboxDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSandboxDialog) {
        AlertDialog(
            onDismissRequest = { showSandboxDialog = false },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            icon = { Text("👑", fontSize = 32.sp) },
            title = {
                Text(
                    text = "Sandbox Testing Mode",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Bank Payment Gateway Sandbox: Payment integration pending. CashMath Pro features will be activated immediately on this build!",
                    color = MutedLabel,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            subManager.activateSubscription(selectedPlanIsLifetime)
                            Toast.makeText(context, "CashMath Pro Activated Successfully! 🎉", Toast.LENGTH_SHORT).show()
                            showSandboxDialog = false
                            onSubscriptionSuccess()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Activate Pro Now", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSandboxDialog = false }) {
                    Text("Cancel", color = MutedLabel)
                }
            }
        )
    }
}

@Composable
private fun ProFeatureRow(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = NeonEmerald,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
