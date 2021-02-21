package com.hfad.moneymanager.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.adapters.TransactionsAdapter
import com.hfad.moneymanager.models.Check
import com.hfad.moneymanager.models.Transaction
import com.hfad.moneymanager.models.Transaction.TransactionType
import kotlinx.android.synthetic.main.activity_check.*
import kotlin.math.abs

class CheckActivity : AppCompatActivity() {

    companion object { const val CHECK_POS = "check_pos" }

    val database = Firebase.database.reference
            .child("users")
            .child(Firebase.auth.currentUser?.uid ?: "")
            .child("checks")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)
        //TODO: виды транзакций
        val check = intent?.extras?.getInt(CHECK_POS)?.let { App.userData?.checks?.get(it) }
        check_act_name.text = check?.name
        check_act_balance.text = String.format(getString(R.string.amount), check?.balance)
        check_act_number.text = check?.number
        check_act_logo.setImageResource(when (check?.type) {
            Check.CheckType.SberCard -> R.drawable.sber_logo
            Check.CheckType.Card -> R.drawable.card_logo
            Check.CheckType.Cash -> R.drawable.cash_logo
            else -> R.drawable.icon_cat_unknown
        })

        val incomes = App.userData?.transactions
                ?.filter { it.card == check?.number && it.type == TransactionType.Income }
                ?.sumOf { it.amount }
        check_incomes.text = String.format(getString(R.string.amount), incomes)
        val expenses = App.userData?.transactions
                ?.filter { it.card == check?.number && it.type == TransactionType.Expense }
                ?.sumOf { it.amount }
        check_expenses.text = String.format(getString(R.string.amount), expenses)
        val thread = incomes?.minus((expenses ?: 0.0)) ?: 0.0
        when {
            thread > 0 -> {
                check_thread.text = String.format(getString(R.string.income_amount), thread)
                check_thread.setTextColor(ContextCompat.getColor(this,
                    android.R.color.holo_green_dark))
            }
            thread < 0 -> {
                check_thread.text = String.format(getString(R.string.expense_amount), abs(thread))
                check_thread.setTextColor(ContextCompat.getColor(this,
                    android.R.color.holo_red_dark))
            }
            else -> check_thread.text = String.format(getString(R.string.amount), thread)
        }

        recycler_check_history.layoutManager = LinearLayoutManager(this)
        recycler_check_history.adapter = App.userData?.transactions
            ?.filter { it.card == check?.number }
            ?.let { TransactionsAdapter(it) }

        btn_check_delete.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(R.string.check_delete_title)
                    .setMessage(R.string.check_delete_desc)
                    .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                        database.child(check?.id ?: "").removeValue()
                                .addOnSuccessListener {
                                    App.userData?.getChecks(this) { finish() }
                                }
                                .addOnFailureListener {
                                    it.message?.let { it1 -> App.errorAlert(it1, this) }
                                }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show()
        }
    }
}