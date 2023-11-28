package com.osti.juniorapp.fragment.giustificazioni

import android.app.AlertDialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.osti.juniorapp.R
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.DipendentiRepository
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.UserRepository
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.network.NetworkRichieste
import com.osti.juniorapp.thread.RiceviDatiThread
import com.osti.juniorapp.utils.GiustificheConverter
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import java.beans.PropertyChangeListener
import kotlin.Int


class GestisciRichiesteFragment : Fragment() {

    lateinit var cardViewSelezionati: CardView
    lateinit var textViewSelezionati: TextView

    lateinit var progressBar: ProgressBar

    lateinit var recyclerView: RecyclerView
    lateinit var refreshView: SwipeRefreshLayout
    lateinit var buttonApprova: CardView
    lateinit var buttonNega: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_lista_richieste, container, false)
        init(view)
        return view
    }

    private fun init(v:View){
        cardViewSelezionati = v.findViewById(R.id.cardView_selezionati)
        textViewSelezionati = v.findViewById(R.id.textView_nSelezionati)

        progressBar = v.findViewById(R.id.progressBar_richieste)

        recyclerView = v.findViewById(R.id.recycler_richiesteManager)

        refreshView = v.findViewById(R.id.swipeRefresh_listaRichieste)
        buttonApprova = v.findViewById(R.id.cardView_approvaRichiesta)
        buttonNega = v.findViewById(R.id.cardView_negaRichiesta)

        buttonApprova.setOnClickListener(this::approvaSelected)
        buttonNega.setOnClickListener(this::negaSelected)

        refreshView.setOnRefreshListener(this::tryGiustUpdate)

        if(UserRepository.logged){
            listenToGiust()
        }
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

    private fun showErrorUpdate(){
        refreshView.isRefreshing = false
        enableRefreshing()
        activity?.runOnUiThread{
            AlertDialog.Builder(requireContext())
                .setTitle("Errore")
                .setMessage("Errore in fase di aggiornamento richieste dal server \n Verificare di essere connessi con il server di Juniorweb")
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    private fun enableRefreshing(){
        refreshView.isEnabled = true
    }

    private fun showSuccessToast(){
        enableRefreshing()
        activity?.runOnUiThread{
            Toasty.success(requireContext(), "Richieste aggiornate con successo").show()
        }
    }

    private fun svuotaListaSelected(){
        (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems.clear()
        textViewSelezionati.text = "0"
        cardViewSelezionati.visibility = View.GONE
    }

    private fun approvaSelected(v:View){
        val user = UserRepository(ParamManager.getLastUserId()).getUser()
        val result = if(user?.type == "livello1"){
            "ok_livello1"
        }
        else{
            "approvato"
        }
        val net = NetworkRichieste(NetworkController.apiCliente)
        for(item in (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems){
            if(result == "apporvato"){
                JuniorApplication.myDatabaseController.setGiustApprovato(item)
            }
            else{
                JuniorApplication.myDatabaseController.setGiustApprovatoLiv1(item)
            }
        }
        progressBar.visibility = View.VISIBLE
        net.setRichiesta(result, (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems){
            if(it.oldValue == "ok"){
                AlertDialog.Builder(requireContext())
                    .setTitle("Completata")
                    .setMessage("Richiesta di giustificativo completata con successo")
                    .setPositiveButton("ok", null)
                    .show()
            }
            else{
                AlertDialog.Builder(requireContext())
                    .setTitle("Errore")
                    .setMessage("Errore nella richiesta di giustificativo, riprovare con una rete valida")
                    .setPositiveButton("ok", null)
                    .show()
            }
            progressBar.visibility = View.GONE
        }
        svuotaListaSelected()
    }

    private fun negaSelected(v:View){
        val net = NetworkRichieste(NetworkController.apiCliente)
        for(item in (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems){
            JuniorApplication.myDatabaseController.setGiustNegato(item)
        }
        progressBar.visibility = View.VISIBLE
        net.setRichiesta("negato", (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems){
            if(it.oldValue == "ok"){
                AlertDialog.Builder(requireContext())
                    .setTitle("Completata")
                    .setMessage("Richiesta di giustificativo completata con successo")
                    .setPositiveButton("ok", null)
                    .show()
            }
            else{
                AlertDialog.Builder(requireContext())
                    .setTitle("Errore")
                    .setMessage("Errore nella richiesta di giustificativo, riprovare con una rete valida")
                    .setPositiveButton("ok", null)
                    .show()
            }
            progressBar.visibility = View.GONE
        }
        svuotaListaSelected()
    }

    fun listenToGiust(){
        MainScope().async {
            val user = UserRepository(ParamManager.getLastUserId()).getUser()
            val dip = DipendentiRepository(user?.idDipendente ?: -1).getDipendente()
            JuniorApplication.myDatabaseController.getGiustFlowNoMieDaGestire(dip?.serverId ?:-1).collect {
                var list = ArrayList<GiustificheRecord>()
                if (it is List<*>) {
                    val intUser = UserRepository(ParamManager.getLastUserId()).getUser()
                    for(item in it){
                        when (intUser?.livello_manager){
                            "livello2" ->{
                                if(item?.richiesto == "ok_livello1") {
                                    list.add(item)
                                }
                            }
                            "livello1" ->{
                                if(item?.richiesto == "richiesto") {
                                    list.add(item)
                                }
                            }
                            else ->{
                                if(item != null && item.richiesto != "approvato" && item.richiesto != "negato") {
                                    list.add(item)
                                }
                            }
                        }
                    }
                    adapt(list)
                }
            }
        }
    }

    private fun adapt(list:List<GiustificheRecord?>){
        if(isAdded){
            recyclerView.adapter = ApprovaNegaGiustAdapter(list, resources, cardViewSelezionati)
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contraint : ConstraintLayout
        val imageViewStatus: ImageView
        val textViewNomeDip : TextView
        val textViewNomeGiust : TextView
        val textViewAbbreviazioneGiust : TextView
        val textViewGiustDal : TextView
        val textViewGiustAl : TextView

        init {
            contraint = itemView.findViewById(R.id.constraintLayout_approvaNega)
            imageViewStatus = itemView.findViewById(R.id.imageView_statusRichiesta)
            textViewNomeDip = itemView.findViewById(R.id.textView_nomeDip)
            textViewNomeGiust = itemView.findViewById(R.id.textView_nomeGiust)
            textViewAbbreviazioneGiust = itemView.findViewById(R.id.textView_abbreviazioneGiust)
            textViewGiustDal = itemView.findViewById(R.id.textView_giustDal)
            textViewGiustAl = itemView.findViewById(R.id.textView_giustAl)

            imageViewStatus.visibility = View.GONE
        }
    }

    class ApprovaNegaGiustAdapter(val list: List<GiustificheRecord?>,val resources:Resources, val cardSlect: CardView) : RecyclerView.Adapter<RecyclerViewHolder>() {

        var selectedItems = ArrayList<Long>()

        override fun getItemViewType(position: Int): Int {
            return R.layout.layout_richiesta
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            if(list!= null){
                val nomeGiust = GiustificheConverter.getNameById(list[position]!!.giu_type_id)

                if(selectedItems.contains(list.get(position)?.giu_id)){
                    holder.contraint.background = ResourcesCompat.getDrawable(resources, R.color.bluosti_light, null)
                }
                else{
                    holder.contraint.background = ResourcesCompat.getDrawable(resources, R.drawable.borders_layout, null)
                }
                holder.contraint.setOnLongClickListener {
                    if(selectedItems.isEmpty()) {
                        it.background =
                            ResourcesCompat.getDrawable(resources, R.color.bluosti_light, null)
                        selectedItems.add(list[position]?.giu_id ?: -1)
                        updateSelect()
                    }
                    return@setOnLongClickListener true
                }
                holder.contraint.setOnClickListener{
                    if(selectedItems.isNotEmpty()){
                        if(selectedItems.contains(list[position]?.giu_id)){
                            it.background = ResourcesCompat.getDrawable(resources, R.drawable.borders_layout, null)
                            selectedItems.remove(list[position]?.giu_id)
                        }
                        else{
                            it.background = ResourcesCompat.getDrawable(resources, R.color.bluosti_light, null)
                            selectedItems.add(list[position]?.giu_id ?: -1)
                        }
                    }
                    updateSelect()
                }
                holder.textViewNomeDip.text = list[position]!!.dip_nome
                holder.textViewNomeGiust.text = nomeGiust
                holder.textViewAbbreviazioneGiust.text = list[position]!!.abbreviazione_giustifica
                holder.textViewGiustDal.text = list[position]!!.data_inizio
                holder.textViewGiustAl.text = list[position]!!.data_fine
            }
        }

        private fun updateSelect(){
            val text = cardSlect.getChildAt(0) as TextView
            if(selectedItems.isEmpty()){
                cardSlect.visibility = View.GONE
            }
            else{
                cardSlect.visibility = View.VISIBLE
                text.text = selectedItems.count().toString()
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

}