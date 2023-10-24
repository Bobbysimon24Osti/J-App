package com.osti.juniorapp.utils

import java.security.MessageDigest
import java.util.UUID

object Generator{

val hashString = "SKdksjdlaskde9wi9fsfdsjkljfweur9wiflsdlvDJJFJIEJJFMKS_-dsdsdsS"

    public fun generateGuid(): String {
        return UUID.randomUUID().toString()
    }

    public fun createCheckHashWithSecret(strg:String): String{
        val str = strg + hashString
        val bytes = str.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    public fun createCheckHash(strg:String): String{
        val str = strg
        val bytes = str.toByteArray()
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)
        val tmp =  digest.fold("") { str, it -> str + "%02x".format(it) }
        return tmp
    }


}