package com.hfad.moneymanager

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.models.Check
import com.hfad.moneymanager.models.Debt

class UserData {

    private val user = Firebase.auth.currentUser
    private val userDbRef = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "")

    var checks: List<Check>? = null
    var debts: List<Debt>? = null

    fun getChecks(context: Context, operation: () -> Unit) {
        userDbRef.child("checks").get()
                .addOnSuccessListener { it1 ->
                    val response = mutableListOf<Check>()
                    it1.children.forEach { response.add(it.getValue<Check>()!!) }
                    checks = response
                    operation.invoke()
                }
                .addOnFailureListener {
                    it.message?.let { it1 -> App.errorAlert(it1, context) }
                }
    }

    fun getDebts(context: Context, operation: () -> Unit) {
        userDbRef.child("debts").get()
                .addOnSuccessListener { it1 ->
                    val response = mutableListOf<Debt>()
                    it1.children.forEach { response.add(it.getValue<Debt>()!!) }
                    debts = response
                    operation.invoke()
                }
                .addOnFailureListener {
                    it.message?.let { it1 -> App.errorAlert(it1, context) }
                }
    }
}