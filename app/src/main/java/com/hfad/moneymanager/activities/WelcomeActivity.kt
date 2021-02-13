package com.hfad.moneymanager.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.models.CheckModel
import com.hfad.moneymanager.models.CheckModel.CheckType
import com.hfad.smstest.SMSModel
import com.hfad.smstest.TransactionModel
import kotlinx.android.synthetic.main.activity_welcome.*
import java.security.Permission
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import java.util.regex.Pattern

class WelcomeActivity : AppCompatActivity() {

    private val user = Firebase.auth.currentUser

    private val databaseRef = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "id")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        checkbox_cash.setOnCheckedChangeListener { buttonView, isChecked ->
            field_cash_start_amount.isEnabled = isChecked
        }

        checkbox_card.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!checkbox_import.isChecked)
                field_card_start_amount.isEnabled = isChecked
            checkbox_import.isEnabled = isChecked
        }

        checkbox_import.setOnCheckedChangeListener { buttonView, isChecked ->
            field_card_start_amount.isEnabled = !isChecked
        }

        btn_welcome_accept.setOnClickListener {
            if (checkbox_cash.isChecked) {
                databaseRef.child("checks").push().setValue(CheckModel(
                        getString(R.string.cash_title),
                        CheckType.Cash, null,
                        field_cash_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 0) {
                databaseRef.child("checks").push().setValue(CheckModel(
                        getString(R.string.sber_card_title),
                        CheckType.SberCard, null,
                        field_card_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 1) {
                databaseRef.child("checks").push().setValue(CheckModel(
                        getString(R.string.card_title),
                        CheckType.Card, null,
                        field_card_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_import.isChecked && checkbox_import.isEnabled) {
                val permStatus = ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_SMS)
                if (permStatus == PackageManager.PERMISSION_GRANTED) {
                    databaseRef.child("transactions").setValue(getSmsTransactions())
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                else ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_SMS), 100)
            }
            else startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)
            databaseRef.child("transactions").setValue(getSmsTransactions())
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private fun getSmsTransactions(): List<TransactionModel> {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, null, null, null)
        var smsList = mutableListOf<SMSModel>()
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
        val transactions = mutableListOf<TransactionModel>()
        val regex = "ECMC(\\d{4}) (\\d{2}:\\d{2}) Покупка (\\d+|\\d+.\\d+)р (.+) Баланс: (\\d+.\\d+)р"
        val pattern = Pattern.compile(regex)
        smsList = smsList.filter { it.address == "900" &&
                it.body?.matches(Regex(regex)) == true }.toMutableList()
        smsList.forEach {
            val matcher = pattern.matcher(it.body as CharSequence)
            matcher.find()
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            transactions.add(
                    TransactionModel(
                            matcher.group(1) ?: "nf",
                            matcher.group(2) ?: "nf",
                            matcher.group(3)?.toDoubleOrNull() ?: 0.0,
                            matcher.group(4) ?: "nf",
                            matcher.group(5)?.toDoubleOrNull() ?: 0.0,
                            sdf.format(Date(it.date?.toLong() ?: 1L))))
        }
        return transactions
    }
}