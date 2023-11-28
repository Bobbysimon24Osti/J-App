package com.osti.juniorapp.application

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.osti.juniorapp.db.tables.UserTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.internal.notify

class UserRepository (userServerId:String?) {

    companion object {
        var key: String? = null
        var logged = false
        var ignore = false

        fun saveUserDataFromServer(userId:String){
            JuniorApplication.myDatabaseController.creaUseroAggiorna((UserTable(userId, badge = -1, idDipendente = -1)))
        }
    }

    private var user: LiveData<UserTable?>?

    init{
        user = JuniorApplication.myDatabaseController.getLiveUser(userServerId)
    }



    fun getUser() : UserTable?{
        return user?.value
    }

    fun getLiveUser() : LiveData<UserTable?>? {
        return user
    }


    fun insert (user:UserTable){
        CoroutineScope(Dispatchers.Default).async {
            user.notify()
        }
        JuniorApplication.myDatabaseController.creaUseroAggiorna(user)
    }

    fun canTimbr():Boolean{
        if((user?.value?.perm_timbrature ?: "null")== "coordinate" || (user?.value?.perm_timbrature ?: "null") == "qualsiasi"){
            return true
        }
        return false
    }

    fun canWorkFlow():Boolean{
        if((user?.value?.perm_workflow ?: "null") == "1"){
            return true
        }
        return false
    }

    fun positionObb():Boolean{
        if((user?.value?.perm_timbrature ?: "null") == "coordinate"){
            return true
        }
        return false
    }


}