package com.cashmath.app.ui.screens.dues

import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.cashmath.app.ui.theme.LocalThemeIsDark
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportExportBottomSheet(
    reportTitle: String,
    pdfFileProvider: () -> File,
    csvFileProvider: () -> File,
    onDismiss: () -> Unit,
    onOpenConsolidatedMasterReport: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val isDark = LocalThemeIsDark.current

    val sheetBg = if (isDark) Color(0xFF131B2E) else Color.White
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldAccent = if (isDark) Color(0xFF10B981) else Color(0xFF059669)
    val royalBlue = Color(0xFF3B82F6)
    val amberGold = Color(0xFFD97706)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        contentColor = titleText,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFCBD5E1)) }
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
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
                        text = reportTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleText
                    )
                    Text(
                        text = "CashMath - Smart Digital Ledger Export",
                        fontSize = 12.sp,
                        color = mutedText
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(cardBg, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = titleText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action 1: Download PDF to Downloads Folder
            ExportOptionTile(
                icon = Icons.Default.Download,
                iconColor = emeraldAccent,
                title = "Download PDF Statement",
                subtitle = "Save directly to device Downloads folder",
                cardBg = cardBg,
                cardBorder = cardBorder,
                titleText = titleText,
                mutedText = mutedText,
                onClick = {
                    try {
                        val pdfFile = pdfFileProvider()
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        downloadsDir.mkdirs()
                        val targetFile = File(downloadsDir, pdfFile.name)
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Toast.makeText(context, "Saved to Downloads: ${targetFile.name}", Toast.LENGTH_LONG).show()
                        onDismiss()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action 2: Share PDF via WhatsApp / Email
            ExportOptionTile(
                icon = Icons.Default.Share,
                iconColor = royalBlue,
                title = "Share PDF via WhatsApp / Email",
                subtitle = "Send statement to clients, partners or CAs",
                cardBg = cardBg,
                cardBorder = cardBorder,
                titleText = titleText,
                mutedText = mutedText,
                onClick = {
                    try {
                        val pdfFile = pdfFileProvider()
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            pdfFile
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share $reportTitle"))
                        onDismiss()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action 3: Export Excel (CSV) Spreadsheet
            ExportOptionTile(
                icon = Icons.Default.TableChart,
                iconColor = amberGold,
                title = "Export Excel Spreadsheet (.csv)",
                subtitle = "Download raw tabular data for accounting & Excel",
                cardBg = cardBg,
                cardBorder = cardBorder,
                titleText = titleText,
                mutedText = mutedText,
                onClick = {
                    try {
                        val csvFile = csvFileProvider()
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        downloadsDir.mkdirs()
                        val targetFile = File(downloadsDir, csvFile.name)
                        FileInputStream(csvFile).use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Toast.makeText(context, "Saved to Downloads: ${targetFile.name}", Toast.LENGTH_LONG).show()
                        onDismiss()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error exporting CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            if (onOpenConsolidatedMasterReport != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        onDismiss()
                        onOpenConsolidatedMasterReport()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = emeraldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export Consolidated Master Report Instead",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = emeraldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExportOptionTile(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    cardBg: Color,
    cardBorder: Color,
    titleText: Color,
    mutedText: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleText
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = mutedText
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = mutedText,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
