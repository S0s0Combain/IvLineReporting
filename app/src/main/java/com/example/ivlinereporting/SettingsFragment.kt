package com.example.ivlinereporting

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val themePreference: Preference? = findPreference("theme")
        themePreference?.setOnPreferenceChangeListener{preference, newValue ->
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
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

    private fun applyTheme(theme: String){
        when(theme){
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
    private fun getAppVersion(): String {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        return packageInfo.versionName
    }

    private fun showHelpDialog() {
        val aboutDeveloperText = "Разработчик"
    }

    private fun showAboutDeveloperDialog() {

    }

}