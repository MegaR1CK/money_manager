package com.hfad.moneymanager.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.fragments.ChecksFragment
import com.hfad.moneymanager.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, ChecksFragment())
            .commit()

        bottom_nav.setOnNavigationItemSelectedListener {
            val ft = supportFragmentManager.beginTransaction()
            when (it.itemId) {
                R.id.menu_checks -> ft.replace(R.id.fragment_container, ChecksFragment())
            }
            ft.commit()
            true
        }
    }

    override fun onBackPressed() {
        Firebase.auth.signOut()
        App.userData = null
        super.onBackPressed()
    }
}