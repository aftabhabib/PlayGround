package com.desimeals.app.util

import com.desimeals.app.data.db.CustomerTransactionEntity

/** Pure accounting logic — unit tested. */
object Accounting {

    /** Customer balance = opening + debits − credits. Positive = Amount Due, negative = Advance. */
    fun customerBalance(opening: Double, txs: List<CustomerTransactionEntity>): Double {
        var b = opening
        for (t in txs) b += t.debit - t.credit
        return b
    }

    fun balanceLabel(balance: Double): String = when {
        balance > 0.005 -> "Amount Due"
        balance < -0.005 -> "Advance Balance"
        else -> "Settled"
    }

    /** Running balances in chronological order. */
    fun runningBalances(opening: Double, txs: List<CustomerTransactionEntity>): List<Double> {
        val out = ArrayList<Double>(txs.size)
        var b = opening
        for (t in txs.sortedWith(compareBy({ it.timestamp }, { it.id }))) {
            b += t.debit - t.credit
            out.add(b)
        }
        return out
    }

    /**
     * Recognized business income for P&L.
     * Meal charges (debits excluding opening/adjustment-increase nuance) count as income;
     * customer payments only reduce receivables and must NOT double-count.
     * Other cashbook income (cash_sale, other_income) counts; capital_in/transfers excluded.
     */
    fun recognizedIncome(
        customerDebits: Double,
        cashbookOtherIncome: Double
    ): Double = customerDebits + cashbookOtherIncome

    fun profit(income: Double, expenses: Double): Double = income - expenses

    fun supplierOutstanding(billed: Double, opening: Double, paid: Double): Double =
        opening + billed - paid

    fun invoiceTotal(subtotal: Double, previousDue: Double, discount: Double): Double =
        subtotal + previousDue - discount
}
