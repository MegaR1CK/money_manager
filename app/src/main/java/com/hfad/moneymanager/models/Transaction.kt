package com.hfad.moneymanager.models

data class Transaction (
        val card: String = "",
        val timeMillis: Long = 0,
        val amount: Double = 0.0,
        val dest: String = "",
        val balance: Double = 0.0,
        val date: String = "",
        val type: TransactionType? = null,
        var category: String? = null) {

    enum class TransactionType {
        Income, Expense
    }
}