package com.cashmath.app.export

import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.service.DuesEmiReportGenerator as ServiceGenerator
import com.cashmath.app.ui.DuesEmiUiState
import java.io.File

object DuesEmiReportGenerator {
    fun generatePdf(
        businessName: String,
        dueParties: List<DuePartyEntity>,
        emiLoans: List<EmiLoanEntity>,
        uiState: DuesEmiUiState,
        file: File
    ): File {
        return ServiceGenerator.generatePdf(
            businessName = businessName,
            dueParties = dueParties,
            emiLoans = emiLoans,
            uiState = uiState,
            file = file
        )
    }

    fun generateCsv(
        dueParties: List<DuePartyEntity>,
        emiLoans: List<EmiLoanEntity>,
        file: File
    ): File {
        return ServiceGenerator.generateCsv(
            dueParties = dueParties,
            emiLoans = emiLoans,
            file = file
        )
    }
}
