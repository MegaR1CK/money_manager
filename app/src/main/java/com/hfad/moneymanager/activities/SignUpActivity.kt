package com.hfad.moneymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btn_sign_up.setOnClickListener {
            val mail = field_mail_reg.text.toString()
            val password = field_password_reg.text.toString()
            val passwordConf = field_password_conf_reg.text.toString()
            if (mail.isNotBlank() && password.isNotBlank() && passwordConf.isNotBlank()) {
                if (password == passwordConf) {
                    Firebase.auth.createUserWithEmailAndPassword(mail, password)
                        .addOnSuccessListener {
                            startActivity(Intent(this, WelcomeActivity::class.java))
                        }
                        .addOnFailureListener {
                            it.message?.let { it1 -> App.errorAlert(it1, this) }
                        }
                }
                else App.errorAlert(getString(R.string.error_conf_password), this)
            }
            else App.errorAlert(getString(R.string.error_empty_field), this)
        }

        btn_to_sign_in.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}