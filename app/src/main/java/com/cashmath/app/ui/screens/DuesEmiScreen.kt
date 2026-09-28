package com.cashmath.app.ui.screens

import androidx.compose.runtime.Composable
import com.cashmath.app.data.BusinessEntity
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.ui.DuesEmiUiState
import com.cashmath.app.ui.screens.dues.DuesTrackerScreen
import java.util.Locale

fun formatIndianCurrency(amount: Double): String {
    return try {
        String.format(Locale("en", "IN"), "₹ %,.2f", amount)
    } catch (_: Exception) {
        "₹ ${"%.2f".format(amount)}"
    }
}

@Composable
fun DuesEmiScreen(
    businessName: String = "CashMath Business",
    businesses: List<BusinessEntity> = emptyList(),
    selectedBusinessId: Long? = null,
    onBusinessSelected: (Long) -> Unit = {},
    onAddBusiness: (String) -> Unit = {},
    onRenameBusiness: (Long, String) -> Unit = { _, _ -> },
    onDeleteBusiness: (BusinessEntity) -> Unit = {},
    dueParties: List<DuePartyEntity>,
    totalToReceive: Double = 0.0,
    totalToPay: Double = 0.0,
    emiLoans: List<EmiLoanEntity> = emptyList(),
    totalMonthlyEmi: Double = 0.0,
    uiState: DuesEmiUiState = DuesEmiUiState(
        totalToReceive = totalToReceive,
        totalToPay = totalToPay,
        activeMonthlyEmiTotal = totalMonthlyEmi,
    ),
    onAddParty: (name: String, phone: String?, toReceive: Double, toPay: Double) -> Unit,
    onEditParty: (DuePartyEntity) -> Unit = {},
    onDeleteParty: (Long) -> Unit,
    onAddLoan: (loanName: String, lenderBank: String, monthlyEmi: Double, dueDay: Int, tenureMonths: Int) -> Unit,
    onEditLoan: (EmiLoanEntity) -> Unit = {},
    onIncrementPaidMonth: (Long) -> Unit,
    onDeleteLoan: (Long) -> Unit,
    onPartyClick: (Long) -> Unit = {},
    onShareDuesPdf: () -> Unit = {},
    onShareDuesCsv: () -> Unit = {},
) {
    DuesTrackerScreen(
        businessName = businessName,
        businesses = businesses,
        selectedBusinessId = selectedBusinessId,
        onBusinessSelected = onBusinessSelected,
        onAddBusiness = onAddBusiness,
        onRenameBusiness = onRenameBusiness,
        onDeleteBusiness = onDeleteBusiness,
        dueParties = dueParties,
        totalToReceive = totalToReceive,
        totalToPay = totalToPay,
        emiLoans = emiLoans,
        totalMonthlyEmi = totalMonthlyEmi,
        uiState = uiState,
        onAddParty = onAddParty,
        onEditParty = onEditParty,
        onDeleteParty = onDeleteParty,
        onAddLoan = onAddLoan,
        onEditLoan = onEditLoan,
        onIncrementPaidMonth = onIncrementPaidMonth,
        onDeleteLoan = onDeleteLoan,
        onPartyClick = onPartyClick,
        onShareDuesPdf = onShareDuesPdf,
        onShareDuesCsv = onShareDuesCsv,
    )
}
