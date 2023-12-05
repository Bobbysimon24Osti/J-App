package com.osti.juniorapp.menu

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.osti.juniorapp.R

class MyConstraintLayout (context:Context,val textView:MyMenuTextView) : FrameLayout(context) {

    lateinit var constrint: ConstraintLayout
    lateinit var text: TextView
    lateinit var icone: ImageView

    var active = false


    init{
        inflate(context, R.layout.myconstraint_menu, this)
        constrint = findViewById(R.id.constraint_menu)
        text = findViewById(R.id.textView_menu_voce)
        icone = findViewById(R.id.imageView_menu_Icon)


        text.text = textView.text
        text.setTextColor(textView.currentTextColor)

        if(text.currentTextColor == Color.LTGRAY){
            selectImageLight(textView)
            active = false
        }
        else{
            selectImage(textView)
            active = true
        }
    }

    private fun selectImage(textView:TextView){
        when(textView.text){
            resources.getString(R.string.menu_timbra)->{
                icone.setImageResource(R.drawable.baseline_location_on_24)
            }
            resources.getString(R.string.menu_gestisci_giustificazioni)->{
                icone.setImageResource(R.drawable.decision_making__1_)
            }
            resources.getString(R.string.menu_richiesta_giust)->{
                icone.setImageResource(R.drawable.baseline_add_circle_outline_24)
            }
            resources.getString(R.string.menu_lista_giust)->{
                icone.setImageResource(R.drawable.baseline_checklist_24)
            }
            resources.getString(R.string.menu_old_stamp)->{
                icone.setImageResource(R.drawable.baseline_lock_clock_24)
            }
            resources.getString(R.string.menu_cartellino)->{
                icone.setImageResource(R.drawable.baseline_calendar_month_24)
            }
            resources.getString(R.string.menu_file)->{
                icone.setImageResource(R.drawable.document)
            }
            resources.getString(R.string.menu_notifiche)->{
                icone.setImageResource(R.drawable.baseline_notifications_none_24)
            }
            resources.getString(R.string.menu_account)->{
                icone.setImageResource(R.drawable.baseline_manage_accounts_24)
            }
            resources.getString(R.string.menu_info)->{
                icone.setImageResource(R.drawable.baseline_info_24)
            }
            resources.getString(R.string.menu_aggiorna) ->{
                icone.setImageResource(R.drawable.baseline_system_update_24)
            }
            resources.getString(R.string.menu_exit)->{
                icone.setImageResource(R.drawable.baseline_exit_to_app_24)
            }
        }
    }

    private fun selectImageLight(textView:TextView){
        when(textView.text){
            resources.getString(R.string.menu_timbra)->{
                icone.setImageResource(R.drawable.baseline_location_on_light_24)
            }
            resources.getString(R.string.menu_richiesta_giust)->{
                icone.setImageResource(R.drawable.baseline_add_circle_outline_light_24)
            }
            resources.getString(R.string.menu_cartellino)->{
                icone.setImageResource(R.drawable.baseline_calendar_month_24_light)
            }
            resources.getString(R.string.menu_lista_giust)->{
                icone.setImageResource(R.drawable.baseline_checklist_light_24)
            }
            resources.getString(R.string.menu_old_stamp)->{
                icone.setImageResource(R.drawable.baseline_lock_clock_light_24)
            }
            resources.getString(R.string.menu_account)->{
                icone.setImageResource(R.drawable.baseline_manage_accounts_light_24)
            }
            resources.getString(R.string.menu_info)->{
                icone.setImageResource(R.drawable.baseline_info_light_24)
            }
        }
    }

}