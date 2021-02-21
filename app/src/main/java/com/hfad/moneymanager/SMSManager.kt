package com.hfad.moneymanager

import android.content.Context
import android.net.Uri
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.models.SMS
import com.hfad.moneymanager.models.Transaction
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class SMSManager (val context: Context, private val categories: MutableMap<String, List<String>>) {

    fun getSmsTransactions(numbers: List<String>? = null, range: LongRange? = null): List<Transaction> {
        var smsList = getSMSList()
        var transactions = mutableListOf<Transaction>()
        val regex = "ECMC(\\d{4}) (\\d{2}:\\d{2}) Покупка (\\d+|\\d+.\\d+)р (.+) Баланс: (\\d+.\\d+)р"
        val pattern = Pattern.compile(regex)

        val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
        val smsCalendar = Calendar.getInstance(TimeZone.getDefault())

        smsList = smsList.filter {
            currentCalendar.timeInMillis = Date().time
            smsCalendar.timeInMillis = it.date?.toLong() ?: 0
            val difference = currentCalendar.get(Calendar.MONTH) - smsCalendar.get(Calendar.MONTH)
            it.address == "900" && it.body?.matches(Regex(regex)) == true &&
                    (difference in 0..1 || difference == -11)
        }

        smsList.forEach { sms ->
            val matcher = pattern.matcher(sms.body as CharSequence)
            matcher.find()
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            transactions.add(
                    Transaction(
                            matcher.group(1) ?: "nf",
                            matcher.group(2) ?: "nf",
                            sms.date?.toLong() ?: 0,
                            matcher.group(3)?.toDoubleOrNull() ?: 0.0,
                            matcher.group(4) ?: "nf",
                            matcher.group(5)?.toDoubleOrNull() ?: 0.0,
                            sdf.format(Date(sms.date?.toLong() ?: 1L))
                    )
            )
        }
        if (numbers != null)
            transactions = transactions.filter { numbers.contains(it.card) }.toMutableList()
        if (range != null)
            transactions = transactions.filter { it.timeMillis in range}.toMutableList()
        return setCategories(transactions)
    }

    private fun getSMSList(): List<SMS> {
        val cursor = context.contentResolver.query(Uri.parse("content://sms/inbox"),
                null, null, null, null)
        val smsList = mutableListOf<SMS>()
        if (cursor?.moveToFirst() == true) {
            do {
                smsList.add(SMS(
                        cursor.getString(0),
                        cursor.getString(2),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(12),
                        cursor.getString(16),
                        cursor.getString(17)
                ))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return smsList
    }

    private fun setCategories(transactions: List<Transaction>): List<Transaction> {
        transactions.forEach lit@{ model ->
            categories.forEach { category ->
                if (category.value.contains(model.dest)) {
                    model.category = category.key
                    return@lit
                }
            }
        }
        return transactions
    }
}