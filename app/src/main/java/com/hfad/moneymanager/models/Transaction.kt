package com.hfad.moneymanager.models

data class Transaction (
        val card: String,
        val time: String,
        val amount: Double,
        val dest: String,
        val balance: Double,
        val date: String,
        var category: String? = null)