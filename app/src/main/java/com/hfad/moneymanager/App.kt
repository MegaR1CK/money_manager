package com.hfad.moneymanager

import android.app.AlertDialog
import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        fun errorAlert(mes: String, context: Context) {
            AlertDialog.Builder(context)
                .setTitle(R.string.error_title)
                .setMessage(mes)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show()
        }
    }
}