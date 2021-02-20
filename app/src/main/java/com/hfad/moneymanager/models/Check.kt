package com.hfad.moneymanager.models

data class Check (
        val id: String = "",
        val name: String = "",
        val type: CheckType = CheckType.Cash,
        val number: String? = null,
        val balance: Double = 0.0,
        val allowImport: Boolean = false) {

    enum class CheckType {
        SberCard, Card, Cash
    }
}