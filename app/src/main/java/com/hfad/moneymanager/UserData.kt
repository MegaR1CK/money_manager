package com.hfad.moneymanager

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.models.CheckModel
import com.hfad.moneymanager.models.DebtModel

class UserData {

    private val user = Firebase.auth.currentUser
    private val userDbRef = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "")

    var checks: List<CheckModel>? = null
    var debts: List<DebtModel>? = null

    fun getChecks(operation: () -> Unit) {
        userDbRef.child("checks").get().addOnSuccessListener { it1 ->
            val response = mutableListOf<CheckModel>()
            it1.children.forEach { response.add(it.getValue<CheckModel>()!!) }
            checks = response
            operation.invoke()
        }
    }

    fun getDebts(operation: () -> Unit) {
        userDbRef.child("debts").get().addOnSuccessListener { it1 ->
            val response = mutableListOf<DebtModel>()
            it1.children.forEach { response.add(it.getValue<DebtModel>()!!) }
            debts = response
            operation.invoke()
        }
    }
}