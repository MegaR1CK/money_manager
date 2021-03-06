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
import java.util.regex.Matcher
import java.util.regex.Pattern

class SMSManager (val context: Context, private val categories: MutableMap<String, List<String>>) {

    fun getSmsTransactions(numbers: List<String>? = null, range: LongRange? = null): List<Transaction> {
        var smsList = getSMSList()
        var transactions = mutableListOf<Transaction>()

        val purchaseRegex = "ECMC(\\d{4}) (\\d{2}:\\d{2}) Покупка (\\d+|\\d+.\\d+)р (.+) Баланс: (\\d+.\\d+)р"
        val purchasePattern = Pattern.compile(purchaseRegex)

        val transferToUserRegex = "Перевод (\\d+)р от (.+)\nБаланс ECMC(\\d{4}): (\\d+.\\d+)р"
        val transferToUserPattern = Pattern.compile(transferToUserRegex)

        val paymentRegex = "ECMC(\\d{4}) (\\d{2}:\\d{2}) Оплата (\\d+|\\d+.\\d+)р Баланс: (\\d+.\\d+)р"
        val paymentPattern = Pattern.compile(paymentRegex)

        val transferFromUserRegex = "ECMC(\\d{4}) (\\d{2}:\\d{2}) перевод (\\d+|\\d+.\\d+)р Баланс: (\\d+.\\d+)р"
        val transferFromUserPattern = Pattern.compile(transferFromUserRegex)

        val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
        val smsCalendar = Calendar.getInstance(TimeZone.getDefault())

        smsList = smsList.filter {
            currentCalendar.timeInMillis = Date().time
            smsCalendar.timeInMillis = it.date?.toLong() ?: 0
            val difference = currentCalendar.get(Calendar.MONTH) - smsCalendar.get(Calendar.MONTH)
            it.address == "900" && (difference in 0..1 || difference == -11) &&
                    (it.body?.matches(Regex(purchaseRegex)) == true ||
                            it.body?.matches(Regex(transferToUserRegex)) == true ||
                            it.body?.matches(Regex(paymentRegex)) == true ||
                            it.body?.matches(Regex(transferFromUserRegex)) == true)
        }

        smsList.forEach { sms ->
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            if (sms.body?.matches(Regex(transferToUserRegex)) == false) {
                val matcher = when {
                    sms.body.matches(Regex(purchaseRegex)) ->
                        purchasePattern.matcher(sms.body as CharSequence)
                    sms.body.matches(Regex(paymentRegex)) ->
                        paymentPattern.matcher(sms.body as CharSequence)
                    sms.body.matches(Regex(transferFromUserRegex)) ->
                        transferFromUserPattern.matcher(sms.body as CharSequence)
                    else -> null
                }
                matcher?.find()
                transactions.add(
                        Transaction(
                                matcher?.group(1) ?: "",
                                sms.date?.toLong() ?: 0,
                                matcher?.group(3)?.toDoubleOrNull() ?: 0.0,
                                if (sms.body.matches(Regex(purchaseRegex)))
                                    matcher?.group(4) ?: ""
                                else  "",
                                if (sms.body.matches(Regex(purchaseRegex)))
                                    matcher?.group(5)?.toDoubleOrNull() ?: 0.0
                                else matcher?.group(4)?.toDoubleOrNull() ?: 0.0,
                                sdf.format(Date(sms.date?.toLong() ?: 1L)),
                                Transaction.TransactionType.Expense,
                                when {
                                    sms.body.matches(Regex(paymentRegex)) ->
                                        context.getString(R.string.cat_payment)
                                    sms.body.matches(Regex(transferFromUserRegex)) ->
                                        context.getString(R.string.cat_transfer)
                                    else -> null
                                }
                        )
                )
            }
            else {
                val matcher = transferToUserPattern.matcher(sms.body as CharSequence)
                matcher.find()
                transactions.add(
                        Transaction(
                                matcher.group(3) ?: "",
                                sms.date?.toLong() ?: 0,
                                matcher.group(1)?.toDoubleOrNull() ?: 0.0,
                                matcher.group(2) ?: "",
                                matcher.group(4)?.toDoubleOrNull() ?: 0.0,
                                sdf.format(Date(sms.date?.toLong() ?: 1L)),
                                Transaction.TransactionType.Income,
                                context.getString(R.string.cat_transfer)
                        )
                )
            }
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
        transactions.forEach lit@{ transaction ->
            if (transaction.category != null) return@lit
            categories.forEach { category ->
                if (category.value.contains(transaction.dest)) {
                    transaction.category = category.key
                    return@lit
                }
            }
        }
        return transactions
    }
}