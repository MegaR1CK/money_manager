package com.hfad.moneymanager.models

class CheckModel (val name: String,
                  val type: CheckType,
                  val number: String?,
                  val balance: Double) {

    enum class CheckType {
        SberCard, Card, Cash
    }
}