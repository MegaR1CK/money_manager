package com.hfad.moneymanager.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.models.Check
import com.hfad.moneymanager.models.DataCheckResponse
import kotlinx.android.synthetic.main.activity_add_check.*

class AddCheckActivity : AppCompatActivity() {
    //TODO: label
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_check)
        setSupportActionBar(add_check_toolbar)

        spinner_check_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 1) {
                    check_number_hint.visibility = View.VISIBLE
                    field_check_number.visibility = View.VISIBLE
                    checkbox_check_import.visibility = View.VISIBLE
                }
                else {
                    check_number_hint.visibility = View.GONE
                    field_check_number.visibility = View.GONE
                    checkbox_check_import.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val database = Firebase.database.reference
            .child("users")
            .child(Firebase.auth.currentUser?.uid ?: "")
            .child("checks")

        val name = field_check_name.text.toString()
        val balance = field_start_amount.text.toString()

        val checkResp = checkData(name, balance)
        if (checkResp.isSuccessful) {
            val pos = spinner_check_type.selectedItemPosition
            if (pos == 0 || pos == 2) {
                val newRef = database.push()
                newRef.setValue(Check(
                        newRef.key ?: "",
                        field_check_name.text.toString(),
                        if (pos == 0) Check.CheckType.Cash
                        else Check.CheckType.Card,
                        balance = balance.toDouble()
                ))
                App.userData?.getChecks(this) { finish() }
            }
            else {
                val number = field_check_number.text.toString()
                val newRef = database.push()
                newRef.setValue(Check(
                        newRef.key ?: "",
                        field_check_name.text.toString(),
                        Check.CheckType.SberCard,
                        if (number.isNotBlank() && number.length == 4) number else null,
                        balance.toDouble(),
                        checkbox_check_import.isChecked
                ))
                App.userData?.getChecks(this) { finish() }
            }
        }
        else App.errorAlert(checkResp.message, this)
        return super.onOptionsItemSelected(item)
    }

    private fun checkData(name: String, balance: String): DataCheckResponse {
        if (name.isBlank() || balance.isBlank())
            return DataCheckResponse(false, getString(R.string.error_empty_field))
        if (balance.toDoubleOrNull() == null)
            return DataCheckResponse(false, getString(R.string.error_incorrect_input))
        return DataCheckResponse(true, "OK")
    }
}