package com.osti.juniorapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonElement
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.R
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.utils.Generator
import com.osti.juniorapp.utils.LogController
import retrofit2.Response
import java.beans.PropertyChangeListener

class ActivationActivity : AppCompatActivity() {

    val log = LogController(LogController.ATTIVAZIONE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)
    }

    private val observer = PropertyChangeListener{ p0 ->
        var response = p0.newValue
        if ((response is Response<*>) && response.isSuccessful && (p0.oldValue is String)){
            response = (response as Response<JsonElement>).body()?.asJsonObject
            if (response != null) {
                val responseBody = response.get("azione")?.asString
                if(responseBody == "activate"){
                    val response0 = response.get("0").asJsonObject
                    if(response0.get("state")?.asString == "NONATTIVA"){
                        showAlert("Attenzione", "Applicazione non attivata", getEditText())
                        log.insertLog("Applicazione non attivata (codice err. 100)")
                    }
                    else if(response0.get("codice_attivazione")?.asString == "NOTVALID"){
                        log.insertLog("Codice App non valido (codice err. 101)")
                        reShowAlert(p0.oldValue as String)
                    }
                    else if (response0.get("tipo_applicazione")?.asString != "J-App"){
                        reShowAlert(p0.oldValue as String)
                        log.insertLog("Applicazione di tipologia differente (codice err. 102)")
                    }
                    else if (response0.get("codice_attivazione")?.asString is String){
                        val guid = ParamManager.getGuid()
                        val code = response0.get("codice_attivazione").asString
                        val localHash = Generator.createCheckHashWithSecret(guid + code)
                        val serverHash = response0.get("0")?.asJsonObject?.get("hash")?.asString
                        if(localHash == serverHash){//if(localHash == serverHash){
                            ActivationController.setActivated()
                            ParamManager.setCodice(response0.get("codice_attivazione").asString)
                            ParamManager.setUrl(response0.get("url").asString)
                            ParamManager.setTipoApp(response0.get("tipo_applicazione").asString)
                            //JuniorApplication.setLastUser(JuniorApplication.myJuniorUser.value)

                            //CODICE VERIFICATO VIENE SALVATO E AVVIATA LA MAIN ACTIVITY
                            openLogin()
                            ParamManager.setCodice(code)
                            log.insertLog("Prima attivazione completata con successo")
                        }
                        else{
                            log.insertLog("Hash non corretto (codice err. 103)")
                        }
                    }
                }
            }
            else{
                showAlert("Errore di connessione", "Errore Generico contattare assistenza", null)
            }
        }
        else if(response == "ERROR"){
            showAlert("Errore di connessione", "impossibile collegarsi al server per verificare il codice", null)
        }
        else{
            showAlert("Errore di connessione", "Errore Generico contattare assistenza", null)
        }
    }

    override fun onResume() {
        super.onResume()
        createAlert()
    }

    private fun getEditText(code:String? = null): EditText{
        val editText = EditText(applicationContext)
        if(code != null){
            editText.setText(code)
        }
        editText.id = View.generateViewId()
        editText.hint = "Codice Attivazione"
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        editText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        return editText
    }

    private fun createAlert(){
        val tmpCod = ParamManager.getCodice()
        if(tmpCod != null){
            reShowAlert(tmpCod)
        }
        else{
            showAlert("Codice Attivazione", "Inserisci un codice di attivazione", getEditText())
        }
    }

    private fun reShowAlert(codice:String){
        val editText = getEditText()
        editText.setText(codice)
        showAlert("Codice Attivazione errato", "Inserisci un codice valido", editText)
    }

    private fun openLogin(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showAlert(title:String, text:String, editText:EditText?){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(text)
            .setCancelable(false)

        if(editText != null){
            builder.setView(editText)
                .setPositiveButton("Inserisci"){_,_ ->
                    if(editText.text.toString() == "b" && BuildConfig.DEBUG){
                        editText.setText("02969631205_11_LUYER")
                    }
                    NetworkController.checkActivationCode(editText.text.toString(), observer)
                }
        }
        builder.show()
    }

    override fun onBackPressed() {
        //NIENTE
    }

}
