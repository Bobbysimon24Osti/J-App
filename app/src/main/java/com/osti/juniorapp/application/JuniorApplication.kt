package com.osti.juniorapp.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.ActivationActivity
import com.osti.juniorapp.activity.ErrorActivity
import com.osti.juniorapp.activity.LoginActivity
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.application.ActivationController.checkResult
import com.osti.juniorapp.application.ActivationController.saveValore
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.DatabaseController
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.db.tables.GiustificheTable
import com.osti.juniorapp.db.tables.JuniorConfigTable
import com.osti.juniorapp.db.tables.UserTable
import com.osti.juniorapp.key.MyKeystore
import com.osti.juniorapp.preferences.JuniorShredPreferences
import com.osti.juniorapp.thread.InvioDatiThread
import com.osti.juniorapp.thread.RiceviDatiThread
import com.osti.juniorapp.utils.GiustificheConverter
import com.osti.juniorapp.utils.LogController
import com.osti.juniorapp.utils.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


class JuniorApplication : Application() {

    lateinit var log: LogController

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init(){
        myDatabaseController = DatabaseController(this)
        log = LogController(LogController.ATTIVAZIONE)

        myDatabaseController.getAngel{

            myKeystore = MyKeystore(this, it.newValue as String?)

            invioDatiThread = InvioDatiThread()

            riceviDatiThread = RiceviDatiThread()

            JuniorShredPreferences.setDBversion(Utils.DB_VERSION, this)

            //START LOCATION LISTENER
            try{
                mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            }
            catch (e:Exception){
                val i = e
            }

            ParamManager.loadFromDb{
                val guid = ParamManager.getGuid()
                val userId = ParamManager.getLastUserId()
                if(guid == null){
                    //GUID NON PRESENTE, L'APP NON E MAI STATA ATTIVATA
                    startActivationActivity()
                }
                else{
                    NetworkController.checkGuid{p0->
                        val res = checkResult(p0)
                        when (res) {
                            ActivationController.ATTIVA -> {
                                log.insertLog("Attivazione completata con successo")
                                startActivity(userId, false)
                            }
                            ActivationController.OFFLINE -> {
                                log.insertLog("Applicazione offline, impossibile raggiungere il server OSTI (codice err. 105)")
                                //APP NON RIESCE A VERIFICARE CODICE PER < 24 H
                                startActivity(userId, true)
                            }
                            ActivationController.NOATTIVA -> {
                                log.insertLog("Applicazione bloccata (codice err. 107)")
                                //IL GUID NON E CORRETTO E QUINDI VA MESSO UN NUOVO CODICE
                                startActivationActivity()
                            }
                            ActivationController.LOGOUT -> {
                                log.insertLog("Cambiato url, logout automatico")
                                startLoginActivity()
                            }
                            else -> {
                                log.insertLog("Errore generico controllo licenza")
                                startErrorActivity(resources.getString(R.string.alert_errore_licenza_generico))
                            }
                        }
                    }
                }
            }
            //CARICO L'ULTIMO CONTROLLO
            JuniorShredPreferences.loadLastCheck(this)

        }
    }

    private fun startActivity(userId:String?, offline:Boolean){
        when(checkAppStatus(userId)){
            "LOGIN"->{
                startLoginActivity()
            }
            "MAIN"->{
                startMainActivity(true, isOffline = offline)
            }
            "ACTIVATION"->{
                startActivationActivity()
            }
        }
    }


