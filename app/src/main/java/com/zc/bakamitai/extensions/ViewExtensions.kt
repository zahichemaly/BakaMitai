package com.zc.bakamitai.extensions

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.zc.bakamitai.R

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun TextView.setAiredText(aired: Boolean) {
    val color: Int
    val text: String
    if (aired) {
        text = context.getString(R.string.aired)
        color = ContextCompat.getColor(context, R.color.color_aired)
    } else {
        text = context.getString(R.string.not_aired)
        color = ContextCompat.getColor(context, R.color.color_not_aired)
    }
    this.text = text
    this.setTextColor(color)
}
