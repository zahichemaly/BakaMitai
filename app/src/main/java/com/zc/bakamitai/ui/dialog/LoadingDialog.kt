package com.zc.bakamitai.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.zc.bakamitai.R

class LoadingDialog(context: Context) {
    private var alertDialog: AlertDialog

    init {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        builder.setView(view)
        builder.setCancelable(false)
        alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun show() = alertDialog.show()
    fun hide() = alertDialog.dismiss()
}
