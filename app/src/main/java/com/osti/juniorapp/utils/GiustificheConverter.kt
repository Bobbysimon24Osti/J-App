package com.osti.juniorapp.utils

import com.google.gson.JsonObject
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.db.tables.GiustificheTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date

object GiustificheConverter {

    var giustifiche: List<GiustificheTable>? = null

    fun getDataInizio (giust:GiustificheTable):String?{
        return getTag("gt_dal", giust)
    }
    fun getDataFine (giust:GiustificheTable):String?{
        return getTag("gt_al", giust)
    }
    fun getOraInizio (giust:GiustificheTable):String?{
        return getTag("gt_dalle", giust)
    }
    fun getOraFine (giust:GiustificheTable):String?{
        return getTag("gt_alle", giust)
    }

    fun getGiustificaTableById(id:Long): GiustificheTable?{
        for (x in giustifiche!!){
            if(x.id == id){
                return x
            }
        }
        return null
    }

    fun getTypeById(id:Long?): String?{
        for (x in giustifiche!!){
            if(x.id == id){
                return getType(x)
            }
        }
        return null
    }

    fun getOreValoreById(id:Long?): String?{
        for (x in giustifiche!!){
            if(x.id == id){
                return getOrevalore(x)
            }
        }
        return null
    }

    fun getNameById(id:Long?): String?{
        for (x in giustifiche!!){
            if(x.id == id){
                return getCompleteName(x)
            }
        }
        return null
    }

    private fun getTag(tag:String, giustifica:GiustificheTable): String?{

        val nomi = try {
           JSONObject(giustifica.datas)
        } catch (e:Exception){
            null
        }

        if(nomi!= null && nomi.has(tag)){
            return nomi.getString(tag)
        }

        return null
    }

    fun getAllCompleteNames(): List<String?>{

        val res= ArrayList<String?>()
        for(item in giustifiche!!){
            res.add(getTag("gt_nome", item))
        }
        return res
    }

    fun getAllCompleteNamesNoCausaleVuota(giust: List<GiustificheTable>): List<String?>{

        val res= ArrayList<String?>()
        for(item in giust){
            if(item.abbreviativo != ""){
                res.add(getTag("gt_nome", item))
            }
        }
        return res
    }

    fun getCompleteName(giust:GiustificheTable): String?{
        return getTag("gt_nome", giust)
    }

    fun getType(giust:GiustificheTable): String?{
        return getTag("gt_tipo", giust)
    }

    fun getOrevalore(giust:GiustificheTable): String?{
        return getTag("gt_orevalore", giust)
    }

    fun getAllAbbreviativiNoCausaleVuota(giust: List<GiustificheTable>): List<String>{
        val res = ArrayList<String>()
        for (x in giust){
            if(x.abbreviativo != ""){
                res.add(x.abbreviativo)
            }
        }
        return res
    }

    fun getAllAbbreviativi(): List<String>{
        val res = ArrayList<String>()
        for (x in giustifiche!!){
            res.add(x.abbreviativo)
        }
        return res
    }

    fun getAllGiust(): List<GiustificheTable>?{
        return giustifiche
    }

    fun getAllGiustNoCausaleVuota(): List<GiustificheTable>{
        val l = ArrayList<GiustificheTable>()
        for(x in giustifiche!!){
            if(x.abbreviativo != ""){
                l.add(x)
            }
        }
        return l
    }

    fun getAllNamesAndAbbr(): List<String>{
        val res = ArrayList<String>()
        var nomi: List<String?>? = null
        var abbreviazioni: List<String?>? = null
        nomi = getAllCompleteNames()

        abbreviazioni = getAllAbbreviativi()

        for ((index, item) in nomi.withIndex()){
            if(item != null && index != nomi.size){
                res.add(item + " - " + abbreviazioni[index])
            }
        }

        return res
    }

