package com.example.ivlinereporting

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView

object DialogUtils {
    fun showEncouragementDialog(context: Context, title: String, message: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_encouragement, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.message)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)

        titleTextView.text = title
        messageTextView.text = message

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)

        dialogView.startAnimation(fadeIn)

        okButton.setOnClickListener {
            dialogView.startAnimation(fadeOut)
            dialog.dismiss()
        }
        dialog.show()

    }
}