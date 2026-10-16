package com.desimeals.app

import com.desimeals.app.data.db.CustomerTransactionEntity
import com.desimeals.app.util.Accounting
import org.junit.Assert.*
import org.junit.Test

class AccountingTest {
    private fun tx(debit: Double = 0.0, credit: Double = 0.0, cat: String = "") =
        CustomerTransactionEntity(customerId = 1, type = if (debit > 0) "DEBIT" else "CREDIT",
            category = cat, description = cat, debit = debit, credit = credit)

    @Test fun balanceFormula() {
        val txs = listOf(tx(debit = 6000.0, cat = "meal_charge"), tx(credit = 2000.0, cat = "payment"))
        assertEquals(4500.0, Accounting.customerBalance(500.0, txs), 0.001) // 500+6000-2000=4500
    }

    @Test fun balanceFormulaExact() {
        val txs = listOf(tx(debit = 6000.0, cat = "meal_charge"), tx(credit = 2000.0, cat = "payment"))
        // opening 500 + 6000 - 2000 = 4500
        assertEquals(4500.0, Accounting.customerBalance(500.0, txs), 0.001)
    }

    @Test fun advanceIsNegative() {
        val txs = listOf(tx(credit = 7000.0, cat = "advance"))
        val b = Accounting.customerBalance(0.0, txs)
        assertTrue(b < 0)
        assertEquals("Advance Balance", Accounting.balanceLabel(b))
    }

    @Test fun dueLabel() {
        assertEquals("Amount Due", Accounting.balanceLabel(100.0))
        assertEquals("Settled", Accounting.balanceLabel(0.0))
    }

    @Test fun runningBalances() {
        val txs = listOf(tx(debit = 100.0), tx(credit = 40.0))
        assertEquals(listOf(100.0, 60.0), Accounting.runningBalances(0.0, txs))
    }

    @Test fun profit() {
        assertEquals(3000.0, Accounting.profit(10000.0, 7000.0), 0.001)
    }

    @Test fun supplierOutstanding() {
        assertEquals(2500.0, Accounting.supplierOutstanding(5000.0, 500.0, 3000.0), 0.001)
    }

    @Test fun invoiceTotal() {
        assertEquals(6500.0, Accounting.invoiceTotal(6000.0, 1000.0, 500.0), 0.001)
    }

    @Test fun paymentsDoNotDoubleCountIncome() {
        // income = debits + otherIncome; payments excluded
        val income = Accounting.recognizedIncome(customerDebits = 6000.0, cashbookOtherIncome = 500.0)
        assertEquals(6500.0, income, 0.001)
    }
}
