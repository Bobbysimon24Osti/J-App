package com.osti.juniorapp.application

import com.google.gson.JsonElement
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.db.tables.JuniorConfigTable
import com.osti.juniorapp.utils.Utils
import retrofit2.Response
import java.beans.PropertyChangeEvent

object ActivationController {

    const val NOATTIVA = "NONATTIVA"
    const val ATTIVA = "ATTIVA"
    const val ERROREPARAM = "ERROREPARAMETRI"
    const val OFFLINE = "OFFLINE"
    const val LOGOUT = "LOGOUT"

    private var activated = false

    var perTimbraVirtuale = "1"
    var perTimbraVirtualeGps = "1"
    var permWorkFlow = "1"
    var workFlowValoriObbligatori = "1"




    fun setActivated(){
        activated = true
    }

    fun setNOTActivated(){
        activated = false
    }

    fun isActivated() : Boolean {
        return activated
    }

    fun canTimbrGps():Boolean{
        try{
            return perTimbraVirtuale == "1" &&
                    perTimbraVirtualeGps == "1" &&
                    JuniorApplication.myJuniorUser.value!!.canTimbr() ?: false
        }
        catch (e:Exception){
            return false
        }
    }

    fun canTimbr():Boolean{
        try{
            return perTimbraVirtuale == "1"&&
                    JuniorApplication.myJuniorUser.value?.canTimbr() ?: false
        }
        catch (e:Exception){
            return false
        }
    }

    fun saveValore(v:JuniorConfigTable){
        when(v.nome){
            Utils.TIMBRVIRTUALE ->{
                perTimbraVirtuale = v.valore
            }
            Utils.TIMBRVIRTUALEGPS -> {
                perTimbraVirtualeGps = v.valore
            }
            Utils.WORKFLOW -> {
                permWorkFlow = v.valore
            }
            Utils.WORKFLOWVALORIOBBLIGATORI -> {
                workFlowValoriObbligatori = v.valore
            }
        }
    }

    fun saveLicenzaInfo(){
        JuniorApplication.myDatabaseController.getLicenzaParams{
            if(it.newValue != null && it.newValue is List<*>){
                for (config in it.newValue as List<JuniorConfigTable>){
                    saveValore(config)
                }
            }
        }
    }

    /**
     * Controlla se il risultato di un controllo su un GUID è andato a buon fine, e nel caso sia andato bene aggiorna i paramtri dell'app in base a quelli scaricati dal server OSTI
     */
    fun checkResult (p0: PropertyChangeEvent) :String{
        StatusController.setOlineOSTI()
        val response = p0.newValue
        if (response is Response<*>) {
            var serverParams = (response as Response<JsonElement>).body()?.asJsonObject
            if (serverParams != null) {
                val responseBody = serverParams.get("azione")?.asString
                if (responseBody == "guid") {
                    serverParams = serverParams.get("0").asJsonObject
                    if (serverParams.get("guid")?.asString == "noGuid") {
                        return NOATTIVA
                    }
                    else if (serverParams.get("guid")?.asString is String && serverParams.get("attiva").asInt == 1) {
                        //GUID VALIDO, APP ATTIVA
                        try{
                            ParamManager.setGuid(serverParams.get("guid").asString)
                            ParamManager.setIdApp(serverParams.get("id").asLong)
                            ParamManager.setCodice(serverParams.get("codice_attivazione").asString)
                            val oldUrl = ParamManager.getUrl()
                            ParamManager.setUrl(serverParams.get("url").asString)
                            ParamManager.setTipoApp(serverParams.get("tipo_applicazione").asString)

                            //Guid controllato e OK, viene impostata l'app come attivata
                            JuniorApplication.setAppActivated()

                            if(oldUrl != null && oldUrl != ParamManager.getUrl()){
                                JuniorApplication.setLastUser(null)
                                return LOGOUT
                            }
                        }
                        catch (e:Exception){
                            //JuniorApplication.showGlobalErrorAlert(context, "Errore controllo licenza, provare a chiudere e riaprire l'applicazione, se il problema persiste contattare l'assistenza")
                            return ERROREPARAM
                        }

                        //Imposto l'app attivata nella variabvile di controllo
                        setActivated()

                        //Guid controllato e OK, l'esecuzione torna al main thread in base all'activity aperta
                        return ATTIVA
                    }
                    else {
                        return NOATTIVA
                    }
                }
            }
        }
        if (response is String){
            //APP OFFLINE
            StatusController.setOfflineOSTI()
            return OFFLINE
        }
        return NOATTIVA
    }
}