package com.osti.juniorapp.menu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView

@SuppressLint("ViewConstructor")
class MyMenuTextView(context: Context, text:String) : AppCompatTextView(context) {


    init {
        val tmpId = View.generateViewId()
        id = tmpId
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setAttributes()
        this.text = text
    }
    fun setAttributes(){
        setPadding(36, 0, 0, 0)
        textSize = 20F
        setTextColor(Color.BLACK)
        textAlignment = TEXT_ALIGNMENT_VIEW_START
    }
}