package com.osti.juniorapp.thread

import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.db.tables.TimbrTable
import com.osti.juniorapp.utils.Utils.TENTATIVIMAXRICHIESTE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class InvioDatiThread() : Thread() {

    val timbrFlow = JuniorApplication.myDatabaseController.getTimbrFlow()
    val giustFlow = JuniorApplication.myDatabaseController.getOfflineGiustFlow()
    var isTimbrStarted = false
    var isGiustStarted = false

    var tentativi = TENTATIVIMAXRICHIESTE

    fun checkForSending(){
        if(!isGiustStarted){
            if(giustScope != null){
                giustScope!!.cancel()
            }
            startSendingGiust()
        }
        if(!isTimbrStarted){
            if(timbrScope != null){
                timbrScope!!.cancel()
            }
            startSendingTimbr()
        }
    }

    var timbrScope : Job? = null
    private fun startSendingTimbr(){
        isTimbrStarted = true
        timbrScope = CoroutineScope(Dispatchers.IO).launch {
            timbrFlow.collect{
                if(it is TimbrTable && !it.onServer){
                    NetworkController.sendTimbr(
                        it
                    )
                }
            }
        }
    }

    var giustScope : Job? = null
    private fun startSendingGiust(){
        isGiustStarted = true
        giustScope = CoroutineScope(Dispatchers.IO).launch {
            giustFlow.collect{
                if(it is GiustificheRecord && !it.onServer){
                    NetworkController.sendGiustifiche(it)
                }
            }
        }
    }
}