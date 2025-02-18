package com.example.ivlinereporting

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import kotlin.system.exitProcess

class MenuHandler(
    private val context: Activity,
    private val drawerLayout: DrawerLayout,
    private val toolbar: Toolbar,
    openStringRes: Int,
    closeStringRes: Int
) {
    private val dialog: AlertDialog.Builder
    private val toggle: ActionBarDrawerToggle

    init {
        toggle =
            ActionBarDrawerToggle(context, drawerLayout, toolbar, openStringRes, closeStringRes)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(context, R.color.black));
        dialog = AlertDialog.Builder(context)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return false
    }

    fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_help -> {
                val intent = Intent(context, HelpActivity::class.java)
                context.startActivity(intent)
            }
            R.id.nav_settings -> {
                context.finish()
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
            }
            R.id.nav_logout -> {
                context.finish()
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            }
            R.id.nav_exit -> {
                context.finishAndRemoveTask()
                exitProcess(0)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun setupThemeSwitch(menu: Menu) {
        val themeSwitchItem = menu.findItem(R.id.nav_theme_switch)
        val themeSwitch = themeSwitchItem.actionView as Switch
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val currentTheme = sharedPreferences.getString("theme", "light")
        themeSwitch.isChecked = currentTheme == "dark"

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            if (isChecked) {
                editor.putString("theme", "dark").apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                editor.putString("theme", "light").apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            context.recreate()
        }
    }
}
