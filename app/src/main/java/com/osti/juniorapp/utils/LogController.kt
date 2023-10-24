package com.osti.juniorapp.utils

import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.db.tables.LogTable
import com.osti.juniorapp.utils.Utils.FORMATDATEHOURS
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.Calendar

class LogController(val type:String) {

    companion object{
        const val NETWORK = "NETWORK"
        const val ATTIVAZIONE = "ATTIVAZIONE"
        const val LOGIN = "LOGIN"
        const val GIUST = "CREA GIUST"
    }

    fun insertLog(msg:String){
        JuniorApplication.myDatabaseController.creaLog(type, msg, FORMATDATEHOURS.format(Calendar.getInstance().timeInMillis))
    }


    fun getLastLogString(observer :PropertyChangeListener){
       JuniorApplication.myDatabaseController.getLastLog{
           var log = it.newValue as LogTable ?: null
            if(log == null){
                val tmpLog = LogTable(type= "PRIMO", message = "Primo Log", dataOra = FORMATDATEHOURS.format(Calendar.getInstance().timeInMillis))
                JuniorApplication.myDatabaseController.creaLog(tmpLog.type, tmpLog.message, tmpLog.dataOra)
                log = tmpLog
            }
           observer.propertyChange(PropertyChangeEvent("LOG CONTROLLER", "LOG CONTROLLER", "${log.dataOra}:  ${log.message}", "${log.dataOra}:  ${log.message}"))
        }
    }

}