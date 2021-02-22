package com.hfad.moneymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hfad.moneymanager.R
import com.hfad.moneymanager.models.Check
import com.hfad.moneymanager.models.Check.CheckType
import com.hfad.moneymanager.models.Transaction
import kotlinx.android.synthetic.main.item_check.view.*

class ChecksAdapter (private val checks: List<Check>, private var transactions: List<Transaction>)
    : RecyclerView.Adapter<ChecksAdapter.CheckHolder>() {

    inner class CheckHolder (val container: ConstraintLayout) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckHolder {
        return CheckHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_check, parent,
            false) as ConstraintLayout)
    }

    override fun onBindViewHolder(holder: CheckHolder, position: Int) {
        val view = holder.container
        val check = checks[position]
        if (check.type == CheckType.SberCard && check.allowImport) {
            transactions = transactions.filter { it.card == check.number }
            view.check_balance.text = String.format(view.context.getString(R.string.amount),
                    transactions.first().balance)
        }
        else view.check_balance.text = String.format(view.context.getString(R.string.amount),
                check.balance)
        view.check_name.text = check.name
        view.check_logo.setImageResource(check.getCheckLogo())
        view.setOnClickListener {
            onCheckClickListener.onCheckClick(position)
        }
    }

    override fun getItemCount() = checks.size

    lateinit var onCheckClickListener: CheckClickListener

    interface CheckClickListener {
        fun onCheckClick(position: Int)
    }
}