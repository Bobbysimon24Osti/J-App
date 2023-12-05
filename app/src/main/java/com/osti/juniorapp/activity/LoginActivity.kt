package com.osti.juniorapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonElement
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUserOld
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.thread.RiceviDatiThread
import com.osti.juniorapp.utils.LogController
import org.json.JSONObject
import retrofit2.Response
import java.beans.PropertyChangeListener

class LoginActivity : AppCompatActivity() {

    val log = LogController(LogController.LOGIN)

    lateinit var accessButton : Button
    lateinit var userView: TextInputEditText
    lateinit var pswView: TextInputEditText
    lateinit var archivioView: TextInputEditText
    lateinit var progressBar: ProgressBar
    lateinit var clearPsw: ImageView
    lateinit var textViewUrl: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        runOnUiThread{
            init()
            if (intent.extras?.getString("ERROR") == "ERROR-PARAM"){
                AlertDialog.Builder(this)
                    .setTitle("Errore")
                    .setMessage(R.string.alert_errore_connessione_server_attivazioni)
                    .setPositiveButton("Ok", null)
                    .show()
            }
        }
    }

    private fun accessButtonListener (v:View) {
        val tmp = userView.text.toString()
        val tmp2 = pswView.text.toString()
        if (tmp.isNotBlank() && tmp2.isNotBlank()){
            if (archivioView.text.toString().isBlank()){
                archivioView.setText("juniorweb")
            }
            if(JuniorApplication.setApiCliente()){
                progressBar.visibility = View.VISIBLE
                NetworkController.firstLogin(userView.text.toString(), pswView.text.toString(),firstLoginObserver, archivioView.text.toString())
            }
        }
        else{
            AlertDialog.Builder(this)
                .setTitle("Errore")
                .setMessage(R.string.alert_compila_tutti_campi)
                .setPositiveButton("Ok", null)
                .show()
        }
    }


    private val firstLoginObserver = PropertyChangeListener{ p0 ->
        progressBar.visibility = View.GONE
        val response = p0.oldValue
        if(response is Response<*> && response.errorBody()== null && (response as Response<JsonElement>).errorBody()?.byteString() == null){//Veridica che la risposta sia arrivata correttamente e che non ci siano stati errori
            val key = response.headers()["x-user-key"]!!
            val serverid = response.headers()["x-user-id"]!!
            JuniorApplication.myKeystore.setKey(key, this)
            JuniorApplication.myJuniorUser.value = JuniorUserOld(serverid, key, "null", "null", "null", "null", "null", "null", "null", null)

            log.insertLog("Primo Login eseguito con successo")

            progressBar.visibility = View.VISIBLE

            //JuniorApplication.riceviDati(serverid, key)

            JuniorApplication.setAppActivated()
            //LOGIN VERIFICATO CON SUCCESSO

            //MEtto a false per evitare problemi con un possibile task precedente
            JuniorApplication.riceviDatiThread?.isDownloading = RiceviDatiThread.IsDownloading()
            startActivity(Intent(applicationContext, MainActivity::class.java)
                .putExtra("LOGIN", "LOGINACTIVITY")
                .putExtra("SERVERIDUSER", serverid)
                .putExtra("KEY", key))
            finish()
        }
        else{
            var str = R.string.alert_dati_errati
            if (response == "RETE"){
                str = R.string.alert_connessione_impossibile
            }
            try{
                if(p0.newValue is Response<*>){
                    val tmpError = p0.newValue as Response<JsonElement>
                    val d = tmpError.errorBody()
                    val byteArray = d?.bytes()
                    val errorStr = String(byteArray!!)
                    val jsonError = JSONObject(errorStr)
                    if(jsonError.has("errore")){
                        val error = jsonError.get("errore")
                        if(error == "Errore nella selezione del database: "){
                            str = R.string.alert_archivio_non_trovato
                        }
                    }
                    if (jsonError.has("login")){
                        str = R.string.alert_utente_o_psw_errati
                    }
                }
            }
            catch (e:Exception){
                str = R.string.alert_errore_rete_generico
            }
            AlertDialog.Builder(this)
                .setTitle("Errore")
                .setMessage(str)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun init(){
        runOnUiThread{
            accessButton = findViewById(R.id.accessbutton)
            userView = findViewById(R.id.insertuser)
            pswView = findViewById(R.id.insertpsw)
            archivioView = findViewById(R.id.insertarchivio)
            progressBar = findViewById(R.id.progressBarLogin)
            clearPsw = findViewById(R.id.imageView_clear_psw)
            textViewUrl = findViewById(R.id.textView_url)

            accessButton.setOnClickListener(this::accessButtonListener)
            clearPsw.setOnClickListener{
                if(pswView.transformationMethod is PasswordTransformationMethod){
                    pswView.transformationMethod = null
                }
                else{
                    pswView.transformationMethod = PasswordTransformationMethod()
                }

            }

            textViewUrl.text = ParamManager.getUrlNoApi()
            val tmp = ParamManager.getArchivio()
            if(tmp!= null && tmp != "null"){
                archivioView.setText(tmp)
            }
        }
    }
    override fun onBackPressed() {
        //NIENTE
    }

}