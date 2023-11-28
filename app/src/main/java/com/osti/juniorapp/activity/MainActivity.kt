package com.osti.juniorapp.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isNotEmpty
import androidx.drawerlayout.widget.DrawerLayout
import com.google.gson.JsonElement
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.fragment.file.FileFragment
import com.osti.juniorapp.R
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.DipendentiRepository
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorApplication.Companion.myDatabaseController
import com.osti.juniorapp.application.JuniorApplication.Companion.setLastFragment
import com.osti.juniorapp.application.StatusController
import com.osti.juniorapp.application.Updater
import com.osti.juniorapp.application.UserRepository
import com.osti.juniorapp.network.NetworkAggiornaApp
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.network.NetworkListener
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.fragment.AccountFragment
import com.osti.juniorapp.fragment.CartellinoFragment
import com.osti.juniorapp.fragment.giustificazioni.GiustificativiDettagliFragment
import com.osti.juniorapp.fragment.giustificazioni.OldGiustificheListFragment
import com.osti.juniorapp.fragment.InfoFragment
import com.osti.juniorapp.fragment.NotificheFragment
import com.osti.juniorapp.fragment.timbrature.OldStampFragment
import com.osti.juniorapp.fragment.timbrature.TimbrVirtualeFragment
import com.osti.juniorapp.fragment.UserLoadWaitFragment
import com.osti.juniorapp.fragment.giustificazioni.GiustificheFragmentSelection
import com.osti.juniorapp.menu.MyConstraintLayout
import com.osti.juniorapp.menu.MyMenuTextView
import com.osti.juniorapp.preferences.JuniorShredPreferences
import com.osti.juniorapp.utils.NavigationMenuOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import retrofit2.Response
import kotlin.system.exitProcess


var isNotifichePermissionRequested = false

class MainActivity : AppCompatActivity(){

    lateinit var navigationMenu : LinearLayout
    lateinit var drawerLayout: DrawerLayout
    lateinit var textViewTitolo: TextView
    lateinit var buttonMenu: ImageView

    lateinit var constraintNavView: ConstraintLayout
    lateinit var textViewStatus: TextView

    lateinit var mPermissionLauncher: ActivityResultLauncher<String>

    val liveUser = UserRepository(ParamManager.getLastUserId()).getLiveUser()

