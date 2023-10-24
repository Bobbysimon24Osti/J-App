package com.osti.juniorapp.db.resolvers

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.osti.juniorapp.db.tables.NomiFileTable

object NomiFileResolver {

    fun createNomiFileFromJson(nomi:JsonArray): List<NomiFileTable>{
        val list = ArrayList<NomiFileTable>()
        for(item in nomi){
            val tmp = item.asJsonObject
            list.add(
                NomiFileTable(
                    tmp.get("fil_id").asLong,
                        tmp.get("fil_nome").asString,
                        tmp.get("fil_estensione").asString,
                        let{
                            if(tmp.get("fil_nome_visualizzato") != JsonNull.INSTANCE){
                                tmp.get("fil_nome_visualizzato").asString
                            }
                            else{
                                null
                            }
                        },
                        tmp.get("fil_mimetype").asString,
                        tmp.get("fil_dataora_upload").asString,
                        tmp.get("fil_valido_fino_al").asString,
                        let{
                                if(tmp.get("fil_note")!= JsonNull.INSTANCE){
                                    tmp.get("fil_note").asString
                                }
                                else{
                                    null
                                }
                        },
                        tmp.get("fld_dataora_visto_prima").asString,
                        tmp.get("fld_dataora_risposta").asString,
                        tmp.get("ute_nome").asString,
                        tmp.get("fil_tipo").asString,
                        tmp.get("fld_risposta").asString,
                        tmp.get("fil_nome_url").asString,
                        tmp.get("file_tipo").asString,
                        tmp.get("file_risposta").asString
            ))
        }
        return list
    }
}