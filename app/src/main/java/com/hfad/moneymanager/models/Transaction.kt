package com.hfad.moneymanager.models

data class Transaction (
        val card: String = "",
        val time: String = "",
        val timeMillis: Long = 0,
        val amount: Double = 0.0,
        val dest: String = "",
        val balance: Double = 0.0,
        val date: String = "",
        var category: String? = null)