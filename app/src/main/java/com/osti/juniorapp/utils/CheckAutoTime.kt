package com.osti.juniorapp.utils

import android.content.Context
import android.provider.Settings

object CheckAutoTime {

    fun checkAutoTime(context:Context):Boolean{
        val autoTime = Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME, 0)
        val autoTimeZone = Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0)
        return (autoTime==1 && autoTimeZone==1)
    }
}