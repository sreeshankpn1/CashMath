package com.cashmath.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import com.cashmath.app.parser.DateTimeUtils
import com.cashmath.app.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    type: TransactionType,
    editingTransaction: TransactionEntity? = null,
    onBack: () -> Unit,
    onSave: (Double, String?, String?, String?, String, Long) -> Unit
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var partyName by remember { mutableStateOf(editingTransaction?.partyName ?: "") }
    var remark by remember { mutableStateOf(editingTransaction?.remark ?: "") }
    var category by remember { mutableStateOf(editingTransaction?.category ?: "General") }
    var paymentMode by remember { mutableStateOf(editingTransaction?.paymentMode ?: "Cash") }
    var selectedTimestamp by remember { mutableLongStateOf(editingTransaction?.transactionTimestamp ?: System.currentTimeMillis()) }

    val themeColor = if (type == TransactionType.INCOME) EmeraldGreen else CardinalRed
    val themeBg = if (type == TransactionType.INCOME) EmeraldLight else CardinalLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (type == TransactionType.INCOME) "Add Cash In Entry" else "Add Cash Out Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Settings, contentDescription = null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Date & Time Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateTimePickerField(
                    label = "Date",
                    value = DateTimeUtils.formatTransactionTime(selectedTimestamp).split(", ")[0],
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                        DatePickerDialog(context, { _, y, m, d ->
                            c.set(y, m, d)
                            selectedTimestamp = c.timeInMillis
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                    }
                )
                DateTimePickerField(
                    label = "Time",
                    value = DateTimeUtils.formatTransactionTime(selectedTimestamp).split(", ").last(),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                        TimePickerDialog(context, { _, h, min ->
                            c.set(Calendar.HOUR_OF_DAY, h)
                            c.set(Calendar.MINUTE, min)
                            selectedTimestamp = c.timeInMillis
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("₹", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = themeColor)
                    Spacer(Modifier.width(12.dp))
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("0", fontSize = 28.sp, color = SlateGray.copy(alpha = 0.3f)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = themeColor)
                    )
                    IconButton(onClick = {}) { Icon(Icons.Default.Calculate, contentDescription = null, tint = themeColor) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fields
            EntryTextField(label = "Party (Customer/Supplier)", value = partyName, onValueChange = { partyName = it }, icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(16.dp))
            EntryTextField(label = "Remark", value = remark, onValueChange = { remark = it }, icon = Icons.Default.Mic)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Attach image or PDF")
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Payment mode", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SlateGray)
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cash", "Online").forEach { mode ->
                    FilterChip(
                        selected = paymentMode == mode,
                        onClick = { paymentMode = mode },
                        label = { Text(mode) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                AssistChip(onClick = {}, label = { Text("+ Add new") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { /* Save & Add New */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & add new")
                }
                Button(
                    onClick = {
                        val a = amount.toDoubleOrNull() ?: 0.0
                        if (a > 0) {
                            onSave(a, partyName.ifBlank { null }, remark.ifBlank { null }, category, paymentMode, selectedTimestamp)
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DateTimePickerField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SlateGray)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Text(value, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Outline)
    }
}

@Composable
fun EntryTextField(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = { Icon(icon, contentDescription = null, tint = SlateGray) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Outline,
            focusedBorderColor = RoyalIndigo
        )
    )
}
