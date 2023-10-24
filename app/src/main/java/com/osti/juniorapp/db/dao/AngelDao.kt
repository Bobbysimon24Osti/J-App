package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.AngelTable

@Dao
interface AngelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(value: AngelTable)

    @Query("select value from angel_table where id = 1")
    fun getValue():String

}