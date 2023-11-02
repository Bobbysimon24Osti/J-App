package com.osti.juniorapp.network

import com.google.gson.JsonElement
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiCliente {


    @Headers("Content-Type: application/json")
    @GET("login.php")
    fun firstLogin(
        @Header("X-Db") db:String,
        @Header("Authorization") psw:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @GET("parametri.php")
    fun login(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @GET("parametri.php")
    fun loginFirstTime(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Query("da_record" ) da:String = "0",
        @Query("n_record" ) n:String = "10"): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @POST("timbrature.php")
    fun sendTImbrature(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Body body: NetworkController.
        TimbrRequest): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @POST("giustificazioni.php")
    fun sendGiustifiche(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Body body: NetworkController.
        GiustRequest): Call<JsonElement>


    @Headers("Content-Type: application/json")
    @GET("giustificazioni.php")
    fun getGiustifiche(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @GET("parametri.php")
    fun testServerCliente(
        @Header("X-Db") db:String,
    @Header("X-User-Id") id:String,
    @Header("X-User-Key") key:String,
    @Header("X-Guid") guid:String,
    @Header("X-Code") code:String,
    @Header("X-Versione-App") versione:String,
    @Header("X-Dispositivo") disp:String): Call<JsonElement>


    @Headers("Content-Type: application/json")
    @PUT("giustificazioni.php")
    fun deleteGiustifica(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Body body: NetworkController.
        DeleteRequest): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @GET("filedip.php")
    fun getFile(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Query("fileurl") url: String): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @GET("buste-comunicazioni.php")
    fun getFileNames(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String): Call<JsonElement>


    @Headers("Content-Type: application/json")
    @GET("r-cartellino.php")
    fun getCartellino(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Query("m") url: String): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("buste-comunicazioni.php")
    fun inviaRisposta(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Body body: NetworkController.
        InviaRispostaFile): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @GET("notifiche.php")
    fun getNotifiche(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @PUT("notifiche.php")
    fun setNotificaLetta(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Body body:NetworkNotifiche.NotificaLetta): Call<JsonElement>

    @Headers("Content-Type: application/json")
    @PUT("giustificazioni.php")
    fun setRichiesta(
        @Header("X-Db") db:String,
        @Header("X-User-Id") id:String,
        @Header("X-User-Key") key:String,
        @Header("X-Guid") guid:String,
        @Header("X-Code") code:String,
        @Header("X-Versione-App") versione:String,
        @Header("X-Dispositivo") disp:String,
        @Body body:NetworkRichieste.RichiesteUpdate): Call<JsonElement>
}