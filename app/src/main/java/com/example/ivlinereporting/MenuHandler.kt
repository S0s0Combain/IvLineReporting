package com.example.ivlinereporting

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
            R.id.nav_help -> {}
            R.id.nav_settings -> {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
            }

            R.id.nav_logout -> {}
            R.id.nav_exit -> {
                context.finishAffinity()
                exitProcess(0)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}