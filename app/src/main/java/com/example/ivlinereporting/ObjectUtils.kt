package com.example.ivlinereporting

import android.content.Context
import android.content.SharedPreferences
import android.widget.EditText

class ObjectUtils(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveObjectIfNotExists(objectEditText: EditText) {
        val objectName = objectEditText.text.toString()
        if (objectName.isNotEmpty() && !isObjectExists(objectName)) {
            val editor = sharedPreferences.edit()
            editor.putString("saved_object", objectName)
            editor.apply()
        }
    }

    private fun isObjectExists(objectName: String): Boolean {
        val savedObject = sharedPreferences.getString("saved_object", null)
        return savedObject == objectName
    }

    fun getSavedObject(): String? {
        return sharedPreferences.getString("saved_object", null)
    }
}