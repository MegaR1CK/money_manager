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
import kotlinx.android.synthetic.main.activity_edit_check.*
import kotlin.random.Random

class EditCheckActivity : AppCompatActivity() {

    companion object { const val CHECK_POS = "check_pos" }

    private var check: Check? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_check)
        setSupportActionBar(add_check_toolbar)

        check = intent?.extras?.getInt(CHECK_POS)?.let { App.userData?.checks?.get(it) }

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

        check?.let {
            field_check_name.setText(it.name)
            field_check_number.setText(it.number)
            field_start_amount.setText(it.balance.toString())
            spinner_check_type.setSelection(when (it.type) {
                Check.CheckType.Cash -> 0
                Check.CheckType.SberCard -> 1
                Check.CheckType.Card -> 2
            })
            checkbox_check_import.isChecked = it.allowImport
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
                val checkRef =
                        if (check != null) database.child(check?.id ?: "")
                        else database.push()
                checkRef.setValue(Check(
                        checkRef.key ?: "",
                        field_check_name.text.toString(),
                        if (pos == 0) Check.CheckType.Cash
                        else Check.CheckType.Card,
                        Random.nextInt(1000, 9999).toString(),
                        balance.toDouble()
                ))
                App.userData?.getChecks(this) { finish() }
            }
            else {
                val number = field_check_number.text.toString()
                if (number.length == 4) {
                    val checkRef =
                            if (check != null) database.child(check?.id ?: "")
                            else database.push()
                    checkRef.setValue(Check(
                            checkRef.key ?: "",
                            field_check_name.text.toString(),
                            Check.CheckType.SberCard,
                            number,
                            balance.toDouble(),
                            checkbox_check_import.isChecked
                    ))
                    App.userData?.getChecks(this) { finish() }
                }
                else App.errorAlert(getString(R.string.error_incorrect_input), this)
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