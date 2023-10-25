package com.osti.juniorapp.utils

import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    const val DB_VERSION = 6
    const val DB_NAME = "JuniorDB"
    const val MINACCURACY = 100000
    const val APPSTATESHARED = "OK"
    const val MAXTIMEPOSITION = 20000L //20000 = 5 secondi
    const val MAXTIME = 86400000 //86400000 24H
    const val LOCATIONTIMEOUT =30000L //60000 1MIN
    const val TENTATIVIMAXRICHIESTE = 3
    val FORMATDATEHOURS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY)
    val NORMALFORMATDATEHOURS = SimpleDateFormat("dd/MM/yyyy   HH:mm", Locale.ITALY)
    val FORMATDATE = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    val FORMATDATEDB = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY)
    val FORMATTIME = SimpleDateFormat("HH:mm", Locale.ITALY)
    val ANNO = SimpleDateFormat("yyyy", Locale.ITALY)
    val MESE = SimpleDateFormat("MM", Locale.ITALY)
    val GIORNO = SimpleDateFormat("dd", Locale.ITALY)
    val ORA = SimpleDateFormat("HH", Locale.ITALY)
    val ORASINGOLA = SimpleDateFormat("H", Locale.ITALY)
    val MINUTISINGOLI = SimpleDateFormat("HH:m", Locale.ITALY)
    val MINUTI = SimpleDateFormat("mm", Locale.ITALY)
    val SECONDI = SimpleDateFormat("ss", Locale.ITALY)
    val SECONDIMINTIMBR = 60000


    const val TIMBRVIRTUALE = "lic_timbratura_virtuale"
    const val TIMBRVIRTUALEGPS = "lic_timbratura_virtuale_gps"
    const val WORKFLOW = "lic_workflow"
    const val WORKFLOWVALORIOBBLIGATORI = "workflow_valori_obbligatori"

    const val NOCITTA = "Localita sconosciuta"


    const val RAGSOCDB ="lic_ragione_sociale"
    const val PIVADB = "lic_piva"


    val opzioniOrdineFile = arrayListOf("Nuovi", "Vecchi", "Nome", "Tipo")
}
