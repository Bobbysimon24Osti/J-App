package com.osti.juniorapp.fragment.timbrature

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.R
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUser
import com.osti.juniorapp.db.tables.DipendentiTable
import com.osti.juniorapp.db.tables.GiustificheTable
import com.osti.juniorapp.db.tables.TimbrTable
import com.osti.juniorapp.listener.MyLocationListener
import com.osti.juniorapp.utils.CheckAutoTime
import com.osti.juniorapp.utils.GiustificheConverter
import com.osti.juniorapp.utils.Utils.FORMATDATEHOURS
import com.osti.juniorapp.utils.Utils.NOCITTA
import com.osti.juniorapp.utils.Utils.NORMALFORMATDATEHOURS
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.beans.PropertyChangeListener
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.timerTask


var isLocationPermissionRequested = false
private var mLastTimbr: Long? = null

class TimbrVirtualeFragment () : Fragment() {

    companion object{
        fun newInstance(serverId:Long) : TimbrVirtualeFragment {
            val fragment = TimbrVirtualeFragment()
            val bund = Bundle()
            bund.putLong("serverId", serverId)
            fragment.arguments = bund
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null){
            JuniorApplication.myDatabaseController.getDipendente(requireArguments().getLong("serverId", -1)){
                if(it.newValue != null){
                    initStampObserver()
                }
            }
        }
    }


    var causale: GiustificheTable? = null

    lateinit var buttonTimbra: Button
    lateinit var progressBar: ProgressBar
    lateinit var clockProgressBar: ProgressBar
    lateinit var timeOutTimer: Timer
    lateinit var textViewLastStampLocation: TextView
    lateinit var textViewServer: TextView
    lateinit var imageViewOnServer: ImageView
    lateinit var textViewActualPos: TextView
    lateinit var textViewLastStampTime: TextView
    lateinit var containerLastStampLocation: CardView
    lateinit var constraint: ConstraintLayout
    lateinit var constraintLayoutPosizione: ConstraintLayout
    lateinit var spinnerCausale: Spinner

    lateinit var observer: Flow<Boolean>

    lateinit var mPermissionLauncher: ActivityResultLauncher<String>

    var isWaitingLocation = false

    var licGps:Boolean? = null


