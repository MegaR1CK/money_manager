package com.hfad.moneymanager.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.adapters.TransactionsAdapter
import com.hfad.moneymanager.models.Check
import kotlinx.android.synthetic.main.activity_check.*
import kotlinx.android.synthetic.main.item_check.view.*
import kotlin.math.abs

class CheckActivity : AppCompatActivity() {

    companion object { const val CHECK_POS = "check_pos" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)
        //TODO: checknumber для всех
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

        val incomes = 0.0
        check_incomes.text = String.format(getString(R.string.amount), incomes)
        val expenses = App.userData?.transactions
            ?.filter { it.card == check?.number }
            ?.sumOf { it.amount }
        check_expenses.text = String.format(getString(R.string.amount), expenses)
        val thread = incomes - (expenses ?: 0.0)
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
    }

    override fun onResume() {
        super.onResume()
        check_act_scroll.scrollTo(0,0)
    }
}