    fun getAllNamesAndAbbrNoCausaleVuota(giust:List<GiustificheTable>): List<String>{
        val res = ArrayList<String>()
        var nomi: List<String?>? = null
        var abbreviazioni: List<String?>? = null

        nomi = getAllCompleteNamesNoCausaleVuota(giust)

        abbreviazioni = getAllAbbreviativiNoCausaleVuota(giust)

        for ((index, item) in nomi.withIndex()){
            if(item != null){
                res.add(item + " - " + abbreviazioni[index])
            }
        }
        return res
    }
    fun getOraFromMin(min:Int):String{
        return convertminIntoHours(min)
    }
    fun getMinFromOra(hour: Date) : Int{
        return (((hour.time + 3600000) / 1000)/60).toInt()
    }

    fun isNoteObb(giust:GiustificheTable): Boolean {
        val valore = getTag("gt_nota_obb_workflow", giust)
        return valore =="1"
    }

    fun getValore(json: JsonObject):String{
        var valore = ""
        if(json.has("gt_orevalore") && json.get("gt_orevalore").toString().trim('"') == "ore"){
            valore = convertminIntoHours(json.get("gz_valore").toString().trim('"').toInt())
        }
        else{
            valore = json.get("gz_valore").toString().trim('"')
        }
        return valore
    }

    fun getValore(giust: GiustificheRecord):String{
        var valore = ""
        if(giust.ore_valore == "ore"){
            valore = convertminIntoHours(giust.valore.toString().substringBefore('.').toInt())
        }
        else{
            valore = giust.valore.toString()
        }
        return valore
    }
    private fun convertminIntoHours(min:Int) :String{
        val oraInizioJs = min
        var tmpInizioH = (oraInizioJs / 60).toString()
        var tmpInizioM = (oraInizioJs % 60).toString()

        if(tmpInizioH.length<2){
            tmpInizioH = "0$tmpInizioH"
        }
        if(tmpInizioM.length<2){
            tmpInizioM = "0$tmpInizioM"
        }
        return "$tmpInizioH:$tmpInizioM"
    }

    fun getRecordFromJson(json:JsonObject): GiustificheRecord{

        return GiustificheRecord(
            json.get("gz_id").asLong,
            let{if(json.get("gt_id") == null || json.get("gt_id").isJsonNull){-1L} else{ json.get("gt_id").asLong} },
            let{if(json.get("gz_dipid") == null || json.get("gz_dipid").isJsonNull){-1L} else{ json.get("gz_dipid").asLong} },
            json.get("gz_dal").toString().trim('"'),
            json.get("gz_al").toString().trim('"'),
            json.get("gz_valore").toString().trim('"').toDouble(),
            json.get("gz_richiesto").toString().trim('"'),
            json.get("gz_note").toString().trim('"'),
            json.get("gz_note_gestito").toString().trim('"'),
            json.get("dip_nome").toString().trim('"'),
            json.get("dip_badge").asInt,
            json.get("gt_nome").toString().trim('"'),
            json.get("gt_tipo").toString().trim('"'),
            json.get("gt_orevalore").toString().trim('"'),
            json.get("utente_definitivo").toString().trim('"'),
            let{if(json.get("utente_definitivo_id") == null || json.get("utente_definitivo_id").isJsonNull){null} else{ json.get("utente_definitivo_id").asLong} },
            json.get("utente_liv1").toString().trim('"'),
            let{if(json.get("utente_liv1_id") == null || json.get("utente_liv1_id").isJsonNull){null} else{ json.get("utente_liv1_id").asLong} },
            json.get("az_abbr").toString().trim('"'),
            json.get("fl_nome").toString().trim('"'),
            json.get("rp_nome").toString().trim('"'),
            json.get("gz_dalle").toString().trim('"').toInt(),
            json.get("gz_alle").toString().trim('"').toInt(),
            json.get("gz_dataora_richiesta").toString().trim('"'),
            json.get("gz_dataora_gestito").toString().trim('"'),
            null,
            json.get("gt_abb")?.toString()?.trim('"'),
            true
            )

    }
}