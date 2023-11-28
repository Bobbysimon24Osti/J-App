package com.osti.juniorapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.application.DipendentiRepository
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.UserRepository
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.db.tables.NotificheTable
import com.osti.juniorapp.fragment.giustificazioni.DettagliOldGiustificativiFragment
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.network.NetworkNotifiche
import com.osti.juniorapp.utils.Utils.FORMATDATEHOURS
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import java.util.Calendar

class NotificheFragment : Fragment() {

    val notificheNetwork =  NetworkNotifiche(NetworkController.apiCliente)

    lateinit var containerDettagli : FragmentContainerView

    lateinit var refresher : SwipeRefreshLayout

    lateinit var recyclerView : RecyclerView

    lateinit var textViewNoNotifiche: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifiche, container, false)
        init(view)
        return view
    }

    private fun init(v:View){

        containerDettagli = v.findViewById(R.id.fragmentContainerView_notificheGiust)

        refresher = v.findViewById(R.id.refresh_notifiche)

        recyclerView = v.findViewById(R.id.recyclerView_notifiche)

        textViewNoNotifiche = v.findViewById(R.id.textView_noNotifiche)


        refresher.setOnRefreshListener {
            refresh()
        }
        //listenToNotifiche()
        refresh()
    }

    private fun refresh(){
        notificheNetwork.getnotifiche{
            if (it.newValue is Deferred<*>){
                (it.newValue as Deferred<Unit>).invokeOnCompletion {
                    val user = UserRepository(ParamManager.getLastUserId()).getUser()
                    val dip = DipendentiRepository(user?.idDipendente ?: -1).getDipendente()
                    JuniorApplication.myDatabaseController.getNotificheList(dip?.serverId ?: -1){
                        refresher.isRefreshing = false
                        if(it.newValue is List<*>){
                            adapt(it.newValue as List<NotificheTable?>)
                        }
                    }
                }
            }
        }
    }


    private fun adapt(list:List<NotificheTable?>) = activity?.runOnUiThread{
        if(isAdded){
            if(list.isNotEmpty()){
                recyclerView.adapter = NotificheAdapter(list, requireActivity() as MainActivity)
                recyclerView.layoutManager = LinearLayoutManager(context)
                textViewNoNotifiche.visibility = View.GONE
            }
            else{
                textViewNoNotifiche.visibility = View.VISIBLE
            }
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val constraintLayoutNotifica: ConstraintLayout
        val textViewTitolo : TextView
        val textViewMessaggio : TextView
        val linearLayoutNuova : LinearLayout
        val imageViewNotifica : ImageView

        init {
            constraintLayoutNotifica = itemView.findViewById(R.id.constraint_notifica)
            textViewTitolo = itemView.findViewById(R.id.textView_titoloNotifica)
            textViewMessaggio = itemView.findViewById(R.id.textView_messaggioNotifica)
            linearLayoutNuova = itemView.findViewById(R.id.linearLayout_nuvaNotifica)
            imageViewNotifica = itemView.findViewById(R.id.imageView_nuovaNotifica)
        }
    }

    class NotificheAdapter(val list: List<NotificheTable?>, val activity: MainActivity) : RecyclerView.Adapter<RecyclerViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return R.layout.layout_lista_notifiche
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            if(list!= null && list.isNotEmpty() && list[position]?.n_ute_id_destinatario.toString() == ParamManager.getLastUserId()){
                val notifica = list[position]
                val net = NetworkNotifiche(NetworkController.apiCliente)
                if(notifica?.n_dataora_letta_app?.replace("\"", "") == "0000-00-00 00:00:00"){
                    holder.imageViewNotifica.setImageResource(R.drawable.baseline_notifications_nuove_24)
                    holder.linearLayoutNuova.visibility = View.VISIBLE
                    val dataOra = FORMATDATEHOURS.format(Calendar.getInstance().timeInMillis)
                    JuniorApplication.myDatabaseController.setDataLettura(notifica.n_id_record_notifica, dataOra)
                    net.setNotificaLetta(notifica.n_id){
                        //null
                    }
                }
                else{
                    holder.imageViewNotifica.setImageResource(R.drawable.baseline_notifications_24)
                    holder.linearLayoutNuova.visibility = View.GONE
                }
                holder.textViewTitolo.text = notifica?.n_oggetto ?: "ERROR"
                holder.textViewMessaggio.text = notifica?.n_messaggio ?: "ERROR"

                holder.constraintLayoutNotifica.setOnClickListener{
                    if (notifica != null && notifica.n_tipo_notifica == "giustificazioni" && !notifica.n_messaggio.contains("richiedo")) {
                        var tmpId: Long? = -1
                        var tmpRichiesto: String? = "-1"
                        JuniorApplication.myDatabaseController.getGiustByServerId(notifica.n_id_record_notifica){
                            if(it.newValue is GiustificheRecord){
                                tmpId = (it.newValue as GiustificheRecord).id
                                tmpRichiesto = (it.newValue as GiustificheRecord).richiesto
                            }
                        }.invokeOnCompletion {
                            showDettagliFragment(tmpId, tmpRichiesto)
                        }
                    }
                    else if (notifica != null && notifica.n_tipo_notifica == "file"){
                        activity.showFileFragment()
                    }
                }
            }
        }

        fun showDettagliFragment(id:Long?, richiesto:String?){
            if(id!=null && richiesto != null){
                val fragment = DettagliOldGiustificativiFragment.newInstance(id, richiesto)
                activity.supportFragmentManager.beginTransaction().apply{
                    replace(R.id.fragmentContainerView_notificheGiust, fragment)
                    commit()
                }
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }
}