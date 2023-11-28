package com.osti.juniorapp.network

import android.os.Build
import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.StatusController
import com.osti.juniorapp.application.StatusController.setOfflineCLI
import com.osti.juniorapp.application.StatusController.setOfflineOSTI
import com.osti.juniorapp.application.StatusController.setOlineCLI
import com.osti.juniorapp.application.StatusController.setOlineOSTI
import com.osti.juniorapp.application.UserRepository
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.db.tables.NomiFileTable
import com.osti.juniorapp.db.tables.TimbrTable
import com.osti.juniorapp.thread.InvioDatiThread
import com.osti.juniorapp.utils.Generator
import com.osti.juniorapp.utils.LogController
import com.osti.juniorapp.utils.MyBase64
import com.osti.juniorapp.utils.Utils.ANNO
import com.osti.juniorapp.utils.Utils.FORMATDATEHOURS
import com.osti.juniorapp.utils.Utils.FORMATTIME
import com.osti.juniorapp.utils.Utils.GIORNO
import com.osti.juniorapp.utils.Utils.MESE
import com.osti.juniorapp.utils.Utils.MINUTI
import com.osti.juniorapp.utils.Utils.ORA
import com.osti.juniorapp.utils.Utils.SECONDI
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar
import java.util.GregorianCalendar


//Metodi per fare le richieste al server + Classi che ne gestiscono le risposte
object NetworkController {

    private var isGuidOnGoing = false
    private var isLoginOnGoing = false
    private var isFirstLoginOnGoing = false
    private var isTimbrOnGoing = false
    private var isCodeCheckOnGoing = false

    val apiAttivazione : ApiOsti? = RetrofitClientAttivazione.instance?.getMyApi()
    var apiCliente : ApiCliente? = null

    val log = LogController(LogController.NETWORK)

    private fun istantiateapi(){
        if(apiCliente == null){
            apiCliente = RetrofitClientJuniorwe.instance?.getMyApi()
        }
    }

    fun checkActivationCode(code:String, observer: PropertyChangeListener){
        if(!isCodeCheckOnGoing){
            isCodeCheckOnGoing = true
            val call = apiAttivazione?.checkActivationCode(CodeCheckRequest(code))
            call?.enqueue(ActivationCallback(observer, code))?: observer.propertyChange(PropertyChangeEvent("NetworkController", "activationCode", "ERROR", "ERROR"))
            log.insertLog("Invio richiesta ATTIVAZIONE")
        }
    }

    class CodeCheckRequest(val code:String){
        val guid = ParamManager.getGuid()
        val versione_app = BuildConfig.VERSION_NAME
    }

