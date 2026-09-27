package com.cashmath.app.ui.screens.dues.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.ui.theme.LocalThemeIsDark
import java.util.Locale
import kotlin.math.abs

@Composable
fun PartyCard(
    party: DuePartyEntity,
    onWhatsAppClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // --- TOP ROW: Avatar, Name, and 3-Dot Menu ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials Circle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFEEF2FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = party.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF4338CA)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = party.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (!party.phone.isNullOrBlank()) party.phone else "Tap to view khata history",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount & Status
                val isDark = LocalThemeIsDark.current
                val isGet = party.toReceive > 0
                val amountVal = if (isGet) party.toReceive else party.toPay
                val partyGreen = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                val partyRed = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
                val amountColor = if (isGet) partyGreen else partyRed

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isGet) "You'll Get" else "You'll Pay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = amountColor
                    )
                    Text(
                        text = "₹ ${String.format(Locale.getDefault(), "%,.2f", abs(amountVal))}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor
                    )
                }

                // Clean 3-Dot Menu for Edit / Delete
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Party") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Party", color = Color(0xFFDC2626)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // --- BOTTOM ROW: Refined WhatsApp Reminder Action ---
            val isDark = LocalThemeIsDark.current
            val whatsappGreen = Color(0xFF25D366)
            val neutralBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
            val neutralBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
            val btnTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onWhatsAppClick,
                    shape = RoundedCornerShape(20.dp),
                    color = neutralBg,
                    border = BorderStroke(1.dp, neutralBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Remind",
                            tint = whatsappGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp Remind",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = btnTextColor
                        )
                    }
                }
            }
        }
    }
}
