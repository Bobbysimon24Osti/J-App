package com.osti.juniorapp.application

import androidx.lifecycle.MutableLiveData
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.network.NetworkController
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener


object StatusController {

    var observe: PropertyChangeListener? = null

    class JuniorStatus(var osti:Boolean = false, var cliente: Boolean = false)

    var statusApp = MutableLiveData(JuniorStatus())//SERVER OSTI


    private fun equal(status: JuniorStatus): Boolean{
        if(status.osti == statusApp.value?.osti && status.cliente == statusApp.value?.cliente){
            return true
        }
        return false
    }

    /**
     * SERVER CLIENTE
     */
    fun setOfflineCLI(){
        val tmp = JuniorStatus(statusApp.value!!.osti, false)
        if(!equal(tmp)){
            observe?.propertyChange(PropertyChangeEvent("STATUS CONTROLLER", "SET ONLINE", statusApp.value!!.cliente, false))
            statusApp.value = tmp
        }
    }
    /**
     * SERVER CLIENTE
     */
    fun setOlineCLI(){
        val tmp = JuniorStatus(statusApp.value!!.osti, true)
        if(!equal(tmp)){
            observe?.propertyChange(PropertyChangeEvent("STATUS CONTROLLER", "SET ONLINE", statusApp.value!!.cliente, true))
            statusApp.value = tmp
        }
    }

    /**
     * SERVER OSTI
     */
    fun setOfflineOSTI(){
        val tmp =JuniorStatus(false, statusApp.value!!.cliente)
        if(!equal(tmp)){
            statusApp.value =tmp
        }
    }
    /**
     * SERVER OSTI
     */
    fun setOlineOSTI(){
        val tmp = JuniorStatus(true, statusApp.value!!.cliente)
        if(!equal(tmp)){
            statusApp.value = tmp
        }
    }


    fun checkOstiServer(activity: MainActivity){
        Updater.updateServer(activity)
    }
    fun checkServerCliente(observer: PropertyChangeListener){
        NetworkController.testServerCliente(observer)
    }

}