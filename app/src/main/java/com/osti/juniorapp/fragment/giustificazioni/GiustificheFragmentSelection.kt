package com.osti.juniorapp.fragment.giustificazioni

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.osti.juniorapp.R

class GiustificheFragmentSelection : Fragment() {

    lateinit var buttonStorico: Button
    lateinit var buttonNuove: Button

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
        buttonStorico = v.findViewById(R.id.button_storicoRichieste)
        buttonNuove = v.findViewById(R.id.button_nuoveRichieste)

        buttonNuove.setOnClickListener{
            activity?.supportFragmentManager?.beginTransaction()?.apply{
                replace(R.id.fragmentContainerView, ApprovaNegaGiustFragment())
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

}