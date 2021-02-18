package com.hfad.moneymanager.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.fragments.ChecksFragment
import com.hfad.moneymanager.fragments.TransactionsFragment
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, ChecksFragment())
            .commit()

        bottom_nav.setOnNavigationItemSelectedListener {
            val ft = supportFragmentManager.beginTransaction()
            when (it.itemId) {
                R.id.menu_checks -> ft.replace(R.id.fragment_container, ChecksFragment())
                R.id.menu_transactions -> ft.replace(R.id.fragment_container, TransactionsFragment())
            }
            ft.commit()
            true
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setTitle(R.string.signout_title)
                .setMessage(R.string.signout_desc)
                .setPositiveButton(R.string.yes) { dialog, which ->
                    Firebase.auth.signOut()
                    App.userData = null
                    startActivity(Intent(this, SignInActivity::class.java))
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }
}