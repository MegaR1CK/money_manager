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
import com.hfad.moneymanager.R
import com.hfad.moneymanager.SMSManager
import com.hfad.moneymanager.models.CheckModel
import com.hfad.moneymanager.models.CheckModel.CheckType
import com.hfad.moneymanager.models.TransactionModel
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    private val user = Firebase.auth.currentUser

    private val databaseUser = Firebase.database.reference
            .child("users")
            .child(user?.uid ?: "id")

    private val smsManager = SMSManager(this)

    var transactions: List<TransactionModel>? = null

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

        btn_welcome_accept.setOnClickListener {

            if (checkbox_import.isChecked) {
                val permStatus = ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_SMS)
                if (permStatus == PackageManager.PERMISSION_GRANTED) {
                    transactions = smsManager.getSmsTransactions()
                    databaseUser
                            .child("transactions")
                            .setValue(transactions)
                }
                else ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_SMS), 100)
            }

            if (checkbox_cash.isChecked) {
                databaseUser.child("checks").push().setValue(CheckModel(
                        getString(R.string.cash_title),
                        CheckType.Cash, null,
                        field_cash_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 0) {
                databaseUser.child("checks").push().setValue(CheckModel(
                        getString(R.string.sber_card_title), CheckType.SberCard,
                        transactions?.first()?.card, transactions?.first()?.balance ?: 0.0
                ))
            }

            if (checkbox_card.isChecked && spinner_card_type.selectedItemPosition == 1) {
                databaseUser.child("checks").push().setValue(CheckModel(
                        getString(R.string.card_title), CheckType.Card, null,
                        field_card_start_amount.text.toString().toDoubleOrNull() ?: 0.0
                ))
            }
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            transactions = smsManager.getSmsTransactions()
            databaseUser
                    .child("transactions")
                    .setValue(transactions)
        }
    }
}