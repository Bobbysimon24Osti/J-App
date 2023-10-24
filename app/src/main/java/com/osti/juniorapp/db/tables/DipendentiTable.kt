package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName =  "dipendenti")
class DipendentiTable(

    var nome:String = "null",

    var badge:Int = -1,

    var assunto:String = "null",

    var licenziato:String = "null",

    @PrimaryKey
    var serverId: Long
) {

}