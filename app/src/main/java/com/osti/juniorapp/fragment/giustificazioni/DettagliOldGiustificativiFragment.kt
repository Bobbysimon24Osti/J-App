package com.osti.juniorapp.fragment.giustificazioni

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonElement
import com.osti.juniorapp.R
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.StatusController
import com.osti.juniorapp.customviews.ConstraintLayoutColored
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.utils.GiustificheConverter
import com.osti.juniorapp.utils.Utils
import retrofit2.Response
//ID LOCALE, DIVERSO DALL'ID DEL SERVER
private const val ID_GIUST = "id"
private const val RICHIESTO = "offline"

class DettagliOldGiustificativiFragment : DialogFragment() {
    private var id: Long? = null
    private var richiesto: String? = null

    lateinit var giust: GiustificheRecord

    lateinit var closeButton: ImageView

    lateinit var textViewStatus: TextView
    lateinit var textViewRichistoIl: TextView
    lateinit var textViewGestitoIl: TextView
    lateinit var textViewNomeGiust: TextView
    lateinit var textViewValoreGiust: TextView
    lateinit var textViewDataOraDal: TextView
    lateinit var textViewDataOraAl: TextView
    lateinit var textViewManager: TextView
    lateinit var textViewNoteManager: TextView
    lateinit var buttonAnnulla: Button

    lateinit var progressbar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getLong(ID_GIUST)
            richiesto = it.getString(RICHIESTO)
        }
    }

    var cont: ViewGroup? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        cont = container

        val v : View = when(richiesto){

            "negato" , "annullato"-> {
                inflater.inflate(R.layout.fragment_dettagli_old_giustificativi_red, container, false)
            }

            "approvato" -> {
                inflater.inflate(R.layout.fragment_dettagli_old_giustificativi_green, container, false)
            }

            "offline", "richiesto"-> {
                inflater.inflate(R.layout.fragment_dettagli_old_giustificativi_yellow, container, false)
            }

            else -> {
                inflater.inflate(R.layout.fragment_dettagli_old_giustificativi, container, false)
            }
        }
        init(v)
        return v
    }

    fun init(v: View) {
        if(id== null){
            return
        }
        activity?.runOnUiThread{
            closeButton = v.findViewById(R.id.imageView_closeDettagliFragment)
            closeButton.setOnClickListener(this::onClick)

            JuniorApplication.myDatabaseController.getGiustificaRecordByLocalId(id!!){
                activity?.runOnUiThread{
                    if(it.newValue != null){
                        giust = it.newValue as GiustificheRecord

                        textViewStatus = v.findViewById(R.id.textView_approvatoNegato)
                        textViewRichistoIl = v.findViewById(R.id.textView_richiestoIl)
                        textViewGestitoIl = v.findViewById(R.id.textView_gestitoIl)
                        textViewNomeGiust = v.findViewById(R.id.textView_nomeGiustificativo)
                        textViewValoreGiust = v.findViewById(R.id.textView_valoreGiust)
                        textViewDataOraDal = v.findViewById(R.id.textView_dataOraInizioGiust)
                        textViewDataOraAl = v.findViewById(R.id.textView_dataOraFineGiust)
                        textViewManager = v.findViewById(R.id.textView_nomeManager)
                        textViewNoteManager = v.findViewById(R.id.textView_noteMasnager)
                        buttonAnnulla = v.findViewById(R.id.button_annullaRichiesta)
                        textViewStatus.text = giust.richiesto

                        progressbar = v.findViewById(R.id.progressBarGiust)

                        textViewRichistoIl.text = Utils.FORMATDATEHOURS.parse(giust.dataOra_richiesta)
                            ?.let { Utils.NORMALFORMATDATEHOURS.format(it) }
                        if(giust.dataOra_gestito != null &&
                            giust.dataOra_gestito != "null" &&
                            giust.dataOra_gestito != "0000-00-00 00:00:00" &&
                            giust.richiesto != "annullato")
                        {
                            textViewGestitoIl.text = Utils.FORMATDATEHOURS.parse(giust.dataOra_gestito!!)
                                ?.let { Utils.NORMALFORMATDATEHOURS.format(it) }
                        }
                        else{
                            textViewGestitoIl.text = "NON GESTITO"
                        }
                        textViewManager.text = giust.utente_definitivo
                        textViewNoteManager.text = giust.note_gestito
                        textViewNomeGiust.text = giust.nome
                        textViewValoreGiust.text = GiustificheConverter.getValore(giust)
                        val tmpDataInizio = Utils.FORMATDATEDB.parse(giust.data_inizio)
                            ?.let { Utils.FORMATDATE.format(it) }
                        val tmpDataFine = Utils.FORMATDATEDB.parse(giust.data_fine)
                            ?.let { Utils.FORMATDATE.format(it) }
                        val strInizio = "${tmpDataInizio}\n\n${
                            GiustificheConverter.getOraFromMin(
                                giust.ora_inizio
                            )
                        }"
                        textViewDataOraDal.text =strInizio
                        val strFine = "${tmpDataFine}\n\n${GiustificheConverter.getOraFromMin(giust.ora_fine)}"
                            textViewDataOraAl.text= strFine
                        buttonAnnulla.setOnClickListener {
                            progressbar.visibility = View.VISIBLE
                            if (ActivationController.isActivated() && StatusController.statusApp.value?.cliente == true && giust.onServer) {
                                NetworkController.deleteGiust(giust) {
                                    if (it.oldValue == true) {
                                        val response =
                                            (it.newValue as Response<JsonElement>).body()?.asJsonObject
                                        JuniorApplication.myDatabaseController.setGiustAnnullato(
                                            response?.get("gz_id")?.asLong
                                        )
                                        reloadFragment()
                                    }
                                    else {
                                        showOfflineDialog()
                                    }
                                }
                            }
                            else if (!giust.onServer) {
                                JuniorApplication.myDatabaseController.setGiustAnnullato(giust.id)
                                reloadFragment()
                            }
                            else {
                                showOfflineDialog()
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onDetach() {
        super.onDetach()
        progressbar.visibility = View.GONE
    }

    private fun reloadFragment() {
        if(id != null && cont != null){
            val tmp = DettagliOldGiustificativiFragment.newInstance(id!!, "annullato")
            activity?.supportFragmentManager?.beginTransaction()
                ?.apply {
                    replace(cont!!.id, tmp)
                    commit()
                }
        }
    }

    private fun showOfflineDialog(){
        progressbar.visibility = View.GONE
        activity?.runOnUiThread {
            AlertDialog.Builder(requireContext())
                .setTitle("Attenzione")
                .setMessage("Impossibile annullare il giustificativo mentre l'app è in modalità offline. \nCollegarsi a una rete e riprovare")
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    private fun calcOraInzio(giust: GiustificheRecord) : String{
        if(giust.abbreviazione_giustifica == "MT"){
            return GiustificheConverter.getValore(giust)
        }
        else{
            return GiustificheConverter.getOraFromMin(giust.ora_inizio)
        }
    }

    private fun calcOraFine(giust: GiustificheRecord) : String{
        if(giust.abbreviazione_giustifica == "MT"){
            return GiustificheConverter.getValore(giust)
        }
        else{
            return GiustificheConverter.getOraFromMin(giust.ora_fine)
        }
    }



    private fun onClick(v: View){
        dismiss()
    }



    companion object {
        fun newInstance(id: Long, richiesto: String) =
            DettagliOldGiustificativiFragment().apply {
                arguments = Bundle().apply {
                    putLong(ID_GIUST, id)
                    putString(RICHIESTO, richiesto)
                }
            }
    }
}