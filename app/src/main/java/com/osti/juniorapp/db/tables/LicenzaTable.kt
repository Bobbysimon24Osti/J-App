package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName =  "juniorconfig", indices = [Index(value = ["nome"], unique = true)])
class JuniorConfigTable (

    var nome:String,

    var valore:String,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
){
}