package com.osti.juniorapp.network

import android.os.Build
import com.google.gson.JsonElement
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUser
import com.osti.juniorapp.db.ParamManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class NetworkNotifiche(val api:ApiCliente?) {

    fun getnotifiche(observer: PropertyChangeListener){
        if(api != null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            if(guid!= null && db != null){
                val call = api.getNotifiche(
                    db,
                    JuniorUser.serverIdUser,
                    JuniorUser.key,
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL
                )
                call.enqueue(NotificheCallback(observer))
                NetworkController.log.insertLog("Invio richiesta NUOVE NOTIFICHE")
            }
        }
    }

    fun setNotificaLetta(id:Long, observer: PropertyChangeListener){
        if(api != null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            if(guid!= null && db != null){
                val call = api.setNotificaLetta(
                    db,
                    JuniorUser.serverIdUser,
                    JuniorUser.key,
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    NotificaLetta(id)
                )
                call.enqueue(NotificaLettaCallback(observer))
                NetworkController.log.insertLog("Invio update NOTIFICA LETTA")
            }
        }
    }

    private class NotificheCallback(val observer: PropertyChangeListener):
        Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            if (response.code() < 400){
                val res = response.body()?.asJsonObject
                if (res != null) {
                    if(res.has("notifiche")){
                        val def = JuniorApplication.myDatabaseController.createNotificheFromJson(res.get("notifiche").asJsonArray)
                        observer.propertyChange(PropertyChangeEvent("NetworkController", "RICHIESTA NUOVE NOTIFICHE", res.get("notifiche"), def))
                        NetworkController.log.insertLog("NUOVE NOTIFICHE DOWNLOAD OK")
                    }
                }
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkController", "RICHIESTA NUOVE NOTIFICHE", "FAIL", "FAIL"))
                NetworkController.log.insertLog("NUOVE NOTIFICHE FAIL")
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            observer.propertyChange(PropertyChangeEvent("NetworkController", "RICHIESTA NUOVE NOTIFICHE", "FAIL", "FAIL"))
            NetworkController.log.insertLog("NUOVE NOTIFICHE FAIL")
        }
    }

    private class NotificaLettaCallback(val observer: PropertyChangeListener):
        Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            if (response.code() < 400){
                val res = response.body()?.asJsonObject
                if (res != null) {
                    if(res.has("update") && res.get("update").asString == "ok"){
                        observer.propertyChange(PropertyChangeEvent("NetworkController", "UPDATE NOTIFICA LETTA", res, res))
                    }
                }
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkController", "UPDATE NOTIFICA LETTA", "FAIL", "FAIL"))
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            observer.propertyChange(PropertyChangeEvent("NetworkController", "UPDATE NOTIFICA LETTA", "FAIL", "FAIL"))
        }
    }

    class NotificaLetta(id:Long){
        val n_id = id
        val letta = true
    }

}