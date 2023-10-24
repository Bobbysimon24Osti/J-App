package com.osti.juniorapp.fragment.file

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.tables.NomiFileTable
import java.io.File

class AccettaNegaFileFrgment() : DialogFragment() {
    var id:Long = -1

    lateinit var constraintButtons: ConstraintLayout
    lateinit var constraintAll: ConstraintLayout
    lateinit var textViewRisposta: TextView

    lateinit var textViewNome: TextView
    lateinit var buttonClose: ImageView
    lateinit var buttonAccetta: Button
    lateinit var buttonNega: Button
    lateinit var buttonApri: Button

    companion object{
        fun newInstance(id:Long): AccettaNegaFileFrgment {
            val fragment = AccettaNegaFileFrgment()
            val bund = Bundle()
            bund.putLong("id", id)
            fragment.arguments = bund
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null){
            requireArguments().getInt("id", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_accetta_nega_file_frgment, container, false)
        init(v)
        return v
    }

    private fun init(v:View){
        activity?.runOnUiThread{
            constraintButtons = v.findViewById(R.id.constraintLayout_accettaNega)
            constraintAll = v.findViewById(R.id.constraintLayout_content)
            textViewRisposta = v.findViewById(R.id.textViewLabel_approvatoNegato)
            textViewNome = v.findViewById(R.id.textView_nomeFile)
            buttonClose = v.findViewById(R.id.imageView_closeFile)
            buttonAccetta = v.findViewById(R.id.button_fileAccetta)
            buttonNega = v.findViewById(R.id.button_fileRifiuta)
            buttonApri = v.findViewById(R.id.button_apriFile)

            constraintAll.setOnClickListener(null)
            buttonClose.setOnClickListener{
                dismiss()
            }

            JuniorApplication.myDatabaseController.getNomeFile(id){
                val file = it.newValue as NomiFileTable

                buttonNega.setOnClickListener{
                    file.fld_risposta = "2"
                    NetworkController.inviaRispostaFile(file){
                        if (it.newValue != "FAIL"){
                            JuniorApplication.myDatabaseController.setRispostaFile(id, "Negato").invokeOnCompletion {
                                file.file_risposta = "Negato"
                                updateViews(file)
                            }
                        }
                        else {
                            showOfflineAlert()
                        }
                    }
                }
                buttonAccetta.setOnClickListener{
                    file.fld_risposta = "1"
                    NetworkController.inviaRispostaFile(file){
                        if (it.newValue != "FAIL"){
                            JuniorApplication.myDatabaseController.setRispostaFile(id, "Accettato").invokeOnCompletion {
                                file.file_risposta = "Accettato"
                                updateViews(file)
                            }
                        }
                        else {
                            showOfflineAlert()
                        }
                    }
                }

                buttonApri.setOnClickListener{
                    downloadAndOpen(file)
                }

                updateViews(file)
            }
        }
    }

    private fun showOfflineAlert(){
        activity?.runOnUiThread{
            AlertDialog.Builder(requireContext())
                .setTitle("Attenzione")
                .setMessage("Impossibile raggiungere il server, per poter accettare o negare un file è necessaria una connessione valida\nRirpovare più tardi")
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    private fun downloadAndOpen(fileName:NomiFileTable?){
        if(fileName!= null){
            val dir = JuniorApplication.getDirFiles(activity)
            val file = File(dir, (fileName.fil_nome_visualizzato ?:fileName.fil_nome ?: "err")+'.'+ (fileName.fil_estensione ?: "pdf"))
            try{
                NetworkController.getFile(file.absolutePath, fileName.fil_nome_url){
                    if(it.newValue == "FAIL"){
                        AlertDialog.Builder(activity?.baseContext)
                            .setTitle("Errore")
                            .setMessage("Impossibile raggiungere il server per scaricare i file.\nRiprovare con una rete valida")
                            .setPositiveButton("Ok", null)
                            .show()
                    }
                    else{
                        JuniorApplication.setLastFragment(FileFragment::class.simpleName, activity)
                        //JuniorApplication.setLastFragment(FileFragment::class.simpleName, activity?.baseContext)
                        activity?.startActivity(
                            Intent(Intent.ACTION_VIEW)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            .setDataAndType(FileProvider.getUriForFile(requireActivity().baseContext, "com.osti.juniorapp.JFileProvider", file), fileName.fil_mimetype))
                    }
                }
            }
            catch (e:Exception){

            }
        }
    }

    private fun updateViews(file:NomiFileTable){
        activity?.runOnUiThread{
            textViewNome.text = file.fil_nome_visualizzato ?: file.fil_nome
            if(file.file_risposta == "Senza risposta"){
                constraintButtons.visibility = View.VISIBLE
                textViewRisposta.visibility = View.GONE
            }
            else{
                constraintButtons.visibility = View.GONE
                textViewRisposta.visibility = View.VISIBLE
                textViewRisposta.text = file.file_risposta
            }
        }
    }
}