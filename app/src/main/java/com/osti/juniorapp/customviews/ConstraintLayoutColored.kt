package com.osti.juniorapp.customviews

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.osti.juniorapp.R

class ConstraintLayoutColored(val constraintLayout: ViewGroup) {


    fun setAnnullato(){
        constraintLayout.background = ColorDrawable(constraintLayout.resources.getColor(R.color.yellow_offline, constraintLayout.resources.newTheme()))
    }

    fun setApprovato(){
        constraintLayout.background = ColorDrawable(constraintLayout.resources.getColor(R.color.green_approvato, constraintLayout.resources.newTheme()))
    }

    fun setNegato(){
        constraintLayout.background = ColorDrawable(constraintLayout.resources.getColor(R.color.red_negato, constraintLayout.resources.newTheme()))
    }


    fun setApprovatoLightBorder(){
        constraintLayout.background = ResourcesCompat.getDrawable(constraintLayout.resources, R.drawable.dettagli_old_giustifiche_approvato_borders, constraintLayout.resources.newTheme())
    }

    fun setNegatoLightBorder(){
        constraintLayout.background = ResourcesCompat.getDrawable(constraintLayout.resources, R.drawable.dettagli_old_giustifiche_negato_borders, constraintLayout.resources.newTheme())
    }

    fun setAnnullatoLightBorder(){
        constraintLayout.background = ResourcesCompat.getDrawable(constraintLayout.resources, R.drawable.dettagli_old_giustifiche_neutro_borders, constraintLayout.resources.newTheme())
    }

    fun setApprovatoLight(){
        constraintLayout.background = ColorDrawable(constraintLayout.resources.getColor(R.color.green_approvato_light, constraintLayout.resources.newTheme()))
    }

    fun setNegatoLight(){
        constraintLayout.background = ColorDrawable(constraintLayout.resources.getColor(R.color.red_negato_light, constraintLayout.resources.newTheme()))
    }

    fun setAnnullatoLight(){
        constraintLayout.background = ColorDrawable(constraintLayout.resources.getColor(R.color.yellow_offline_light, constraintLayout.resources.newTheme()))
    }

}