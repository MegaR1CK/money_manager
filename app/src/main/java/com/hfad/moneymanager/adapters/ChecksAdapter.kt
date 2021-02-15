package com.hfad.moneymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hfad.moneymanager.R
import com.hfad.moneymanager.models.CheckModel
import com.hfad.moneymanager.models.CheckModel.CheckType
import kotlinx.android.synthetic.main.item_check.view.*

class ChecksAdapter (private val checks: List<CheckModel>)
    : RecyclerView.Adapter<ChecksAdapter.CheckHolder>() {

    inner class CheckHolder (val container: ConstraintLayout) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckHolder {
        return CheckHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_check, parent,
            false) as ConstraintLayout)
    }

    override fun onBindViewHolder(holder: CheckHolder, position: Int) {
        val view = holder.container
        val check = checks[position]
        view.check_name.text =
                if (check.type == CheckType.SberCard && check.number != null)
                    String.format(view.context.getString(R.string.sber_card_with_number), check.number)
                else check.name
        view.check_balance.text = String.format(view.context.getString(R.string.amount), check.balance)
        view.check_logo.setImageResource(when (check.type) {
            CheckType.SberCard -> R.drawable.sber_logo
            CheckType.Card -> R.drawable.card_logo
            CheckType.Cash -> R.drawable.cash_logo
        })
    }

    override fun getItemCount() = checks.size
}