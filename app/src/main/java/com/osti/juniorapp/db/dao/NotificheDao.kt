package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.NotificheTable
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(notifica:NotificheTable)

    @Query("SELECT * from notifiche")
    fun getNotificheFlow() : Flow<List<NotificheTable>>

    @Query("SELECT * from notifiche WHERE n_ute_id_destinatario = :id ORDER BY n_dataora_creata DESC")
    fun getNotificheList(id:Long) : List<NotificheTable>

    @Query("UPDATE notifiche SET n_dataora_letta_app = :dataOra WHERE n_id_record_notifica = :id" )
    fun setDataLettura(id:Long, dataOra:String)
}