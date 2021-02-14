package com.hfad.moneymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hfad.moneymanager.R
import com.hfad.moneymanager.models.DebtModel
import kotlinx.android.synthetic.main.item_debt.view.*

class DebtAdapter (private val debts: List<DebtModel>) : RecyclerView.Adapter<DebtAdapter.DebtHolder>() {

    inner class DebtHolder(val container: ConstraintLayout) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtHolder {
        return DebtHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_debt, parent,
                false) as ConstraintLayout)
    }

    override fun onBindViewHolder(holder: DebtHolder, position: Int) {
        val view = holder.container
        val debt = debts[position]
        view.debt_name.text =
                if (debt.type == DebtModel.DebtType.fromMe)
                    String.format(view.context.getString(R.string.debt_name_fromMe), debt.person)
                else String.format(view.context.getString(R.string.debt_name_toMe), debt.person)
        view.debt_amount.text = String.format(view.context.getString(R.string.amount), debt.amount)
        view.debt_date.text = debt.date
    }

    override fun getItemCount() = debts.size
}