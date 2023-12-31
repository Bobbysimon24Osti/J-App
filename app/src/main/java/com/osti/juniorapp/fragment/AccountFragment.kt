package com.osti.juniorapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.LoginActivity
import com.osti.juniorapp.application.DipendentiRepository
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.UserRepository
import com.osti.juniorapp.db.ParamManager
import com.osti.juniorapp.db.tables.LogTable

class AccountFragment : Fragment() {

    lateinit var buttonEsci: Button
    lateinit var textViewUserId: TextView
    lateinit var textViewUserName: TextView
    lateinit var textViewUserType: TextView
    lateinit var textViewDipId: TextView
    lateinit var textViewDipName: TextView
    lateinit var textViewDipBadge: TextView
    lateinit var textViewLastMsg: TextView
    lateinit var textViewIdApp: TextView
    lateinit var textViewCodiceAtt: TextView
    lateinit var textViewUrl: TextView
    lateinit var textViewDatabase: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_account, container, false)
        // Inflate the layout for this fragment
        activity?.runOnUiThread{
            init(v)
        }
        return v
    }

    private fun init(view:View){
        activity?.runOnUiThread{
            buttonEsci = view.findViewById(R.id.button_esci)
            textViewUserId = view.findViewById(R.id.textView_idUtente)
            textViewUserName = view.findViewById(R.id.textView_nomeUtente)
            textViewUserType = view.findViewById(R.id.textView_tipoUtente)

            textViewDipBadge = view.findViewById(R.id.textView_numeroBadge)
            textViewDipName = view.findViewById(R.id.textView_nomeDipendente)
            textViewDipId = view.findViewById(R.id.textView_idDipendente)

            textViewIdApp = view.findViewById(R.id.textView_idApp)
            textViewCodiceAtt = view.findViewById(R.id.textView_codiceAtt)
            textViewUrl = view.findViewById(R.id.textView_url)
            textViewDatabase = view.findViewById(R.id.textView_database)

            textViewLastMsg = view.findViewById(R.id.textView_ultimoMsg)

            refreshData()


            JuniorApplication.myDatabaseController.getLastLog{
                activity?.runOnUiThread{
                    textViewLastMsg.text =(it.newValue as LogTable?)?.message ?: "Errore"
                }
            }
            buttonEsci.setOnClickListener(this::onClickEsci)
        }
    }

    private fun refreshData(){
        activity?.runOnUiThread {
            val user = UserRepository(ParamManager.getLastUserId()).getUser()
            val dipendente = DipendentiRepository(user?.idDipendente ?: -1).getDipendente()
            if(user!= null && dipendente!= null && dipendente.serverId != -1L){
                val param = ParamManager
                textViewUserId.text = "Id sul server: " + user.server_id
                textViewUserType.text = "Tipo accesso: " + user.type
                textViewUserName.text = "Nome: " + user.name

                textViewDipId.text = "Id sul server: " + dipendente.serverId.toString()
                textViewDipName.text = "Nome: " + dipendente.nome
                textViewDipBadge.text = "Badge: " + dipendente.badge.toString()

                textViewIdApp.text = "Id App: " + param.getIdApp()
                textViewCodiceAtt.text = "Codice attivazione app: " + param.getCodice()
                textViewUrl.text = "Url server Juniorweb: " + param.getUrlNoApi()
                textViewDatabase.text = "Database Juniorweb: " + param.getDatabase()
            }
        }
    }

    private fun onClickEsci(v:View){
        //startActivity(Intent(super.getContext(), LoginActivity::class.java))
        ParamManager.setLastUserId(null)
        UserRepository.logged = false

        //startActivity(Intent(requireContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }


}