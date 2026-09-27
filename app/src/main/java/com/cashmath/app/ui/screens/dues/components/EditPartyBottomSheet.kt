package com.cashmath.app.ui.screens.dues.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.ui.theme.*
import com.cashmath.app.util.ContactPickerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPartyBottomSheet(
    party: DuePartyEntity,
    onDismiss: () -> Unit,
    onSave: (DuePartyEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember(party) { mutableStateOf(party.name) }
    var phone by remember(party) { mutableStateOf(party.phone ?: "") }
    var toReceiveText by remember(party) { mutableStateOf(party.toReceive.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }) }
    var toPayText by remember(party) { mutableStateOf(party.toPay.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        contactUri?.let { uri ->
            val (cName, cPhone) = ContactPickerHelper.extractContactDetails(context, uri)
            if (cName.isNotBlank() && name.isBlank()) name = cName
            if (cPhone.isNotBlank()) phone = cPhone.replace(Regex("[^0-9+]"), "")
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = RoyalIndigo,
        focusedBorderColor = RoyalIndigo,
        unfocusedBorderColor = BorderHairline
    )
    val fieldStyle = TextStyle(
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Customer / Supplier", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Party Name *") },
                    placeholder = { Text("e.g., Sharma Enterprises, Rahul Verma, Vendor") },
                    colors = fieldColors,
                    textStyle = fieldStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("e.g., 98765 43210") },
                    trailingIcon = {
                        IconButton(onClick = {
                            try {
                                contactPickerLauncher.launch(null)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }) {
                            Icon(Icons.Default.Contacts, contentDescription = "Pick Contact", tint = RoyalIndigo)
                        }
                    },
                    colors = fieldColors,
                    textStyle = fieldStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = toReceiveText, onValueChange = { toReceiveText = it }, label = { Text("Amount You'll Receive (₹)") }, colors = fieldColors, textStyle = fieldStyle, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = toPayText, onValueChange = { toPayText = it }, label = { Text("Amount You'll Pay (₹)") }, colors = fieldColors, textStyle = fieldStyle, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val toRec = toReceiveText.toDoubleOrNull() ?: party.toReceive
                        val toPay = toPayText.toDoubleOrNull() ?: party.toPay
                        val updated = party.copy(
                            name = name,
                            phone = phone.ifBlank { null },
                            toReceive = toRec,
                            toPay = toPay,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(updated)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
