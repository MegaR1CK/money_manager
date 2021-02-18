package com.hfad.moneymanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.SMSManager
import com.hfad.moneymanager.adapters.TransactionsAdapter
import kotlinx.android.synthetic.main.fragment_transactions.view.*
import java.util.*

class TransactionsFragment : Fragment() {

    private val database = Firebase.database.reference
        .child("users")
        .child(Firebase.auth.currentUser?.uid ?: "")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)
        //TODO: кнопка обновления транзакций в меню

        if (App.userData?.transactions == null)
            activity?.let { App.userData?.getTransactions(it) { sendLastTransactions() } }
        else {
            view.recycler_transactions?.layoutManager = LinearLayoutManager(activity)
            view.recycler_transactions?.adapter =
                App.userData?.transactions?.let{ TransactionsAdapter(it) }
        }
        return view
    }

    private fun sendLastTransactions() {
        val smsManager = App.userData?.categories?.let { activity?.let { it1 -> SMSManager(it1, it) } }
        database.child("lastCheckTime").get()
            .addOnSuccessListener {
                val lastCheckTime = it.getValue<Long>() ?: Date().time
                val lastTransactions = smsManager
                    ?.getSmsTransactions(range = LongRange(lastCheckTime, Date().time))
                lastTransactions?.forEach { transaction ->
                    database.child("transactions").push().setValue(transaction)
                }
                database.child("lastCheckTime").setValue(Date().time)
                App.userData?.transactions?.addAll(lastTransactions ?: listOf())
                App.userData?.transactions?.sortByDescending { it.timeMillis }
                view?.recycler_transactions?.layoutManager = LinearLayoutManager(activity)
                view?.recycler_transactions?.adapter =
                    App.userData?.transactions?.let { TransactionsAdapter(it) }
            }
            .addOnFailureListener {
                it.message?.let { it1 -> activity?.let { it2 -> App.errorAlert(it1, it2) } }
            }
    }
}