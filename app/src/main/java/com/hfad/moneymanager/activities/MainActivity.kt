package com.hfad.moneymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.UserData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            delay(2000)
            if (Firebase.auth.currentUser != null) {
                App.userData = UserData()
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            }
            else
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
        }
    }
}