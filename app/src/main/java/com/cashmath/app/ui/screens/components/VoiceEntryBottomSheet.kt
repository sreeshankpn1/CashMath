package com.cashmath.app.ui.screens.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.ui.screens.formatIndianCurrency
import com.cashmath.app.utils.ParsedVoiceEntry
import com.cashmath.app.utils.VoiceEntryParser
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceEntryBottomSheet(
    onDismiss: () -> Unit,
    onConfirmEntry: (amount: Double, isCashIn: Boolean, partyName: String) -> Unit
) {
    var spokenText by remember { mutableStateOf("") }
    var parsedEntry by remember { mutableStateOf<ParsedVoiceEntry?>(null) }
    var isListening by remember { mutableStateOf(true) }

    // Pulsating animation for microphone
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            spokenText = text
            parsedEntry = VoiceEntryParser.parse(text)
        }
    }

    LaunchedEffect(Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak e.g., 'Received 5000 from Ramesh'")
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            isListening = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isListening) "Listening to Voice..." else "Voice Entry Detected",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Glowing Pulsating Mic Icon
            Box(
                modifier = Modifier
                    .size((70 * (if (isListening) pulseScale else 1f)).dp)
                    .background(Color(0xFF0F2646), CircleShape)
                    .border(2.dp, Color(0xFF10B981), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (spokenText.isNotBlank()) {
                Text(
                    text = "\"$spokenText\"",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (parsedEntry != null) {
                val entry = parsedEntry!!
                val isCashIn = entry.isCashIn

                // Parsed Confirmation Tile
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, if (isCashIn) Color(0xFF10B981) else Color(0xFFEF4444))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCashIn) "🟢 CASH IN" else "🔴 CASH OUT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isCashIn) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Text(
                                text = formatIndianCurrency(entry.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (isCashIn) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Remark: ${entry.partyOrNote}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onConfirmEntry(entry.amount, entry.isCashIn, entry.partyOrNote)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm & Add to Ledger ✓", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else if (!isListening) {
                Text(
                    text = "Could not detect amount. Please try again speaking clearly.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "Speak e.g.: 'Received 5000 from Ramesh' or 'Paid 1200 for petrol'",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
