package com.osti.juniorapp.listener

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.utils.Utils.MINACCURACY
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.*


object MyLocationListener : LocationListenerCompat{
    var currentPosition = MutableLiveData<Location>()

    var observer : PropertyChangeListener? = null


    fun cancelObserver(){
        observer = null
    }


    fun savePosition(location:Location){
        if(location.accuracy < MINACCURACY){
            try{
                val tmpLoc = location
                val tmpAcc = tmpLoc.accuracy.toInt()
                tmpLoc.accuracy = tmpAcc.toFloat()
                currentPosition.value = tmpLoc
                observer?.propertyChange(PropertyChangeEvent("MyLocationListener", "Location", currentPosition.value, currentPosition.value))
                //observer = null
            }
            catch (e:Exception){
                //observer = null
            }
            //Log.e("LOCATION TIME ${p0.time} ACCUARCY: ${p0.accuracy}", p0.altitude.toString() + "-----" + p0.longitude.toString())
        }
    }

    override fun onLocationChanged(p0: Location) {
        savePosition(p0)
    }


    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
    }

    fun getLocation(observer: PropertyChangeListener){
        MyLocationListener.observer = observer
    }

    fun stopListener(){
        JuniorApplication.mLocationManager.removeUpdates(this)
    }

    @SuppressLint("MissingPermission") //DEVE GIA ESSERE STATO FATTO CONtROLLO PERMESSI
    fun startLocationListener(context: Context) {
        val criteria = Criteria()
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.accuracy = Criteria.ACCURACY_FINE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val provider = JuniorApplication.mLocationManager.getBestProvider(criteria, true) ?: LocationManager.FUSED_PROVIDER
            val builder = LocationRequestCompat.Builder(1000)
                .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(1000)
            val request = builder.build()
            LocationManagerCompat.requestLocationUpdates(JuniorApplication.mLocationManager, provider, request, context.mainExecutor, this)
            /*CoroutineScope(Dispatchers.Default).async {
                while (true){
                    delay(10000)
                    getManual(context)
                }
            }
             */
        }
        else{
            startLocationListenerLegacy(context)
        }
    }

    @SuppressLint("MissingPermission") //DEVE GIA ESSERE STATO FATTO CONtROLLO PERMESSI
    private fun startLocationListenerLegacy(context: Context) { //PER ANDROID VECCHIO
        LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null){
                    /*AlertDialog.Builder(context)
                        .setTitle("Errore")
                        .setMessage("Impossibile Recuperare posizione")
                        .setPositiveButton("Ok", null)
                        .show()*/
                }
                else {
                    savePosition(location)
                }
            }
    }
}

