package com.cashmath.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.*
import com.cashmath.app.parser.DateFilterType
import com.cashmath.app.ui.components.PremiumBalanceCard
import com.cashmath.app.ui.theme.LocalThemeIsDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    bookName: String,
    categorySummaries: List<CategorySummary>,
    partySummaries: List<PartySummary>,
    paymentModeSummaries: List<PaymentModeSummary>,
    totalIn: Double,
    totalOut: Double,
    selectedFilter: DateFilterType,
    onFilterSelected: (DateFilterType) -> Unit,
    onBack: () -> Unit,
    onSharePdf: () -> Unit,
    onShareCsv: () -> Unit
) {
    val isDark = LocalThemeIsDark.current

    val screenBg = if (isDark) Color(0xFF0A0E17) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF131B2E) else Color.White
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldAccent = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Parties", "Categories", "Modes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports: $bookName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = titleText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        },
        bottomBar = {
            ReportsBottomBar(onSharePdf, onShareCsv, cardBg, cardBorder, titleText)
        },
        containerColor = screenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(screenBg)
                .padding(horizontal = 16.dp)
        ) {
            DateFilterBar(selectedFilter, onFilterSelected, isDark, emeraldAccent, cardBg, cardBorder, titleText, mutedText)
            Spacer(modifier = Modifier.height(12.dp))
            PremiumBalanceCard(totalIn - totalOut, totalIn, totalOut)
            Spacer(modifier = Modifier.height(16.dp))

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = emeraldAccent,
                edgePadding = 0.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) emeraldAccent else mutedText
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> OverviewContent(categorySummaries, cardBg, cardBorder, titleText, mutedText)
                    1 -> PartyContent(partySummaries, cardBg, cardBorder, titleText, mutedText)
                    2 -> CategoryContent(categorySummaries, totalOut, titleText, mutedText, emeraldAccent, cardBorder)
                    3 -> ModeContent(paymentModeSummaries, cardBg, cardBorder, titleText, mutedText)
                }
            }
        }
    }
}

@Composable
fun DateFilterBar(
    selected: DateFilterType,
    onSelected: (DateFilterType) -> Unit,
    isDark: Boolean,
    emeraldAccent: Color,
    cardBg: Color,
    cardBorder: Color,
    titleText: Color,
    mutedText: Color
) {
    val filters = DateFilterType.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selected == filter
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(filter) },
                label = {
                    Text(
                        text = filter.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = emeraldAccent,
                    selectedLabelColor = Color.White,
                    containerColor = cardBg,
                    labelColor = mutedText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) emeraldAccent else cardBorder
                )
            )
        }
    }
}

@Composable
fun OverviewContent(
    summaries: List<CategorySummary>,
    cardBg: Color,
    cardBorder: Color,
    titleText: Color,
    mutedText: Color
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Spending Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = mutedText)
        Spacer(modifier = Modifier.height(12.dp))
        summaries.forEach { summary ->
            ReportRow(summary.category, "₹${"%.2f".format(summary.total)}", titleText, cardBg, cardBorder, mutedText)
        }
    }
}

@Composable
fun PartyContent(
    summaries: List<PartySummary>,
    cardBg: Color,
    cardBorder: Color,
    titleText: Color,
    mutedText: Color
) {
    LazyColumn {
        items(summaries) { party ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(party.merchantOrPayee, fontWeight = FontWeight.Bold, color = titleText, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Party interactions", fontSize = 11.sp, color = mutedText)
                    }
                    val net = party.totalIn - party.totalOut
                    Text(
                        "₹${"%.2f".format(net)}",
                        color = if (net >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryContent(
    summaries: List<CategorySummary>,
    totalOut: Double,
    titleText: Color,
    mutedText: Color,
    emeraldAccent: Color,
    cardBorder: Color
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        summaries.forEach { summary ->
            val percentage = if (totalOut > 0) (summary.total / totalOut).toFloat() else 0f
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(summary.category, fontWeight = FontWeight.SemiBold, color = titleText)
                    Text("${(percentage * 100).toInt()}%", color = mutedText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = emeraldAccent,
                    trackColor = cardBorder
                )
            }
        }
    }
}

@Composable
fun ModeContent(
    summaries: List<PaymentModeSummary>,
    cardBg: Color,
    cardBorder: Color,
    titleText: Color,
    mutedText: Color
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        summaries.forEach { summary ->
            ReportRow(summary.sourceApp, "₹${"%.2f".format(summary.total)}", titleText, cardBg, cardBorder, mutedText)
        }
    }
}

@Composable
fun ReportRow(
    label: String,
    value: String,
    textColor: Color,
    cardBg: Color,
    cardBorder: Color,
    mutedText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = mutedText, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun ReportsBottomBar(
    onSharePdf: () -> Unit,
    onShareCsv: () -> Unit,
    cardBg: Color,
    cardBorder: Color,
    titleText: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBg,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onSharePdf,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Share PDF", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onShareCsv,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Export Excel", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
