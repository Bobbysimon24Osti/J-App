package com.osti.juniorapp.application

import androidx.lifecycle.MutableLiveData
import com.osti.juniorapp.db.tables.DipendentiTable
import com.osti.juniorapp.db.tables.UserTable
import okhttp3.internal.notify
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

object JuniorUser {


    var userLogged = false

    var updated = MutableLiveData(false)

    var serverIdUser :String = "null"
    var key:String = "null"
    var name:String = "null"
    var type:String = "null"
    var permTimbrature:String = "non_autorizzato"
    var permWorkFlow:String = "0"
    var permCartellino:String = "0"
    var nascondiTimbrature : String = "0"
    var livelloManager : String = "unico"
    object JuniorDipendente{
        var nome: String = "null"
        var badge:Int = -1
        var serverId:Long = -1
        var assunto : String = "null"
        var licenziato : String = "null"
    }

    fun saveUserOnDb(){
        val tmpUserTable = UserTable(serverIdUser ?:"-1", name ?:"null", type?:"null", permTimbrature?:"null", permWorkFlow?:"null", permCartellino?:"null", JuniorDipendente.badge, JuniorDipendente.serverId, livelloManager?:"null")
        JuniorApplication.myDatabaseController.creaUser(tmpUserTable)
    }

    fun saveDipOnDb(){
        val tmpDipTable = DipendentiTable(JuniorDipendente.nome, JuniorDipendente.badge, JuniorDipendente.assunto, JuniorDipendente.licenziato, JuniorDipendente.serverId)
        JuniorApplication.myDatabaseController.creaDipendente(tmpDipTable)
    }

    fun saveAllOnDb(){
        saveUserOnDb()
        saveDipOnDb()
        updated.value = true
    }

    fun getUserFromDb(id:String, observer: PropertyChangeListener? = null) {
        JuniorApplication.myDatabaseController.getUser(id) {
            if (it.newValue != null) {
                val tmpUser = it.newValue as UserTable
                if (JuniorApplication.myKeystore.activeKey != null) {
                    JuniorApplication.myDatabaseController.getDipendente(tmpUser.idDipendente){
                        val tmpDip = it.newValue as DipendentiTable?
                        serverIdUser = tmpUser.server_id
                        key = JuniorApplication.myKeystore.activeKey!!
                        name = tmpUser.name
                        type = tmpUser.type
                        permTimbrature = tmpUser.perm_timbrature
                        permWorkFlow = tmpUser.perm_workflow
                        permCartellino = tmpUser.perm_cartellino
                        nascondiTimbrature = tmpUser.nascondi_timbrature
                        livelloManager = tmpUser.livello_manager

                        JuniorDipendente.nome = tmpDip?.nome ?: "null"
                        JuniorDipendente.badge = tmpDip?.badge ?: -1
                        JuniorDipendente.serverId = tmpDip?.serverId ?: -1
                        JuniorDipendente.assunto = tmpDip?.assunto ?: "null"
                        JuniorDipendente.licenziato = tmpDip?.licenziato ?: "null"
                    }
                }
            }
            observer?.propertyChange(PropertyChangeEvent("JUNIOR USER", "JUNIOR USER", null, null))
        }
    }

    fun toUserTable (): UserTable{
        return UserTable(serverIdUser?:"-1",
                name?:"null",
                type?:"null",
                permTimbrature,
                permWorkFlow,
                permCartellino,
            JuniorDipendente.badge,
            JuniorDipendente.serverId,
                nascondiTimbrature,
                livelloManager)
    }

    fun parseAndSaveUserTable(user:UserTable?){
        if(user!= null && user != toUserTable()){
            serverIdUser = user.server_id
            key = JuniorApplication.myKeystore.activeKey!!
            name = user.name
            type = user.type
            permTimbrature = user.perm_timbrature
            permWorkFlow = user.perm_workflow
            permCartellino = user.perm_cartellino
            nascondiTimbrature = user.perm_timbrature
            livelloManager = user.livello_manager
        }
    }

    fun canTimbr():Boolean{
        if(permTimbrature == "coordinate" || permTimbrature == "qualsiasi"){
            return true
        }
        return false
    }

    fun canWorkFlow():Boolean{
        if(permWorkFlow == "1"){
            return true
        }
        return false
    }

    fun positionObb():Boolean{
        if(permTimbrature == "coordinate"){
            return true
        }
        return false
    }

}