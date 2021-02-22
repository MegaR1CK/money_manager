package com.hfad.moneymanager.models

import com.hfad.moneymanager.R

data class Check (
        val id: String = "",
        val name: String = "",
        val type: CheckType = CheckType.Cash,
        val number: String = "",
        val balance: Double = 0.0,
        val allowImport: Boolean = false) {

    enum class CheckType {
        SberCard, Card, Cash
    }

    fun getCheckLogo(): Int {
        return when (type) {
            CheckType.SberCard -> R.drawable.sber_logo
            CheckType.Card -> R.drawable.card_logo
            CheckType.Cash -> R.drawable.cash_logo
        }
    }
}