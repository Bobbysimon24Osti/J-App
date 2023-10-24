package com.osti.juniorapp.utils

class JuniorLicenza (cod:String?, ragS:String?, pI:String?, att:String?){
    val codice:String?
    val ragSoc:String?
    val pIva:String?
    val attivazione:String?

    init {
        codice = cod
        ragSoc = ragS
        pIva = pI
        attivazione = att
    }
}