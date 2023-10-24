package com.osti.juniorapp.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.network.NetworkController
import java.io.File
import java.util.Calendar

class CartellinoFragment : Fragment() {

    private lateinit var constraintMesi: ConstraintLayout

    private lateinit var cardViewGennaio: CardView
    private lateinit var cardViewFebbraio: CardView
    private lateinit var cardViewMarzo: CardView
    private lateinit var cardViewAprile: CardView
    private lateinit var cardViewMaggio: CardView
    private lateinit var cardViewGiugno: CardView
    private lateinit var cardViewLuglio: CardView
    private lateinit var cardViewAgosto: CardView
    private lateinit var cardViewSettembre: CardView
    private lateinit var cardViewOttobre: CardView
    private lateinit var cardViewNovembre: CardView
    private lateinit var cardViewDicembre: CardView

    private lateinit var progressBar: ProgressBar
    private lateinit var textViewAnno: TextView
    private lateinit var annoPiu: ImageView
    private lateinit var annoMeno: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_cartellino, container, false)
        init(v)
        return v
    }

    private fun init(v:View) {
        activity?.runOnUiThread{
            constraintMesi = v.findViewById(R.id.constraint_mesi)

            cardViewGennaio = v.findViewById(R.id.cardView_gennaio)
            cardViewFebbraio = v.findViewById(R.id.cardView_febbraio)
            cardViewMarzo = v.findViewById(R.id.cardView_marzo)
            cardViewAprile = v.findViewById(R.id.cardView_aprile)
            cardViewMaggio = v.findViewById(R.id.cardView_maggio)
            cardViewGiugno = v.findViewById(R.id.cardView_giugno)
            cardViewLuglio = v.findViewById(R.id.cardView_luglio)
            cardViewAgosto = v.findViewById(R.id.cardView_agosto)
            cardViewSettembre = v.findViewById(R.id.cardView_settembre)
            cardViewOttobre = v.findViewById(R.id.cardView_ottobre)
            cardViewNovembre = v.findViewById(R.id.cardView_novembre)
            cardViewDicembre = v.findViewById(R.id.cardView_dicembre)

            progressBar = v.findViewById(R.id.progressBar_cartellini)
            textViewAnno = v.findViewById(R.id.textView_cartelliniAnno)
            annoPiu = v.findViewById(R.id.imageView_annoPiu)
            annoMeno = v.findViewById(R.id.imageView_annoMeno)

            textViewAnno.text = Calendar.getInstance().get(Calendar.YEAR).toString()

            cardViewGennaio.tag = "01"
            cardViewFebbraio.tag = "02"
            cardViewMarzo.tag = "03"
            cardViewAprile.tag = "04"
            cardViewMaggio.tag = "05"
            cardViewGiugno.tag = "06"
            cardViewLuglio.tag = "07"
            cardViewAgosto.tag = "08"
            cardViewSettembre.tag = "09"
            cardViewOttobre.tag = "10"
            cardViewNovembre.tag = "11"
            cardViewDicembre.tag = "12"

            cardViewGennaio.setOnClickListener(this::onClick)
            cardViewFebbraio.setOnClickListener(this::onClick)
            cardViewMarzo.setOnClickListener(this::onClick)
            cardViewAprile.setOnClickListener(this::onClick)
            cardViewMaggio.setOnClickListener(this::onClick)
            cardViewGiugno.setOnClickListener(this::onClick)
            cardViewLuglio.setOnClickListener(this::onClick)
            cardViewAgosto.setOnClickListener(this::onClick)
            cardViewSettembre.setOnClickListener(this::onClick)
            cardViewOttobre.setOnClickListener(this::onClick)
            cardViewNovembre.setOnClickListener(this::onClick)
            cardViewDicembre.setOnClickListener(this::onClick)

            annoPiu.setOnClickListener(this::annoPiu)
            annoMeno.setOnClickListener(this::annoMeno)

            selectActualMonth()
        }
    }

    private fun selectActualMonth(){
        var mese = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString()
        if(mese.length == 1){
            mese = "0$mese"
        }
        for (view in constraintMesi.children){
            if(view.tag == mese && view is CardView){
                val tmpConstraint = view.getChildAt(0)
                tmpConstraint.background = ResourcesCompat.getDrawable(resources, R.drawable.mese_attuale_background, null)
            }
        }
    }

    private fun annoPiu(v:View){
        try{
            val anno = textViewAnno.text.toString()
            textViewAnno.text = (anno.toInt() + 1).toString()
        }
        catch (e:Exception){

        }
    }

    private fun annoMeno(v:View){
        try{
            val anno = textViewAnno.text.toString()
            textViewAnno.text = (anno.toInt() - 1).toString()
        }
        catch (e:Exception){

        }
    }

    private fun onClick(v:View){
        val dir = JuniorApplication.getDirFiles(requireActivity())
        val file = File(dir, "Cartellino${textViewAnno.text}-${v.tag}.pdf")
        progressBar.visibility = View.VISIBLE
        NetworkController.getCartellino(file.absolutePath, "${textViewAnno.text}-${v.tag}"){
            progressBar.visibility = View.GONE
            if (it.newValue == "FAIL"){
                AlertDialog.Builder(requireContext())
                    .setTitle("Errore")
                    .setMessage("Impossibile raggiungere il server per aggiornare il cartellino.\nRiprovare con una rete valida")
                    .setPositiveButton("Ok", null)
                    .show()
            }
            else{
                JuniorApplication.setLastFragment(CartellinoFragment::class.simpleName, activity)
                requireActivity().startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        .setDataAndType(FileProvider.getUriForFile(requireActivity().baseContext, "com.osti.juniorapp.JFileProvider", file), "application/pdf"))
            }
        }
    }
}