package com.example.ivlinereporting

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val themePreference: Preference? = findPreference("theme")
        themePreference?.setOnPreferenceChangeListener { preference, newValue ->
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext())
            sharedPreferences.edit().putString("theme", newValue as String).apply()
            applyTheme(newValue as String)
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitleColors()
    }

    private fun updateTitleColors() {
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val titleColor = if (isDarkMode) {
            getResources().getColor(R.color.yellow, null)
        } else {
            getResources().getColor(R.color.yellow, null)
        }

        val preferenceScreen = preferenceScreen
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            if (preference is PreferenceCategory) {
                preference.title = preference.title.toString()
                preference.icon?.setTint(titleColor)
                for (j in 0 until preference.preferenceCount) {
                    val subPreference = preference.getPreference(j)
                    subPreference.title = subPreference.title.toString()
                    subPreference.icon?.setTint(titleColor)
                }
            } else {
                preference.title = preference.title.toString()
                preference.icon?.setTint(titleColor)
            }
        }
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        requireActivity().recreate()
    }
}
