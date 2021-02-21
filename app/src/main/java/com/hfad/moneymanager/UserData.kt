package com.hfad.moneymanager

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.models.Check
import com.hfad.moneymanager.models.Debt
import com.hfad.moneymanager.models.Transaction
import java.util.*

class UserData {

    private val user = Firebase.auth.currentUser
    private val userDbRef = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "")

    var checks: List<Check>? = null
    var debts: List<Debt>? = null
    var transactions: MutableList<Transaction>? = null
    var categories: MutableMap<String, List<String>>? = null

    fun initUserData(context: Context, operation1: (() -> Unit)? = null,
                     operation2: (() -> Unit)? = null) {
        val getLastTransactions = { getLastTransactions(context) { operation1?.invoke() } }
        val getTransactions = { getTransactions(context) { getLastTransactions.invoke() } }

        getTransactionCategories(context) {
            getChecks(context) { getTransactions.invoke() }
            getDebts(context) { operation2?.invoke() }
        }
    }

    private fun getLastTransactions(context: Context, operation: () -> Unit) {
        val database = Firebase.database.reference
            .child("users")
            .child(Firebase.auth.currentUser?.uid ?: "")

        val smsManager = categories?.let { SMSManager(context, it) }

        database.child("lastCheckTime").get()
            .addOnSuccessListener { it ->
                val lastCheckTime = it.getValue<Long>() ?: Date().time
                val importChecks = checks?.filter { it.allowImport }?.map { it.number }
                val lastTransactions = smsManager
                    ?.getSmsTransactions(importChecks, LongRange(lastCheckTime, Date().time))
                lastTransactions?.forEach { transaction ->
                    database.child("transactions").push().setValue(transaction)
                }
                database.child("lastCheckTime").setValue(Date().time)
                App.userData?.transactions?.addAll(lastTransactions ?: listOf())
                App.userData?.transactions?.sortByDescending { it.timeMillis }
                operation.invoke()
            }
            .addOnFailureListener {
                it.message?.let { it1 -> App.errorAlert(it1, context) }
            }
    }

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

    private fun getTransactions(context: Context, operation: () -> Unit) {
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

    fun getTransactionCategories(context: Context, operation: () -> Unit) {
        val databaseCat = Firebase.database.reference.child("categories")
        val response = mutableMapOf<String, List<String>>()
        databaseCat.get()
            .addOnSuccessListener {
                it.children.forEach { it1 ->
                    response[it1.key ?: "key"] = it1.getValue<List<String>>() ?: listOf()
                }
                categories = response
                operation.invoke()
            }
            .addOnFailureListener {
                it.message?.let { it1 -> App.errorAlert(it1, context) }
            }
    }
}