package com.osti.juniorapp.network

import android.os.Build
import com.google.gson.JsonElement
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.db.ParamManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class NetworkAggiornaApp(val api:ApiOsti?) {

    fun getUltimaVersione(versioneJWeb:String, observer: PropertyChangeListener){
        if(api != null && ActivationController.isActivated() && versioneJWeb != "null"){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val serverid = ParamManager.getLastUserId()
            if(guid!= null && db != null && serverid != null){
                val tmp = VersioneRequest(versioneJWeb)
                val call = api.getUltimaVersione(
                    db,
                    serverid,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    tmp.versione_jweb
                )
                call.enqueue(VersioneCallback(observer))
                NetworkController.log.insertLog("Invio richiesta LINK AGGIORNA APP")
            }
        }

    }

    class VersioneRequest (versioneJWeb: String){
        val versione_jweb = versioneJWeb
    }

    private class VersioneCallback(val observer: PropertyChangeListener):
        Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            if (response.code() < 400){
                observer.propertyChange(PropertyChangeEvent("NetworkController", "RICHIESTA ULTIMA VERSIONE APP", response, response.body()?.toString()))
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkController", "RICHIESTA ULTIMA VERSIONE APP", "FAIL", "FAIL"))
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            observer.propertyChange(PropertyChangeEvent("NetworkController", "RICHIESTA ULTIMA VERSIONE APP", "FAIL", "FAIL"))
        }
    }

}