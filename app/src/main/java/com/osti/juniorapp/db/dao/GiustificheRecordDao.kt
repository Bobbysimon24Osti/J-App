package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.osti.juniorapp.db.tables.GiustificheRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface GiustificheRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(datas: GiustificheRecord)

    @Query("DELETE FROM giustifiche_record WHERE 1=1")
    fun clearGiust()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun multipleInsert(datas:List<GiustificheRecord>)

    @Update
    fun updateTest(giust:GiustificheRecord)

    @Query("select * from giustifiche_record ORDER BY data_inizio DESC, ora_inizio DESC")
    fun getAllGiustifiche() : List<GiustificheRecord>

    @Query("select * from giustifiche_record where dip_id = :dipId ORDER BY data_inizio DESC, ora_inizio DESC, dataOra_richiesta DESC")
    fun getAllGiustificheByDip(dipId: Long) : List<GiustificheRecord>

    @Query("update giustifiche_record set onServer = 1, giu_id= :serverId where id = :giuId")
    fun setOnServer(giuId: Long, serverId:Long)

    @Query("update giustifiche_record set richiesto = \"annullato\"where giu_id = :serverId")
    fun setAnnullato(serverId: Long)

    @Query("select * from giustifiche_record where onServer = 0")
    fun getOfflineGiust () : List<GiustificheRecord?>

    @Query("select * from giustifiche_record where onServer = 0 LIMIT(1)")
    fun getOfflineGiustFlow () : Flow<GiustificheRecord>

    @Query("select * from giustifiche_record where id = :id")
    fun getGiustificaRecordByLocalId (id:Long) : GiustificheRecord

    @Query("select * from giustifiche_record where dip_id = :dip ORDER BY data_inizio DESC, ora_inizio DESC, dataOra_richiesta DESC")
    fun getGiustFlow (dip:Long) : Flow<List<GiustificheRecord?>>

    @Query("select * from giustifiche_record where dip_id = :dip ORDER BY data_inizio DESC, ora_inizio DESC, dataOra_richiesta DESC")
    fun getSingleGiustFlow (dip:Long) : Flow<GiustificheRecord?>

    @Query("select * from giustifiche_record WHERE dip_id != :dipId AND (richiesto = \"richiesto\" OR richiesto = \"ok_livello1\") ORDER BY data_inizio DESC, ora_inizio DESC, dataOra_richiesta DESC")
    fun getGiustFlowNoMieDaGestire (dipId:Long) : Flow<List<GiustificheRecord?>> //Tutte le giustificazioni da gestire apparte quelle richieste dall'utente lggato

    @Query("select * from giustifiche_record WHERE dip_id != :dipId AND (richiesto = \"richiesto\" OR richiesto = \"ok_livello1\") ORDER BY data_inizio DESC, ora_inizio DESC, dataOra_richiesta DESC")
    fun getGiustFlowNoMieDaGestireStatic (dipId:Long) : List<GiustificheRecord?> //Tutte le giustificazioni da gestire apparte quelle richieste dall'utente lggato

    @Query("select * from giustifiche_record WHERE dip_id != :dipId ORDER BY data_inizio DESC, ora_inizio DESC, dataOra_richiesta DESC")
    fun getGiustFlowStorico (dipId:Long) : Flow<List<GiustificheRecord?>>

    @Query("select * from giustifiche_record where giu_id = :id")
    fun getGiustificaRecordByServerId (id:Long) : GiustificheRecord

    @Query("UPDATE giustifiche_record SET richiesto = \"approvato\" where giu_id = :id")
    fun setApprovato (id:Long)
    @Query("UPDATE giustifiche_record SET richiesto = \"ok_livello1\" where giu_id = :id")
    fun setApprovatoLiv1 (id:Long)


    @Query("UPDATE giustifiche_record SET richiesto = \"negato\" where giu_id = :id")
    fun setNegato (id:Long)
}