package com.cashmath.app.ui.screens.members

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.model.AccessScope
import com.cashmath.app.data.model.BusinessMemberEntity
import com.cashmath.app.data.model.MemberRole
import com.cashmath.app.util.ContactPickerHelper
import com.cashmath.app.utils.WhatsAppReminderHelper
import java.util.UUID

private val DeepObsidian = Color(0xFF0A0E17)
private val SurfaceCard = Color(0xFF141A28)
private val CardBorder = Color(0xFF222C42)
private val NeonEmerald = Color(0xFF00E676)
private val NeonCoral = Color(0xFFFF5252)
private val MutedLabel = Color(0xFF8E9BB0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberBottomSheet(
    businessName: String,
    businessId: String,
    books: List<BookEntity>,
    existingMember: BusinessMemberEntity? = null,
    onDismiss: () -> Unit,
    onSaveMember: (BusinessMemberEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var inviteViaPhone by remember { mutableStateOf(existingMember?.email.isNullOrBlank()) }
    var name by remember { mutableStateOf(existingMember?.name ?: "") }
    var phone by remember { mutableStateOf(existingMember?.phone ?: "") }
    var email by remember { mutableStateOf(existingMember?.email ?: "") }
    var selectedRole by remember { mutableStateOf(existingMember?.role ?: MemberRole.STAFF) }
    var selectedScope by remember { mutableStateOf(existingMember?.scope ?: AccessScope.ALL_BOOKS) }
    var assignedBookIds by remember { mutableStateOf(existingMember?.assignedBookIds?.toSet() ?: emptySet()) }
    var canAccessDuesEmi by remember { mutableStateOf(existingMember?.canAccessDuesEmi ?: false) }

    val phonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
                val uri = result.data!!.data!!
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                        val cName = if (nameIdx != -1) cursor.getString(nameIdx) ?: "" else ""
                        val cPhone = if (phoneIdx != -1) cursor.getString(phoneIdx) ?: "" else ""

                        if (cName.isNotBlank()) name = cName
                        if (cPhone.isNotBlank()) {
                            phone = cPhone.replace(Regex("[^0-9+]"), "")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                Text(
                    text = if (existingMember != null) "Edit Member Permissions" else "Add Team Member",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedLabel)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Invite Mode Toggle
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
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (inviteViaPhone) NeonEmerald else Color.Transparent)
                        .clickable { inviteViaPhone = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("WhatsApp (Phone)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (inviteViaPhone) Color.Black else Color.White)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!inviteViaPhone) NeonEmerald else Color.Transparent)
                        .clickable { inviteViaPhone = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Email Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!inviteViaPhone) Color.Black else Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            DarkTextField(
                label = "Member Full Name *",
                value = name,
                onValueChange = { name = it },
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Phone or Email Field
            if (inviteViaPhone) {
                DarkTextField(
                    label = "Mobile Number *",
                    value = phone,
                    onValueChange = { phone = it },
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                    phonePickerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Contacts, contentDescription = "Pick Contact", tint = NeonEmerald)
                        }
                    }
                )
            } else {
                DarkTextField(
                    label = "Email Address *",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Role Selection Cards
            Text("Select Access Role", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MutedLabel)
            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                Triple(MemberRole.ADMIN, "Admin / Manager", "Add/edit entries & view full reports across assigned books."),
                Triple(MemberRole.STAFF, "Staff / Sales", "Record cash entries & attach bills. Cannot delete entries or books."),
                Triple(MemberRole.VIEWER, "Read-Only Viewer", "View-only access for CAs, Partners & External Auditors.")
            ).forEach { (role, title, desc) ->
                val isSelected = selectedRole == role
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedRole = role },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, if (isSelected) NeonEmerald else CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedRole = role },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(desc, fontSize = 11.sp, color = MutedLabel)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Book Access Scope
            Text("Book Access Scope", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MutedLabel)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = selectedScope == AccessScope.ALL_BOOKS,
                    onClick = { selectedScope = AccessScope.ALL_BOOKS },
                    label = { Text("All Books in $businessName", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceCard,
                        labelColor = MutedLabel
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedScope == AccessScope.SPECIFIC_BOOKS,
                    onClick = { selectedScope = AccessScope.SPECIFIC_BOOKS },
                    label = { Text("Specific Books Only", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceCard,
                        labelColor = MutedLabel
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedScope == AccessScope.SPECIFIC_BOOKS) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Check Assigned Books:", fontSize = 11.sp, color = MutedLabel, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        books.forEach { book ->
                            val isBookChecked = assignedBookIds.contains(book.id.toString())
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        assignedBookIds = if (isBookChecked) {
                                            assignedBookIds - book.id.toString()
                                        } else {
                                            assignedBookIds + book.id.toString()
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isBookChecked,
                                    onCheckedChange = { checked ->
                                        assignedBookIds = if (checked) assignedBookIds + book.id.toString() else assignedBookIds - book.id.toString()
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(book.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dues & EMI Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dues & EMI Tracker Access", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Allow viewing customer/supplier dues & loan repayments", fontSize = 11.sp, color = MutedLabel)
                    }
                    Switch(
                        checked = canAccessDuesEmi,
                        onCheckedChange = { canAccessDuesEmi = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonEmerald)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.isBlank() || (inviteViaPhone && phone.isBlank()) || (!inviteViaPhone && email.isBlank())) {
                        Toast.makeText(context, "Please enter all required member details", Toast.LENGTH_SHORT).show()
                    } else {
                        val newEntity = BusinessMemberEntity(
                            id = existingMember?.id ?: UUID.randomUUID().toString(),
                            businessId = businessId,
                            name = name.trim(),
                            phone = if (inviteViaPhone) phone.trim() else null,
                            email = if (!inviteViaPhone) email.trim() else null,
                            role = selectedRole,
                            scope = selectedScope,
                            assignedBookIds = assignedBookIds.toList(),
                            canAccessDuesEmi = canAccessDuesEmi
                        )

                        onSaveMember(newEntity)

                        if (inviteViaPhone) {
                            val msg = "Hi $name, you've been invited as ${selectedRole.name} on '$businessName' on CashMath. Join here: https://cashmath.app/join?biz=$businessId&code=${newEntity.inviteCode}"
                            WhatsAppReminderHelper.sendDirectWhatsApp(context, phone, msg)
                        } else {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                                putExtra(Intent.EXTRA_SUBJECT, "Invitation to join '$businessName' on CashMath")
                                putExtra(Intent.EXTRA_TEXT, "Hi $name,\n\nYou have been invited to collaborate on '$businessName' as a ${selectedRole.name}.\n\nClick to accept: https://cashmath.app/join?biz=$businessId&code=${newEntity.inviteCode}")
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Invite saved!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (inviteViaPhone) "Save & Send WhatsApp Invite" else "Save & Send Email Invite",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DarkTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MutedLabel) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = NeonEmerald) },
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceCard,
            unfocusedContainerColor = SurfaceCard,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = NeonEmerald,
            unfocusedBorderColor = CardBorder,
            cursorColor = NeonEmerald
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
