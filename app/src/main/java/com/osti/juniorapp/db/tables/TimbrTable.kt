package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timbrature")
class TimbrTable(
    val dip_id: String,
    val dataOra: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Int? = null,
    val autoTime: Boolean = false,
    val citta: String? = null,
    val onServer: Boolean = false,
    val causale: Long?
) {
    @PrimaryKey (autoGenerate = true)
    var id = 0
}