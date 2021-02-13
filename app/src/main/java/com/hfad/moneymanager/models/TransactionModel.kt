package com.hfad.smstest

data class TransactionModel (
    val card: String,
    val time: String,
    val amount: Double,
    val dest: String,
    val balance: Double,
    val date: String)