    private fun init(v: View) {

        activity?.runOnUiThread{
            buttonTimbra = v.findViewById(R.id.buttonTimbra)
            textViewLastStampLocation = v.findViewById(R.id.textView_ultimaTimbratura)
            textViewServer = v.findViewById(R.id.textViewOnserver)
            imageViewOnServer = v.findViewById(R.id.imageView_lastStamp_onServer)
            textViewActualPos = v.findViewById(R.id.textViewActualPos)
            textViewLastStampTime = v.findViewById(R.id.textViewLastStampTime)
            containerLastStampLocation = v.findViewById(R.id.containerLastStampLocation)
            constraint = v.findViewById(R.id.constraint_timbrState)
            constraintLayoutPosizione = v.findViewById(R.id.constraintLayout_position)
            spinnerCausale = v.findViewById(R.id.spinner_causale)

            buttonTimbra.setOnClickListener(this::onClickTimbra)
            textViewActualPos.setOnClickListener(this::onClickAuth)


            MyLocationListener.currentPosition.observe(viewLifecycleOwner) {
                //OBSERVER POSIZIONE ATTUALE
                refreshActualPos()
            }

            refreshActualPos()

            observeCoonfigs()

            progressBar = v.findViewById(R.id.progressBar)

            imageViewOnServer.visibility = View.INVISIBLE
            textViewServer.visibility = View.INVISIBLE

            initSpinner(requireContext())
        }

        /*val i = Calendar.getInstance()
        for (x in 0..10000){
            i.add(Calendar.MINUTE, 1)
            JuniorApplication.myDatabaseController.creaTimbr(TimbrTable("578", FORMATDATEHOURS.format(i.timeInMillis), 1.0, 1.0 , 1, true, "Bologna", false, 0)){

            }
        }*/

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        initPermissionRequest(context)

        if (!CheckAutoTime.checkAutoTime(context)) {
            AlertDialog.Builder(context)
                .setTitle("Attenzione")
                .setMessage(
                    "L'orario del dispositivo non è sincronizzato automaticamente con il server, questo verrà segnalato nel cartellino. \n" +
                            "Attivare la sincronizzazione automatica?"
                )
                .setPositiveButton("Si") { _, _ ->
                    startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    val configFlow = JuniorApplication.myDatabaseController.getSpinnerConfigFlow()

    private fun observeLastStamp(){
        CoroutineScope(Dispatchers.IO).async {
            observer.collect{
                refreshDatas()
            }
        }
    }

    private fun initStampObserver(){
            //OBSERVER NUOVE TIMBRATURE
            refreshDatas()
            observer = JuniorApplication.myDatabaseController.getlastStampDipendenteLive(JuniorUser.JuniorDipendente.serverId)
            observeLastStamp()
    }

    fun observeCoonfigs(){
        CoroutineScope(Dispatchers.IO).launch {
            configFlow.collect{
                licGps = JuniorApplication.myDatabaseController.getConfig("lic_timbratura_virtuale_gps").valore == "1"
                if(JuniorApplication.myDatabaseController.getConfig("account_user_timbratura_virtuale_con_causale").valore != "0" && JuniorApplication.myDatabaseController.getConfig("lic_produzione").valore != "1" ) {
                    spinnerVisible(true)
                }
                else{
                    spinnerVisible(false)
                }
            }
        }
    }

    private fun spinnerVisible(visible:Boolean){
        activity?.runOnUiThread {
            if(visible){
                spinnerCausale.visibility = View.VISIBLE
                spinnerCausale.invalidate()
            }
            else{
                spinnerCausale.visibility = View.GONE
                spinnerCausale.invalidate()
            }
        }
    }


    private fun initSpinner(context: Context){
        CoroutineScope(Dispatchers.IO).launch{
                JuniorApplication.myDatabaseController.getGiustificheFlow().collect {
                    if (it.isNotEmpty()) {
                        val nomiGiustifiche = GiustificheConverter.getAllNamesAndAbbr()
                        showSpinner(nomiGiustifiche, context)
                    }
                }
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun showSpinner(nomiGiustifiche: List<String>, context: Context) {
        activity?.runOnUiThread {
                spinnerCausale.adapter = ArrayAdapter(
                    context,
                    R.layout.spinner_text_causali_layout,
                    nomiGiustifiche
                )
                spinnerCausale.onItemSelectedListener =
                    MySelectionListener(this)
                spinnerCausale.setPopupBackgroundDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.color.bluosti_light,
                        null
                    )
                )
        }
    }

    private fun initPermissionRequest(context: Context) {
        if (ActivationController.canTimbrGps()) {
            val isLocationPermissionOk = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            mPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isLocationPermissionRequested = true
                }

            if (!isLocationPermissionRequested) {
                mPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else if (!isLocationPermissionOk && isLocationPermissionRequested) {
                AlertDialog.Builder(context)
                    .setTitle("Permesso localizzazione negato")
                    .setMessage("Consentire controllo posizione per permettere di timbrare con la localizzazione")
                    .setPositiveButton("Apri Impostazioni") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Nega", null)
                    .show()
            }
        }
    }

    private fun refreshDatas() {
        if (JuniorUser.JuniorDipendente.serverId != -1L) {
            JuniorApplication.myDatabaseController.getlastStampDipendente(JuniorUser.JuniorDipendente.serverId){
                activity?.runOnUiThread {
                    if(it.newValue != null){
                        val tmpTimbr = it.newValue as TimbrTable
                        imageViewOnServer.visibility = View.VISIBLE
                        textViewServer.visibility = View.VISIBLE
                        var tmpString = "Ultima Timbratura: \n" + tmpTimbr.dataOra
                        textViewLastStampTime.text = FORMATDATEHOURS.parse(tmpTimbr.dataOra)
                            ?.let { NORMALFORMATDATEHOURS.format(it) }
                        // MOSTRA/NASCONDI CONTAINER LOCALITA PER ULTIMA TIMBRATURA
                        if (tmpTimbr.citta == null) {
                            hideLastStampLocation()
                        } else {
                            textViewLastStampLocation.text = tmpTimbr.citta
                            showLastStampLocation()
                        }
                        //MOSTRA SE LA TIMBRATURA E SUL SERVER O MENO
                        if (tmpTimbr.onServer) {
                            tmpString = activity?.resources?.getString(R.string.state_on_server) ?: "error"
                            imageViewOnServer.setImageResource(R.drawable.cloud_online)
                            textViewServer.text = tmpString
                        }
                        else {
                            tmpString = activity?.resources?.getString(R.string.state_offline) ?: "ErrNo"
                            imageViewOnServer.setImageResource(R.drawable.cloud_offline)
                            textViewServer.text = tmpString
                        }
                    }
                    else {
                        imageViewOnServer.visibility = View.INVISIBLE
                        textViewServer.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun refreshActualPos() {
        val tmpStr: String = if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && MyLocationListener.currentPosition.value != null
        ) {
            val tmpLoc = MyLocationListener.currentPosition.value
            enableButton()
            "Accuratezza GPS: ${tmpLoc!!.accuracy.toInt()} m" //"Posizione Attuale:\n\nLatitudine: ${tmpLoc!!.latitude.toInt()}\nLongitudine: ${tmpLoc.longitude.toInt()}\n"
        } else {
            resources.getString(R.string.text_view_posizione_negata)
        }
        textViewActualPos.text = tmpStr
        textViewActualPos.invalidate()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_timbr_virtuale, container, false)
        init(v)
        return v
    }

    override fun onResume() {
        super.onResume()
        val isLocationEnabled =
            LocationManagerCompat.isLocationEnabled(JuniorApplication.mLocationManager)
        if (isLocationEnabled) {
            if (ActivationController.canTimbrGps() && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                MyLocationListener.startLocationListener(requireContext())
                enableButton()
            }
        }
        else {
            if (JuniorUser.positionObb() && licGps == true) {
                dismissButton()
                showPositionMissingAlert()
            }
        }
    }

    private fun dismissButton() {
        buttonTimbra.isClickable = false
        buttonTimbra.setBackgroundColor(resources.getColor(R.color.bluosti_disabled, null))
        constraintLayoutPosizione.setBackgroundColor(
            resources.getColor(
                R.color.red_negato_light,
                null
            )
        )
        textViewActualPos.text = resources.getString(R.string.text_view_posizione_negata)
        buttonTimbra.invalidate()

    }

    private fun enableButton() {
        buttonTimbra.isClickable = true
        buttonTimbra.setBackgroundResource(R.drawable.selector_timbr_button)
        constraintLayoutPosizione.background = null
        buttonTimbra.invalidate()
    }

    private fun showPositionMissingAlert() {
        activity?.runOnUiThread {
            AlertDialog.Builder(requireContext())
                .setTitle("Attenzione")
                .setMessage("Localizzazione disattivata, riattivarla per poter timbrare")
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        MyLocationListener.stopListener()
    }

    val timer = Timer()

    fun onClickTimbra(view: View) {
        if (!isOpenRecently()) {
            if (ActivationController.canTimbrGps() && JuniorUser.positionObb() && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startProgressBar()
                //RICHIESTA TIMBRATURA CON GPS
                //pressButton()
                //PERMESSI LOCALIZZAZIONE CONCESSI, LAVORA NORMALMENTE
                if (MyLocationListener.currentPosition.value == null) {
                    isWaitingLocation = true
                    MyLocationListener.getLocation(locationObserver)
                    timer.schedule(timeoutDetector(), 4000)
                } else {
                    saveStamp(MyLocationListener.currentPosition.value)
                }
                //unPressButton()
            } else if (ActivationController.canTimbr()) {
                //TIMBRATURA SENZA GPS
                saveStamp()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Impossibile eseguire timbratura")
                    .setMessage(R.string.alert_no_permessi_timbratura)
                    .setPositiveButton("Ok", null)
                    .show()
            }
        }
    }

    private fun startProgressBar() {
        activity?.runOnUiThread {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun stopProgressBar() {
        activity?.runOnUiThread {
            progressBar.visibility = View.GONE
        }
    }

    fun isOpenRecently(): Boolean {
        if(BuildConfig.DEBUG){
            //return false
        }
        val i = Calendar.getInstance()
        i.set(Calendar.SECOND, 0)
        i.set(Calendar.MILLISECOND, 0)
        if (i.timeInMillis == mLastTimbr) {
            showAlertToast()
            return true
        }
        return false
    }

    private fun onClickAuth(v: View) {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (ActivationController.canTimbr()) {
            MyLocationListener.startLocationListener(requireContext())
        }
    }

    fun timeoutDetector() = timerTask {
        MyLocationListener.cancelObserver()
        saveStamp(null)
    }


    private fun showAlert() {
        requireActivity().runOnUiThread {
            AlertDialog.Builder(requireContext())
                .setTitle("Errore Timeout")
                .setMessage(R.string.alert_timeout_attesa_posizione)
                .setPositiveButton("Ok") { _, _ ->
                    //Tempo per ricerca posizione scaduto, salvo senza coordinate
                    activity?.runOnUiThread {
                        saveStamp()
                    }
                }
                .show()
        }
    }

    private fun invalidateLastStamp() {
        textViewLastStampLocation.invalidate()
        textViewServer.invalidate()
    }

    val locationObserver = PropertyChangeListener { p0 ->
        timer.cancel()
        if (isWaitingLocation) {
            isWaitingLocation = false
            val p2 = p0.newValue
            saveStamp(p2 as Location)
            //unPressButton()
        }
    }

    private fun alertNoPosObbligatoria() {
        activity?.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle("Attenzione")
                .setMessage("Impossibile recuperare la posizione, l'utente attuale richiede sempre la posizione in fase di timbratura, premere \"OK\" per farla comunque senza posizione, altrimenti annullare e riprovare in un altro momento")
                .setPositiveButton("Ok") { _, _ ->
                    saveStampInDb(null)
                    invalidateLastStamp()
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
    }

    private fun saveStamp(location: Location? = Location("NOPOS")) {
        try {
            stopProgressBar()
            if ((location == null || location.provider == "NOPOS") && JuniorUser.positionObb()) {
                alertNoPosObbligatoria()
            }
            else{
                saveStampInDb(location)
            }
        }
        catch (e: Exception) {
            saveStampInDb(null)
        }
    }

    private fun saveStampInDb(location: Location?) {
        try {
            if (location != null) {
                saveInDatabase(
                    location.latitude,
                    location.longitude,
                    location.accuracy?.toInt()
                )
            } else {
                //Impossibile recuperare posizione, salva solo orario
                saveInDatabase(null, null, null)
            }
            requireActivity().runOnUiThread {
                showSuccessToast()
            }
            invalidateLastStamp()
        } catch (e: Exception) {
            val i = e
        }
    }

    private fun setCitta(lat: Double, lon: Double, id:Long){
        if (ActivationController.canTimbrGps()) {
            if(lat != 0.0 && lon != 0.0) {
                val geo = Geocoder(requireContext(), Locale.ITALY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geo.getFromLocation(
                        lat,
                        lon,
                        1
                    ) {
                        JuniorApplication.myDatabaseController.setCitta(
                            id,
                            it.get(0)?.locality ?: NOCITTA
                        )
                    }
                } else {
                    val f = geo.getFromLocation(
                        lat,
                        lon,
                        1
                    )
                    JuniorApplication.myDatabaseController.setCitta(
                        id,
                        f?.get(0)?.locality ?: NOCITTA
                    )
                }
            }
        }
    }

    private fun showSuccessToast() {
        Toasty.success(requireContext(), R.string.toast_successo_timbratura).show()
    }

    private fun showAlertToast() {
        Toasty.info(requireContext(), R.string.toast_timbratura_fatta_recentemente).show()
    }

    private fun saveInDatabase(lat: Double?, lon: Double?, acc: Int?) {
        val timbr = TimbrTable(
            JuniorUser.JuniorDipendente.serverId.toString(),
            FORMATDATEHOURS.format(Calendar.getInstance().timeInMillis),
            lat ?: 0.0,
            lon ?: 0.0,
            acc ?: 0,
            CheckAutoTime.checkAutoTime(requireContext()),
            let{if((lat == null || lat == 0.0) && (lon == null || lon == 0.0) && (acc == null || acc == 0)){
                "Posizione non rilevata"
            }
               else{
                NOCITTA
               }},
            false,
            causale?.id
        )

        JuniorApplication.myDatabaseController.creaTimbr(timbr) {
            if(it.newValue != null &&
                it.newValue is Long &&
                it.newValue == 1L){
                initStampObserver()
            }
            else if (it.newValue is Long && lat!=null && lon!= null){
                val id = it.newValue as Long
                setCitta(lat, lon, id)
            }
        }

        val i = Calendar.getInstance()
        i.set(Calendar.SECOND, 0)
        i.set(Calendar.MILLISECOND, 0)
        mLastTimbr = i.timeInMillis

    }

    private fun showLastStampLocation() {
        containerLastStampLocation.visibility = View.VISIBLE
    }

    private fun hideLastStampLocation() {
        containerLastStampLocation.visibility = View.GONE
    }

    class MySelectionListener(val fragment: TimbrVirtualeFragment) :
        OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            val nomi = GiustificheConverter.getAllGiust()
            if (p1 is TextView && !nomi.isNullOrEmpty()) {
                fragment.causale = nomi[p2]
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            //NIENTE
        }
    }

}