    private fun showAlertAppOffline(){
        runOnUiThread(){
            AlertDialog.Builder(this)
                .setTitle("Attenzione")
                .setMessage(R.string.alert_app_offline)
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    var justCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.main_toolbar))

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        /**
         * Carico in memoria lo user nel db
         */
        loadUserFromDb()
        runOnUiThread{
            init()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotificheRequest()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotificheRequest() {
        if (ActivationController.canTimbrGps() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                mPermissionLauncher =
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                        isNotifichePermissionRequested = true
                        if(!it){
                            AlertDialog.Builder(this)
                                .setTitle("Permesso notifiche negato")
                                .setMessage("Consentire notifiche per permettere di ricevere le comunicazioni riguardanti buste paga e giustificativi")
                                .setPositiveButton("Apri Impostazioni") { _, _ ->
                                    startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
                                }
                                .setNegativeButton("Nega", null)
                                .show()
                        }
                    }

                mPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var firstime = true

    override fun onResume() {
        if(!firstime){
            Updater.updateServer(this)
        }
        else{
            Updater.updateNoCheck()
        }
        firstime = false
        //startRIghtFragment()
        if (intent.extras?.getString("notifica") == "notifica"){
            setLastFragment(NotificheFragment::class.simpleName, this)
            startRIghtFragment()
        }
        super.onResume()
    }

    private fun init() = runOnUiThread(){
            navigationMenu = findViewById(R.id.navigation_menu)
            drawerLayout = findViewById(R.id.drawer_layout)
            textViewTitolo = findViewById(R.id.textView_titoloSchermata)
            buttonMenu = findViewById(R.id.menu_button)

            textViewStatus = findViewById(R.id.textView_app_status)

            buttonMenu.setOnClickListener(this::showNavMenu)

            //Aggiorno la navigation bar con le sezioni autorizzate
            updateNavView()

            initListeners()
    }

    private fun initListeners(){
        CoroutineScope(Dispatchers.Unconfined).async {
            initNetworkListener()

            listenNetworkChanges()

            listenToUserChanges()
        }
    }

    private fun cercaAggiornamenti(){
        if(NetworkController.apiAttivazione != null && ActivationController.isActivated()){
            val network = NetworkAggiornaApp(NetworkController.apiAttivazione)
            network.getUltimaVersione(ParamManager.getVersioneJW() ?: "null"){
                if(it.oldValue is Response<*>){
                    val params = (it.oldValue as Response<JsonElement>).body()!!.asJsonObject
                    if(params.has("ultima_versione_cliente")){
                        val versioneApp = params.get("ultima_versione_cliente").asString
                        if(BuildConfig.VERSION_NAME < versioneApp){
                            if(params.has("url")){
                                val url = params.get("url").asString
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(url)
                                startActivity(intent)
                            }
                        }
                        else {
                            AlertDialog.Builder(this)
                                .setTitle("App Aggiornata")
                                .setMessage("Ultima versione disponibile già installata")
                                .setPositiveButton("Ok", null)
                                .show()
                        }
                    }
                }
                else{
                    showNoConn()
                }
            }
        }
        else{
            showNoConn()
        }
    }

    private fun showNoConn() = runOnUiThread{
        AlertDialog.Builder(this)
            .setTitle("Impossibile connettersi al server")
            .setMessage("Connettersi a internet per controllare ultima versione disponibile")
            .setPositiveButton("Ok", null)
            .show()
    }


    private fun loadUserFromDb(){
        if(intent.hasExtra("OFFLINE") && intent.getBooleanExtra("OFFLINE", false)){
            showAlertAppOffline()
        }
    }

    private fun listenToUserChanges() {
        if (liveUser?.hasObservers() != true){
            liveUser?.observe(this){
                if(!UserRepository.logged && !UserRepository.ignore){
                    startLoginActivity()
                }
                else{
                    updateNavView()
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
                        setLastFragment(TimbrVirtualeFragment::class.simpleName, this)
                    }
                    startRIghtFragment()
                }
            }
        }
    }

    private fun listenNetworkChanges(){
        setAppStatus(StatusController.statusApp.value!!.osti, StatusController.statusApp.value!!.osti)
        StatusController.statusApp.observe(this) {
            setAppStatus(it.osti, it.cliente)
        }
    }

    private fun initNetworkListener(){
        myDatabaseController.getActivationCode{
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            var networkChangeListener :NetworkListener

            if(it.newValue != null && it.newValue is String){
                networkChangeListener = NetworkListener(this)
                connectivityManager.registerNetworkCallback(networkRequest, networkChangeListener)
            }
        }
    }

    private fun updateNavView() = runOnUiThread(){
        if(navigationMenu.isNotEmpty()){

            navigationMenu.removeAllViews()
        }
        val repUser = UserRepository(ParamManager.getLastUserId())
        val user = repUser.getLiveUser()
        if(UserRepository.logged && user != null){
            setLastFragment(CartellinoFragment::class.simpleName, this)
            for(x in NavigationMenuOptions.options){
                if (x == R.string.menu_gestisci_giustificazioni && user.value?.type != "admin" && user.value?.type != "superAdmin" && user.value?.type != "manager"){
                    continue
                }
                if(x == R.string.menu_old_stamp && user.value?.nascondi_timbrature == "1"){
                    continue
                }
                if(x == R.string.menu_file && user.value?.perm_workflow == "0"){
                    continue
                }
                if(x == R.string.menu_gestisci_giustificazioni && user.value?.perm_workflow == "0"){
                    continue
                }

                var tmpTextView: MyMenuTextView? = MyMenuTextView(this, resources.getString(x))
                if(resources.getString(x) == resources.getString(R.string.menu_richiesta_giust) ||
                    resources.getString(x) == resources.getString(R.string.menu_lista_giust) ||
                    resources.getString(x) == resources.getString(R.string.menu_cartellino) ||
                    resources.getString(x) == resources.getString(R.string.menu_gestisci_giustificazioni) ||
                    resources.getString(x) == resources.getString(R.string.menu_file)){
                    if(ActivationController.permWorkFlow == "1"){
                        if(user.value?.perm_workflow != "1"){
                            tmpTextView?.setTextColor(Color.LTGRAY)
                        }
                        else if (ActivationController.perTimbraVirtuale == "0" || !repUser.canTimbr()){
                            setLastFragment(GiustificativiDettagliFragment::class.simpleName, this)
                        }
                    }
                    else{
                        tmpTextView = null
                    }
                }
                else if(resources.getString(x) == resources.getString(R.string.menu_timbra) || resources.getString(x) == resources.getString(R.string.menu_old_stamp)){
                    if(ActivationController.perTimbraVirtuale == "1"){
                        if(!repUser.canTimbr()){
                            tmpTextView?.setTextColor(Color.LTGRAY)
                        }
                        else{
                            setLastFragment(TimbrVirtualeFragment::class.simpleName, this)
                        }
                    }
                    else{
                        tmpTextView = null
                    }
                }
                if(tmpTextView != null){
                    val tmpLayout = MyConstraintLayout(this, tmpTextView)
                    navigationMenu.addView(tmpLayout)
                    if(tmpLayout.active){
                        tmpLayout.setOnClickListener(this::onClickMenuOption)
                    }
                }
            }
            navigationMenu.invalidate()
        }
    }

    //Imposta scritta ONLINE o OFFLINE NELLA NAVIGATION BAR
    fun setAppStatus(osti:Boolean, cliente:Boolean) = runOnUiThread{
        if(!osti){
            textViewStatus.text = "Offline"
            textViewStatus.setTextColor(resources.getColor(R.color.red_negato, null))
            textViewStatus.invalidate()
        }
        else{
            if(cliente){
                textViewStatus.text = "Online"
                textViewStatus.setTextColor(resources.getColor(R.color.bluosti, null))
            }
            else{
                textViewStatus.text = "Online"
                textViewStatus.setTextColor(resources.getColor(R.color.yellow_offline, null))
            }
        }
    }

    //QUANDO VIENE CLI
    fun onClickMenuOption(v: View){
        lockFragments = false
        if ( v is MyConstraintLayout){
            when (v.textView.text){
                resources.getString(R.string.menu_old_stamp) -> {
                    showOldStampFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_gestisci_giustificazioni) -> {
                    showGestisciGiust()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_account) -> {
                    showAccountFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_timbra) -> {
                    showTimbrFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_richiesta_giust) -> {
                    showGiustificheFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_lista_giust) -> {
                    showOldGiustificheFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_cartellino) -> {
                    showCartellinoFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_file) -> {
                    showFileFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_notifiche) -> {
                    showNotificheFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_info) -> {
                    showInfoFragment()
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                resources.getString(R.string.menu_aggiorna)->{
                    cercaAggiornamenti()
                }
                resources.getString(R.string.menu_exit) -> {
                    finishAndRemoveTask()
                    exitProcess(0)
                }
            }
        }
    }

    private var actualTimbrFragment: TimbrVirtualeFragment? = null
    private fun showTimbrFragment(){
        val user = UserRepository(ParamManager.getLastUserId()).getUser()
        val dipendente = DipendentiRepository(user?.idDipendente ?: -1).getDipendente()
        setTitoloSchermata(resources.getString(R.string.menu_timbra))
        if(user != null && intent.extras?.getString("LOGIN") != "LOGINACTIVITY" && user.name =="null"){
            //ARRIVATO DIPENDENTE NULLO DA SERVER, QUANDO NON C'è UN DIPENDENTE ASSOCIATO ALL'UTENTE
            AlertDialog.Builder(this)
                .setTitle("Non autorizzato")
                .setMessage(R.string.alert_dipendente_null_timbr)
                .setPositiveButton("Ok", null)
                .show()
        }
        else if (ActivationController.canTimbr() && dipendente != null){
            try{
                supportFragmentManager.beginTransaction().apply {
                    if (actualTimbrFragment != null) {
                        detach(actualTimbrFragment!!)
                    }
                    actualTimbrFragment = TimbrVirtualeFragment.newInstance(dipendente.serverId)
                    replace(R.id.fragmentContainerView, actualTimbrFragment!!)
                    commit()
                }
            }
            catch (e:Exception){
                val i = e
            }
        }
        else{
            //VUOL DIRE CHE L'UTENTE NON è AUTORIZZATO A TIMBRARE
            /*AlertDialog.Builder(this)
                .setTitle("Non autorizzato")
                .setMessage(R.string.alert_no_permessi_timbratura)
                .setPositiveButton("Ok", null)
                .show()*/
            showAccountFragment()
        }
    }

    fun setTitoloSchermata(str:String){
        textViewTitolo.text = str
    }

    private fun showNavMenu(v:View){
        drawerLayout.openDrawer(GravityCompat.END)
    }

    private fun showLoadFrgment(){
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainerView, UserLoadWaitFragment())
                commit()
            }
    }
    private fun showOldStampFragment(){
        setTitoloSchermata(resources.getString(R.string.menu_old_stamp))
        try{
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainerView, OldStampFragment())
                commit()
            }
        }
        catch (e:Exception){
            val i = e
        }
    }

    private fun showGestisciGiust(){
        setTitoloSchermata(resources.getString(R.string.menu_gestisci_giustificazioni))
        try{
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainerView, GiustificheFragmentSelection())
                commit()
            }
        }
        catch (e:Exception){
            val i = e
        }
    }

    private fun showAccountFragment(){
        setTitoloSchermata(resources.getString(R.string.menu_account))
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, AccountFragment())
            commit()
        }
    }

    fun showFileFragment(){
        setTitoloSchermata(resources.getString(R.string.menu_file))
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, FileFragment())
            commit()
        }
    }

    private fun showNotificheFragment(){
        lockFragments = true
        setTitoloSchermata(resources.getString(R.string.menu_notifiche))
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, NotificheFragment())
            commit()
        }
    }

    private fun showGiustificheDisambiguationFragment(){
        setTitoloSchermata(resources.getString(R.string.menu_gestisci_giustificazioni))
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, GiustificheFragmentSelection())
            commit()
        }
    }

    private fun showGiustificheFragment(){
        val user = UserRepository(ParamManager.getLastUserId())
        val dipendente = DipendentiRepository(user.getUser()?.idDipendente ?: -1).getDipendente()
        if(intent.extras?.getString("LOGIN") != "LOGINACTIVITY" && dipendente!= null && dipendente.nome =="null"){
            //ARRIVATO DIPENDENTE NULLO DA SERVER, QUANDO NON C'è UN DIPENDENTE ASSOCIATO ALL'UTENTE
            AlertDialog.Builder(this)
                .setTitle("Non autorizzato")
                .setMessage(R.string.alert_dipendente_null_giust)
                .setPositiveButton("Ok", null)
                .show()
        }
        else if(ActivationController.permWorkFlow == "1" && user.canWorkFlow()){
            setTitoloSchermata(resources.getString(R.string.menu_richiesta_giust))
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainerView, GiustificativiDettagliFragment())
                commit()
            }
        }
        else{
            /*AlertDialog.Builder(this)
                .setTitle("Attenzione")
                .setMessage(R.string.alert_no_permessi_workflow)
                .setPositiveButton("Ok"){_,_ ->
                    showAccountFragment()
                }
                .show()*/
            showAccountFragment()
        }
    }

    fun showOldGiustificheFragment(){
        setTitoloSchermata(resources.getString(R.string.menu_lista_giust))
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, OldGiustificheListFragment())
            commit()
        }
    }

    fun showCartellinoFragment(){
        setTitoloSchermata(resources.getString(R.string.menu_cartellino))
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, CartellinoFragment())
            commit()
        }
    }

    private fun showInfoFragment(){
        setTitoloSchermata(resources.getString(R.string.menu_info))
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, InfoFragment())
            commit()
        }
    }

    private fun getLastFragment(): String? {
        return JuniorShredPreferences.getSharedPref( "LAST FRAGMENT", this)
    }

    fun getFileAsLastFragment():String{
        val temp = JuniorShredPreferences.getSharedPref("FILEFRAGMENT", this) ?: "false"
        JuniorShredPreferences.setSharedPref("false", "FILEFRAGMENT", this)
        return temp
    }

    var lockFragments = false
    fun startRIghtFragment() {
        val user = UserRepository(ParamManager.getLastUserId()).getUser()
        val dipendente = DipendentiRepository(user?.idDipendente ?: -1).getDipendente()
        val tmp = getFileAsLastFragment()
        //Se è filegragment l'ultimo vuol dire che deve aprirsi di nuovo lui perchè è stato visualizzato un file con un'app esterna
        if(tmp != "true" && user != null && dipendente != null){
            if ((user.type == "manager" || user.type == "superAdmin" || user.type == "admin") && dipendente.nome == "null"){
                setLastFragment(GiustificheFragmentSelection::class.simpleName, this)
            }
            if (nuoveNotifiche){
                setLastFragment(NotificheFragment::class.simpleName, this)
            }
        }
        else{
            setLastFragment(FileFragment::class.simpleName, this)
        }

        if(!lockFragments){
            when (getLastFragment()){
                TimbrVirtualeFragment::class.simpleName-> {
                    showTimbrFragment()
                }
                GiustificheFragmentSelection::class.simpleName-> {
                    showGiustificheDisambiguationFragment()
                }
                AccountFragment::class.simpleName -> {
                    showAccountFragment()
                }
                OldStampFragment::class.simpleName -> {
                    showOldStampFragment()
                }
                GiustificativiDettagliFragment::class.simpleName -> {
                    showGiustificheFragment()
                }
                CartellinoFragment::class.simpleName -> {
                    showCartellinoFragment()
                }
                NotificheFragment::class.simpleName -> {
                    showNotificheFragment()
                }
                InfoFragment::class.simpleName -> {
                    showInfoFragment()
                }
                OldGiustificheListFragment::class.simpleName -> {
                    showOldGiustificheFragment()
                }
                else-> {
                    showTimbrFragment()
                }
            }
        }
    }


    fun startLoginActivity(){
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

        finish()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //nothing
        }
    }
}
