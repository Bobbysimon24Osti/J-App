package com.osti.juniorapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.osti.juniorapp.db.tables.ParametriTable

@Dao
interface ParametriDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun creaParametri(params : ParametriTable)

    @Query ("update parametri set codice = :codice")
    fun setCodiceAttivazione (codice:String)

    @Query ("select codice from parametri")
    fun getCodiceAttivazione () :String?

    @Query ("select archivio from parametri")
    fun getArchivio () :String?

    @Query ("update parametri set archivio = :archivio")
    fun setArchivio (archivio:String)

    @Query ("select guid from parametri")
    fun getGuid () :String?

    @Query ("update parametri set guid = :guid")
    fun setGuid (guid:String)

    @Query("select count(guid) from parametri")
    fun contaParametri(): Int?

    @Query("select * from parametri")
    fun getParametri(): ParametriTable?

    @Query ("update parametri set url = :url")
    fun setUrl (url:String)

    @Query ("select url from parametri")
    fun getUrl () :String?

    @Query ("select lastUserId from parametri")
    fun getLastUserId () :String?

    @Query ("update parametri set lastUserId = :id")
    fun setLastUserId (id:String?)

    @Query ("select tipoApp from parametri")
    fun getTipoApp () :String?

    @Query ("select dataAtiivazione from parametri")
    fun getDataAttivazione () :String?

    @Query ("update parametri set dataAtiivazione = :data")
    fun setDataAttivazione (data: String)

    @Query ("update parametri set tipoApp = :tipo")
    fun setTipoApp (tipo:String?)

    @Query ("update parametri set app_id = :id")
    fun setIdApp (id:Long)

    @Query ("select app_id from parametri")
    fun getIdApp () : Long?
}