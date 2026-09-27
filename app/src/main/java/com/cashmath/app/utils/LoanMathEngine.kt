package com.cashmath.app.utils

import kotlin.math.pow

object LoanMathEngine {

    /**
     * Derives the Annual Interest Rate (% p.a.) given Principal, Monthly EMI, and Tenure in Months.
     * Uses binary search over the standard annuity EMI formula:
     * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     */
    fun calculateAnnualInterestRate(
        principal: Double,
        monthlyEmi: Double,
        tenureMonths: Int
    ): Double {
        if (principal <= 0.0 || monthlyEmi <= 0.0 || tenureMonths <= 0) return 0.0
        val totalPayment = monthlyEmi * tenureMonths
        if (totalPayment <= principal + 1.0) return 0.0 // No-Cost EMI / 0% Interest

        var lowRate = 0.00001
        var highRate = 0.10
        var bestMonthlyRate = 0.0

        for (i in 0..60) {
            val midRate = (lowRate + highRate) / 2.0
            val factor = (1.0 + midRate).pow(tenureMonths)
            val emiCalculated = (principal * midRate * factor) / (factor - 1.0)

            if (emiCalculated < monthlyEmi) {
                lowRate = midRate
            } else {
                highRate = midRate
            }
            bestMonthlyRate = midRate
        }

        val annualRatePercent = bestMonthlyRate * 12.0 * 100.0
        return if (annualRatePercent.isNaN() || annualRatePercent.isInfinite()) 0.0 else (Math.round(annualRatePercent * 10.0) / 10.0)
    }

    /**
     * Calculates monthly EMI for a given Principal, Annual Interest Rate (% p.a.), and Tenure (Months).
     */
    fun calculateEmi(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        if (principal <= 0.0 || tenureMonths <= 0) return 0.0
        if (annualRate <= 0.0) return principal / tenureMonths

        val r = (annualRate / 100.0) / 12.0
        val factor = (1.0 + r).pow(tenureMonths)
        val emi = (principal * r * factor) / (factor - 1.0)
        return if (emi.isNaN() || emi.isInfinite()) 0.0 else emi
    }
}
