package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "giustifiche")
class GiustificheTable (

    @PrimaryKey
    var id:Long,

    var abbreviativo:String,

    var datas:String

        ) {
}