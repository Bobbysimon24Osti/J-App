package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "notifiche")
class NotificheTable (
    val n_ute_id_creata:Long?,
    val n_ute_id_destinatario:Long?,
    val n_dataora_creata:String,
    val n_dataora_inviata_app:String,
    val n_dataora_letta_app:String,
    val n_oggetto:String,
    val n_messaggio:String,
    val n_tipo_notifica:String,
    val n_id_record_notifica:Long,
    @PrimaryKey
    val n_id:Long
)