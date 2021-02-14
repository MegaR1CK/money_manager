package com.hfad.moneymanager.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.R
import com.hfad.moneymanager.SMSManager
import com.hfad.moneymanager.models.CheckModel
import com.hfad.moneymanager.models.CheckModel.CheckType
import com.hfad.moneymanager.models.SMSModel
import com.hfad.moneymanager.models.TransactionModel
import kotlinx.android.synthetic.main.activity_welcome.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class WelcomeActivity : AppCompatActivity() {

    private val user = Firebase.auth.currentUser

    private val databaseUser = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "id")

    private val smsManager = SMSManager(this)

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
                databaseUser.child("checks").push().setValue(CheckModel(
                        getString(R.string.cash_title),
                        CheckType.Cash, null,
                        field_cash_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 0) {
                databaseUser.child("checks").push().setValue(CheckModel(
                        getString(R.string.sber_card_title),
                        CheckType.SberCard, null,
                        field_card_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 1) {
                databaseUser.child("checks").push().setValue(CheckModel(
                        getString(R.string.card_title),
                        CheckType.Card, null,
                        field_card_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_import.isChecked && checkbox_import.isEnabled) {
                val permStatus = ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_SMS)
                if (permStatus == PackageManager.PERMISSION_GRANTED) {
                    databaseUser
                            .child("transactions")
                            .setValue(smsManager.getSmsTransactions())
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
            databaseUser.child("transactions").setValue(smsManager.getSmsTransactions())
        startActivity(Intent(this, HomeActivity::class.java))
    }
}