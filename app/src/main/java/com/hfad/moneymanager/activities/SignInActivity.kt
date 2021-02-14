package com.hfad.moneymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.UserData
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val auth = Firebase.auth
        if (auth.currentUser != null) {
            App.userData = UserData()
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btn_sign_in.setOnClickListener {
            val email = field_mail.text.toString()
            val password = field_password.text.toString()
            if (email.isNotBlank() && password.isNotBlank()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        App.userData = UserData()
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    .addOnFailureListener {
                        it.message?.let { it1 -> App.errorAlert(it1, this) }
                    }
            }
            else App.errorAlert(getString(R.string.error_empty_field), this)
        }

        btn_to_sign_up.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}