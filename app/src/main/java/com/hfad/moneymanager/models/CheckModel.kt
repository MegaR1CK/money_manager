package com.hfad.moneymanager.models

data class CheckModel (val name: String = "",
                  val type: CheckType = CheckType.Cash,
                  val number: String? = null,
                  val balance: Double = 0.0) {

    enum class CheckType {
        SberCard, Card, Cash
    }
}