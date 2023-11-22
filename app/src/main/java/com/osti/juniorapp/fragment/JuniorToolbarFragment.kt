package com.osti.juniorapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUser

class JuniorToolbarFragment : Fragment() {

    private lateinit var nameTextView:TextView
    private lateinit var buttonMenu: ImageView

    private var drawer:DrawerLayout? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.junior_toolbar, container, false)
        init(v)
        return v
    }

    private fun init(v:View){
        activity?.runOnUiThread{
            buttonMenu.setOnClickListener(this::onClickMenu)

            drawer = activity?.findViewById(R.id.drawer_layout)

            nameTextView.text = JuniorUser.JuniorDipendente.nome
        }
    }

    fun onClickMenu(view:View){
        drawer?.openDrawer(GravityCompat.END)
    }
}