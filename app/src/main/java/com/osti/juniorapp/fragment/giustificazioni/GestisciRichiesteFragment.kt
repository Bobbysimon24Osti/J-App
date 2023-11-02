package com.osti.juniorapp.fragment.giustificazioni

import android.app.AlertDialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUserOld
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.network.NetworkRichieste
import com.osti.juniorapp.utils.GiustificheConverter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlin.Int


class GestisciRichiesteFragment : Fragment() {

    lateinit var cardViewSelezionati: CardView
    lateinit var textViewSelezionati: TextView

    lateinit var progressBar: ProgressBar

    lateinit var recyclerView: RecyclerView
    lateinit var buttonApprova: Button
    lateinit var buttonNega: Button

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
        buttonApprova = v.findViewById(R.id.button_approvaGiust)
        buttonNega = v.findViewById(R.id.button_negaGiust)

        buttonApprova.setOnClickListener(this::approvaSelected)
        buttonNega.setOnClickListener(this::negaSelected)

        val use = JuniorApplication.myJuniorUser.value

        if(use!=null){
            listenToGiust(use)
        }
    }

    private fun svuotaListaSelected(){
        (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems.clear()
        textViewSelezionati.text = "0"
        cardViewSelezionati.visibility = View.GONE
    }

    private fun approvaSelected(v:View){
        val net = NetworkRichieste(NetworkController.apiCliente)
        for(item in (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems){
            JuniorApplication.myDatabaseController.setGiustApprovato(item)
        }
        progressBar.visibility = View.VISIBLE
        net.setRichiesta("approvato", (recyclerView.adapter as ApprovaNegaGiustAdapter).selectedItems){
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

    fun listenToGiust(user: JuniorUserOld){
        MainScope().async {
            JuniorApplication.myDatabaseController.getGiustFlowNoMieDaGestire(user.dipentende!!.serverId).collect {
                var list = ArrayList<GiustificheRecord>()
                if (it is List<*>) {
                    for(item in it){
                       if(item?.richiesto == "richiesto") {
                           list.add(item)
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
        val textViewNomeDip : TextView
        val textViewNomeGiust : TextView
        val textViewAbbreviazioneGiust : TextView
        val textViewGiustDal : TextView
        val textViewGiustAl : TextView

        init {
            contraint = itemView.findViewById(R.id.constraintLayout_approvaNega)
            textViewNomeDip = itemView.findViewById(R.id.textView_nomeDip)
            textViewNomeGiust = itemView.findViewById(R.id.textView_nomeGiust)
            textViewAbbreviazioneGiust = itemView.findViewById(R.id.textView_abbreviazioneGiust)
            textViewGiustDal = itemView.findViewById(R.id.textView_giustDal)
            textViewGiustAl = itemView.findViewById(R.id.textView_giustAl)
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
                    it.background = ResourcesCompat.getDrawable(resources, R.color.bluosti_light, null)
                    selectedItems.add(list[position]?.giu_id ?:-1)
                    updateSelect()
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