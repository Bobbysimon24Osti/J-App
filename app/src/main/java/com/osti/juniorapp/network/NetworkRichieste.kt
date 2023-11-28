package com.osti.juniorapp.network

import android.os.Build
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.db.ParamManager
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class NetworkRichieste(val api:ApiCliente?) {

    fun setRichiesta(status:String, ids:List<Long>, observer: PropertyChangeListener){
        if(api != null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && db != null && serverId != null){
                val call = api.setRichiesta(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    RichiesteUpdate(status, ArrayList(ids))
                )
                call.enqueue(RichiesteCallback(observer))
                NetworkController.log.insertLog("Invio richiesta NUOVE NOTIFICHE")
            }
        }
    }

    private class RichiesteCallback(val observer: PropertyChangeListener):
        Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            if (response.code() < 400){
                val res = response.body()?.asJsonObject
                if (res != null) {
                    if(res.has("update") && res.get("update").asString == "ok"){
                        observer.propertyChange(PropertyChangeEvent("NetworkRichieste", "INVIO GESTIONE RICHIESTA GIUSTIFICATIVO", res.get("update").asString, res))
                        NetworkController.log.insertLog("INVIO RICHIESTE OK")
                    }
                }
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkRichieste", "INVIO GESTIONE RICHIESTA GIUSTIFICATIVO", "FAIL", "FAIL"))
                NetworkController.log.insertLog("INVIO RICHIESTE FAIL")
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            observer.propertyChange(PropertyChangeEvent("NetworkRichieste", "INVIO GESTIONE RICHIESTA GIUSTIFICATIVO", "FAIL", "FAIL"))
            NetworkController.log.insertLog("INVIO RICHIESTE FAIL")
        }
    }

    class RichiesteUpdate(status: String, ids: ArrayList<Long>){
        val gz_id = ids
        val gz_richiesto = status
    }
}