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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.model.AccessScope
import com.cashmath.app.data.model.BusinessMemberEntity
import com.cashmath.app.data.model.MemberRole
import java.net.URLEncoder
import java.util.UUID

private val DeepObsidian = Color(0xFF0A0E17)
private val SurfaceCard = Color(0xFF141A28)
private val CardBorder = Color(0xFF222C42)
private val NeonEmerald = Color(0xFF00E676)
private val NeonCoral = Color(0xFFFF5252)
private val SlateLabel = Color(0xFF8E9BB0)
private val AdminBlue = Color(0xFF5E9CFF)
private val ViewerAmber = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBookMembersBottomSheet(
    businessId: String,
    businessName: String,
    bookId: String,
    bookName: String,
    members: List<BusinessMemberEntity>,
    onDismiss: () -> Unit,
    onSaveMember: (BusinessMemberEntity) -> Unit,
    onDeleteMember: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )
    val context = LocalContext.current

    var newMemberName by remember { mutableStateOf("") }
    var newMemberContact by remember { mutableStateOf("") } // Phone or Email
    var selectedRole by remember { mutableStateOf(MemberRole.STAFF) }

    // Member currently selected for editing/removing
    var memberToEdit by remember { mutableStateOf<BusinessMemberEntity?>(null) }
    var memberToDelete by remember { mutableStateOf<BusinessMemberEntity?>(null) }

    // 100% Reliable Direct Phone + Name Picker (Zero Permissions Required)
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
                val contactUri: Uri = result.data!!.data!!
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val name = cursor.getString(0) ?: ""
                        val rawNumber = cursor.getString(1) ?: ""
                        if (name.isNotBlank()) newMemberName = name
                        if (rawNumber.isNotBlank()) {
                            newMemberContact = rawNumber.replace(Regex("[^0-9+]"), "")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not read contact", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidian,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Manage Book Members",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Members in '$bookName'",
                        color = SlateLabel,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).background(SurfaceCard, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Members List
            Text(
                text = "CURRENT MEMBERS",
                color = SlateLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Owner Tile (Always Present)
                item {
                    MemberTile(
                        member = BusinessMemberEntity(
                            businessId = businessId,
                            name = "You (Owner / Admin)",
                            role = MemberRole.OWNER,
                            phone = "Primary Owner"
                        ),
                        onEditClick = {},
                        onDeleteClick = {}
                    )
                }

                // Assigned Members (e.g. Jithi)
                items(members.filter { it.role != MemberRole.OWNER }) { member ->
                    MemberTile(
                        member = member,
                        onEditClick = { memberToEdit = member },
                        onDeleteClick = { memberToDelete = member }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add New Member Section
            Text(
                text = "ADD NEW MEMBER",
                color = SlateLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Member Name Input with Contact Picker Icon
            OutlinedTextField(
                value = newMemberName,
                onValueChange = { newMemberName = it },
                label = { Text("Member Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SlateLabel) },
                trailingIcon = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                        contactPickerLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.ContactPhone, contentDescription = "Pick Contact", tint = NeonEmerald)
                    }
                },
                colors = darkTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mobile Number or Email Input
            val isEmailInput = newMemberContact.contains("@")
            OutlinedTextField(
                value = newMemberContact,
                onValueChange = { newMemberContact = it },
                label = { Text("Mobile Number or Email Address") },
                leadingIcon = {
                    Icon(
                        imageVector = if (isEmailInput) Icons.Default.Mail else Icons.Default.Phone,
                        contentDescription = null,
                        tint = SlateLabel
                    )
                },
                placeholder = { Text("e.g. 9876543210 or user@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = if (isEmailInput) KeyboardType.Email else KeyboardType.Phone),
                colors = darkTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Role Selector Chips (Admin, Staff, Viewer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleSelectionChip("Admin", MemberRole.ADMIN, selectedRole == MemberRole.ADMIN, modifier = Modifier.weight(1f)) { selectedRole = it }
                RoleSelectionChip("Staff", MemberRole.STAFF, selectedRole == MemberRole.STAFF, modifier = Modifier.weight(1f)) { selectedRole = it }
                RoleSelectionChip("Viewer", MemberRole.VIEWER, selectedRole == MemberRole.VIEWER, modifier = Modifier.weight(1f)) { selectedRole = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add to Book Button
                Button(
                    onClick = {
                        if (newMemberName.isNotBlank() && newMemberContact.isNotBlank()) {
                            val newEntity = BusinessMemberEntity(
                                id = UUID.randomUUID().toString(),
                                businessId = businessId,
                                name = newMemberName.trim(),
                                phone = if (!isEmailInput) newMemberContact.trim() else null,
                                email = if (isEmailInput) newMemberContact.trim() else null,
                                role = selectedRole,
                                scope = AccessScope.SPECIFIC_BOOKS,
                                assignedBookIds = listOf(bookId)
                            )
                            onSaveMember(newEntity)
                            newMemberName = ""
                            newMemberContact = ""
                            Toast.makeText(context, "Member added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter both name and contact", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("+ Add to Book", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                // WhatsApp Invite Button
                Button(
                    onClick = {
                        if (newMemberContact.isNotBlank() && !isEmailInput) {
                            sendWhatsAppInvite(context, newMemberName, newMemberContact, businessName, bookName, selectedRole.name)
                        } else {
                            Toast.makeText(context, "Enter a valid phone number to invite", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Invite via WhatsApp", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }

    // INTERACTIVE EDIT MEMBER SHEET
    memberToEdit?.let { targetMember ->
        EditMemberAccessSheet(
            member = targetMember,
            onDismiss = { memberToEdit = null },
            onUpdate = { updated ->
                onSaveMember(updated)
                memberToEdit = null
            },
            onDelete = { id ->
                onDeleteMember(id)
                memberToEdit = null
            }
        )
    }

    // CONFIRM REMOVE MEMBER DIALOG
    memberToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = NeonCoral) },
            title = { Text("Remove ${target.name}?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This member will lose access to $bookName immediately.", color = SlateLabel) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMember(target.id)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCoral),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel", color = SlateLabel)
                }
            }
        )
    }
}

@Composable
private fun MemberTile(
    member: BusinessMemberEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val roleColor = when (member.role) {
        MemberRole.OWNER -> SlateLabel
        MemberRole.ADMIN -> AdminBlue
        MemberRole.STAFF -> NeonEmerald
        MemberRole.VIEWER -> ViewerAmber
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF1E283E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = member.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = member.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = member.phone ?: member.email ?: "", color = SlateLabel, fontSize = 12.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Role Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(roleColor.copy(alpha = 0.15f))
                        .border(1.dp, roleColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = member.role.name, color = roleColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (member.role != MemberRole.OWNER) {
                    Spacer(modifier = Modifier.width(6.dp))

                    // EDIT BUTTON (Pencil)
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp).background(Color(0xFF1E283E), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Role", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // REMOVE BUTTON (Trash)
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp).background(Color(0xFF381F26), CircleShape)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove Member", tint = NeonCoral, modifier = Modifier.size(16.dp))
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
            .background(if (isSelected) NeonEmerald else SurfaceCard)
            .border(1.dp, if (isSelected) NeonEmerald else CardBorder, RoundedCornerShape(10.dp))
            .clickable { onSelect(role) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMemberAccessSheet(
    member: BusinessMemberEntity,
    onDismiss: () -> Unit,
    onUpdate: (BusinessMemberEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    var editedRole by remember { mutableStateOf(member.role) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidian,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Text(text = "Edit Member Access", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = member.name, color = SlateLabel, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Change Role", color = SlateLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleSelectionChip("Admin", MemberRole.ADMIN, editedRole == MemberRole.ADMIN, modifier = Modifier.weight(1f)) { editedRole = it }
                RoleSelectionChip("Staff", MemberRole.STAFF, editedRole == MemberRole.STAFF, modifier = Modifier.weight(1f)) { editedRole = it }
                RoleSelectionChip("Viewer", MemberRole.VIEWER, editedRole == MemberRole.VIEWER, modifier = Modifier.weight(1f)) { editedRole = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { onUpdate(member.copy(role = editedRole)) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDelete(member.id) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCoral.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, NeonCoral.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = NeonCoral, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Remove Member from Book", color = NeonCoral, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun sendWhatsAppInvite(context: Context, name: String, phone: String, businessName: String, bookName: String, role: String) {
    try {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").let { if (it.length == 10) "91$it" else it }
        val msg = """
*TEAM INVITATION: CashMath*

Hi $name,

You have been invited to collaborate on *$businessName* ($bookName) as *$role*.

Download CashMath here to join:
https://play.google.com/store/apps/details?id=com.cashmath.app
        """.trimIndent()

        val encoded = URLEncoder.encode(msg, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.whatsapp") }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun darkTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = NeonEmerald,
    unfocusedBorderColor = CardBorder,
    focusedLabelColor = NeonEmerald,
    unfocusedLabelColor = SlateLabel
)
