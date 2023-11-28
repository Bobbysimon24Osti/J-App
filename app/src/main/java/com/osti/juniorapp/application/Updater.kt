package com.osti.juniorapp.application

import android.app.Activity
import android.content.Intent
import com.osti.juniorapp.activity.ActivationActivity
import com.osti.juniorapp.activity.LoginActivity
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.utils.LogController
import retrofit2.Response

object Updater {

    fun updateServer(activity: Activity){
        NetworkController.checkGuid{
            if(it.newValue is Response<*> && (it.newValue as Response<*>).isSuccessful){
                StatusController.setOlineOSTI()
                if(ActivationController.checkResult(it) == ActivationController.ATTIVA){
                    JuniorApplication.riceviDati(ParamManager.getLastUserId() ?: "null", UserRepository.key?:"null")
                    JuniorApplication.inviaDati()
                }
                else if (ActivationController.checkResult(it) == ActivationController.NOATTIVA){
                    activity.startActivity(Intent(activity, ActivationActivity::class.java))
                    ParamManager.setLastUserId(null)
                    activity.finish()
                }
                else if (ActivationController.checkResult(it) == ActivationController.LOGOUT){
                    activity.startActivity(Intent(activity, LoginActivity::class.java))
                    ParamManager.setLastUserId(null)
                    activity.finish()
                }
            }
            else{
                StatusController.setOfflineOSTI()
            }
        }
    }

    fun updateNoCheck(){
        JuniorApplication.riceviDati(ParamManager.getLastUserId() ?: "null", UserRepository.key?:"null")
        JuniorApplication.inviaDati()
    }
}