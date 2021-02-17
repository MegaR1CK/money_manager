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
import com.hfad.moneymanager.models.DebtModel
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

        if (person.isNotBlank() && amount.isNotBlank()) {
            if (amount.toDoubleOrNull() != null && (date.length == 10 || date.isEmpty())) {
                database.push().setValue(DebtModel(
                    amount.toDouble(), person,
                    if (pos == 0) DebtModel.DebtType.fromMe else DebtModel.DebtType.toMe,
                    if (date.isNotBlank()) date else null
                ))
                App.userData?.getDebts(this) { finish() }
            }
            else App.errorAlert(getString(R.string.error_incorrect_input), this)
        }
        else App.errorAlert(getString(R.string.error_empty_field), this)

        return super.onOptionsItemSelected(item)
    }
}