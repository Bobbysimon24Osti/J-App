package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "giustifiche_record",
        indices = [Index(value = ["giu_id"], unique = true)])
class GiustificheRecord (
        val giu_id:Long?,
        val giu_type_id:Long,
        val dip_id:Long,
        val data_inizio:String,
        val data_fine:String,
        val valore:Double,
        val richiesto:String,
        val note:String? = null,
        val note_gestito:String? = null,
        val dip_nome:String,
        val dip_badge:Int,
        val nome:String,
        val tipo:String,
        val ore_valore:String,
        val utente_definitivo:String? = null,
        val utente_definitivo_id:Long? = null,
        val utente_lv1:String? = null,
        val utente_lv1_id:Long? = null,
        val abbreviazione_azienda:String? = null,
        val filiale:String? = null,
        val reparto:String? = null,
        val ora_inizio:Int,
        val ora_fine:Int,
        val dataOra_richiesta:String,
        val dataOra_gestito:String? = null,
        val festivi:Boolean? = null,
        val abbreviazione_giustifica: String?,
        val onServer:Boolean = false
    ) {
        @PrimaryKey(autoGenerate = true)
        //LOCAL ID
        var id = 0L
}