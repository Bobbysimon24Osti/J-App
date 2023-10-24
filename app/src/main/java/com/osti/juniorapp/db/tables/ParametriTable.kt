package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName =  "parametri")
class ParametriTable(
    @PrimaryKey
    var guid: String,

    var app_id: Long? = null,

    var codice:String? = null,

    var archivio:String? = null,

    var url:String? = null,

    var tipoApp:String? = null,

    var dataAtiivazione:String? = null,

    var lastUserId:String? = null
) {

}