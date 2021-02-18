package com.hfad.moneymanager

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.models.Check
import com.hfad.moneymanager.models.Debt
import com.hfad.moneymanager.models.Transaction

class UserData {

    private val user = Firebase.auth.currentUser
    private val userDbRef = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "")

    var checks: List<Check>? = null
    var debts: List<Debt>? = null
    var transactions: MutableList<Transaction>? = null
    var categories: MutableMap<String, List<String>> = getTransactionCategories()

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

    fun getTransactions(context: Context, operation: () -> Unit) {
        userDbRef.child("transactions").get()
            .addOnSuccessListener { it1 ->
                val response = mutableListOf<Transaction>()
                it1.children.forEach { response.add(it.getValue<Transaction>()!!) }
                transactions = response
                operation.invoke()
            }
            .addOnFailureListener {
                it.message?.let { it1 -> App.errorAlert(it1, context) }
            }
    }

    private fun getTransactionCategories(): MutableMap<String, List<String>> {
        val databaseCat = Firebase.database.reference.child("categories")
        val categories = mutableMapOf<String, List<String>>()
        databaseCat.get().addOnSuccessListener {
            it.children.forEach { it1 ->
                categories[it1.key ?: "key"] = it1.getValue<List<String>>() ?: listOf()
            }
        }
        return categories
    }
}