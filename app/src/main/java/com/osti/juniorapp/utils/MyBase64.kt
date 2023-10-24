package com.osti.juniorapp.utils

import android.os.Build
import java.util.Base64

object MyBase64 {

    fun encode(str:ByteArray) :String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(str)
        }
        else {
           android.util.Base64.encodeToString(str, android.util.Base64.DEFAULT)
        }
    }

    fun decode(str:String?) : ByteArray?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getDecoder().decode(str)
        }
        else {
            android.util.Base64.decode(str, android.util.Base64.DEFAULT)
        }

    }
}