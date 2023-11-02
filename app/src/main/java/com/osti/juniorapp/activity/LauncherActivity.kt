package com.osti.juniorapp.activity

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

var nuoveNotifiche = false

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        //tstst()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        val i = intent.extras
        if(i!= null){
            nuoveNotifiche = true
        }
        application.onCreate()
    }

    fun tstst() {
        CoroutineScope(Dispatchers.Default).async{
            delay(5000)
            agaga()
        }
    }

    fun agaga() = runOnUiThread{
        AlertDialog.Builder(this)
            .setTitle("Attenzione")
            .setMessage("L'applicazione ci ha messo troppo tempo")
            .setPositiveButton("Ok", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}