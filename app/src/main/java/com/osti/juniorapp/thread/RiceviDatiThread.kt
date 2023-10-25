package com.osti.juniorapp.thread

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.network.NetworkNotifiche
import com.osti.juniorapp.utils.Utils.TENTATIVIMAXRICHIESTE
import retrofit2.Response
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class RiceviDatiThread : Thread() {

    class IsDownloading(){
        private var giustifiche = false
        private var notifiche = false

        private var downloading = false

        fun setDownloadOn(){
            giustifiche = true
            notifiche = true
            downloading = true
        }
        fun setGiust(boo:Boolean){
            giustifiche = boo
            if(giustifiche || notifiche){
                downloading = true
            }
            if(!giustifiche && !notifiche){
                downloading = false
            }
        }
        fun setNotifiche(boo:Boolean){
            notifiche = boo
            if(giustifiche || notifiche){
                downloading = true
            }
            if(!giustifiche && !notifiche){
                downloading = false
            }
        }

        fun getDownloading() : Boolean{
            return downloading
        }

    }

    var isDownloading = IsDownloading()

    companion object{

        /**
         * Serve per quando si deve aspettare il download completato delle giustificazioni
         */
        var observeScaricoGiust: PropertyChangeListener? = null


        private fun setGiustScaricate(boo:Boolean){
            if(observeScaricoGiust != null){
                observeScaricoGiust!!.propertyChange(PropertyChangeEvent("Scarico Dati", "richieste giustifiche",  false, boo))
            }
        }

        /**
         * Serve per quando si deve aspettare il download completato
         */
        var observeScaricoNotifiche: PropertyChangeListener? = null
        var nuoveNotifiche: Boolean = false

        private fun setNotificheScaricate(boo:Boolean){
            nuoveNotifiche = boo
            if(observeScaricoNotifiche != null){
                observeScaricoNotifiche!!.propertyChange(PropertyChangeEvent("Scarico Dati", "richieste notifiche",  false, boo))
            }
        }
    }

    var tentativi = TENTATIVIMAXRICHIESTE

    private val giustificheObs = PropertyChangeListener{ p0 ->
        //set scaricate si usa anche se è offline perchè ovviamente non c'è niente da aspettare in questi casi ma si mostra il db offline
        if(p0.oldValue == true){
            setGiustScaricate(true)
            JuniorApplication.myDatabaseController.clearGiust()
            JuniorApplication.updateLocalGiust(((p0.newValue as Response<*>).body() as JsonObject).getAsJsonArray("giustificazioni"))
        }
        else{
            setGiustScaricate(false)
        }
        isDownloading.setGiust(false)
    }

    var firstScaricoNotifiche = true
    private val notificheObs = PropertyChangeListener{ p0 ->
        //set scaricate si usa anche se è offline perchè ovviamente non c'è niente da aspettare in questi casi ma si mostra il db offline
        if(!firstScaricoNotifiche){

        }
        if(p0.oldValue == true){
            setNotificheScaricate(true)

        }
        else{
            setNotificheScaricate(false)
        }
        isDownloading.setNotifiche(false)
    }

    val loginObs = PropertyChangeListener{
        if(it.propertyName == "OK" && it.newValue!= null && it.oldValue != null){
            val datas = it.newValue as JsonObject
            val params = (it.oldValue as Response<JsonElement>).body()?.asJsonObject!!
            JuniorApplication.updateUserParams(params, datas.get("serverId").asString, datas.get("key").asString)
            if(params.has("versione_jweb")){
                ParamManager.setVersioneJW(params.get("versione_jweb").asString)
            }

            //Scarico Giustificazioni
            NetworkController.getGiustifiche(giustificheObs)

            //Scarico notifiche
            val network = NetworkNotifiche(NetworkController.apiCliente)
            network.getnotifiche(notificheObs)
        }
    }

    fun downloadFromServer(serverId:String? = null, key:String? = null){
        if(!isDownloading.getDownloading()){
            isDownloading.setDownloadOn()
            if(serverId == null || key == null){
                val user = JuniorApplication.myJuniorUser
                if(user != null){
                    NetworkController.login(user.value!!.serverIdUser, user.value!!.key, obs = loginObs)
                }
            }
            else{
                NetworkController.login(serverId, key, obs = loginObs)
            }
        }
    }

}