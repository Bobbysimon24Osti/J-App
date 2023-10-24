package com.osti.juniorapp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.TimbrTable
import kotlinx.coroutines.flow.Flow

@Dao
interface TimbrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun creaTimbr(timbratura : TimbrTable): Long

    @Query("update timbrature set onServer = 1 where id = :id")
    fun updateTimbronServer(id:Int)

    @Query("update timbrature set citta = :citta where id=:id")
    fun setCitta(id:Long, citta:String)

    @Query("select * from timbrature where dip_id = :dip ORDER BY id DESC LIMIT 1000")
     fun getAllTimbr (dip:Long) : List<TimbrTable>

    @Query("select * from timbrature where onServer = 0 LIMIT(1)")
    fun getLastOfflineTimbr () : Flow<TimbrTable?>

    @Query("select * from timbrature where onServer = 1")
    fun getUploadedTimbr () : List<TimbrTable>

    @Query("select * from timbrature where id = :id")
    fun getTimbr (id:Long) : TimbrTable

    @Query("select * from timbrature where dip_id = :idDipendente")
    fun getTimbrByDip(idDipendente:Long): List<TimbrTable>

    @Query("select * from timbrature where dip_id = :idDipendente ORDER BY id DESC LIMIT 1")
    fun getLastTimbrByDip(idDipendente:Long): TimbrTable

    @Query("select onServer from timbrature where dip_id = :idDipendente ORDER BY id DESC LIMIT 1")
    fun getLiveLastTimbrByDip(idDipendente:Long): Flow<Boolean>
}