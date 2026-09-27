package com.cashmath.app.ui.components

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.parser.DateTimeUtils
import com.cashmath.app.ui.screens.formatIndianCurrency
import com.cashmath.app.util.FileStorageHelper
import java.util.Calendar
import java.util.Locale

enum class EntryMode {
    CASH_IN,
    CASH_OUT
}

enum class InputMode {
    AMOUNT,
    REMARKS
}

private val DeepObsidian = Color(0xFF0A0E17)
private val SurfaceCard = Color(0xFF141A28)
private val KeypadKeyBg = Color(0xFF192132)
private val KeypadBorder = Color(0xFF222C42)
private val NeonEmerald = Color(0xFF10B981)
private val NeonCoral = Color(0xFFEF4444)
private val MutedLabel = Color(0xFF8E9BB0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashInOutEntrySheet(
    mode: EntryMode,
    onDismiss: () -> Unit,
    onSaveEntry: (amount: Double, remarks: String, paymentMode: String, attachmentPath: String?, timestamp: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val isCashIn = mode == EntryMode.CASH_IN
    val accentColor = if (isCashIn) NeonEmerald else NeonCoral
    val badgeTitle = if (isCashIn) "• CASH IN" else "• CASH OUT"

    var showCalculatorSheet by remember { mutableStateOf(false) }
    var inputMode by remember { mutableStateOf(InputMode.AMOUNT) }
    var expression by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf("Cash") }
    var selectedAttachmentPath by remember { mutableStateOf<String?>(null) }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val paymentModes = listOf("Cash", "GPay", "PhonePe", "Paytm", "Online/UPI", "Bank Transfer", "Cheque")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = FileStorageHelper.saveAttachmentLocally(context, uri)
            selectedAttachmentPath = localPath ?: uri.toString()
        }
    }

    fun evaluate(expr: String): Double {
        if (expr.isBlank()) return 0.0
        return try {
            val tokens = expr.replace("×", "*").replace("÷", "/").split(Regex("""(?<=[+\-*/])|(?=[+\-*/])"""))
            var acc = tokens.firstOrNull()?.toDoubleOrNull() ?: 0.0
            var i = 1
            while (i < tokens.size - 1) {
                val op = tokens[i]
                val next = tokens[i + 1].toDoubleOrNull() ?: 0.0
                when (op) {
                    "+" -> acc += next
                    "-" -> acc -= next
                    "*" -> acc *= next
                    "/" -> if (next != 0.0) acc /= next
                }
                i += 2
            }
            acc
        } catch (_: Exception) {
            0.0
        }
    }

    val liveEvaluatedResult = remember(expression) { evaluate(expression) }

    fun onKeyTapped(key: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        when (key) {
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "+" -> {
                if (expression.isNotEmpty() && !expression.last().toString().matches(Regex("""[+\-×÷]"""))) {
                    expression += "+"
                }
            }
            "-" -> {
                if (expression.isNotEmpty() && !expression.last().toString().matches(Regex("""[+\-×÷]"""))) {
                    expression += "-"
                }
            }
            "×" -> {
                if (expression.isNotEmpty() && !expression.last().toString().matches(Regex("""[+\-×÷]"""))) {
                    expression += "×"
                }
            }
            "÷" -> {
                if (expression.isNotEmpty() && !expression.last().toString().matches(Regex("""[+\-×÷]"""))) {
                    expression += "÷"
                }
            }
            "." -> {
                if (!expression.endsWith(".")) {
                    expression += "."
                }
            }
            else -> {
                expression += key
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidian,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HEADER ROW: Type Badge, Date/Time Pill, Close Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = badgeTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // Clickable Date/Time Pill
                Surface(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                cal.set(Calendar.YEAR, year)
                                cal.set(Calendar.MONTH, month)
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                selectedTimestamp = cal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, KeypadBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MutedLabel, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateTimeUtils.formatTransactionTime(selectedTimestamp).split(",").firstOrNull() ?: "Today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedLabel)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. CALCULATOR-ENABLED AMOUNT DISPLAY
            Card(
                onClick = {
                    inputMode = InputMode.AMOUNT
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, if (inputMode == InputMode.AMOUNT) accentColor else KeypadBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showCalculatorSheet = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(KeypadKeyBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculator",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (expression.contains("+") || expression.contains("-") || expression.contains("×") || expression.contains("÷")) {
                            Text(
                                text = expression,
                                fontSize = 13.sp,
                                color = MutedLabel,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "AMOUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedLabel,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = formatIndianCurrency(liveEvaluatedResult),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. PAYMENT MODE SELECTOR CHIPS
            Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MutedLabel)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(paymentModes) { mode ->
                    val isSelected = selectedPaymentMode == mode
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedPaymentMode = mode
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) accentColor else SurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else KeypadBorder),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. REMARKS / NOTES INPUT
            OutlinedTextField(
                value = remarks,
                onValueChange = {
                    remarks = it
                    if (inputMode != InputMode.REMARKS) {
                        inputMode = InputMode.REMARKS
                    }
                },
                label = { Text("Remarks / Notes") },
                placeholder = { Text("e.g., Steel advance, Labour salary, Rent") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        inputMode = InputMode.REMARKS
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = KeypadBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. BILL / RECEIPT ATTACHMENT
            if (selectedAttachmentPath == null) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, KeypadBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceCard)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Bill / Photo", fontSize = 12.sp, color = Color.White)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, accentColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bill Attached ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                        IconButton(onClick = { selectedAttachmentPath = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = NeonCoral, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. 4x4 CALCULATOR KEYPAD (MUTUALLY EXCLUSIVE WITH QWERTY KEYBOARD)
            AnimatedVisibility(
                visible = inputMode == InputMode.AMOUNT,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val keypad = listOf(
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "-"),
                    listOf(".", "0", "⌫", "+")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    keypad.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { key ->
                                TactileNumpadKey(
                                    label = key,
                                    onClick = { onKeyTapped(key) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. BOTTOM ACTION BUTTON
            Button(
                onClick = {
                    if (liveEvaluatedResult > 0.0) {
                        onSaveEntry(liveEvaluatedResult, remarks.trim(), selectedPaymentMode, selectedAttachmentPath, selectedTimestamp)
                        onDismiss()
                    }
                },
                enabled = liveEvaluatedResult > 0.0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isCashIn) "Save Cash In (${formatIndianCurrency(liveEvaluatedResult)})" else "Save Cash Out (${formatIndianCurrency(liveEvaluatedResult)})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showCalculatorSheet) {
            CashInOutCalculatorBottomSheet(
                transactionType = if (isCashIn) CalculatorTransactionType.CASH_IN else CalculatorTransactionType.CASH_OUT,
                initialAmount = liveEvaluatedResult,
                onDismiss = { showCalculatorSheet = false },
                onConfirmAmount = { calculated ->
                    expression = if (calculated % 1.0 == 0.0) String.format(Locale.getDefault(), "%.0f", calculated) else String.format(
                        Locale.getDefault(), "%.2f", calculated)
                    showCalculatorSheet = false
                }
            )
        }
    }
}

@Composable
private fun TactileNumpadKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "keyScale"
    )

    val isOperator = label in listOf("+", "-", "×", "÷")

    Box(
        modifier = modifier
            .height(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOperator) Color(0xFF24324F) else KeypadKeyBg)
            .border(1.dp, KeypadBorder, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (label == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = Color(0xFFFF6B6B),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOperator) Color(0xFF5E9CFF) else Color.White
            )
        }
    }
}
