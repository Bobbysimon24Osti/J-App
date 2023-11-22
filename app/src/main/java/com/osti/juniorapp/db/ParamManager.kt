package com.osti.juniorapp.db

import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.tables.JuniorConfigTable
import com.osti.juniorapp.db.tables.LogTable
import com.osti.juniorapp.db.tables.ParametriTable
import com.osti.juniorapp.utils.Generator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

object ParamManager {

    private var guid :String? = null
    private var idApp :Long? = null
    private var codice:String? = null
    private var database:String? = null
    private var url:String? = null
    private var tipoApp:String? = null
    private var lastUserId:String? = null
    private var lastLog:String? = null
    private var versioneJW:String? = null

    fun loadFromDb(complete:PropertyChangeListener){
        CoroutineScope(Dispatchers.IO).async {
            val numParam = DatabaseController.myDB.mParametriDao().contaParametri()
            if (numParam != null && numParam < 1) {
                DatabaseController.myDB.mParametriDao()
                    .creaParametri(ParametriTable(Generator.generateGuid(), null))
            }

            val tempParams = DatabaseController.myDB.mParametriDao().getParametri()
            guid = tempParams?.guid
            idApp = tempParams?.app_id
            codice = tempParams?.codice
            database = tempParams?.archivio
            url = tempParams?.url
            tipoApp = tempParams?.tipoApp
            lastUserId = tempParams?.lastUserId
            lastLog = DatabaseController.myDB.mLogDao().getLastLog()?.message
            versioneJW = DatabaseController.myDB.mConfigDao().getParam("versione_jweb").valore
        }.invokeOnCompletion {
            complete.propertyChange(PropertyChangeEvent("PARAM MANAGER", "PARAM MANAGER", "OK", "OK"))
        }
    }

    fun getVersioneJW():String?{
        return versioneJW
    }

    fun setVersioneJW(versione:String){
        versioneJW = versione
        CoroutineScope(Dispatchers.IO).async {
            DatabaseController.myDB.mConfigDao().insert(JuniorConfigTable("versione_jweb", versione))
        }
    }

    fun getGuid():String?{
        return guid
    }

    fun getArchivio():String{
        return database ?: "null"
    }

    fun setGuid(guid:String){
        this.guid = guid
        JuniorApplication.myDatabaseController.setGuid(guid)
    }

    fun getCodice():String?{
        return codice
    }

    fun setCodice(codice:String){
        this.codice = codice
        JuniorApplication.myDatabaseController.setActivationCode(codice)
    }
    fun getDatabase():String?{
        return database
    }

    fun setLastLog(log:LogTable){
        this.lastLog = log.message
        JuniorApplication.myDatabaseController.creaLog(log.type, log.message, log.dataOra)
    }
    fun getLastLog():String?{
        return lastLog
    }

    fun setDatabase(database:String){
        this.database = database
        JuniorApplication.myDatabaseController.setArchivio(database)
    }
    fun getUrl():String?{
        return url
    }
    fun getUrlNoApi():String{
        var tmpUrl = url
        tmpUrl = tmpUrl?.replace("/api/", " ")
        return tmpUrl ?: "error truncating url"
    }

    fun setUrl(url:String){
        var res = url
        if(url.length >3){
            if(url[url.lastIndex] != '/'){
                res += "/"
            }
            if(url.takeLast(4) != "api/"){
                res += "api/"
            }
            if(this.url != res){
                //Url modificato, facvcio uscire lo user perchè cambiato server
                NetworkController.apiCliente = null
                JuniorApplication.myDatabaseController.setLastUserId(null)
            }
            this.url = res
            JuniorApplication.myDatabaseController.setUrl(res)
        }
    }

    fun getTipoApp():String?{
        return tipoApp
    }

    fun setTipoApp(tipo:String){
        this.tipoApp = tipo
        JuniorApplication.myDatabaseController.setTipoApp(tipo)
    }
    fun getLastUserId():String?{
        return lastUserId
    }

    fun setLastUserId(lastUserId:String?){
        this.lastUserId = lastUserId
        JuniorApplication.myDatabaseController.setLastUserId(lastUserId)
    }

    fun setIdApp (id:Long) {
        this.idApp = id
        JuniorApplication.myDatabaseController.setIdApp(id)
    }

    fun getIdApp (): Long? {
        return idApp
    }
}