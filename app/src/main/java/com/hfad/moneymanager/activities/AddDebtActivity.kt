package com.hfad.moneymanager.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.models.DataCheckResponse
import com.hfad.moneymanager.models.Debt
import kotlinx.android.synthetic.main.activity_add_debt.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

class AddDebtActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_debt)
        setSupportActionBar(add_debt_toolbar as Toolbar)
        val mask = MaskImpl
            .createTerminated(UnderscoreDigitSlotsParser().parseSlots("__.__.____"))
        mask.isHideHardcodedHead = true
        MaskFormatWatcher(mask).installOn(field_debt_date)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val database = Firebase.database.reference
            .child("users")
            .child(Firebase.auth.currentUser?.uid ?: "")
            .child("debts")

        val person = field_debt_person.text.toString()
        val amount = field_debt_amount.text.toString()
        val pos = spinner_debt_type.selectedItemPosition
        val date = field_debt_date.text.toString()

        val checkResp = checkData(person, amount, date)
        if (checkResp.isSuccessful) {
            database.push().setValue(Debt(
                    amount.toDouble(), person,
                    if (pos == 0) Debt.DebtType.fromMe else Debt.DebtType.toMe,
                    if (date.isNotBlank()) date else null
            ))
            App.userData?.getDebts(this) { finish() }
        }
        else App.errorAlert(checkResp.message, this)
        return super.onOptionsItemSelected(item)
    }

    private fun checkData(person: String, amount: String, date: String): DataCheckResponse {
        if (person.isBlank() || amount.isBlank())
            return DataCheckResponse(false, getString(R.string.error_empty_field))
        if (amount.toDoubleOrNull() == null || (date.length != 10 && date.isNotBlank()))
            return DataCheckResponse(false, getString(R.string.error_incorrect_input))
        return DataCheckResponse(true, "OK")
    }
}