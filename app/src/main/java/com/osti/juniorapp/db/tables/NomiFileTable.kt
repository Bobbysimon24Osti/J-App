package com.osti.juniorapp.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName =  "nomi_file")
class NomiFileTable(
        @PrimaryKey
        var fil_id: Long,

        var fil_nome: String? = null,

        var fil_estensione:String? = null,

        var fil_nome_visualizzato:String? = null,

        var fil_mimetype:String? = null,

        var fil_dataora_upload:String? = null,

        var fil_valido_fino_al:String =  "0000-00-00",

        var fil_note:String? =  null,

        var fld_dataora_visto_prima:String =  "2023-07-18 12:08:34",

        var fld_dataora_risposta:String =  "0000-00-00 00:00:00",

        var ute_nome:String =  "admin",

        var fil_tipo:String =  "4",

        var fld_risposta:String =  "0",

        var fil_nome_url:String =  "735beeef-a837-44ef-9844-242268ad1e58",

        var file_tipo:String =  "Comunicazioni",

        var file_risposta:String =  ""

) {

}
