package com.osti.juniorapp.network

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiOsti {

    @POST("activate.php")
    fun checkActivationCode(@Body body: NetworkController.CodeCheckRequest): Call<JsonElement>

    @POST("checkGuid.php")
    fun checkGuid(@Body body: NetworkController.GuidCheckRequest): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @GET("download.php")
    fun getUltimaVersione(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Query("versione_jweb") versioneJW:String) : Call<JsonElement>


    companion object {
        //const val BASE_URL_OSTI = "http://192.168.10.23:90/testing/"
        const val BASE_URL_OSTI = "https://app.ostisistemiweb.it/"
    }
}
