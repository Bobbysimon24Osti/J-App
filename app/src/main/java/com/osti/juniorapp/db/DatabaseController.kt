package com.osti.juniorapp.db

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.osti.juniorapp.application.JuniorUser
import com.osti.juniorapp.db.resolvers.JuniorNotificheResolver
import com.osti.juniorapp.db.tables.AngelTable
import com.osti.juniorapp.db.tables.DipendentiTable
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.db.tables.GiustificheTable
import com.osti.juniorapp.db.tables.JuniorConfigTable
import com.osti.juniorapp.db.tables.LogTable
import com.osti.juniorapp.db.tables.NomiFileTable
import com.osti.juniorapp.db.tables.NotificheTable
import com.osti.juniorapp.db.tables.TimbrTable
import com.osti.juniorapp.db.tables.UserTable
import com.osti.juniorapp.utils.GiustificheConverter
import com.osti.juniorapp.utils.JuniorLicenza
import com.osti.juniorapp.utils.LogController
import com.osti.juniorapp.utils.Utils.FORMATDATE
import com.osti.juniorapp.utils.Utils.PIVADB
import com.osti.juniorapp.utils.Utils.RAGSOCDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.Calendar

class DatabaseController (context: Context) {


    companion object{
        lateinit var myDB : JuniorDatabase

        var permessoTimbraVirtuale = 0
        var permessoTimbraturaGps = 0
    }

