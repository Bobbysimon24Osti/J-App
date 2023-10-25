package com.osti.juniorapp.fragment.giustificazioni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUser
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.utils.GiustificheConverter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlin.Int


class ApprovaNegaGiustFragment : Fragment() {

    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_approva_nega_giust, container, false)
        init(view)
        return view
    }

    private fun init(v:View){
        recyclerView = v.findViewById(R.id.recycler_richiesteManager)
        val use = JuniorApplication.myJuniorUser.value
        if(use!=null){
            listenToGiust(use)
        }
    }

    fun listenToGiust(user: JuniorUser){
        MainScope().async {
            JuniorApplication.myDatabaseController.getGiustFlow(user).collect {
                if (it is List<*>) {
                    adapt(it)
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
        val textViewNomeDip : TextView
        val textViewNomeGiust : TextView
        val textViewAbbreviazioneGiust : TextView
        val textViewGiustDal : TextView
        val textViewGiustAl : TextView

        init {
            textViewNomeDip = itemView.findViewById(R.id.textView_nomeDip)
            textViewNomeGiust = itemView.findViewById(R.id.textView_nomeGiust)
            textViewAbbreviazioneGiust = itemView.findViewById(R.id.textView_abbreviazioneGiust)
            textViewGiustDal = itemView.findViewById(R.id.textView_giustDal)
            textViewGiustAl = itemView.findViewById(R.id.textView_giustAl)
        }
    }

    class ApprovaNegaGiustAdapter(val list: List<GiustificheRecord?>) : RecyclerView.Adapter<RecyclerViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return R.layout.layout_approva_nega_giustifiche
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            if(list!= null && list[position]?.richiesto == "richiesto"){
                val nomeGiust = GiustificheConverter.getNameById(list[position]!!.giu_type_id)

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