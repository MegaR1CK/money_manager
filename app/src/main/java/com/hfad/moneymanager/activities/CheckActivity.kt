package com.hfad.moneymanager.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.adapters.TransactionsAdapter
import com.hfad.moneymanager.models.Transaction.TransactionType
import kotlinx.android.synthetic.main.activity_check.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class CheckActivity : AppCompatActivity() {

    companion object { const val CHECK_POS = "check_pos" }

    private val database = Firebase.database.reference
            .child("users")
            .child(Firebase.auth.currentUser?.uid ?: "")
            .child("checks")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        val checkPos = intent?.extras?.getInt(CHECK_POS)
        val check = checkPos?.let { App.userData?.checks?.get(it) }

        val incomes = App.userData?.transactions
                ?.filter { it.card == check?.number && it.type == TransactionType.Income &&
                        getPeriodInMonth(it.date) == 0 }
                ?.sumOf { it.amount }
        check_incomes.text = String.format(getString(R.string.amount), incomes)
        val expenses = App.userData?.transactions
                ?.filter { it.card == check?.number && it.type == TransactionType.Expense &&
                        getPeriodInMonth(it.date) == 0}
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

        btn_check_edit.setOnClickListener {
            startActivity(Intent(this, EditCheckActivity::class.java)
                    .putExtra(EditCheckActivity.CHECK_POS, checkPos))
        }
    }

    override fun onStart() {
        super.onStart()
        val checkPos = intent?.extras?.getInt(CHECK_POS)
        val check = checkPos?.let { App.userData?.checks?.get(it) }
        check_act_name.text = check?.name
        check_act_balance.text = String.format(getString(R.string.amount), check?.balance)
        check_act_number.text = check?.number
        check_act_logo.setImageResource(check?.getCheckLogo() ?: R.drawable.icon_cat_unknown)
    }

    private fun getPeriodInMonth(date: String): Int {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
        currentCalendar.time = Date()
        val secondCalendar = Calendar.getInstance(TimeZone.getDefault())
        secondCalendar.time = sdf.parse(date) ?: Date()
        return currentCalendar.get(Calendar.MONTH) - secondCalendar.get(Calendar.MONTH)
    }
}