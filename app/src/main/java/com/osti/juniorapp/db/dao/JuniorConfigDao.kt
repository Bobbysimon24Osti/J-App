package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.JuniorConfigTable
import kotlinx.coroutines.flow.Flow

@Dao
interface JuniorConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(config:JuniorConfigTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun multipleInsert(config:List<JuniorConfigTable>)

    @Query("select * from juniorconfig where nome = :nome")
    fun getParam(nome:String) : JuniorConfigTable

    @Query("select * from juniorconfig")
    fun getLincenzaParam() : List<JuniorConfigTable>

    @Query("select * from juniorconfig")
    fun getAllParams() : List<JuniorConfigTable>

    @Query("select nome from juniorconfig")
    fun getNomi() : List<String>

    @Query("select * from juniorconfig")
     fun getSpinnerConfigFlow() : Flow<JuniorConfigTable>
}