package com.osti.juniorapp.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log")
class LogTable(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var message:String,

    var type:String,

    @ColumnInfo(name = "data_ora")
    var dataOra:String
) {
}