    private class ActivationCallback(val observer: PropertyChangeListener, val codice:String): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            log.insertLog("Richiesta attivazione: Risposta OK ")
            setOlineOSTI()
            observer.propertyChange(PropertyChangeEvent("NetworkController", "activate",  codice, response))
            isCodeCheckOnGoing = false
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            setOfflineOSTI()
            log.insertLog("Richiesta attivazione: Risposta ERROR (codice err. 104)-> cause: ${t.cause}   msg: ${t.message}")
            observer.propertyChange(PropertyChangeEvent("NetworkController", "activate",  "Connessione al server non riuscita", "ERROR"))
            isCodeCheckOnGoing = false
        }
    }

    fun checkGuid(observer: PropertyChangeListener){
        if(!isGuidOnGoing){
            isGuidOnGoing = true
            val request = GuidCheckRequest()
            val call = apiAttivazione?.checkGuid(request)
            call?.enqueue(GuidCheckCallaback(observer))?: observer.propertyChange(PropertyChangeEvent("NetworkController", "guid", "ERROR", "ERROR"))
            log.insertLog("Richiesta controllo GUID")
        }
    }
    private class GuidCheckCallaback(val observer: PropertyChangeListener): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            log.insertLog("Richiesta guid: Risposta OK ")
            setOlineOSTI()
            observer.propertyChange(PropertyChangeEvent("NetworkController", "guid",  response, response))
            isGuidOnGoing = false
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            setOfflineOSTI()
            log.insertLog("Richiesta guid: Risposta ERROR (codice err. 104)-> cause: ${t.cause}   msg: ${t.message} ")
            observer.propertyChange(PropertyChangeEvent("NetworkController", "guid",  t.toString(), t.toString()))
            isGuidOnGoing = false
        }
    }

    class GuidCheckRequest{
        val guid = ParamManager.getGuid()
        val msg = ParamManager.getLastLog() ?: "msg"
        val versione_app = BuildConfig.VERSION_NAME
    }


    //LOGIN E SCARICO PARAMETRI (parametri.php)
    fun login(serverId:String?, key:String?, archivio: String? = ParamManager.getArchivio(), obs:PropertyChangeListener){
        istantiateapi()
        if(!isLoginOnGoing && apiCliente!= null && serverId != null && key != null){
            isLoginOnGoing = true
            if(archivio != null){
                ParamManager.setDatabase(archivio)
                val call = apiCliente?.login(
                    archivio,
                    serverId,
                    key,
                    let{
                        if(ParamManager.getGuid() != null){
                            ParamManager.getGuid()!!
                        }
                        else{
                            "null"
                        }
                    },
                    let{
                        if(ParamManager.getCodice() != null){
                            ParamManager.getCodice()!!
                        }
                        else{
                            "null"
                        }
                    },
                    BuildConfig.VERSION_NAME,
                    Build.MODEL)
                call?.enqueue(LoginCallaback(serverId, key, obs))
                log.insertLog("Invio richiesta LOGIN ")
            }
        }
    }

    private class LoginCallaback(val serverId: String, val key: String, val observer: PropertyChangeListener): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            setOlineCLI()
            val result = JsonObject()
            result.addProperty("serverId", serverId)
            result.addProperty("key", key)
            try{
                log.insertLog("Richiesta LOGIN: OK")
                if (response.errorBody()?.byteString() == null && response.body()?.asJsonObject != null && response.raw().code == 200){ //Veridica che la risposta sia arrivata correttamente e che non ci siano stati errori
                    StatusController.setOlineCLI()
                    log.insertLog("Login verificato con successo")
                    try{
                        JuniorApplication.setAppActivated()
                        observer.propertyChange(PropertyChangeEvent("NetworkController", "OK",  response, result))
                    }
                    catch (e:Exception){
                        log.insertLog("Errore in fase di salvataggio parametri user (codice err. 200)")
                        observer.propertyChange(PropertyChangeEvent("NetworkController", "ERR-PARAM",  response, result))
                    }
                }
                else if (response is Response<*>){
                    //NON AUTORIZZATO, IL SERVER RIFIUTA LA CONNESSIONE PER PROBLEMI DI AUTORIZZAZIONE; DI SOLITO QUANDO SI CAMBIA URL E L'UTENTE E LOGGATO SUL VECCHIO SERVER
                    log.insertLog("Errore in fase di richiesta parametri user (codice err. 201)")
                    observer.propertyChange(PropertyChangeEvent("NetworkController", "ERR-AUTH",  response, result))
                    ParamManager.setLastUserId(null)
                    StatusController.setOlineCLI()
                }
                else{
                    //ERRORE DI RETE
                    StatusController.setOfflineCLI()
                    log.insertLog("Errore in fase di richiesta parametri user (codice err. 202)")
                    observer.propertyChange(PropertyChangeEvent("NetworkController", "ERR-RETE",  response, result))
                }
            }
            catch (e:Exception){
                StatusController.setOfflineCLI()
                observer.propertyChange(PropertyChangeEvent("NetworkController", "ERR-SCONOSCIUTO",  response, result))
            }

            isLoginOnGoing = false
        }
        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            StatusController.setOfflineCLI()
            log.insertLog("Richiesta LOGIN: ERROR (codice err. 204)-> cause: ${t.cause}   msg: ${t.message} ")
            observer.propertyChange(PropertyChangeEvent("NetworkController", "NULL",  "ERR-FAIL", "RETE"))
            isLoginOnGoing = false
        }
    }

    fun firstLogin(user:String, psw:String, observer: PropertyChangeListener, archivio: String = ParamManager.getArchivio()){
        istantiateapi()
        if (!isFirstLoginOnGoing && apiCliente!= null){
            isFirstLoginOnGoing = true
            val i = ParamManager.getGuid()!!
            val h = ParamManager.getCodice()!!
            ParamManager.setDatabase(archivio)
            var tm2 = "Basic " + MyBase64.encode("$user:$psw".encodeToByteArray())
           if(tm2.substring(tm2.length-1) == "\n"){
               tm2 = tm2.dropLast(2)
           }
            val call = apiCliente?.firstLogin(
                archivio,
                tm2,
                "id",
                "key",
                let{
                    if(ParamManager.getGuid() != null){
                        ParamManager.getGuid()!!
                    }
                    else{
                        "null"
                    }
                },
                let{
                    if(ParamManager.getCodice() != null){
                        ParamManager.getCodice()!!
                    }
                    else{
                        "null"
                    }
                },
                BuildConfig.VERSION_NAME,
                Build.MODEL)
            call?.enqueue(FirstLoginCallaback(observer))?: observer.propertyChange(PropertyChangeEvent("NetworkController", "firstLogin", "ERROR", "ERROR"))
            log.insertLog("Invio ichiesta PRIMO LOGIN")
        }
    }



    private class FirstLoginCallaback(val observer: PropertyChangeListener): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            log.insertLog("Richiesta PRIMO LOGIN: OK")

            observer.propertyChange(PropertyChangeEvent("NetworkController", "firstlogin",  response, response))
            isFirstLoginOnGoing = false
        }
        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            setOfflineCLI()
            log.insertLog("Richiesta PRIMO LOGIN: ERROR (cause 204)-> cause: ${t.cause}   msg: ${t.message}")
            observer.propertyChange(PropertyChangeEvent("NetworkController", "firstlogin",  "RETE", t.toString()))
            isFirstLoginOnGoing = false
        }
    }


    fun sendTimbr(timbr:TimbrTable){
            istantiateapi()
            if(apiCliente!= null && ActivationController.isActivated()){
                val guid = ParamManager.getGuid()
                val db = ParamManager.getArchivio()
                val codice = ParamManager.getCodice()
                val serverId = ParamManager.getLastUserId()
                if(guid!= null && db != null && serverId != null){
                    val tmp = TimbrRequest(timbr, guid, db)
                    val call = apiCliente?.sendTImbrature(
                        db,
                        serverId,
                        JuniorApplication.myKeystore.activeKey ?: "null",
                        guid,
                        codice ?: "null",
                        BuildConfig.VERSION_NAME,
                        Build.MODEL,
                        tmp
                    )
                    call?.enqueue(TimbrCallaback(timbr.id))
                    log.insertLog("Invio richiesta SALVATAGGIO TIMBRATURA SU SERVER")
                }
            }
    }

    private class TimbrCallaback(val id_timbr:Int): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            InvioDatiThread.lastSendId = -1
            setOlineCLI()
            var tmp = response.body()?.asJsonObject
            val error = response.errorBody()?.byteString()
            if (tmp != null) {
                if (response.code() == 201 && tmp.get("insert").asString == "ok"){
                    JuniorApplication.setTimbrOnServer(id_timbr)
                    InvioDatiThread.lastSendId = id_timbr
                    log.insertLog("SALVATAGGIO TIMBRATURA (id: $id_timbr) SU SERVER: OK")
                }

            }
            else if(tmp == null && response.code() == 401 && response.raw().message == "Unauthorized"){
                log.insertLog("SALVATAGGIO TIMBRATURA SU SERVER: NON AUTORIZZATO (codice err. 300)")
            }
            else{
                log.insertLog("SALVATAGGIO TIMBRATURA SU SERVER: ERRORE SCONOSCIUTO (codice err. 305)")
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            InvioDatiThread.lastSendId = -1
            setOfflineCLI()
            log.insertLog("Richiesta SALVATAGGIO TIMBRATURA SU SERVER: ERROR (codice err. 304)-> cause: ${t.cause}   msg: ${t.message}")
        }
    }


    class TimbrRequest(timbr:TimbrTable, guid:String, db:String){
        val tipo = "presenza"
        val transponder = "null"
        val anno = FORMATDATEHOURS.parse(timbr.dataOra)?.let { ANNO.format(it) }
        val mese = FORMATDATEHOURS.parse(timbr.dataOra)?.let { MESE.format(it) }
        val giorno = FORMATDATEHOURS.parse(timbr.dataOra)?.let { GIORNO.format(it) }
        val ora = FORMATDATEHOURS.parse(timbr.dataOra)?.let { ORA.format(it) }
        val minuti = FORMATDATEHOURS.parse(timbr.dataOra)?.let { MINUTI.format(it) }
        val secondi = FORMATDATEHOURS.parse(timbr.dataOra)?.let { SECONDI.format(it) }
        val causale = timbr.causale.toString()
        val latitudine = timbr.latitude.toString()
        val longitudine = timbr.longitude.toString()
        val attendibilita = timbr.accuracy
        val gps_data = "GPSDATA"
        val gps_ora = "GPSORA"
        val dip_id = timbr.dip_id
        val data_ora_automatica:Int = let{
            if(timbr.autoTime){1}
            else{0}
        }


        var ck :String

        init{
            val normalCk = tipo.trim(' ') +
                    transponder.trim(' ') +
                    anno?.trim(' ') +
                    mese?.trim(' ')+
                    giorno?.trim(' ')+
                    ora?.trim(' ')+
                    minuti?.trim(' ')+
                    secondi?.trim(' ')+
                    causale.trim(' ')+
                    latitudine.trim(' ')+
                    longitudine.trim(' ')+
                    attendibilita.toString().trim(' ') +
                    gps_data.trim(' ')+
                    gps_ora.trim(' ')+
                    dip_id.trim(' ')+
                    db.trim(' ') +
                    guid.trim(' ') +
                    data_ora_automatica.toString().trim(' ') +
                    "KljhdeS@0"
            ck = Generator.createCheckHash(Generator.createCheckHash(normalCk))
        }

    }

    //GIUSTIFICAZIONI

    fun sendGiustifiche(giustifica:GiustificheRecord){
        istantiateapi()
        if(apiCliente!= null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && db != null && serverId != null){
                val tmp = GiustRequest(giustifica)
                val call = apiCliente?.sendGiustifiche(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    tmp
                )
                call?.enqueue(GiustCallaback(giustifica.id))
                log.insertLog("Invio richiesta SALVATAGGIO RICHIESTA GIUSTIFICAZIONE SU SERVER")
            }
        }
    }

    private class GiustCallaback(val id_giust:Long): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            setOlineCLI()
            var tmp = response.body()?.asJsonObject
            val error = response.errorBody()?.byteString()
            if (tmp != null) {
                if (response.code() == 201 && tmp.get("insert").asString == "ok" && tmp.has("gz_id")){
                    val serverId = tmp.get("gz_id").asLong
                    JuniorApplication.setGiustOnServer(id_giust, serverId)
                    log.insertLog("INVIO RICHIESTA GIUSTIFICAZIONE (id: $id_giust) SU SERVER: OK")
                }
            }
            if((tmp == null && response.code() == 401 && response.raw().message == "Unauthorized") || response.code() == 404){
                log.insertLog("INVIO RICHIESTA GIUSTIFICAZIONE SU SERVER: NON AUTORIZZATO (codice err. 300)")
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            setOfflineCLI()
            log.insertLog("INVIO SALVATAGGIO RICHIESTA GIUSTIFICAZIONE SU SERVER: ERROR (codice err. 304)-> cause: ${t.cause}   msg: ${t.message}")
        }
    }

    fun getGiustifiche(observer: PropertyChangeListener){
        istantiateapi()
        if(apiCliente!= null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && db != null && serverId != null){
                val call = apiCliente?.getGiustifiche(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL
                )
                call?.enqueue(GiustDownloadCallback(observer))?: observer.propertyChange(PropertyChangeEvent("NetworkController", "sendTimbr", "ERROR", "ERROR"))
                log.insertLog("Invio richiesta DOWNLOAD RICHIESTE GIUSTIFICAZIONI PRESENTI SUL SERVER")
            }
        }
    }

    private class GiustDownloadCallback(val observer: PropertyChangeListener): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            setOlineCLI()
            var tmp = response.body()?.asJsonObject
            val error = response.errorBody()?.byteString()
            if (tmp != null) {
                if (response.code() == 200 && tmp.has("giustificazioni")){
                    log.insertLog("DOWNLOAD GIUSTIFICAZIONI DAL SERVER: ok")
                    observer.propertyChange(PropertyChangeEvent("NetworkController", "giust",  true, response))
                }
            }
            if((tmp == null && response.code() == 401 && response.raw().message == "Unauthorized") || response.code() == 404){
                log.insertLog("DOWNLOAD GIUSTIFICAZIONI DAL SERVER: NON AUTORIZZATO (codice err. 300)")
                observer.propertyChange(PropertyChangeEvent("NetworkController", "giust",  "Unauthorized", "Unauthorized"))
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            setOfflineCLI()
            log.insertLog("DOWNLOAD GIUSTIFICAZIONI DAL SERVER: ERROR (codice err. 304)-> cause: ${t.cause}   msg: ${t.message}")
            observer.propertyChange(PropertyChangeEvent("NetworkController", "giust",  false, t.toString()))
        }
    }

    class GiustRequest(giustifica:GiustificheRecord){
        val dip_id = giustifica.dip_id
        val gt_id = giustifica.giu_type_id
        val dal = giustifica.data_inizio
        val al = giustifica.data_fine
        val valore = giustifica.valore
        val festivi = giustifica.festivi
        val note = giustifica.note
        val dalle = giustifica.ora_inizio
        val alle = giustifica.ora_fine
    }

    fun deleteGiust(giust:GiustificheRecord?, observer: PropertyChangeListener){
        istantiateapi()
        if(apiCliente!= null && ActivationController.isActivated() && giust!= null){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && db != null && serverId != null){
                val request = DeleteRequest(giust)
                val call = apiCliente?.deleteGiustifica(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    request
                )
                call?.enqueue(GiustDeleteCallback(giust.giu_id, observer))
                log.insertLog("Invio richiesta ANNULLAMENTO RICHIESTA GIUSTIFICATIVO ID:$giust")
            }
        }
    }

    private class GiustDeleteCallback(val id:Long?, val observer: PropertyChangeListener): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            setOlineCLI()
            var tmp = response.body()?.asJsonObject
            val error = response.errorBody()?.byteString()
            if (tmp != null) {
                if (response.code() == 200 && tmp.has("update") && tmp.get("update").asString == "ok"){
                    log.insertLog("INVIO RICHIESTA ANNULLAMENTO GIUSTIFICAZIONI DAL SERVER ID: $id: ok")
                    observer.propertyChange(PropertyChangeEvent("NetworkController", "DELETE GIUSTIFICA",  true, response))
                }
            }
            if(tmp == null && response.code() == 400 || response.code() == 404){
                log.insertLog("INVIO RICHIESTA ANNULLAMENTO GIUSTIFICAZIONI DAL SERVER: ERRORE GENERICO (codice err. 300)")
                observer.propertyChange(PropertyChangeEvent("NetworkController", "DELETE GIUSTIFICA",  "ERROR", "ERROR"))
            }
            if(tmp == null && response.code() == 401 && response.raw().message == "Unauthorized"){
                log.insertLog("INVIO RICHIESTA ANNULLAMENTO GIUSTIFICAZIONI DAL SERVER: NON AUTORIZZATO (codice err. 300)")
                observer.propertyChange(PropertyChangeEvent("NetworkController", "DELETE GIUSTIFICA",  "Unauthorized", "Unauthorized"))
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            setOfflineCLI()
            log.insertLog("INVIO RICHIESTA ANNULLAMENTO GIUSTIFICAZIONI DAL SERVER: ERROR (codice err. 304)-> cause: ${t.cause}   msg: ${t.message}")
            observer.propertyChange(PropertyChangeEvent("NetworkController", "DELETE GIUSTIFICA",  false, t.toString()))
        }
    }

    class DeleteRequest(giustifica:GiustificheRecord){
        val gz_id = giustifica.giu_id
        val gz_richiesto = "annullato"
    }


    fun getFileNames(observer: PropertyChangeListener){
        istantiateapi()
        if(apiCliente!=null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && serverId != null){
                val call = apiCliente?.getFileNames(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL
                )
                call?.enqueue(FileNamesCallback(observer))
            }
        }
    }

    private class FileNamesCallback(val observer: PropertyChangeListener): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            val body = response.body()
            if(body != null && response.code() != 404){
                observer.propertyChange(PropertyChangeEvent("NetworkController", "GET NOMI FILE", response, response.body()))
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkController", "GET NOMI FILE", "FAIL", "FAIL"))
            }
        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            observer.propertyChange(PropertyChangeEvent("NetworkController", "GET NOMI FILE", "FAIL", "FAIL"))
        }
    }

    fun getFile(directory: String, url:String, observer: PropertyChangeListener){
        istantiateapi()
        if(apiCliente!= null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val request = FIleRequest(url)
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && serverId != null){
                val call = apiCliente?.getFile(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    request.url
                )
                call?.enqueue(FileCallback(observer, directory))
            }
        }
    }

    fun getCartellino(directory: String, annoMese:String, observer: PropertyChangeListener){
        istantiateapi()
        if(apiCliente!= null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val request = CartellinoRequest(annoMese)
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && serverId != null){
                val call = apiCliente?.getCartellino(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    request.m
                )
                call?.enqueue(FileCallback(observer, directory))
            }
        }
        else{
            observer.propertyChange(PropertyChangeEvent("NetworkController", "FILE DOWNLOAD", "FAIL", "FAIL"))
        }
    }

    class CartellinoRequest(annoMese:String){
        val m = annoMese
    }

    private class FileCallback(val observer: PropertyChangeListener, val directory:String): Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.code() != 404 && response.body()?.contentType() != null){
                saveFile(response.body(), directory)
                observer.propertyChange(PropertyChangeEvent("NetworkController", "FILE DOWNLOAD", response, response.body()?.toString()))
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkController", "FILE DOWNLOAD", "FAIL", "FAIL"))
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            observer.propertyChange(PropertyChangeEvent("NetworkController", "FILE DOWNLOAD", "FAIL", "FAIL"))
        }
    }
    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return pathWhereYouWantToSaveFile
        }catch (e:Exception){
            Log.e("saveFile",e.toString())
        }
        finally {
            input?.close()
        }
        return ""
    }

    class FIleRequest(val url: String)

    fun inviaRispostaFile(file: NomiFileTable, observer: PropertyChangeListener){
        istantiateapi()
        if(apiCliente!= null && ActivationController.isActivated()){
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val request = InviaRispostaFile(file)
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && serverId != null){
                val call = apiCliente?.inviaRisposta(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL,
                    request
                )
                call?.enqueue(RispostaFileCallback(observer))
            }
        }
    }

    class InviaRispostaFile(file:NomiFileTable){
        val fil_id = file.fil_id
        val fld_risposta = file.fld_risposta
    }

    private class RispostaFileCallback(val observer: PropertyChangeListener): Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.code() != 404 && response.body()?.contentType() != null){
                observer.propertyChange(PropertyChangeEvent("NetworkController", "FILE DOWNLOAD", response, response.body()?.toString()))
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkController", "FILE DOWNLOAD", "FAIL", "FAIL"))
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            observer.propertyChange(PropertyChangeEvent("NetworkController", "FILE DOWNLOAD", "FAIL", "FAIL"))
        }
    }


    private fun hoursToMinutes(time: String) : String{
        try{
            val date = FORMATTIME.parse(time)
            val c = GregorianCalendar()
            c.time = date!!
            val h = c.get(Calendar.HOUR_OF_DAY)
            val m = c.get(Calendar.MINUTE)
            val res = (h*60) + m

            return res.toString()
        }
        catch (e:java.lang.Exception){
            val j = e
        }
        return time
    }

    private fun doubleToMinutes(va: String) : String{
        try{
            var h = "0"
            var m = "0"
            if(va.contains(':')){
                h = va.substringBefore(':')
                m = va.substringAfter(':')
            }
            else{
                h = va
            }

            val res = (h.toInt()*60) + m.toInt()
            return res.toString()
        }
        catch (e:java.lang.Exception){
            val j = e
            try{
                var h = "0"
                var m = "0"
                if(va.contains('.')){
                    h = va.substringBefore('.')
                    m = va.substringAfter('.')
                }
                else{
                    h = va
                }

                val res = (h.toInt()*60) + m.toInt()
                return res.toString()
            }
            catch (e:java.lang.Exception){
                val j = e
            }
        }
        return va
    }

    fun testServerCliente(observer:PropertyChangeListener) {
        istantiateapi()
        if (apiCliente != null && UserRepository.logged) {
            val guid = ParamManager.getGuid()
            val db = ParamManager.getArchivio()
            val codice = ParamManager.getCodice()
            val serverId = ParamManager.getLastUserId()
            if(guid!= null && db != null && serverId != null){
                val tmp = ""
                val call = apiCliente?.testServerCliente(
                    db,
                    serverId,
                    JuniorApplication.myKeystore.activeKey ?: "null",
                    guid,
                    codice ?: "null",
                    BuildConfig.VERSION_NAME,
                    Build.MODEL
                )
                call?.enqueue(TestClienteCallback(observer))
            }
        }
    }

    private class TestClienteCallback(val observer:PropertyChangeListener): Callback<JsonElement> {
        override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
            log.insertLog("TEST SERVER CLIENTE: CONNESSIONE OK")
            setOlineCLI()
            if(response is Response<*> && response.errorBody() != null){
                observer.propertyChange(PropertyChangeEvent("NetworkController", "testCliente",  "ERROR", "ERROR"))
            }
            else{
                observer.propertyChange(PropertyChangeEvent("NetworkController", "testCliente",  "OK", "OK"))
            }

        }

        override fun onFailure(call: Call<JsonElement>, t: Throwable) {
            log.insertLog("TEST SERVER CLIENTE: CONNESSIONE FALLITA (codice err. 401")
            setOfflineCLI()
            observer.propertyChange(PropertyChangeEvent("NetworkController", "testCliente",  "ERROR", "ERROR"))
        }
    }
}