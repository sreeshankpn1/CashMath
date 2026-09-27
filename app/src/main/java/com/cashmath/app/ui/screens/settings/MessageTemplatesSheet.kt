package com.cashmath.app.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeepObsidian = Color(0xFF0A0E17)
private val SurfaceCard = Color(0xFF141A28)
private val CardBorder = Color(0xFF222C42)
private val NeonEmerald = Color(0xFF00E676)
private val MutedLabel = Color(0xFF8E9BB0)

private const val DEFAULT_RECEIVE_TEMPLATE = """*PAYMENT REMINDER: OUTSTANDING BALANCE*

Hi {party_name},

This is a follow-up regarding your pending balance of *₹{amount}* with *{business_name}*.

────────────────────────
📌 *Balance Due:* ₹{amount}
📅 *Status:* Due for Immediate Settlement
────────────────────────

Please arrange for the settlement today to keep your account up to date.

_If you have already transferred this amount, kindly reply with the confirmation or screenshot._

Thank you for your prompt attention.

*{business_name}*"""

private const val DEFAULT_PAY_TEMPLATE = """*PAYMENT SETTLEMENT UPDATE*

Hi {party_name},

Hope you are doing well.

According to our accounts at *{business_name}*, we have a pending payable balance of *₹{amount}* to settle with you.

────────────────────────
📌 *Amount to Pay You:* ₹{amount}
📅 *Status:* Ready for Settlement
────────────────────────

Please share your preferred UPI ID or Bank Account details so we can transfer this amount to you right away.

Thank you!

*{business_name}*"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTemplatesSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("rupeemath_settings_prefs", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = To Receive, 1 = To Pay

    var receiveTemplate by remember {
        mutableStateOf(prefs.getString("custom_template_receive", DEFAULT_RECEIVE_TEMPLATE) ?: DEFAULT_RECEIVE_TEMPLATE)
    }
    var payTemplate by remember {
        mutableStateOf(prefs.getString("custom_template_pay", DEFAULT_PAY_TEMPLATE) ?: DEFAULT_PAY_TEMPLATE)
    }

    val activeText = if (selectedTab == 0) receiveTemplate else payTemplate

    fun updateActiveText(newText: String) {
        if (selectedTab == 0) {
            receiveTemplate = newText
        } else {
            payTemplate = newText
        }
    }

    fun insertTag(tag: String) {
        updateActiveText(activeText + " $tag")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidian,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Message Templates",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Customize default WhatsApp reminder text",
                        fontSize = 13.sp,
                        color = MutedLabel
                    )
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

            // Segmented Tab Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) NeonEmerald else Color.Transparent)
                        .clickable { selectedTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text("To Receive (Collection)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.Black else Color.White)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) NeonEmerald else Color.Transparent)
                        .clickable { selectedTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text("To Pay (Settlement)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.Black else Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Tag Chips Strip
            Text("Tap tag to insert:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedLabel)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("{party_name}", "{amount}", "{business_name}").forEach { tag ->
                    AssistChip(
                        onClick = { insertTag(tag) },
                        label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E9CFF)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF192132)),
                        border = BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Template Editor Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                BasicTextField(
                    value = activeText,
                    onValueChange = { updateActiveText(it) },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(NeonEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 240.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset to Default Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        if (selectedTab == 0) {
                            receiveTemplate = DEFAULT_RECEIVE_TEMPLATE
                        } else {
                            payTemplate = DEFAULT_PAY_TEMPLATE
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MutedLabel, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset to Factory Default", fontSize = 12.sp, color = MutedLabel)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Template Button
            Button(
                onClick = {
                    prefs.edit()
                        .putString("custom_template_receive", receiveTemplate)
                        .putString("custom_template_pay", payTemplate)
                        .apply()
                    Toast.makeText(context, "Templates saved successfully! ✓", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Custom Templates", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
