package com.hfad.moneymanager.models

data class Debt (val amount: Double = 0.0,
                 val person: String = "",
                 val type: DebtType? = null,
                 val date: String? = "") {

    enum class DebtType {
        fromMe, toMe
    }
}