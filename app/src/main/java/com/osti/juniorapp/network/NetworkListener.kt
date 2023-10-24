package com.osti.juniorapp.network

import android.net.ConnectivityManager
import android.net.Network
import com.google.gson.JsonElement
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.StatusController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import retrofit2.Response
import java.beans.PropertyChangeEvent
import kotlin.coroutines.EmptyCoroutineContext

class NetworkListener(val activity: MainActivity) : ConnectivityManager.NetworkCallback(){

    override fun onLost(network: Network) {
        activity.runOnUiThread{
            setServersSTatus(osti = false, cliente = false)
        }
        firstTime = false
    }

    private fun setCliente(online:String) : Boolean{
        return online == "OK"
    }

    fun setServersSTatus(osti:Boolean?, cliente:Boolean?) = activity.runOnUiThread{
        if(cliente != null){
            if(cliente) {
                StatusController.setOlineCLI()
            }
            else{
                StatusController.setOfflineCLI()
            }
        }
        if(osti != null){
            if(osti) {
                StatusController.setOlineOSTI()
                ActivationController.setActivated()
            }
            else{
                StatusController.setOfflineOSTI()
            }
        }
    }

    private fun setOsti(online: PropertyChangeEvent) : Boolean{
        var response = online.oldValue
        if (response is Response<*>){
            response = (response as Response<JsonElement>).body()?.asJsonObject
            return response != null
        }
        else{
            return false
        }
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        //niente
    }

    //La prima volta non deve fare il controllo, ci pensa già l'app all'avvio
    var firstTime = true
    override fun onAvailable(network: Network) {
        CoroutineScope(EmptyCoroutineContext).async {
            if(!firstTime){
                delay(5000)
                StatusController.checkOstiServer(activity)
                delay(1000)
                StatusController.checkServerCliente{
                    if(it.newValue is String){
                        setServersSTatus(null, setCliente(it.newValue.toString()))
                    }
                }
            }
            firstTime = false
        }
    }
}