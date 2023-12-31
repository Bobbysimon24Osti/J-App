package com.osti.juniorapp.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.DipendentiTable

@Dao
interface DipendentiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun creaDip(dip : DipendentiTable)

    @Query ("select * from dipendenti where serverId=:serverId")
    fun getDip (serverId:Long) : DipendentiTable?

    @Query ("select * from dipendenti where serverId=:serverId")
    fun getLiveDip (serverId:Long) : LiveData<DipendentiTable?>
}