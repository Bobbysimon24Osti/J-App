package com.osti.juniorapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.osti.juniorapp.R

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val message = intent.extras?.getString("ERROR")

        if (message != null && message != ""){
            android.app.AlertDialog.Builder(this)
                .setTitle("Errore")
                .setMessage(message)
                .setCancelable(false)
                .create().show()

            //CIAOOOOO
        }
    }
    override fun onBackPressed() {
        //NIENTE
    }

}