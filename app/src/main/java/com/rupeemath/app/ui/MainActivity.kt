package com.rupeemath.app.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rupeemath.app.data.AppDatabase
import com.rupeemath.app.data.TransactionEntity
import com.rupeemath.app.data.TransactionType
import com.rupeemath.app.service.TransactionNotificationService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getDatabase(this).transactionDao()

        setContent {
            val transactions by dao.getAllTransactions().collectAsState(initial = emptyList())
            val totalExpense by dao.getTotalExpenses().collectAsState(initial = 0.0)
            val totalIncome by dao.getTotalIncome().collectAsState(initial = 0.0)
            val hasPermission = remember { isNotificationServiceEnabled() }

            RupeeMathScreen(
                transactions = transactions,
                totalExpense = totalExpense ?: 0.0,
                totalIncome = totalIncome ?: 0.0,
                hasPermission = hasPermission,
                onRequestPermission = { openNotificationAccessSettings() }
            )
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex())
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    private fun openNotificationAccessSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

@Composable
fun RupeeMathScreen(
    transactions: List<TransactionEntity>,
    totalExpense: Double,
    totalIncome: Double,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("RupeeMath", fontSize = 28.sp, style = MaterialTheme.typography.headlineLarge)
            Text("Smart Cashbook & Financial Guide", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasPermission) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Enable Automatic Tracking", style = MaterialTheme.typography.titleMedium)
                        Text("Allow RupeeMath to automatically record GPay, PhonePe, and Bank payments on this device.", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRequestPermission) {
                            Text("Enable Access")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Financial Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Spent (Cash Out)", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${"%.2f".format(totalExpense)}", fontSize = 20.sp, color = Color(0xFFD32F2F))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Received (Cash In)", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${"%.2f".format(totalIncome)}", fontSize = 20.sp, color = Color(0xFF388E3C))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions yet. Intercepting notifications in background...", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(transactions) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tx.merchantOrPayee, style = MaterialTheme.typography.bodyLarge)
                                Text("${tx.sourceApp} • ${tx.category}", fontSize = 12.sp, color = Color.Gray)
                            }
                            val prefix = if (tx.type == TransactionType.EXPENSE) "- ₹" else "+ ₹"
                            val color = if (tx.type == TransactionType.EXPENSE) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            Text(
                                text = "$prefix${"%.2f".format(tx.amount)}",
                                color = color,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
