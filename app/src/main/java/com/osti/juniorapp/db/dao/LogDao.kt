package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.LogTable

@Dao
interface LogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(log:LogTable)

    @Query("select * from log where id = :id")
    fun getLog(id:Long):LogTable

    @Query("select * from log")
    fun getAllLogs():List<LogTable>

    @Query("select* from log where type = :type")
    fun getLogByType(type:String) :List<LogTable>

    @Query("select * from log ORDER BY id DESC LIMIT 1")
    fun getLastLog():LogTable?

}