package com.osti.juniorapp.fragment.giustificazioni

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUserOld
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.utils.GiustificheConverter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

class StoricoRichiesteFragment : Fragment() {


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

    private fun init(v: View){
        recyclerView = v.findViewById(R.id.recycler_richiesteManager)
        buttonApprova = v.findViewById(R.id.button_approvaGiust)
        buttonNega = v.findViewById(R.id.button_negaGiust)

        buttonApprova.visibility = View.GONE
        buttonNega.visibility = View.GONE

        val use = JuniorApplication.myJuniorUser.value

        if(use!=null){
            listenToGiust(use)
        }
    }

    fun listenToGiust(user: JuniorUserOld){
        MainScope().async {
            JuniorApplication.myDatabaseController.getGiustFlowStorico(user.dipentende!!.serverId).collect {
                var list = ArrayList<GiustificheRecord>()
                if (it is List<*>) {
                    when (user.livelloManager){
                        "livello1" -> {
                            for(item in it){
                                if(item != null && item.richiesto != "ok_livello1") {
                                    list.add(item)
                                }
                            }
                        }
                        "livello2" -> {
                            for(item in it){
                                if(item != null && item.richiesto == "ok_livello1") {
                                    list.add(item)
                                }
                            }
                        }
                        else -> {
                            for(item in it){
                                if(item != null && item.richiesto != "richiesto") {
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
            recyclerView.adapter = ApprovaNegaGiustAdapter(list)
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

    class ApprovaNegaGiustAdapter(val list: List<GiustificheRecord?>) : RecyclerView.Adapter<RecyclerViewHolder>() {

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

                holder.contraint.setOnLongClickListener {
                    it.setBackgroundColor(Color.CYAN)
                    selectedItems.add(list[position]?.giu_id ?:-1)
                    return@setOnLongClickListener true
                }
                holder.textViewNomeDip.text = list[position]!!.dip_nome
                holder.textViewNomeGiust.text = nomeGiust
                holder.textViewAbbreviazioneGiust.text = list[position]!!.abbreviazione_giustifica
                holder.textViewGiustDal.text = list[position]!!.data_inizio
                holder.textViewGiustAl.text = list[position]!!.data_fine
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

}