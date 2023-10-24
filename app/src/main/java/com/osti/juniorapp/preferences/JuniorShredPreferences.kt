package com.osti.juniorapp.preferences

import android.content.Context

object JuniorShredPreferences {

    val archive = "JUNIORPREF"

    var lastCodeCheck = -1L

    public fun loadLastCheck(context: Context){
        lastCodeCheck = context.getSharedPreferences(archive, Context.MODE_PRIVATE).getLong("LASTCHECK", -1)
    }

    public fun setSharedPref(str:String, key:String, context: Context){
        context.getSharedPreferences(archive, Context.MODE_PRIVATE).edit().putString(key, str).apply()
    }

    public fun getSharedPref(key:String, context: Context):String?{
        return context.getSharedPreferences(archive, Context.MODE_PRIVATE).getString(key, null)
    }

    public fun setDBversion(version:Int, context: Context){
        context.getSharedPreferences(archive, Context.MODE_PRIVATE).edit().putInt("DBversion", version).apply()
    }

    public fun getDBversion(context: Context) : Int{
        return context.getSharedPreferences(archive, Context.MODE_PRIVATE).getInt("DBversion", -1)
    }
}