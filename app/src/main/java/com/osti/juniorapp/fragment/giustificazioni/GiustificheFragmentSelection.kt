package com.osti.juniorapp.fragment.giustificazioni

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.osti.juniorapp.R
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.StatusController

class GiustificheFragmentSelection : Fragment() {

    lateinit var textViewNoInternet: TextView

    lateinit var buttonStorico: CardView
    lateinit var buttonNuove: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_giustifiche_disambiguation, container, false)
        init(v)
        return v
    }

    private fun init(v:View){
        textViewNoInternet = v.findViewById(R.id.textView_noInternet_richieste)

        buttonStorico = v.findViewById(R.id.cardView_storicoRichieste)
        buttonNuove = v.findViewById(R.id.cardView_nuoveRichieste)

        if(ActivationController.isActivated() && StatusController.statusApp.value?.cliente == true){
            textViewNoInternet.visibility = View.GONE
            buttonNuove.isClickable = true
            buttonStorico.isClickable = true
            buttonNuove.setOnClickListener{
                activity?.supportFragmentManager?.beginTransaction()?.apply{
                    replace(R.id.fragmentContainerView, GestisciRichiesteFragment())
                    commit()
                }
            }

            buttonStorico.setOnClickListener{
                activity?.supportFragmentManager?.beginTransaction()?.apply{
                    replace(R.id.fragmentContainerView, StoricoRichiesteFragment())
                    commit()
                }
            }
        }
        else{
            textViewNoInternet.visibility = View.VISIBLE
            buttonNuove.isClickable = false
            buttonStorico.isClickable = false
        }
    }

}