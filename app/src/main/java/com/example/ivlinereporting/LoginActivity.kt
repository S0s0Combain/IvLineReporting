package com.example.ivlinereporting

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import java.sql.Types

class LoginActivity : AppCompatActivity() {
    lateinit var loginEditText: TextInputEditText
    lateinit var passwordEditText: TextInputEditText

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        val theme = sharedPreferences.getString("theme", "light")
        applyTheme(theme as String)
        setTheme(R.style.Theme_IvLineReporting)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        val savedLogin = loadLogin()
        if (savedLogin != null) {
            loginEditText.setText(savedLogin)
        }
    }

    fun applyTheme(theme: String?) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            val v: View? = currentFocus
            if (v is EditText) {
                val rect = Rect()
                v.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun enterButton_onClick(v: View) {
        if (loginEditText.text.toString().isEmpty()) {
            Toast.makeText(applicationContext, "Введите логин", Toast.LENGTH_SHORT).show()
            return
        }
        if (passwordEditText.text.toString().isEmpty()) {
            Toast.makeText(applicationContext, "Введите пароль", Toast.LENGTH_SHORT).show()
            return
        }
        val currentLogin = loginEditText.text.toString()
        val currentPassword = passwordEditText.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            val result = authentificateUser(currentLogin, currentPassword)
            withContext(Dispatchers.Main) {
                if (result) {
                    val savedLogin = loadLogin()
                    if (savedLogin == null || savedLogin != currentLogin) {
                        showSaveLoginDialog(currentLogin)
                    } else {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Неверный логин или пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun showSaveLoginDialog(login: String) {
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialogYellow)
        dialog.setTitle("Сохранить логин")
        dialog.setMessage("Хотите сохранить этот логин?")
        dialog.setPositiveButton("Да") { dialog, which ->
            saveLogin(login)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        dialog.setNegativeButton("Нет") { dialog, which ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        dialog.show()
    }

    fun saveLogin(login: String) {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("saved_login", login)
        editor.apply()
    }

    fun loadLogin(): String? {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("saved_login", null)
    }

    private fun authentificateUser(login: String, password: String): Boolean {
        val dbConnection = DatabaseConnection()
        var isAuthentificated = false
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        val connection: java.sql.Connection = dbConnection.CreateConnection()
        val callableStatement = connection.prepareCall("{call dbo.AuthentificateUser(?, ?, ?)}")
        callableStatement.setString(1, login)
        callableStatement.setString(2, password)
        callableStatement.registerOutParameter(3, Types.BIT)
        callableStatement.execute()
        isAuthentificated = callableStatement.getBoolean(3)
        dbConnection.CloseConnection(connection)
        return isAuthentificated
    }
}
