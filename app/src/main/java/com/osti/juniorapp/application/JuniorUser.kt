package com.osti.juniorapp.application

import android.os.Parcel
import android.os.Parcelable
import com.osti.juniorapp.db.tables.DipendentiTable
import com.osti.juniorapp.db.tables.UserTable
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class JuniorUser{


    class JuniorDip {
        var nome: String = "null"
        var badge:Int = -1
        var serverId:Long = -1

        constructor(serverId : Long, nome:String, badge:Int, assunto:String, licenziato:String) {

            this.nome = nome
            this.badge = badge
            this.serverId = serverId

            JuniorApplication.myDatabaseController.creaDipendente(DipendentiTable(nome, badge, assunto, licenziato, serverId))
        }

        constructor(dip:DipendentiTable){
                    this.nome = dip.nome
                    this.badge = dip.badge
                    this.serverId = dip.serverId
        }

        override fun equals(other: Any?): Boolean {
            if(other is JuniorDip){
                if(nome == other.nome &&
                        badge == other.badge &&
                        serverId == other.serverId){
                    return true
                }
                return false
            }
            return false
        }

        override fun hashCode(): Int {
            var result = nome.hashCode()
            result = 31 * result + badge
            result = 31 * result + serverId.hashCode()
            return result
        }
    }

    lateinit var serverIdUser :String
    lateinit var key:String
    lateinit var name:String
    lateinit var type:String
    var permTimbrature:String = "non_autorizzato"
    var permWorkFlow:String = "0"
    var permCartellino:String = "0"
    var dipentende : JuniorDip? = null
    var nascondiTimbrature : String = "0"


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

    fun getServerId():String?{
        if(this::serverIdUser.isInitialized){
            return serverIdUser
        }
        return null
    }

    fun getserverKey():String?{
        if(this::key.isInitialized){
            return key
        }
        return null
    }

    /**
     * Costruttore per quando si conosce solo l'id, se esiste già quello user nel db viene semplicemente caricato in base alle informazioni presenti e impostato come ultimo user utilizzato
     */
    constructor(id: String) {
        getUserFromDb(id) {
            if (it.newValue != null) {
                val tmpUser = it.newValue as JuniorUser
                if (JuniorApplication.myKeystore.activeKey != null) {
                    this.serverIdUser = tmpUser.serverIdUser
                    this.key = JuniorApplication.myKeystore.activeKey!!
                    this.name = tmpUser.name
                    this.type = tmpUser.type
                    this.permTimbrature = tmpUser.permTimbrature
                    this.permWorkFlow = tmpUser.permWorkFlow
                    this.permCartellino = tmpUser.permCartellino
                    this.nascondiTimbrature = tmpUser.nascondiTimbrature

                    JuniorApplication.myDatabaseController.getDipendente(tmpUser.dipentende?.serverId){
                        if(it.newValue != null){
                            dipentende = JuniorDip(it.newValue as DipendentiTable)
                        }
                    }

                }
            }
            JuniorApplication.setLastUser(this)
        }
    }

    constructor(server_id:String, key:String, name:String, type:String, perm_timbrature:String, perm_workFlow:String, perm_cartellino:String, nascondiTimbrature:String,  dip: JuniorDip?){

        this.serverIdUser = server_id
        this.key = key
        this.name = name
        this.type = type
        this.permTimbrature = perm_timbrature
        this.permWorkFlow = perm_workFlow
        this.permCartellino = perm_cartellino
        this.nascondiTimbrature = nascondiTimbrature
        this.dipentende = dip

        JuniorApplication.myDatabaseController.getUser(server_id){
                if(it.newValue == null){
                creaOnDb(server_id)
            }
                else{
                    val tmpUserTable = UserTable(
                        server_id,
                        name,
                        type,
                        perm_timbrature,
                        perm_workFlow,
                        perm_cartellino,
                        dip?.badge ?: -1,
                        dip?.serverId ?: -1,
                        nascondiTimbrature
                    )

                    JuniorApplication.updateUserOnDb(
                        tmpUserTable
                    )
                }
            JuniorApplication.setLastUser(this)
        }
    }

    fun creaOnDb (id:String){
        val tmpUserTable = UserTable(id, name, type, permTimbrature, permWorkFlow, permCartellino, dipentende?.badge ?: -1, dipentende?.serverId ?:-1)
        JuniorApplication.myDatabaseController.creaUser(tmpUserTable)
    }

    /**
     * Ritorna lo user nell'observer
     */
    fun getUserFromDb(id:String, observer: PropertyChangeListener){
        JuniorApplication.myDatabaseController.getUser(id) {
            if (it.newValue != null) {
                val tmpUser = it.newValue as UserTable
                if (JuniorApplication.myKeystore.activeKey != null) {
                    JuniorApplication.myDatabaseController.getDipendente(tmpUser.idDipendente){
                            if (it.newValue != null) {
                                val tmpDip = it.newValue as DipendentiTable
                                val user =  JuniorUser(
                                    tmpUser.server_id,
                                    JuniorApplication.myKeystore.activeKey!!,
                                    tmpUser.name,
                                    tmpUser.type,
                                    tmpUser.perm_timbrature,
                                    tmpUser.perm_workflow,
                                    tmpUser.perm_cartellino,
                                    tmpUser.nascondi_timbrature,
                                    JuniorDip(
                                        tmpUser.idDipendente,
                                        tmpDip.nome,
                                        tmpDip.badge,
                                        tmpDip.assunto,
                                        tmpDip.licenziato
                                    )
                                )
                                observer.propertyChange(PropertyChangeEvent("JUNIOR USER", "JUNIOR USER", user, user))
                            }
                            else {
                                val user =  JuniorUser(
                                    tmpUser.server_id,
                                    JuniorApplication.myKeystore.activeKey!!,
                                    tmpUser.name,
                                    tmpUser.type,
                                    tmpUser.perm_timbrature,
                                    tmpUser.perm_workflow,
                                    tmpUser.perm_cartellino,
                                    tmpUser.nascondi_timbrature,
                                    null
                                )
                                observer.propertyChange(PropertyChangeEvent("JUNIOR USER", "JUNIOR USER", user, user))
                            }
                        }
                }
            }
            observer.propertyChange(PropertyChangeEvent("JUNIOR USER", "JUNIOR USER", null, null))
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other is JuniorUser){
            if (serverIdUser == other.serverIdUser &&
                    key == other.key &&
                    name == other.name &&
                    type == other.type &&
                    permTimbrature == other.permTimbrature &&
                    permWorkFlow == other.permWorkFlow &&
                    nascondiTimbrature == other.nascondiTimbrature &&
                    dipentende == other.dipentende){
                return true
            }
            return false
        }
        return false
    }

    override fun hashCode(): Int {
        var result = serverIdUser.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + permTimbrature.hashCode()
        result = 31 * result + permWorkFlow.hashCode()
        result = 31 * result + permCartellino.hashCode()
        result = 31 * result + nascondiTimbrature.hashCode()
        result = 31 * result + (dipentende?.hashCode() ?: 0)
        return result
    }
}