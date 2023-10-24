package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "angel_table")
class AngelTable (

    @PrimaryKey
    var id:Int = 1,

    var value:String
        ) {
}