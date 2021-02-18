package com.hfad.moneymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.UserData
import com.hfad.moneymanager.models.DataCheckResponse
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btn_sign_up.setOnClickListener {
            val mail = field_mail_reg.text.toString()
            val password = field_password_reg.text.toString()
            val passwordConf = field_password_conf_reg.text.toString()
            val checkResp = checkData(mail, password, passwordConf)
            if (checkResp.isSuccessful) {
                Firebase.auth.createUserWithEmailAndPassword(mail, password)
                        .addOnSuccessListener {
                            App.userData = UserData()
                            startActivity(Intent(this, WelcomeActivity::class.java))
                        }
                        .addOnFailureListener {
                            it.message?.let { it1 -> App.errorAlert(it1, this) }
                        }
            }
            else App.errorAlert(checkResp.message, this)
        }

        btn_to_sign_in.setOnClickListener {
            finish()
        }
    }

    private fun checkData(mail: String, password: String, passwordConf: String): DataCheckResponse {
        if (mail.isBlank() || password.isBlank() || passwordConf.isBlank())
            return DataCheckResponse(false, getString(R.string.error_empty_field))
        if (password != passwordConf)
            return DataCheckResponse(false, getString(R.string.error_conf_password))
        return DataCheckResponse(true, "OK")
    }
}