    companion object {
        lateinit var myDatabaseController: DatabaseController
        lateinit var myKeystore: MyKeystore
        lateinit var mLocationManager:LocationManager

        lateinit var invioDatiThread:InvioDatiThread
        lateinit var riceviDatiThread:RiceviDatiThread

        var myJuniorUser: MutableLiveData<JuniorUser?> = MutableLiveData(null)
        var isUserChecked = false

        fun setAppActivated() {
            isUserChecked = true
        }

        fun setApiCliente() :Boolean{
            if(ParamManager.getUrl() != null && NetworkController.apiCliente == null){
                return true
            }
            if(NetworkController.apiCliente != null){
                return true
            }
            return false
        }

        fun setTimbrOnServer(id_timbr:Int){
            myDatabaseController.setTimbrUploaded(id_timbr)
        }

        fun setGiustOnServer(id_giust:Long, serverId: Long){
            myDatabaseController.setGiustOnServer(id_giust, serverId)
        }


        /** Invio sia timbrature che giustificazioni*/
        fun inviaDati() {
            if (ActivationController.isActivated()
                && myJuniorUser.value != null){
                invioDatiThread.isGiustStarted = false
                invioDatiThread.isTimbrStarted = false
                invioDatiThread.checkForSending()
            }
        }

        fun riceviDati(serverId:String? = null, key:String? = null){
            if(serverId != null || key != null){
                riceviDatiThread.downloadFromServer(serverId, key)
            }
            else if (ActivationController.isActivated()
                && myJuniorUser.value != null){
                riceviDatiThread.downloadFromServer()
            }
        }

        fun setLastUser(user: JuniorUser?){
            if(user== null){
                isUserChecked = false
            }
            ParamManager.setLastUserId(user?.getServerId())
            myJuniorUser.value = user
        }

        fun updateUserOnDb(user:UserTable){
            myDatabaseController.updateUser(user)
        }
        fun updateUserParams(params: JsonObject, serverId:String, key:String){
            val utente = params.get("utente")?.asJsonObject ?: return
            val name = utente.get("ute_nome")?.asString?: return
            val type = utente.get("ute_accesso")?.asString?: return
            val permTimbratura = utente.get("ute_timbrvirtuale")?.asString?: return
            val permWorkFlow = utente.get("ute_workflow")?.asString?: return
            val permCartellino = utente.get("ute_cartellini")?.asString?: return
            val serverIdDipendente = utente.get("ute_dipautorizzato")?.asLong ?:return
            val nascondiTimbrature = utente.get("ute_nasconditimbrature")?.asString ?:return
            val livelloManager = utente.get("ute_liv_manager")?.asString ?:return

            val jsDipendente = params.get("dipendente")?.asJsonObject?: return
            val newDip = createDip(jsDipendente, serverIdDipendente)

            val jsConfig = params.get("parametri")?.asJsonObject?: return
            if(jsConfig.has("versione_jweb")){
                ParamManager.setVersioneJW(jsConfig.get("versione_jweb").asString)
            }
            checkConfig(jsConfig)

            val jsGiustificativi = params.get("giustificativi")?.asJsonArray?: return
            checkGiustifiche(jsGiustificativi)

            //Verifica se i dati sono uguali o vanno ad aggiornare effettivamente qualcosa
            val oldUser = myJuniorUser.value
            val newUser = JuniorUser(
                serverId,
                key,
                name,
                type,
                permTimbratura,
                permWorkFlow,
                permCartellino,
                nascondiTimbrature,
                livelloManager,
                newDip
            )

            if (oldUser.hashCode() != newUser.hashCode()){
                setLastUser(newUser)
            }
            //context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            //activity.finish()
            val strPIvaDb = jsConfig.get("lic_piva").asString + "_" + ParamManager.getArchivio()
            val strUserId = "ute_id_" + newUser.serverIdUser
            Firebase.messaging.subscribeToTopic(strPIvaDb)
            Firebase.messaging.subscribeToTopic(strUserId)
        }

        fun updateLocalGiust (datas:JsonArray){
            for(i in 0 until datas.size()){
                myDatabaseController.creaGiustificheRecord(GiustificheConverter.getRecordFromJson(datas[i] as JsonObject))
            }
        }


        fun setLastFragment(fragment:String?, context: Context?){
            if (fragment!= null && context != null){
                JuniorShredPreferences.setSharedPref(fragment, "LAST FRAGMENT", context)
            }
        }

        private fun checkGiustifiche(giust:JsonArray){
            val json =JSONArray(giust.toString())

            val giustificazioni = arrayListOf<GiustificheTable>()

            for(x in 0 until json.length()){
                val giustificativo = JSONObject(json[x].toString())
                val id = giustificativo.get("gt_id") as String
                val abbreviativo = giustificativo.get("gt_abb") as String
                giustificazioni.add(GiustificheTable(id.toLong(), abbreviativo, giustificativo.toString()))
            }

            if(giustificazioni.isNotEmpty()){
                myDatabaseController.creaMultipleGiustifiche(giustificazioni)
            }
        }

        private fun checkConfig(configs: JsonObject){
            val json =JSONObject(configs.toString())
            val nomiResponse = json.names()

            val newConfigs = arrayListOf<JuniorConfigTable>()

            for (i in 0 until configs.size()){
                val configTmp = JuniorConfigTable(nomiResponse!![i].toString(), configs[nomiResponse[i].toString()].asString)
                newConfigs.add(configTmp)
                saveValore(configTmp)
            }

            myDatabaseController.creaMultipleConfig(newConfigs)
        }

        private fun createDip(dip:JsonObject, id:Long) : JuniorUser.JuniorDip {

            val dipName:String = try{
                dip.get("dip_nome")?.asString?: "null"
            } catch (e:Exception){
                "null"
            }

            val badge:Int = try{
                dip.get("dip_badge").asInt
            } catch (e:Exception){
                -1
            }
            val assunto:String = try{
                dip.get("dip_assunto").asString
            } catch (e:Exception){
                "null"
            }

            val licenziato:String = try{
                dip.get("dip_licenziato").asString
            } catch (e:Exception){
                "null"
            }

            return JuniorUser.JuniorDip(id, dipName, badge, assunto, licenziato)
        }

        /**
         * Ritorna la schermata che va aperta quando deve essere ripristinata la grafica, se la schermata è la Main non fa altro che ricaricare lo user in memoria (JuniorUser)
         * */
        fun checkAppStatus(userId:String?) : String{
            val code = ParamManager.getCodice()
            if(userId== null && code == "null" || code == null){
                //Se nullo sia codice attivazione che id utente vuol dire che non ha mai fatto la verifica del codice di attivazione
                return "ACTIVATION"
            }
            else if (userId == null){
                //Se nullo solo id utente vuol dire che non ha mai fatto l'acesso nessuno qunidi va mostrato il login
                return "LOGIN"
            }
            else{
                //Se c'è un id allora un utente è già registrato, e possiamo entrare con la chiave
                myJuniorUser.value = JuniorUser(userId)
                //login()
                return "MAIN"
            }
        }

        fun getDirFiles(activity: Activity?): String{
            val dir = activity?.filesDir?.absolutePath  + "/" + JuniorApplication.myJuniorUser.value?.name
            val file = File(dir)
            if(!file.exists()){
                file.mkdirs()
            }
            return dir
        }
    }

    private fun startActivationActivity(){
        startActivity(
            Intent(this, ActivationActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    private fun startErrorActivity(msg:String){
        startActivity(
            Intent(this, ErrorActivity::class.java)
                .putExtra("ERROR", msg)
                .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    private fun startLoginActivity(){
        startActivity(
            Intent(this, LoginActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    private fun startMainActivity(isLoading:Boolean = false, isOffline:Boolean = false){
        val tmpIntent = Intent(this, MainActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )
        if(isLoading){
            tmpIntent.putExtra("FIRSTLOAD", true)
        }
        if(isOffline){
            tmpIntent.putExtra("OFFLINE", true)
        }
        startActivity(tmpIntent)
    }

}