    init {
        myDB = JuniorDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).async {
            GiustificheConverter.giustifiche = getOfflineGiust()
        }
    }

    private suspend fun getOfflineGiust(): List<GiustificheTable> {
        return myDB.mGiustificheDao().getAllGiustifiche()
    }

    /*
    MISTI
     */

    fun getLicenseInfo(observer:PropertyChangeListener){
        CoroutineScope(Dispatchers.IO).async {
            val code = getActivationCode()
            val ragSoc = getConfig(RAGSOCDB).valore
            val pIva = getConfig(PIVADB).valore
            val attivazione = getDataAttivazione()

            val licenza =JuniorLicenza(code, ragSoc, pIva, attivazione)

            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET LICENSE", licenza, licenza))
        }
    }

    /*
    PARAMETRI
     */

    fun setActivationCode(code:String){
        CoroutineScope(Dispatchers.IO).async{
            myDB.mParametriDao().setCodiceAttivazione(code)
            myDB.mParametriDao().setDataAttivazione(FORMATDATE.format(Calendar.getInstance().timeInMillis))
        }
    }
    fun getActivationCode(observer: PropertyChangeListener) {
        CoroutineScope((Dispatchers.IO)).async {
            val cod = myDB.mParametriDao().getCodiceAttivazione()
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GETCODE", cod, cod))
        }
    }
    fun getActivationCode(): String?{
        return myDB.mParametriDao().getCodiceAttivazione()
    }
    fun getGuid():String? = runBlocking{
        return@runBlocking myDB.mParametriDao().getGuid()
    }
    fun setGuid(guid:String){
        CoroutineScope(Dispatchers.IO).async{
            myDB.mParametriDao().setGuid(guid)
        }
    }
    fun setUrl(url:String){
        CoroutineScope(Dispatchers.IO).async{
            myDB.mParametriDao().setUrl(url)
        }
    }
    fun getUrl():String? = runBlocking{
        return@runBlocking myDB.mParametriDao().getUrl()
    }
    fun getArchivio(observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val archivio = myDB.mParametriDao().getArchivio()
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GETARCHIVIO", archivio, archivio))
        }

    }
    fun setArchivio(archivio:String) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mParametriDao().setArchivio(archivio)
        }
    }
    fun setLastUserId(id:String?) {
        if(id == null){
            JuniorUser.userLogged = false
        }
        CoroutineScope(Dispatchers.IO).async{
            myDB.mParametriDao().setLastUserId(id)
        }
    }
    fun getLastUserId(observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val userId = myDB.mParametriDao().getLastUserId()
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET LAS USER ID", userId, userId))
        }

    }
    fun setTipoApp(tipo:String?){
        CoroutineScope(Dispatchers.IO).async{
            myDB.mParametriDao().setTipoApp(tipo)
        }
    }
    fun getTipoApp():String? = runBlocking{
        return@runBlocking myDB.mParametriDao().getTipoApp()
    }

    suspend fun getDataAttivazione(): String? {
        return myDB.mParametriDao().getDataAttivazione()
    }

    fun getIdApp():Long? = runBlocking{
        return@runBlocking myDB.mParametriDao().getIdApp()
    }

    fun setIdApp(id:Long) {
        CoroutineScope(Dispatchers.IO).async {
            myDB.mParametriDao().setIdApp(id)
        }
    }

    /*
    UTENTI
     */
    fun getUser(user_id:String, observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val user = myDB.mUsersDao().getUser(user_id)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GETUSER", user, user))
        }

    }

    fun creaUser(user:UserTable) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mUsersDao().creaUser(user)
            myDB.mParametriDao().setLastUserId(user.server_id)
        }
    }
    fun updateUser(user:UserTable) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mUsersDao().setUser(user.name, user.type, user.perm_timbrature, user.perm_workflow, user.badge, user.idDipendente, user.server_id, user.nascondi_timbrature, user.livello_manager)
        }
    }



    /*
    TIMBRATURE
     */
    fun creaTimbr(timbratura:TimbrTable, observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async{
            val id = myDB.mTimbrDao().creaTimbr(timbratura)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "CREA TIMBR", id, id))
        }
    }
    fun setCitta(id:Long, citta:String){
        CoroutineScope(Dispatchers.IO).async {
            myDB.mTimbrDao().setCitta(id, citta)
        }
    }
    fun setTimbrUploaded (id:Int)  {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mTimbrDao().updateTimbronServer(id)
        }
    }

    fun getTimbrFlow (dipId: Long): Flow<TimbrTable?>{
        return myDB.mTimbrDao().getLastOfflineTimbr(dipId)
    }

    fun getAllTimbr(dip:Long, observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val timbrList = myDB.mTimbrDao().getAllTimbr(dip)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET ALL TIMBR", timbrList, timbrList))
        }

    }
    fun getTimbr(id:Long):TimbrTable = runBlocking{
        return@runBlocking myDB.mTimbrDao().getTimbr(id)
    }
    fun getlastStampDipendente(serverId: Long, observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val stamp = myDB.mTimbrDao().getLastTimbrByDip(serverId)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET LAST STAMP DIPENDENTE", stamp, stamp))
        }
    }
    fun getlastStampDipendenteLive(serverId: Long):Flow<Boolean> {
        return myDB.mTimbrDao().getLiveLastTimbrByDip(serverId)
    }

    /*
    DIPENDENTE
     */
    fun creaDipendente(dip:DipendentiTable) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mDipDao().creaDip(dip)
        }
    }

    fun getDipendente(serverId:Long?, observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val dip = myDB.mDipDao().getDip(serverId ?:-900)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET LAST STAMP DIPENDENTE", dip, dip))
        }
    }

    /*
    CONFIGURAZIONE JUNIOR
     */
    fun creaConfig(config: JuniorConfigTable) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mConfigDao().insert(JuniorConfigTable(config.nome, config.valore))
        }
    }

    fun creaMultipleConfig(config: List<JuniorConfigTable>)  {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mConfigDao().multipleInsert(config)
        }
    }

    fun getConfig(nome:String) :JuniorConfigTable {
        return myDB.mConfigDao().getParam(nome)
    }

    fun getLicenzaParams(observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val params = myDB.mConfigDao().getLincenzaParam()
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET PARAMETRI LICANZA", params, params))
        }
    }

    fun getSpinnerConfigFlow(): Flow<JuniorConfigTable> {
        return myDB.mConfigDao().getSpinnerConfigFlow()
    }

    fun getAllConfig() : JSONArray = runBlocking {
        return@runBlocking JSONArray(myDB.mConfigDao().getAllParams().toString())
    }

    fun getNomiConfigs() : List<String> = runBlocking {
        return@runBlocking myDB.mConfigDao().getNomi()
    }


    /*
    ANGEL
     */

    fun insertAngel(angel:AngelTable) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mAngelDao().insert(angel)
        }

    }

    fun getAngel(observer: PropertyChangeListener){
        CoroutineScope(Dispatchers.IO).async {
            val angel = myDB.mAngelDao().getValue()
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET ANGEL", angel, angel))
        }
    }


    /*
    GIUSTIFICAZIONI
     */
    fun insertGiustifica(giustifica:GiustificheTable) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mGiustificheDao().insert(giustifica)
        }

    }
    val nessunaCausale = "{\"gt_id\":\"0\",\"gt_abb\":\"\",\"gt_nome\":\"Nessuna Causale\",\"gt_tipo\":\"sw\"}"
    fun creaMultipleGiustifiche(giustifiche: List<GiustificheTable>) {

        CoroutineScope(Dispatchers.IO).async{
            myDB.mGiustificheDao().svuotaGiustifiche()
            //Inserisco la prima causale vuota per avere l'opzione in fase di timbratura
            myDB.mGiustificheDao().insert(GiustificheTable(0, "", nessunaCausale))
            myDB.mGiustificheDao().multipleInsert(giustifiche)
            GiustificheConverter.giustifiche = myDB.mGiustificheDao().getAllGiustifiche()
        }
    }

    fun getGiustifiche(): List<GiustificheTable> {
        return myDB.mGiustificheDao().getAllGiustifiche()
    }

    fun getGiustificheNoCausaleVuota(): List<GiustificheTable> = runBlocking{
        return@runBlocking myDB.mGiustificheDao().getAllGiustificheNoCausaleVuota()
    }

    fun getGiustificheFlow(): Flow<List<GiustificheTable>> {
        return myDB.mGiustificheDao().getAllGiustificheFlow()
    }

    fun getGiustificheFlowNoCausaleVuota(): Flow<List<GiustificheTable>> {
        return myDB.mGiustificheDao().getAllGiustificheFlowNoCausaleVuota()
    }

    fun getAbbreviazioniGiustifiche(): List<String> = runBlocking{
        return@runBlocking myDB.mGiustificheDao().getAllAbbrevGiustifiche()
    }

    fun getAbbreviazioniGiustificheNoCausaleVuota(): List<String> = runBlocking{
        return@runBlocking myDB.mGiustificheDao().getAllAbbrevGiustificheNoCausaleVuota()
    }

    fun getGiustificaById(id:Long)= runBlocking{
        return@runBlocking myDB.mGiustificheDao().getGiustificaById(id)
    }


    /*
    LOGS
     */

    fun creaLog(type:String, msg:String, dataOra:String) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mLogDao().insert(LogTable(message = msg, type = type, dataOra = dataOra))
        }
    }

    fun getLogByType(type:String): List<LogTable> = runBlocking {
        return@runBlocking myDB.mLogDao().getLogByType(type)
    }

    fun getAllLogs(): List<LogTable> = runBlocking {
        return@runBlocking myDB.mLogDao().getAllLogs()
    }

    fun getLastLog(observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val log = myDB.mLogDao().getLastLog()
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET LAST LOG", log, log))
        }
    }

    /*
    GIUSTIFICHE RECORD (RICHIESTE DI GIUSTIFICAZIONI)
     */
    val log = LogController(LogController.GIUST)
    fun creaGiustificheRecord(giust: GiustificheRecord){
        CoroutineScope(Dispatchers.IO).async{
              val i = myDB.mGiustificheRecordDao().insert(giust)
        }
    }

    fun clearGiust(){
        CoroutineScope(Dispatchers.IO).async {
            myDB.mGiustificheRecordDao().clearGiust()
        }
    }

    fun setGiustOnServer(id:Long, serverId:Long) {
        CoroutineScope(Dispatchers.IO).async{
            myDB.mGiustificheRecordDao().setOnServer(id, serverId)
        }
    }

    /**
     * Inserire SERVER ID
     */


    fun setGiustAnnullato(serverId:Long?) {
        if(serverId != null){
            CoroutineScope(Dispatchers.IO).async{
                myDB.mGiustificheRecordDao().setAnnullato(serverId)
            }
        }
    }

    fun getSingleGiustFlow(dipId:Long): Flow<GiustificheRecord?> {
        return myDB.mGiustificheRecordDao().getSingleGiustFlow(dipId)
    }

    fun getAllGiustByDip(dip: Long, observer: PropertyChangeListener){
        CoroutineScope(Dispatchers.IO).async {
            val giust = myDB.mGiustificheRecordDao().getAllGiustificheByDip(dip)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET GIUST BY DIPENDENTE", giust, giust))
        }
    }

    fun getGiustificaRecordByLocalId(id:Long, observer: PropertyChangeListener){
        CoroutineScope(Dispatchers.IO).async {
            val giust = myDB.mGiustificheRecordDao().getGiustificaRecordByLocalId(id)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET GIUST BY LOCAL ID", giust, giust))
        }
    }

    fun getGiustByServerId(serverId:Long, observer: PropertyChangeListener) : Deferred<Unit>{
        return CoroutineScope(Dispatchers.IO).async {
            val giust = myDB.mGiustificheRecordDao().getGiustificaRecordByServerId(serverId)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET GIUST BY SERVER ID", giust, giust))
        }
    }

    fun getGiustFlowNoMieDaGestire (dipId:Long): Flow<List<GiustificheRecord?>>{
        return myDB.mGiustificheRecordDao().getGiustFlowNoMieDaGestire(dipId)
    }

    fun getGiustFlow (dip: Long): Flow<List<GiustificheRecord?>>{
        return myDB.mGiustificheRecordDao().getGiustFlow(dip)
    }

    fun setGiustApprovato(id:Long){
        CoroutineScope(Dispatchers.IO).async {
            myDB.mGiustificheRecordDao().setApprovato(id)
        }
    }

    fun setGiustApprovatoLiv1(id:Long){
        CoroutineScope(Dispatchers.IO).async {
            myDB.mGiustificheRecordDao().setApprovatoLiv1(id)
        }
    }
    fun setGiustNegato(id:Long){
        CoroutineScope(Dispatchers.IO).async {
            myDB.mGiustificheRecordDao().setNegato(id)
        }
    }

    fun getGiustFlowStorico(dipId:Long) : Flow<List<GiustificheRecord?>>{
        return myDB.mGiustificheRecordDao().getGiustFlowStorico(dipId)
    }

    /*
    NOMI FILE
     */

    fun creaNome(nome:NomiFileTable){
        CoroutineScope(Dispatchers.IO).async {
            myDB.mNomiFileDao().insert(nome)
        }
    }

    fun clearNomi(): Deferred<Unit>{
         return CoroutineScope(Dispatchers.IO).async {
            myDB.mNomiFileDao().clear()
        }
    }

    fun getNomiFlow():Flow<List<NomiFileTable>> {
        return myDB.mNomiFileDao().getNomiFlow()
    }

    fun getNomeFile(id:Long, observer:PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).async {
            val file = myDB.mNomiFileDao().getNomeFile(id)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET FILE BY ID", file, file))
        }
    }

    fun setRispostaFile(id:Long, risposta:String): Deferred<Unit>{
        return CoroutineScope(Dispatchers.IO).async {
            myDB.mNomiFileDao().setRispostaFile(id, risposta)
        }
    }

    /*
    NOTIFICHE
     */

    fun createNotificheFromJson(json:JsonArray) : Deferred<Unit>{
        return CoroutineScope(Dispatchers.IO).async {
            for (item in json){
                myDB.mNotificheDao().create(JuniorNotificheResolver(item as JsonObject).getDbTable())
            }
        }
    }


    fun getNotificheList(dipId:Long, observer: PropertyChangeListener) {
        CoroutineScope(Dispatchers.IO).launch {
            val notifiche = myDB.mNotificheDao().getNotificheList(dipId)
            observer.propertyChange(PropertyChangeEvent("DATABASE CONTROLLER", "GET NOTIFICHE LIST", notifiche, notifiche))
        }
    }

    fun setDataLettura(id:Long, dataOra:String){
        CoroutineScope(Dispatchers.IO).async {
            myDB.mNotificheDao().setDataLettura(id, dataOra)
        }
    }
}