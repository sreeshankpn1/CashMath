package com.cashmath.app.ui.screens.members

import android.app.Activity
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.model.AccessScope
import com.cashmath.app.data.model.BusinessMemberEntity
import com.cashmath.app.data.model.MemberRole
import java.net.URLEncoder
import java.util.UUID

private val SurfaceWhite = Color(0xFFFFFFFF)
private val CardSoftSlate = Color(0xFFF8FAFC)
private val CardBorder = Color(0xFFE2E8F0)
private val DeepNavyText = Color(0xFF0F172A)
private val MutedSlateLabel = Color(0xFF64748B)
private val EmeraldDark = Color(0xFF059669)
private val AdminBlue = Color(0xFF3B82F6)
private val ViewerAmber = Color(0xFFF59E0B)
private val DangerRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMembersBottomSheet(
    businessName: String,
    businessId: String,
    books: List<BookEntity>,
    members: List<BusinessMemberEntity>,
    onDismiss: () -> Unit,
    onSaveMember: (BusinessMemberEntity) -> Unit,
    onDeleteMember: (BusinessMemberEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var inviteViaPhone by remember { mutableStateOf(true) } // true = Phone/WhatsApp, false = Gmail Cloud Sync
    var newMemberName by remember { mutableStateOf("") }
    var newMemberContact by remember { mutableStateOf("") } // Phone or Email
    var selectedRole by remember { mutableStateOf(MemberRole.STAFF) }
    var selectedScope by remember { mutableStateOf(AccessScope.ALL_BOOKS) }
    var assignedBookIds by remember { mutableStateOf(setOf<String>()) }
    var canAccessDuesEmi by remember { mutableStateOf(false) }

    var memberToEdit by remember { mutableStateOf<BusinessMemberEntity?>(null) }
    var memberToDelete by remember { mutableStateOf<BusinessMemberEntity?>(null) }

    // Smart Contact Picker (Phone or Email based on tab)
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
                val uri: Uri = result.data!!.data!!
                if (inviteViaPhone) {
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

                            if (cName.isNotBlank()) newMemberName = cName
                            if (cPhone.isNotBlank()) {
                                newMemberContact = cPhone.replace(Regex("[^0-9+]"), "")
                            }
                        }
                    }
                } else {
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                    )
                    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)
                            val emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                            val cName = if (nameIdx != -1) cursor.getString(nameIdx) ?: "" else ""
                            val cEmail = if (emailIdx != -1) cursor.getString(emailIdx) ?: "" else ""

                            if (cName.isNotBlank()) newMemberName = cName
                            if (cEmail.isNotBlank()) {
                                newMemberContact = cEmail.lowercase().trim()
                            } else {
                                Toast.makeText(context, "No email found for this contact. Please enter Gmail address manually.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No email found for this contact. Please enter Gmail address manually.", Toast.LENGTH_LONG).show()
                        }
                    } ?: Toast.makeText(context, "No email found for this contact. Please enter Gmail address manually.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not load contact details", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        contentColor = DeepNavyText,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFCBD5E1)) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Team & Member Permissions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavyText
                    )
                    Text(
                        text = "Manage staff, partners & book access in $businessName",
                        fontSize = 13.sp,
                        color = MutedSlateLabel
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(CardSoftSlate, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DeepNavyText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active Team Members List
            Text(
                text = "CURRENT MEMBERS (${members.size + 1})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSlateLabel,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Owner Tile
                MemberTileRow(
                    name = "You (Primary Owner)",
                    contact = "Owner / Administrator",
                    role = MemberRole.OWNER,
                    scopeText = "Access: All Books",
                    onEditClick = {},
                    onDeleteClick = {}
                )

                // Team Members
                members.filter { it.role != MemberRole.OWNER }.forEach { member ->
                    val scopeText = if (member.scope == AccessScope.ALL_BOOKS) "Access: All Books" else "Access: ${member.assignedBookIds.size} Books"
                    MemberTileRow(
                        name = member.name,
                        contact = member.phone ?: member.email ?: "No contact info",
                        role = member.role,
                        scopeText = scopeText,
                        isConnected = member.isConnected,
                        inviteCode = member.inviteCode,
                        onResendInvite = {
                            val code = member.inviteCode
                            if (!member.phone.isNullOrBlank()) {
                                sendWhatsAppInvite(context, member.name, member.phone, businessName, member.role.name, code)
                            } else if (!member.email.isNullOrBlank()) {
                                sendEmailInvite(context, member.name, member.email, businessName, member.role.name, code)
                            }
                        },
                        onEditClick = { memberToEdit = member },
                        onDeleteClick = { memberToDelete = member }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dual Segmented Selector (WhatsApp vs. Cloud Sync Email)
            Text(
                text = "INVITE METHOD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSlateLabel,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSoftSlate)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (inviteViaPhone) EmeraldDark else Color.Transparent)
                        .clickable {
                            inviteViaPhone = true
                            newMemberContact = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("WhatsApp (Phone)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (inviteViaPhone) Color.White else DeepNavyText)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!inviteViaPhone) EmeraldDark else Color.Transparent)
                        .clickable {
                            inviteViaPhone = false
                            newMemberContact = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cloud Sync (Gmail)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!inviteViaPhone) Color.White else DeepNavyText)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inline Add New Member Section
            OutlinedTextField(
                value = newMemberName,
                onValueChange = { newMemberName = it },
                label = { Text("Member Full Name *", color = MutedSlateLabel) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldDark) },
                trailingIcon = {
                    IconButton(onClick = {
                        try {
                            val intent = if (inviteViaPhone) {
                                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                            } else {
                                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI)
                            }
                            contactPickerLauncher.launch(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }) {
                        Icon(Icons.Default.ContactPhone, contentDescription = "Pick Contact", tint = EmeraldDark)
                    }
                },
                colors = lightTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone or Email Input with Proper Keyboard Options
            OutlinedTextField(
                value = newMemberContact,
                onValueChange = {
                    newMemberContact = if (inviteViaPhone) it else it.lowercase().trim()
                },
                label = {
                    Text(
                        text = if (inviteViaPhone) "Mobile Number *" else "Google Account / Gmail Address *",
                        color = MutedSlateLabel
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (inviteViaPhone) Icons.Default.Phone else Icons.Default.Mail,
                        contentDescription = null,
                        tint = EmeraldDark
                    )
                },
                placeholder = {
                    Text(
                        text = if (inviteViaPhone) "e.g. 9876543210" else "e.g. user@gmail.com",
                        color = MutedSlateLabel
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (inviteViaPhone) KeyboardType.Phone else KeyboardType.Email
                ),
                colors = lightTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Role Selection Chips
            Text("Select Access Role", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MutedSlateLabel, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleSelectionChip("Admin", MemberRole.ADMIN, selectedRole == MemberRole.ADMIN, modifier = Modifier.weight(1f)) { selectedRole = it }
                RoleSelectionChip("Staff", MemberRole.STAFF, selectedRole == MemberRole.STAFF, modifier = Modifier.weight(1f)) { selectedRole = it }
                RoleSelectionChip("Viewer", MemberRole.VIEWER, selectedRole == MemberRole.VIEWER, modifier = Modifier.weight(1f)) { selectedRole = it }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // INLINE BOOK ACCESS SCOPE
            Text("Book Access Scope", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MutedSlateLabel, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedScope == AccessScope.ALL_BOOKS,
                    onClick = { selectedScope = AccessScope.ALL_BOOKS },
                    label = { Text("All Books in $businessName", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CardSoftSlate,
                        selectedLabelColor = DeepNavyText,
                        containerColor = CardSoftSlate,
                        labelColor = MutedSlateLabel
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = CardBorder,
                        selectedBorderColor = EmeraldDark,
                        enabled = true,
                        selected = selectedScope == AccessScope.ALL_BOOKS
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedScope == AccessScope.SPECIFIC_BOOKS,
                    onClick = { selectedScope = AccessScope.SPECIFIC_BOOKS },
                    label = { Text("Specific Books Only", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CardSoftSlate,
                        selectedLabelColor = DeepNavyText,
                        containerColor = CardSoftSlate,
                        labelColor = MutedSlateLabel
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = CardBorder,
                        selectedBorderColor = EmeraldDark,
                        enabled = true,
                        selected = selectedScope == AccessScope.SPECIFIC_BOOKS
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedScope == AccessScope.SPECIFIC_BOOKS && books.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSoftSlate),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Check Assigned Books:", fontSize = 11.sp, color = MutedSlateLabel, fontWeight = FontWeight.Bold)
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
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldDark)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(book.name, color = DeepNavyText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Performance-Optimized Form Validation Check
            val cleanDigits = remember(newMemberContact) { newMemberContact.replace(Regex("[^0-9]"), "") }
            val isFormValid by remember(newMemberName, newMemberContact, inviteViaPhone, cleanDigits) {
                derivedStateOf {
                    val isValidContact = if (inviteViaPhone) cleanDigits.length >= 10 else (newMemberContact.contains("@") && newMemberContact.contains("."))
                    newMemberName.trim().length >= 2 && isValidContact
                }
            }

            // Primary Action Button (Pinned/Accessible above IME)
            Button(
                onClick = {
                    if (isFormValid) {
                        keyboardController?.hide()
                        val newCode = UUID.randomUUID().toString().take(6).uppercase()
                        val newMember = BusinessMemberEntity(
                            id = UUID.randomUUID().toString(),
                            businessId = businessId,
                            name = newMemberName.trim(),
                            phone = if (inviteViaPhone) newMemberContact.trim() else null,
                            email = if (!inviteViaPhone) newMemberContact.trim() else null,
                            role = selectedRole,
                            scope = selectedScope,
                            assignedBookIds = assignedBookIds.toList(),
                            canAccessDuesEmi = canAccessDuesEmi,
                            inviteCode = newCode,
                            status = "ACTIVE"
                        )

                        // 1. SAVE TO ROOM DATABASE IMMEDIATELY
                        onSaveMember(newMember)

                        // 2. LAUNCH INVITE VIA WHATSAPP OR GMAIL
                        if (inviteViaPhone) {
                            sendWhatsAppInvite(context, newMemberName.trim(), newMemberContact.trim(), businessName, selectedRole.name, newCode)
                        } else {
                            sendEmailInvite(context, newMemberName.trim(), newMemberContact.trim(), businessName, selectedRole.name, newCode)
                        }

                        // 3. RESET INPUTS & KEEP SHEET OPEN FOR INSTANT REACTIVE LIST UPDATE
                        newMemberName = ""
                        newMemberContact = ""
                        selectedRole = MemberRole.STAFF
                        selectedScope = AccessScope.ALL_BOOKS
                        assignedBookIds = emptySet()
                        Toast.makeText(context, "Member saved & added to team! ✓", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldDark,
                    disabledContainerColor = EmeraldDark.copy(alpha = 0.35f)
                )
            ) {
                Icon(
                    imageVector = if (inviteViaPhone) Icons.AutoMirrored.Filled.Send else Icons.Default.Mail,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (inviteViaPhone) "+ Add & Send WhatsApp Invite" else "+ Add & Send Email Invite",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit Member Role / Scope Sheet
    memberToEdit?.let { target ->
        EditMemberAccessSheet(
            member = target,
            books = books,
            onDismiss = { memberToEdit = null },
            onUpdate = { updated ->
                onSaveMember(updated)
                memberToEdit = null
            },
            onDelete = {
                onDeleteMember(target)
                memberToEdit = null
            }
        )
    }

    // Revoke Confirmation Dialog
    memberToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed) },
            title = { Text("Remove ${target.name}?", color = DeepNavyText, fontWeight = FontWeight.Bold) },
            text = { Text("This member will immediately lose access to $businessName ledgers.", color = MutedSlateLabel) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMember(target)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel", color = MutedSlateLabel)
                }
            }
        )
    }
}

@Composable
private fun MemberTileRow(
    name: String,
    contact: String,
    role: MemberRole,
    scopeText: String,
    isConnected: Boolean = true,
    inviteCode: String = "",
    onResendInvite: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val roleColor = when (role) {
        MemberRole.OWNER -> MutedSlateLabel
        MemberRole.ADMIN -> AdminBlue
        MemberRole.STAFF -> EmeraldDark
        MemberRole.VIEWER -> ViewerAmber
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSoftSlate),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = roleColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = name, color = DeepNavyText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        if (role != MemberRole.OWNER) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isConnected) EmeraldDark.copy(alpha = 0.15f) else ViewerAmber.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, if (isConnected) EmeraldDark.copy(alpha = 0.4f) else ViewerAmber.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (isConnected) "🟢 Active" else "🟡 Pending ($inviteCode)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) EmeraldDark else ViewerAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(text = "$contact • $scopeText", color = MutedSlateLabel, fontSize = 11.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = roleColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, roleColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = role.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (role != MemberRole.OWNER) {
                    Spacer(modifier = Modifier.width(6.dp))

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFE2E8F0), CircleShape)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = DeepNavyText, modifier = Modifier.size(18.dp))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Resend Invitation", color = DeepNavyText, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onResendInvite()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Role & Book Scope", color = DeepNavyText, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = DeepNavyText, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove Member", color = DangerRed, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleSelectionChip(
    title: String,
    role: MemberRole,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (MemberRole) -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) EmeraldDark else CardSoftSlate)
            .border(1.dp, if (isSelected) EmeraldDark else CardBorder, RoundedCornerShape(10.dp))
            .clickable { onSelect(role) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else DeepNavyText,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMemberAccessSheet(
    member: BusinessMemberEntity,
    books: List<BookEntity>,
    onDismiss: () -> Unit,
    onUpdate: (BusinessMemberEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    var editedRole by remember { mutableStateOf(member.role) }
    var editedScope by remember { mutableStateOf(member.scope) }
    var editedBookIds by remember { mutableStateOf(member.assignedBookIds.toSet()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFCBD5E1)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Text(text = "Edit Member Access", color = DeepNavyText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = member.name, color = MutedSlateLabel, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Change Role", color = MutedSlateLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleSelectionChip("Admin", MemberRole.ADMIN, editedRole == MemberRole.ADMIN, modifier = Modifier.weight(1f)) { editedRole = it }
                RoleSelectionChip("Staff", MemberRole.STAFF, editedRole == MemberRole.STAFF, modifier = Modifier.weight(1f)) { editedRole = it }
                RoleSelectionChip("Viewer", MemberRole.VIEWER, editedRole == MemberRole.VIEWER, modifier = Modifier.weight(1f)) { editedRole = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Book Access Scope", color = MutedSlateLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = editedScope == AccessScope.ALL_BOOKS,
                    onClick = { editedScope = AccessScope.ALL_BOOKS },
                    label = { Text("All Books", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = editedScope == AccessScope.SPECIFIC_BOOKS,
                    onClick = { editedScope = AccessScope.SPECIFIC_BOOKS },
                    label = { Text("Specific Books Only", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (editedScope == AccessScope.SPECIFIC_BOOKS && books.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                books.forEach { book ->
                    val isChecked = editedBookIds.contains(book.id.toString())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editedBookIds = if (isChecked) editedBookIds - book.id.toString() else editedBookIds + book.id.toString()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                editedBookIds = if (checked) editedBookIds + book.id.toString() else editedBookIds - book.id.toString()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldDark)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(book.name, color = DeepNavyText, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { onUpdate(member.copy(role = editedRole, scope = editedScope, assignedBookIds = editedBookIds.toList())) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDelete(member.id) },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Remove Member from Business", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun sendWhatsAppInvite(context: Context, name: String, phone: String, businessName: String, role: String, inviteCode: String) {
    try {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").let { if (it.length == 10) "91$it" else it }
        val msg = """
Hi $name, you've been invited as $role to '$businessName' on CashMath.

1. Install CashMath: https://play.google.com/store/apps/details?id=com.cashmath.app
2. Open app -> Tap 'Join via Invite Code' in Settings
3. Enter Code: $inviteCode
        """.trimIndent()

        val encoded = URLEncoder.encode(msg, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.whatsapp") }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

private fun sendEmailInvite(context: Context, name: String, email: String, businessName: String, role: String, inviteCode: String) {
    try {
        val subject = "CashMath Team Invitation: $businessName"
        val body = """
Hi $name,

You have been invited as $role to collaborate on '$businessName' on CashMath.

1. Install CashMath: https://play.google.com/store/apps/details?id=com.cashmath.app
2. Open CashMath -> Tap 'Join via Invite Code' in Settings
3. Enter 6-Digit Code: $inviteCode

Welcome to the team!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send Email Invite via"))
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open email app", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun lightTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CardSoftSlate,
    unfocusedContainerColor = CardSoftSlate,
    focusedTextColor = DeepNavyText,
    unfocusedTextColor = DeepNavyText,
    focusedBorderColor = EmeraldDark,
    unfocusedBorderColor = CardBorder,
    focusedLabelColor = EmeraldDark,
    unfocusedLabelColor = MutedSlateLabel
)
