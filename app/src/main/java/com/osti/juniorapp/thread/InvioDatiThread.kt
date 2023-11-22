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

class InvioDatiThread(dipId:Long) : Thread() {

    val timbrFlow = JuniorApplication.myDatabaseController.getTimbrFlow(dipId)
    val giustFlow = JuniorApplication.myDatabaseController.getSingleGiustFlow(dipId)
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
    companion object {
        var lastSendId: Int = -1
    }

    private fun startSendingTimbr(){
        isTimbrStarted = true
        timbrScope = CoroutineScope(Dispatchers.IO).launch {
            timbrFlow.collect{
                if(it is TimbrTable && !it.onServer && it.id != lastSendId){
                    InvioDatiThread.lastSendId = it.id
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