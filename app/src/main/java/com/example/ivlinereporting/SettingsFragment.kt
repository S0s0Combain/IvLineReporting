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

        val appNamePreference: Preference? = findPreference("app_name")
        appNamePreference?.summary = getString(R.string.app_name)

        val appVersionPreference: Preference? = findPreference("app_version")
        appVersionPreference?.summary = getAppVersion()

        val appDeveloperPreference: Preference? = findPreference("app_developer")
        appDeveloperPreference?.summary = "Кудринский Артем"

        val helpPreference: Preference? = findPreference("help")
        helpPreference?.setOnPreferenceClickListener {
            showHelpDialog()
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

    private fun getAppVersion(): String {
        val packageInfo =
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        return packageInfo.versionName
    }

    private fun showHelpDialog() {
        val helpText = "Если у вас возникли проблемы при использовании приложения, пожалуйста, обратитесь по адресу "
        val emailAddress = "dev.assist@.yandex.ru"
        val fullText = "$helpText$emailAddress"

        val spannableString = SpannableString(fullText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                    putExtra(Intent.EXTRA_SUBJECT, "Помощь по приложению")
                    putExtra(Intent.EXTRA_TEXT, "Здравствуйте, \n\nУ меня возникли проблемы с использованием приложения...")
                }
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Нет приложения для отправки почты", Toast.LENGTH_SHORT).show()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light)
            }
        }

        spannableString.setSpan(clickableSpan, helpText.length, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Помощь")
        dialog.setMessage(spannableString)
        dialog.setPositiveButton("Ок") { dialog, _ -> dialog.dismiss() }

        val alertDialog = dialog.create()
        alertDialog.show()

        // Сделать текст кликабельным
        val messageView = alertDialog.findViewById<TextView>(android.R.id.message)
        messageView?.movementMethod = LinkMovementMethod.getInstance()
    }

}
