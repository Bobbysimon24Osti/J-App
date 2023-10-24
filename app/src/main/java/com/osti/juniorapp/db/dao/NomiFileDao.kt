package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.NomiFileTable
import kotlinx.coroutines.flow.Flow

@Dao
interface NomiFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data:NomiFileTable)

    @Query("select * from nomi_file ORDER BY fil_dataora_upload DESC")
    fun getNomiFlow() : Flow<List<NomiFileTable>>

    @Query("select * from nomi_file where fil_id = :id")
    fun getNomeFile(id:Long) : NomiFileTable

    @Query("update nomi_file set file_risposta = :risp where fil_id = :id")
    fun setRispostaFile(id:Long, risp:String)

    @Query("DELETE FROM nomi_file")
    fun clear()
}