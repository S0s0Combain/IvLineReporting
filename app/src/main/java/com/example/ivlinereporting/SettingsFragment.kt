package com.example.ivlinereporting

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate

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
        val helpText =
            "Если у вас возникли проблемы при использовании приложения, пожалуйста, обратитесь по адресу "

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Помощь")
        dialog.setMessage(helpText)
        dialog.setPositiveButton("Ок") { dialog, _ -> dialog.dismiss() }
        dialog.show()
    }
}
