package com.hfad.moneymanager.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.SMSManager
import com.hfad.moneymanager.models.Check
import com.hfad.moneymanager.models.Check.CheckType
import com.hfad.moneymanager.models.Transaction
import kotlinx.android.synthetic.main.activity_welcome.*
import java.util.*
import kotlin.random.Random

class WelcomeActivity : AppCompatActivity() {

    private val user = Firebase.auth.currentUser

    private val databaseUser = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "id")

    var transactions: List<Transaction>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        checkbox_cash.setOnCheckedChangeListener { buttonView, isChecked ->
            layout_cash_settings.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        checkbox_card.setOnCheckedChangeListener { buttonView, isChecked ->
            layout_card_settings.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        checkbox_import.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                card_start_amount_title.visibility = View.GONE
                field_card_start_amount.visibility = View.GONE
                card_rub.visibility = View.GONE
            }
            else {
                card_start_amount_title.visibility = View.VISIBLE
                field_card_start_amount.visibility = View.VISIBLE
                card_rub.visibility = View.VISIBLE
            }
        }

        spinner_card_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0)
                    checkbox_import.visibility = View.VISIBLE
                else {
                    checkbox_import.visibility = View.GONE
                    field_card_start_amount.visibility = View.VISIBLE
                    card_start_amount_title.visibility = View.VISIBLE
                    card_rub.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        App.userData?.getTransactionCategories(this) {}

        btn_welcome_accept.setOnClickListener {
            var success = true
            val smsManager = App.userData?.categories?.let { it1 -> SMSManager(this, it1) }
            if (checkbox_import.isChecked) {
                val permStatus = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_SMS)
                if (permStatus == PackageManager.PERMISSION_GRANTED)
                    transactions = smsManager?.getSmsTransactions()
                else ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_SMS), 100)
            }

            if (checkbox_cash.isChecked) {
                val newRef = databaseUser.child("checks").push()
                newRef.setValue(Check(
                        newRef.key ?: "",
                        getString(R.string.cash_title),
                        CheckType.Cash,
                        Random.nextInt(1000, 9999).toString(),
                        field_cash_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 0) {
                val number = field_card_number.text.toString()
                if ((number.isNotBlank() && number.length == 4) || number.isBlank()) {
                    transactions = transactions?.filter { it.card == number }
                    val newRef = databaseUser.child("checks").push()
                    newRef.setValue(Check(
                            newRef.key ?: "",
                            getString(R.string.sber_card_title),
                            CheckType.SberCard,
                            number,
                            transactions?.first()?.balance ?: 0.0,
                            checkbox_import.isChecked
                    ))
                    transactions?.let {
                        databaseUser
                            .child("transactions")
                            .setValue(transactions)
                        databaseUser
                            .child("lastCheckTime")
                            .setValue(Date().time)
                    }
                }
                else success = false
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 1) {
                val newRef = databaseUser.child("checks").push()
                newRef.setValue(Check(
                        newRef.key ?: "",
                        getString(R.string.card_title), CheckType.Card,
                        Random.nextInt(1000, 9999).toString(),
                        field_card_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }
            if (success) startActivity(Intent(this, HomeActivity::class.java))
            else App.errorAlert(getString(R.string.error_incorrect_input), this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        val smsManager = App.userData?.categories?.let { it1 -> SMSManager(this, it1) }
        if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)
            transactions = smsManager?.getSmsTransactions()
    }
}