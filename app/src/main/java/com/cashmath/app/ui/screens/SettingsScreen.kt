package com.cashmath.app.ui.screens

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.auth.FirebaseAuthManager
import com.cashmath.app.backup.CloudBackupManager
import com.cashmath.app.data.AppThemeMode
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.BusinessEntity
import com.cashmath.app.data.model.BusinessMemberEntity
import com.cashmath.app.review.InAppReviewManager
import com.cashmath.app.service.DailyPulseScheduler
import com.cashmath.app.subscription.SubscriptionManager
import com.cashmath.app.subscription.SubscriptionState
import com.cashmath.app.subscription.UserPlan
import com.cashmath.app.ui.screens.members.ManageMembersBottomSheet
import com.cashmath.app.ui.screens.settings.MessageTemplatesSheet
import com.cashmath.app.ui.screens.subscription.SubscriptionScreen
import com.cashmath.app.ui.theme.LocalThemeIsDark
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentBusiness: BusinessEntity?,
    isBiometricEnabled: Boolean,
    themeMode: AppThemeMode,
    books: List<BookEntity> = emptyList(),
    members: List<BusinessMemberEntity> = emptyList(),
    onUpdateBusinessProfile: (BusinessEntity) -> Unit = {},
    onToggleBiometricLock: (Boolean) -> Unit = {},
    onSelectThemeMode: (AppThemeMode) -> Unit = {},
    onSaveMember: (BusinessMemberEntity) -> Unit = {},
    onDeleteMember: (BusinessMemberEntity) -> Unit = {},
    onOpenAuthScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalThemeIsDark.current

    val badgeBg = if (isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF)
    val badgeIconTint = if (isDark) Color(0xFF818CF8) else Color(0xFF4338CA)
    val brandLinkColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4338CA)

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = if (isDark) Color(0xFF4F46E5) else Color(0xFF1E293B),
        uncheckedThumbColor = if (isDark) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
        uncheckedTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
    )

    var showEditProfileDialog by remember { mutableStateOf(false) }

    val subManager = remember { SubscriptionManager(context) }
    val subState by subManager.subscriptionState.collectAsState(
        initial = SubscriptionState(
            isPro = false,
            plan = UserPlan.FREE,
            expiryTimestamp = 0L,
            maxBusinessesAllowed = 1,
            maxMembersPerBusiness = 2
        )
    )
    var showPaywallSheetInSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Profile",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Business Profile Card
            Text("Business Profile", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = badgeIconTint)
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = currentBusiness?.name ?: "Primary Business",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val subtitleText = listOfNotNull(
                            currentBusiness?.role ?: "Primary Admin",
                            currentBusiness?.gstNumber?.let { "GST: $it" },
                            currentBusiness?.phone?.let { "Ph: $it" }
                        ).joinToString(" • ")
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Business", tint = badgeIconTint)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            var showTeamMembersSheet by remember { mutableStateOf(false) }

            // Team & Member Permissions Card
            Text("Team & Member Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Card(
                onClick = { showTeamMembersSheet = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Team & Member Permissions",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Manage staff, CAs, partners & book access",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = "Manage",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeIconTint,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            if (showTeamMembersSheet) {
                ManageMembersBottomSheet(
                    businessName = currentBusiness?.name ?: "Business",
                    businessId = currentBusiness?.id?.toString() ?: "1",
                    books = books,
                    members = members,
                    onDismiss = { showTeamMembersSheet = false },
                    onSaveMember = onSaveMember,
                    onDeleteMember = onDeleteMember
                )
            }

            var showJoinInviteCodeDialog by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showJoinInviteCodeDialog = true }) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = brandLinkColor, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Join via Invite Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandLinkColor)
                }
            }

            if (showJoinInviteCodeDialog) {
                JoinInviteCodeDialog(
                    onDismiss = { showJoinInviteCodeDialog = false },
                    onConfirm = { code ->
                        showJoinInviteCodeDialog = false
                        Toast.makeText(context, "Checking Invite Code '$code'...", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            var showMessageTemplatesSheet by remember { mutableStateOf(false) }

            // Message Templates Card
            Text("WhatsApp Reminder Templates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Card(
                onClick = { showMessageTemplatesSheet = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Message Templates",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Customize default WhatsApp reminder text",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            if (showMessageTemplatesSheet) {
                MessageTemplatesSheet(
                    onDismiss = { showMessageTemplatesSheet = false }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Pro Membership Banner Card
            Text("Subscription Plan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = if (subState.isPro) Color(0xFFFEF3C7) else badgeBg, shape = CircleShape) {
                            Text(if (subState.isPro) "👑" else "⚡", modifier = Modifier.padding(8.dp), fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (subState.isPro) "CashMath Pro Active" else "Free Starter Plan",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (subState.isPro) "Unlimited Businesses, Books & Team Sync" else "Single business limit • Upgrade to Pro for unlimited access",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val proBtnBg = if (isDark) Color(0xFF1E293B) else Color(0xFF0F172A)
                    Button(
                        onClick = { showPaywallSheetInSettings = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = proBtnBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (subState.isPro) "Manage Pro Membership" else "Upgrade to CashMath Pro 👑", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (showPaywallSheetInSettings) {
                SubscriptionScreen(
                    onDismiss = { showPaywallSheetInSettings = false },
                    onSubscriptionSuccess = { showPaywallSheetInSettings = false }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Security & App Preferences
            Text("App Preferences & Security", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column {
                    // Biometric Lock Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Biometric App Lock", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Require fingerprint or device PIN on launch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = onToggleBiometricLock,
                            colors = switchColors
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Theme Selector
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Text("Theme Preference", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                AppThemeMode.SYSTEM to "System",
                                AppThemeMode.LIGHT to "Light",
                                AppThemeMode.DARK to "Dark"
                            ).forEach { (mode, label) ->
                                val isSelected = themeMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectThemeMode(mode) },
                                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedContainerColor = badgeIconTint,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Battery Optimization / Auto-Start Helper Card for Xiaomi/Vivo/Oppo/Realme
            Text("On-Time Reminders & Auto-Start", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Card(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Open Settings -> App Info to enable Auto-Start and battery permissions.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BatterySaver, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ensure On-Time Reminders", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Enable Auto-Start & disable battery restrictions (MIUI / Vivo / Oppo / Realme)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            val prefs = remember { context.getSharedPreferences("cashmath_settings_prefs", Context.MODE_PRIVATE) }
            var isPulseEnabled by remember { mutableStateOf(prefs.getBoolean("pulse_enabled", true)) }
            var pulseHour by remember { mutableIntStateOf(prefs.getInt("pulse_hour", 21)) }
            var pulseMinute by remember { mutableIntStateOf(prefs.getInt("pulse_minute", 0)) }

            val formattedPulseTime = remember(pulseHour, pulseMinute) {
                val amPm = if (pulseHour >= 12) "PM" else "AM"
                val displayHour = when {
                    pulseHour == 0 -> 12
                    pulseHour > 12 -> pulseHour - 12
                    else -> pulseHour
                }
                String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, pulseMinute, amPm)
            }

            Spacer(Modifier.height(16.dp))

            // Daily Business Pulse Scheduler Card
            Text("Daily Business Pulse", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Evening Business Pulse", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Automated daily closing balance & dues summary", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isPulseEnabled,
                            onCheckedChange = { enabled ->
                                isPulseEnabled = enabled
                                prefs.edit().putBoolean("pulse_enabled", enabled).apply()
                                DailyPulseScheduler.schedule(context, pulseHour, pulseMinute, enabled)
                                Toast.makeText(context, if (enabled) "Daily Pulse Scheduled at $formattedPulseTime" else "Daily Pulse Disabled", Toast.LENGTH_SHORT).show()
                            },
                            colors = switchColors
                        )
                    }

                    if (isPulseEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBg)
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            pulseHour = hourOfDay
                                            pulseMinute = minute
                                            prefs.edit().putInt("pulse_hour", hourOfDay).putInt("pulse_minute", minute).apply()
                                            DailyPulseScheduler.schedule(context, hourOfDay, minute, true)
                                            Toast.makeText(context, "Pulse time updated!", Toast.LENGTH_SHORT).show()
                                        },
                                        pulseHour,
                                        pulseMinute,
                                        false
                                    ).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Scheduled Closing Hour", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = formattedPulseTime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Cloud Backup & Account Section
            Text("Cloud Backup & Sync", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            val currentUser = FirebaseAuthManager.currentUser
            var isCloudBusy by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (currentUser != null) "Account: ${currentUser.email ?: "Signed In"}" else "Guest Account (Offline)",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentUser != null) "Cloud backup & sync active" else "Sign in to backup ledgers to Google Cloud",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (currentUser == null) {
                        Button(
                            onClick = onOpenAuthScreen,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = badgeIconTint),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Sign In with Google", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isCloudBusy = true
                                        val res = CloudBackupManager.backupData(context)
                                        isCloudBusy = false
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Backup uploaded to Google Cloud successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Backup failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isCloudBusy,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, badgeIconTint)
                            ) {
                                Text("Backup Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badgeIconTint)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        isCloudBusy = true
                                        val res = CloudBackupManager.restoreData(context)
                                        isCloudBusy = false
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Data restored from Google Cloud successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Restore failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isCloudBusy,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = badgeIconTint),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Restore Data", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                scope.launch {
                                    FirebaseAuthManager.signOut(context)
                                    Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign Out", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Rate App on Google Play Card
            Card(
                onClick = {
                    (context as? Activity)?.let { act ->
                        InAppReviewManager.launchReviewFlow(act)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.StarRate, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Rate CashMath on Play Store ⭐", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Help us grow by leaving a quick rating & feedback", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showEditProfileDialog && currentBusiness != null) {
        EditBusinessDialog(
            business = currentBusiness,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { updatedBiz ->
                onUpdateBusinessProfile(updatedBiz)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun EditBusinessDialog(
    business: BusinessEntity,
    onDismiss: () -> Unit,
    onConfirm: (BusinessEntity) -> Unit
) {
    var name by remember { mutableStateOf(business.name) }
    var phone by remember { mutableStateOf(business.phone ?: "") }
    var gstin by remember { mutableStateOf(business.gstNumber ?: "") }
    var address by remember { mutableStateOf(business.address ?: "") }

    val isDark = LocalThemeIsDark.current

    val dialogBg = if (isDark) Color(0xFF141A28) else Color(0xFFFFFFFF)
    val titleColor = if (isDark) Color.White else Color(0xFF111827)

    val inputBg = if (isDark) Color(0xFF0F1523) else Color(0xFFF3F4F6)
    val inputBorder = if (isDark) Color(0xFF222C42) else Color(0xFFD1D5DB)
    val inputTextColor = if (isDark) Color.White else Color(0xFF111827)
    val labelColor = if (isDark) Color(0xFF8E9BB0) else Color(0xFF6B7280)

    val accentColor = if (isDark) Color(0xFF10B981) else Color(0xFF059669)
    val buttonTextColor = Color.White

    val tileColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = inputBg,
        unfocusedContainerColor = inputBg,
        focusedTextColor = inputTextColor,
        unfocusedTextColor = inputTextColor,
        focusedBorderColor = accentColor,
        unfocusedBorderColor = inputBorder,
        focusedLabelColor = accentColor,
        unfocusedLabelColor = labelColor,
        cursorColor = accentColor
    )

    val fieldTextStyle = TextStyle(
        color = inputTextColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                text = "Edit Business Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Business Name *") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = accentColor) },
                    colors = tileColors,
                    textStyle = fieldTextStyle,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / WhatsApp Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = accentColor) },
                    colors = tileColors,
                    textStyle = fieldTextStyle,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it.uppercase() },
                    label = { Text("GSTIN") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = accentColor) },
                    colors = tileColors,
                    textStyle = fieldTextStyle,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Business Address") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = accentColor) },
                    colors = tileColors,
                    textStyle = fieldTextStyle,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            business.copy(
                                name = name.trim(),
                                phone = phone.ifBlank { null },
                                gstNumber = gstin.ifBlank { null },
                                address = address.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.4f)
                )
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = buttonTextColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = labelColor, fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun JoinInviteCodeDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    val isDark = LocalThemeIsDark.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF141A28) else Color.White,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Join Business via Invite Code", color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the 6-character code provided by your Business Owner / Admin:", fontSize = 12.sp, color = if (isDark) Color(0xFF8E9BB0) else Color.Gray)
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase().take(6) },
                    label = { Text("6-Digit Invite Code") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF222C42)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (code.length == 6) onConfirm(code) },
                enabled = code.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Connect Business", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF8E9BB0))
            }
        }
    )
}
