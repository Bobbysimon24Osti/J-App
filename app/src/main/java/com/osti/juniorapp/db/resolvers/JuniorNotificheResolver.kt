package com.osti.juniorapp.db.resolvers

import com.google.gson.JsonObject
import com.osti.juniorapp.db.tables.NotificheTable

class JuniorNotificheResolver(data:JsonObject) {

    val utenteDestinatarioId = let { if(data.has("n_ute_id_destinatario")){ data.get("n_ute_id_destinatario").asLong }
        else{ -1 }}

    val msg = let { if(data.has("n_messaggio")){ data.get("n_messaggio").toString().replace("\\n", "\n").replace("\\r", "\r").replace("\"", "") }
        else{ "ERROR" }}

    val titolo = let{ if(data.has("n_oggetto")){ data.get("n_oggetto").toString().replace("\"", "") }
        else{ "ERROR" }}

    val utenteCreataId = let{ if(data.has("n_ute_id_creata")){ data.get("n_ute_id_creata").asLong }
        else{ -1 }}

    val dataOraCreata = let{ if(data.has("n_dataora_creata")){ data.get("n_dataora_creata").toString().replace("\"", "") }
        else{ "-1" }}

    val dataOraInviata = let{ if(data.has("n_dataora_inviata_app")){ data.get("n_dataora_inviata_app").toString().replace("\"", "") }
        else{ "-1" }}

    val dataOraLetto = let{ if(data.has("n_dataora_letta_app")){ data.get("n_dataora_letta_app").toString().replace("\"", "") }
        else{ "-1" }}

    val tipo = let{ if(data.has("n_tipo_notifica")){ data.get("n_tipo_notifica").toString().replace("\"", "") }
        else{ "-1" }}

    val id = let{ if(data.has("n_id")){ data.get("n_id").asLong }
        else{ -1 }}

    val idRichiesta = let{ if(data.has("n_id_record_notifica")){ data.get("n_id_record_notifica").asLong }
        else{ -1 }}

    fun getDbTable() : NotificheTable{
        return NotificheTable(
            utenteCreataId,
            utenteDestinatarioId,
            dataOraCreata,
            dataOraInviata,
            dataOraLetto,
            titolo,
            msg,
            tipo,
            idRichiesta,
            id)
    }
}