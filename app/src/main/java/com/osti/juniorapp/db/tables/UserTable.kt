package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName =  "users")
class UserTable(

    var server_id:String = "null",

    var name:String = "null",

    var type:String = "null",

    var perm_timbrature:String = "non_autorizzato",

    var perm_workflow:String = "0",

    var perm_cartellino:String = "0",

    var badge:Int,

    var idDipendente:Long,

    var nascondi_timbrature:String = "0",

    var livello_manager:String = "unico",

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0



) {

}