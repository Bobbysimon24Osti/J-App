package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.GiustificheTable
import kotlinx.coroutines.flow.Flow


@Dao
interface GiustificheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(datas:GiustificheTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun multipleInsert(datas:List<GiustificheTable>)

    @Query("select * from giustifiche where id= :id")
    fun getGiustifica(id:Long) : GiustificheTable

    @Query("select * from giustifiche where abbreviativo= :abbreviativo")
    fun getGiustifica(abbreviativo:String) : GiustificheTable

    @Query("select * from giustifiche")
    fun getAllGiustifiche() : List<GiustificheTable>

    @Query("select * from giustifiche where id != 0")
    fun getAllGiustificheNoCausaleVuota() : List<GiustificheTable>

    @Query("select * from giustifiche")
    fun getAllGiustificheFlow() : Flow<List<GiustificheTable>>

    @Query("select * from giustifiche where id != 0")
    fun getAllGiustificheFlowNoCausaleVuota() : Flow<List<GiustificheTable>>

    @Query("select abbreviativo from giustifiche")
    fun getAllAbbrevGiustifiche() : List<String>

    @Query("select abbreviativo from giustifiche where id != 0")
    fun getAllAbbrevGiustificheNoCausaleVuota() : List<String>

    @Query("select * from giustifiche where id = :id")
    fun getGiustificaById(id:Long) : GiustificheTable

    @Query ("delete from giustifiche")
    fun svuotaGiustifiche ()
}