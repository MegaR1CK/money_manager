package com.hfad.moneymanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.models.Transaction
import com.hfad.moneymanager.models.Transaction.TransactionType
import kotlinx.android.synthetic.main.item_transaction.view.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionsAdapter (private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionHolder>() {

    inner class TransactionHolder (val container: ConstraintLayout)
        : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        return TransactionHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false) as ConstraintLayout)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val view = holder.container
        val transaction = transactions[position]
        val context = view.context
        view.transaction_category.text = transaction.category ?: context.getString(R.string.cat_unknown)
        view.transaction_dest.text = transaction.dest
        view.transaction_check.text = App.userData?.checks?.find { it.number == transaction.card }?.name
        view.transaction_amount.text =
                if (transaction.type == TransactionType.TransferToUser)
                    String.format(context.getString(R.string.income_amount), transaction.amount)
                else String.format(context.getString(R.string.expense_amount), transaction.amount)
        view.transaction_date.text = dateConverter(transaction.date, context)
        view.transaction_icon.setImageResource(when (transaction.category) {
            context.getString(R.string.cat_food) -> R.drawable.icon_cat_food
            context.getString(R.string.cat_fun) -> R.drawable.icon_cat_fun
            context.getString(R.string.cat_products) -> R.drawable.icon_cat_products
            context.getString(R.string.cat_purchases) -> R.drawable.icon_cat_purchases
            context.getString(R.string.cat_transport) -> R.drawable.icon_cat_transport
            context.getString(R.string.cat_transfer) -> R.drawable.baseline_swap_horiz_24
            context.getString(R.string.cat_payment) -> R.drawable.icon_cat_payment
            else -> R.drawable.icon_cat_unknown
        })
    }

    override fun getItemCount() = transactions.size

    private fun dateConverter(date: String, context: Context): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val transactionCalendar = Calendar.getInstance(TimeZone.getDefault())
        transactionCalendar.time = sdf.parse(date) ?: Date()
        val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
        currentCalendar.time = Date()
        if (currentCalendar.get(Calendar.DAY_OF_MONTH) == transactionCalendar.get(Calendar.DAY_OF_MONTH))
            return context.getString(R.string.date_today)
        if (currentCalendar.get(Calendar.DAY_OF_MONTH) - transactionCalendar.get(Calendar.DAY_OF_MONTH) == 1)
            return context.getString(R.string.date_yesterday)
        return date
    }
}