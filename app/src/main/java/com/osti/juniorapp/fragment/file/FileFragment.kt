package com.osti.juniorapp.fragment.file

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.StatusController
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.db.resolvers.NomiFileResolver
import com.osti.juniorapp.db.tables.NomiFileTable
import com.osti.juniorapp.preferences.JuniorShredPreferences
import com.osti.juniorapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File

var selectedButtonId: Int? = null

class FileFragment : Fragment() {
    var ordineFile = MutableLiveData<Int>(0)

    //lateinit var spinnerOrdine: Spinner

    lateinit var recyclerView: RecyclerView

    lateinit var textViewNoFile: TextView

    lateinit var progressBar: ProgressBar

    lateinit var buttonNuovi: Button
    lateinit var buttonTutti: Button

    var nomi: ArrayList<NomiFileTable>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_file, container, false)
        init(v)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listenToNomi()
        getFileNamesFromServer()
    }

    private fun init(v:View){
        activity?.runOnUiThread{
            //spinnerOrdine = v.findViewById(R.id.spinner_ordinaFile)
            recyclerView = v.findViewById(R.id.recycler_nomiFile)

            textViewNoFile = v.findViewById(R.id.textView_noFile)

            progressBar = v.findViewById(R.id.progressBar_file)

            buttonNuovi = v.findViewById(R.id.button_nuoviFile)
            buttonTutti = v.findViewById(R.id.button_tuttiFile)

            selectedButtonId = buttonNuovi.id

            buttonNuovi.setOnClickListener(this::onClick)
            buttonTutti.setOnClickListener(this::onClick)

            buttonNuovi.background = ResourcesCompat.getDrawable(resources, R.drawable.selector_button_exit, null)
        }
    }

    private fun updateView(nomi:List<NomiFileTable>? = null){
        if(!nomi.isNullOrEmpty()){
            this.nomi = nomi as ArrayList<NomiFileTable>
        }
        activity?.runOnUiThread {
            if (this.nomi != null){
                var tmpNomi = ArrayList<NomiFileTable>()
                if (selectedButtonId == buttonNuovi.id){
                    for (item in this.nomi!!){
                        if(item.fld_dataora_visto_prima == "0000-00-00 00:00:00"){
                            tmpNomi.add(item)
                        }
                    }
                }
                else{
                    tmpNomi = this.nomi as ArrayList<NomiFileTable>
                }
                recyclerView.adapter = CustomAdapter(tmpNomi, requireActivity() as MainActivity, progressBar)
                recyclerView.layoutManager = LinearLayoutManager(context)

                if(tmpNomi.isEmpty()){
                    textViewNoFile.visibility = View.VISIBLE
                }
                else{
                    textViewNoFile.visibility = View.GONE
                }
            }
            else{
                textViewNoFile.visibility = View.VISIBLE
            }

            /*showSpinner(Utils.opzioniOrdineFile, requireContext())
            ordineFile.value =
                JuniorShredPreferences.getSharedPref("ORDINE FILE", requireContext())?.toInt() ?: 0*/
        }
    }

    private fun onClick(v:View){
        if(selectedButtonId != v.id){
            selectedButtonId = v.id
            updateView()
            if(selectedButtonId == buttonNuovi.id){
                buttonTutti.background = buttonNuovi.background
                buttonNuovi.background = ResourcesCompat.getDrawable(resources, R.drawable.selector_button_exit, null)
                //buttonTutti.setBackgroundColor(resources.getColor(com.google.android.material.R.color.btn, null))
            }
            else{
                buttonNuovi.background = buttonTutti.background
                buttonTutti.background = ResourcesCompat.getDrawable(resources, R.drawable.selector_button_exit, null)
            }
        }
    }

    private fun showSpinner(opzioni: List<String>, context: Context) {
        /*activity?.runOnUiThread {
            spinnerOrdine.adapter = ArrayAdapter(
                context,
                R.layout.spinner_text_causali_layout,
                opzioni
            )
            spinnerOrdine.onItemSelectedListener =
                MySelectionListener(this)
            spinnerOrdine.setPopupBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.color.bluosti_light,
                    null
                )
            )
        }*/
    }

    class MySelectionListener(val fragment: FileFragment) :
        AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            val ordine = Utils.opzioniOrdineFile[p2]
            JuniorShredPreferences.setSharedPref(ordine, "ORDINE FILE", fragment.requireContext())
            fragment.ordineFile.value = p2
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            //NIENTE
        }
    }

    private fun listenToNomi(){
        CoroutineScope(Dispatchers.IO).async {
            JuniorApplication.myDatabaseController.getNomiFlow().collect{
                if(!it.isNullOrEmpty()){
                    updateView(it)
                }
            }
        }
    }

    private fun getFileNamesFromServer(){
        if(StatusController.statusApp.value?.osti == true && StatusController.statusApp.value?.cliente == true){
            NetworkController.getFileNames{
                if(it.newValue == "FAIL"){
                    showAlertOffline()
                }
                else if(it.newValue is JsonElement){
                    val i = (it.newValue as JsonObject).getAsJsonArray("elencoFileDipendente")
                    clearDbNomi().invokeOnCompletion {
                        saveNomiFile(NomiFileResolver.createNomiFileFromJson(i))
                    }
                }
            }
        }
        else{
            showAlertOffline()
        }
    }
    private fun showAlertOffline(){
        activity?.runOnUiThread{
            AlertDialog.Builder(requireContext())
                .setTitle("Errore")
                .setMessage("Impossibile raggiungere il server per aggiornare i file.\nRiprovare con una rete valida")
                .setPositiveButton("Ok"){_,_ ->
                    (activity as MainActivity).startRIghtFragment()
                }
                .show()
        }
    }

    private fun clearDbNomi(): Deferred<Unit>{
        return JuniorApplication.myDatabaseController.clearNomi()
    }

    private fun saveNomiFile(list: List<NomiFileTable>){
        for (item in list){
            JuniorApplication.myDatabaseController.creaNome(item)
        }
    }

    class CustomAdapter(private val nomi: List<NomiFileTable>?,val activity: MainActivity,val progressBar: ProgressBar) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {


        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder)
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            var textViewNome: TextView
            var textViewData: TextView
            var imageViewIcon: ImageView
            var constraint: ConstraintLayout

            init {
                // Define click listener for the ViewHolder's View
                textViewNome = view.findViewById(R.id.textView_nomeFile)
                textViewData = view.findViewById(R.id.textView_dataFile)
                imageViewIcon = view.findViewById(R.id.imageView_file)
                constraint = view.findViewById(R.id.constraint_file)
            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.nomi_file_layout, viewGroup, false)
            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val fileName = nomi?.get(position)
            if (fileName != null) {
                viewHolder.textViewNome.text = fileName.fil_nome
                viewHolder.textViewData.text = fileName.fil_dataora_upload
                if(fileName.fil_estensione == "pdf"){
                    viewHolder.imageViewIcon.setImageResource(R.drawable.pdf_file)
                }
                if(fileName.fil_estensione == "png"){
                    viewHolder.imageViewIcon.setImageResource(R.drawable.png_file_)
                }

                if (fileName.file_tipo == "Comunicazioni" || fileName.file_tipo == "Comunicazioni con conferma"){
                    viewHolder.imageViewIcon.setImageResource(R.drawable.megaphone)
                }

                if(fileName.file_tipo == "Comunicazioni con conferma"){
                    viewHolder.constraint.setOnClickListener{
                        activity.supportFragmentManager.beginTransaction().apply{
                            replace(R.id.fragmentContainerView_accettaNega, AccettaNegaFileFrgment.newInstance(fileName.fil_id))
                            commit()
                        }
                    }
                }
                else{
                    viewHolder.constraint.setOnClickListener{
                        downloadAndOpen(fileName)
                    }
                }
            }
        }

        private fun downloadAndOpen(fileName:NomiFileTable?){
            if(fileName!= null){
                val dir = JuniorApplication.getDirFiles(activity)
                val file = File(dir, (fileName.fil_nome_visualizzato ?:fileName.fil_nome ?: "err")+'.'+ (fileName.fil_estensione ?: "pdf"))
                try{
                    progressBar.visibility = View.VISIBLE
                    NetworkController.getFile(file.absolutePath, fileName.fil_nome_url){
                        progressBar.visibility = View.GONE
                        if(it.newValue == "FAIL"){
                            AlertDialog.Builder(activity.baseContext)
                                .setTitle("Errore")
                                .setMessage("Impossibile raggiungere il server per scaricare i file.\nRiprovare con una rete valida")
                                .setPositiveButton("Ok", null)
                                .show()
                        }
                        else{
                            JuniorApplication.setLastFragment(FileFragment::class.simpleName, activity)
                            //JuniorApplication.setLastFragment(FileFragment::class.simpleName, activity.baseContext)
                            activity.startActivity(Intent(Intent.ACTION_VIEW)
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                .setDataAndType(FileProvider.getUriForFile(activity.baseContext, "com.osti.juniorapp.JFileProvider", file), fileName.fil_mimetype))
                        }
                    }
                }
                catch (e:Exception){

                }
            }
        }


        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = nomi?.size ?: 0

    }
}