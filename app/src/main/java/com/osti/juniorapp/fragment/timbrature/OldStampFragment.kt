package com.osti.juniorapp.fragment.timbrature

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.db.tables.TimbrTable
import com.osti.juniorapp.utils.Utils.FORMATDATEHOURS
import com.osti.juniorapp.utils.Utils.NORMALFORMATDATEHOURS


class OldStampFragment : Fragment() {

    lateinit var textViewNoTimbr: TextView
    lateinit var recyclerView: RecyclerView

    companion object{
        var packageManager : PackageManager? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_old_stamp_menu, container, false)
        // Inflate the layout for this fragment
        init(v)
        return v
    }

    fun init(v:View){
        activity?.runOnUiThread{
            val dip = JuniorApplication.myJuniorUser.value?.dipentende
            if(dip != null){
                recyclerView = v.findViewById(R.id.recycler_old_stamp_menu)
                textViewNoTimbr = v.findViewById(R.id.textView_noTimbr)
                JuniorApplication.myDatabaseController.getAllTimbr(dip.serverId){
                    if(it.newValue != null){
                        val timbr = it.newValue as List<TimbrTable>
                        showTimbr(timbr)
                    }
                }
            }
        }
    }

    private fun showTimbr (list:List<TimbrTable>){
        activity?.runOnUiThread {
            if(list.isEmpty()){
                textViewNoTimbr.visibility = View.VISIBLE
            }
            else{
                textViewNoTimbr.visibility = View.GONE
                recyclerView.adapter = CustomAdapter(list, resources, requireContext())
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
            packageManager = activity?.packageManager
        }
    }

    class CustomAdapter(private val oldStamp: List<TimbrTable>, val res:Resources, val context: Context) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder)
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textViewData: TextView
            val onServer: TextView
            val citta: TextView
            val causale: TextView
            val image: ImageView
            val constraintLayout : ConstraintLayout

            init {
                // Define click listener for the ViewHolder's View
                textViewData = view.findViewById(R.id.textView_dataOra_timbratura)
                onServer = view.findViewById(R.id.old_stamp_on_server)
                citta = view.findViewById(R.id.textView_oldStamp_posizione)
                causale = view.findViewById(R.id.textView_causale_timbratura)
                image = view.findViewById(R.id.imageView_onServer)

                constraintLayout = view.findViewById(R.id.constraintLayout_oldStamp_onServer)

            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.old_stamp_layout, viewGroup, false)
            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            val data = FORMATDATEHOURS.parse(oldStamp[position].dataOra)
            viewHolder.textViewData.text = NORMALFORMATDATEHOURS.format(data ?: 0)


            if(oldStamp[position].latitude != null){
                viewHolder.constraintLayout.tag = "${oldStamp[position].latitude},${oldStamp[position].longitude}"
            }

            if(oldStamp[position].citta!= null){
                viewHolder.citta.text = oldStamp[position].citta
            }
            else{
                viewHolder.citta.text = "LOCALITA SCONOSCIUTA"
            }

            //MOSTRA SE LA TIMBRATURA E SUL SERVER O MENO

            if(oldStamp[position].onServer){
                viewHolder.onServer.text = res.getString(R.string.state_on_server)
                viewHolder.image.setImageResource(R.drawable.cloud_online)
            }
            else{
                viewHolder.onServer.text = res.getString(R.string.state_offline)
                viewHolder.image.setImageResource(R.drawable.cloud_offline)
            }
            viewHolder.constraintLayout.setOnClickListener(this::onClick)
        }

        private fun onClick(v:View){
            AlertDialog.Builder(context)
                .setTitle("Aprire Posizione")
                .setMessage("Aprire posizione su google maps?")
                .setNegativeButton("Annulla", null)
                .setPositiveButton("Apri"){_,_ ->
                    val gmmIntentUri: Uri = Uri.parse("geo:${v.tag}?q=${v.tag}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (packageManager != null) {
                        startActivity(v.context, mapIntent, null)
                    }
                }
                .show()

        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = oldStamp.size

    }

}