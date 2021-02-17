package com.hfad.moneymanager

import android.content.Context
import android.net.Uri
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.models.SMSModel
import com.hfad.moneymanager.models.TransactionModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class SMSManager (val context: Context) {

    private val categories = initCategories()

    fun getSmsTransactions(): List<TransactionModel> {
        var smsList = getSMSList()
        val transactions = mutableListOf<TransactionModel>()
        val regex = "ECMC(\\d{4}) (\\d{2}:\\d{2}) Покупка (\\d+|\\d+.\\d+)р (.+) Баланс: (\\d+.\\d+)р"
        val pattern = Pattern.compile(regex)

        val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
        val smsCalendar = Calendar.getInstance(TimeZone.getDefault())

        smsList = smsList.filter {
            currentCalendar.timeInMillis = Date().time
            smsCalendar.timeInMillis = it.date?.toLong() ?: 0
            val difference = currentCalendar.get(Calendar.MONTH) - smsCalendar.get(Calendar.MONTH)
            it.address == "900" && it.body?.matches(Regex(regex)) == true && (difference in 0..1 ||
                    difference == -11)
        }

        smsList.forEach { sms ->
            val matcher = pattern.matcher(sms.body as CharSequence)
            matcher.find()
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            transactions.add(
                    TransactionModel(
                            matcher.group(1) ?: "nf",
                            matcher.group(2) ?: "nf",
                            matcher.group(3)?.toDoubleOrNull() ?: 0.0,
                            matcher.group(4) ?: "nf",
                            matcher.group(5)?.toDoubleOrNull() ?: 0.0,
                            sdf.format(Date(sms.date?.toLong() ?: 1L))
                    )
            )
        }
        return setCategories(transactions)
    }

    private fun getSMSList(): List<SMSModel> {
        val cursor = context.contentResolver.query(Uri.parse("content://sms/inbox"),
                null, null, null, null)
        val smsList = mutableListOf<SMSModel>()
        if (cursor?.moveToFirst() == true) {
            do {
                smsList.add(SMSModel(
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

    private fun initCategories(): MutableMap<String, List<String>> {
        val databaseCat = Firebase.database.reference.child("categories")
        val categories = mutableMapOf<String, List<String>>()
        databaseCat.get().addOnSuccessListener {
            it.children.forEach { it1 ->
                categories[it1.key ?: "key"] = it1.getValue<List<String>>() ?: listOf()
            }
        }
        return categories
    }

    private fun setCategories(transactions: List<TransactionModel>): List<TransactionModel> {
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