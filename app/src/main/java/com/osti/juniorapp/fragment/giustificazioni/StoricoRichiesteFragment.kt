package com.osti.juniorapp.fragment.giustificazioni

import android.app.AlertDialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.thread.RiceviDatiThread
import com.osti.juniorapp.utils.GiustificheConverter
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import java.beans.PropertyChangeListener



class StoricoRichiesteFragment : Fragment() {

    lateinit var refreshView: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    lateinit var buttonApprova: CardView
    lateinit var buttonNega: CardView

    lateinit var textViewNoGiust: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_lista_richieste, container, false)
        init(view)
        return view
    }

    private fun init(v: View){
        refreshView = v.findViewById(R.id.swipeRefresh_listaRichieste)
        recyclerView = v.findViewById(R.id.recycler_richiesteManager)
        buttonApprova = v.findViewById(R.id.cardView_approvaRichiesta)
        buttonNega = v.findViewById(R.id.cardView_negaRichiesta)
        textViewNoGiust = v.findViewById(R.id.textView_noGiustifiche)

        refreshView.setOnRefreshListener(this::tryGiustUpdate)

        buttonApprova.visibility = View.GONE
        buttonNega.visibility = View.GONE

        listenToGiust()

    }

    fun listenToGiust(){
        MainScope().async {
            JuniorApplication.myDatabaseController.getGiustFlowStorico(JuniorApplication.myJuniorUser.value?.dipentende?.serverId ?: -1).collect {
                var list = ArrayList<GiustificheRecord>()
                if (it is List<*>) {
                    when (JuniorApplication.myJuniorUser.value?.livelloManager){
                        "livello1" -> {
                            for(item in it){
                                if(item != null && item.richiesto == "ok_livello1") {
                                    list.add(item)
                                }
                            }
                        }
                        else -> {
                            for(item in it){
                                if(item != null && item.richiesto == "approvato" || item!!.richiesto == "negato") {
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


    private fun adapt(list:List<GiustificheRecord?>){
        if(list.isNotEmpty()){
            if(isAdded){
                textViewNoGiust.visibility = View.GONE
                recyclerView.adapter = ApprovaNegaGiustAdapter(list, resources, requireActivity() as MainActivity)
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
        }
        else{
            textViewNoGiust.visibility = View.VISIBLE
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val constraintContainer: ConstraintLayout
        val imageviewStatus : ImageView
        val textViewNomeDip : TextView
        val textViewNomeGiust : TextView
        val textViewAbbreviazioneGiust : TextView
        val textViewGiustDal : TextView
        val textViewGiustAl : TextView

        init {
            constraintContainer = itemView.findViewById(R.id.constraintLayout_approvaNega)
            imageviewStatus = itemView.findViewById(R.id.imageView_statusRichiesta)
            textViewNomeDip = itemView.findViewById(R.id.textView_nomeDip)
            textViewNomeGiust = itemView.findViewById(R.id.textView_nomeGiust)
            textViewAbbreviazioneGiust = itemView.findViewById(R.id.textView_abbreviazioneGiust)
            textViewGiustDal = itemView.findViewById(R.id.textView_giustDal)
            textViewGiustAl = itemView.findViewById(R.id.textView_giustAl)
        }
    }

    class ApprovaNegaGiustAdapter(val list: List<GiustificheRecord?>, val resources: Resources, val activity: MainActivity) : RecyclerView.Adapter<RecyclerViewHolder>() {

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

                if(list[position]?.richiesto == "approvato"){
                    holder.imageviewStatus.setImageResource(R.drawable.status_circle_approvato)
                }
                if(list[position]?.richiesto == "negato"){
                    holder.imageviewStatus.setImageResource(R.drawable.status_circle_negato)
                }
                holder.textViewNomeDip.text = list[position]!!.dip_nome
                holder.textViewNomeGiust.text = nomeGiust
                holder.textViewAbbreviazioneGiust.text = list[position]!!.abbreviazione_giustifica
                holder.textViewGiustDal.text = list[position]!!.data_inizio
                holder.textViewGiustAl.text = list[position]!!.data_fine

                holder.constraintContainer.setOnClickListener{
                    showDettagliFragment(list[position]?.id, list[position]?.richiesto)
                }
            }
        }

        fun showDettagliFragment(id:Long?, richiesto:String?){
            if(id!=null && richiesto != null){
                val tmp = DettagliOldGiustificativiFragment.newInstance(id, richiesto)
                activity.supportFragmentManager.beginTransaction().apply{
                    replace(R.id.fragmentContainerView_dettagli_storicoGiust, tmp)
                    commit()
                }
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

}