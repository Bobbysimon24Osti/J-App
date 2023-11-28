package com.osti.juniorapp.application

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.osti.juniorapp.db.tables.DipendentiTable
import com.osti.juniorapp.db.tables.UserTable

class DipendentiRepository(dipServerId:Long) {

    private var dipendente: LiveData<DipendentiTable?>

    fun getDipendente() : DipendentiTable?{
        return dipendente.value
    }

    fun getLiveDipendente() : LiveData<DipendentiTable?> {
        return dipendente
    }

    init {
        dipendente = JuniorApplication.myDatabaseController.getLiveDipendente(dipServerId)
    }

    fun insert(dipendentiTable: DipendentiTable){
        JuniorApplication.myDatabaseController.creaDipendente(dipendentiTable)
    }

}