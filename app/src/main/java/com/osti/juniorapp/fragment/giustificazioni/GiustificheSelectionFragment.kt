package com.osti.juniorapp.fragment.giustificazioni

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.StatusController
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.thread.RiceviDatiThread
import com.osti.juniorapp.utils.GiustificheConverter
import com.osti.juniorapp.utils.Utils
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import java.beans.PropertyChangeListener

class GiustificheSelectionFragment : Fragment() {
    lateinit var recyclerSelection: RecyclerView
    lateinit var textViewEmpty: TextView
    lateinit var imageViewRefresh: ImageView

    lateinit var refreshView: SwipeRefreshLayout

    lateinit var giustFlow : Flow<List<GiustificheRecord?>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_old_giustificativi, container, false)
        init(v)
        return v
    }

    private fun init(v:View) {
        activity?.runOnUiThread{
            recyclerSelection = v.findViewById(R.id.recycler_giustificativi_select)
            textViewEmpty = v.findViewById(R.id.textView_noOldGiust)
            imageViewRefresh = v.findViewById(R.id.imageView_refreschRichieste)

            refreshView = v.findViewById(R.id.refreshLayout_giust)

            val dip = JuniorApplication.myJuniorUser.value?.dipentende
            if(dip!=null){
                giustFlow = JuniorApplication.myDatabaseController.getGiustFlow(dip.serverId)
            }
            listenToChanges()

            StatusController.observe = PropertyChangeListener{
                if(it.newValue != null && it.newValue is Boolean && it.newValue == true){
                    enableRefreshing()
                }
            }
            imageViewRefresh.setOnClickListener{tryGiustUpdate()}
            refreshView.setOnRefreshListener(this:: tryGiustUpdate)
        }
    }
    fun enableRefreshing(){
        refreshView.isEnabled = true
    }



    private fun tryGiustUpdate(){
        refreshView.isEnabled = false
        if(ActivationController.isActivated()){
            RiceviDatiThread.observeScaricoGiust= downloadObserver
            JuniorApplication.riceviDati()
        }
        else{
            showErrorUpdate()
        }
    }

    private val downloadObserver = PropertyChangeListener{
        if(it.newValue != null && it.newValue is Boolean && it.newValue == true){
            refreshView.isRefreshing = false
            showSuccessToast()
        }
        else{
            showErrorUpdate()
        }
        enableRefreshing()
    }

    private fun showSuccessToast(){
        enableRefreshing()
        activity?.runOnUiThread{
            Toasty.success(requireContext(), "Richieste aggiornate con successo").show()
        }
    }

    private fun showErrorUpdate(){
        refreshView.isRefreshing = false
        enableRefreshing()
        activity?.runOnUiThread{
            AlertDialog.Builder(requireContext())
                .setTitle("Errore")
                .setMessage("Errore in fase di aggiornamento giustificativi dal server \n Verificare di essere connessi con il server di Juniorweb")
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    private fun listenToChanges(){
        MainScope().async {
            giustFlow.collect{
                if(it is List<*>){
                    adapt(it)
                }
            }
        }
    }

    private fun adapt(l:List<GiustificheRecord?>){
        recyclerSelection.adapter = CustomAdapter(l, requireActivity() as MainActivity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dip = JuniorApplication.myJuniorUser.value?.dipentende
        if(dip != null) {
            JuniorApplication.myDatabaseController.getAllGiustByDip(dip.serverId){
                if(it.newValue != null){
                    showRecyclerView(it.newValue as List<GiustificheRecord>)
                }
            }
        }
    }

    private fun showRecyclerView(giust:List<GiustificheRecord>){
        if(isAdded){
            activity?.runOnUiThread {
                if(giust.isEmpty()){
                    textViewEmpty.visibility = View.VISIBLE
                }
                else{
                    textViewEmpty.visibility = View.GONE
                    recyclerSelection.adapter = CustomAdapter(giust, requireActivity() as MainActivity)
                    recyclerSelection.layoutManager = LinearLayoutManager(context)
                }
            }
        }
    }

    class CustomAdapter(private val giustifiche: List<GiustificheRecord?>,val activity: MainActivity) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder)
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textViewAbbr: TextView
            val textViewNome: TextView
            val textViewData: TextView
            val textViewOra: TextView
            val textViewValore: TextView
            val imageViewOnServer: ImageView
            val textViewOnServer: TextView
            val constraintGiust: ConstraintLayout
            val imageViewStatus: ImageView

            init {
                // Define click listener for the ViewHolder's View
                textViewAbbr = view.findViewById(R.id.textView_abbr_giust)
                textViewNome= view.findViewById(R.id.textView_giust)
                textViewData = view.findViewById(R.id.textView_data_oldGiust)
                textViewOra = view.findViewById(R.id.textView_ora_oldGiust)
                textViewValore = view.findViewById(R.id.textView_valore)
                imageViewOnServer = view.findViewById(R.id.imageView_giustifica_onServer)
                textViewOnServer = view.findViewById(R.id.textView_giustifica_onServer)
                constraintGiust = view.findViewById(R.id.constraint_giust_row)
                imageViewStatus = view.findViewById(R.id.imageView_status)
            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.giustificativi_selection_layout, viewGroup, false)
            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            val giust = giustifiche[position]
            if(giust!= null){
                viewHolder.textViewAbbr.text = giust.abbreviazione_giustifica
                viewHolder.textViewNome.text = giust.nome


                val tmpDataInizio = Utils.FORMATDATEDB.parse(giust.data_inizio)
                    ?.let { Utils.FORMATDATE.format(it) }

                val tmpDataFine = Utils.FORMATDATEDB.parse(giust.data_fine)
                    ?.let { Utils.FORMATDATE.format(it) }


                val tmpStr= "Dal giorno $tmpDataInizio al $tmpDataFine"
                viewHolder.textViewData.text = tmpStr

                val oraInizio = GiustificheConverter.getOraFromMin(giust.ora_inizio)
                val oraFine = GiustificheConverter.getOraFromMin(giust.ora_fine)

                val tmpStr2= "Dalle ore $oraInizio alle ore $oraFine"
                viewHolder.textViewOra.text = tmpStr2


                val valore = GiustificheConverter.getValore(giust)
                val tmpStr3= "Valore: $valore"
                viewHolder.textViewValore.text = tmpStr3

                if(giust.onServer){
                    viewHolder.imageViewOnServer.setImageResource(R.drawable.cloud_online)
                    viewHolder.textViewOnServer.text = activity.resources.getString(R.string.state_on_server)
                }
                else{
                    viewHolder.imageViewOnServer.setImageResource(R.drawable.cloud_offline)
                    viewHolder.textViewOnServer.text = activity.resources.getString(R.string.state_offline)
                }

                if(giust.richiesto == "richiesto"){
                    viewHolder.imageViewStatus.setImageResource(R.drawable.status_circle_richiesto)
                }
                if(giust.richiesto == "approvato"){
                    viewHolder.imageViewStatus.setImageResource(R.drawable.status_circle_approvato)
                }
                if(giust.richiesto == "negato" || giust.richiesto == "annullato"){
                    viewHolder.imageViewStatus.setImageResource(R.drawable.status_circle_negato)
                }

                viewHolder.constraintGiust.setOnClickListener{
                    showDettagliFragment(giust.id, giust.richiesto)
                }
            }
        }

        fun showDettagliFragment(id:Long?, richiesto:String?){
            if(id!=null && richiesto != null){
                val tmp = DettagliOldGiustificativiFragment.newInstance(id, richiesto)
                activity.supportFragmentManager.beginTransaction().apply{
                    replace(R.id.fragmentContainerView_dettagli_giustificazioni, tmp)
                    commit()
                }
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = giustifiche.size

    }
}