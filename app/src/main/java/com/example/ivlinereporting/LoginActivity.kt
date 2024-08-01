package com.example.ivlinereporting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Scroller
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    lateinit var loginEditText: TextInputEditText
    lateinit var passwordEditText: TextInputEditText

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val theme = sharedPreferences.getString("theme", "light")
        applyTheme(theme as String)
        setTheme(R.style.Theme_IvLineReporting)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
    }

    fun applyTheme(theme: String?){
        when(theme){
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            val v: View? = currentFocus
            if (v is EditText) {
                val scroller = Scroller(this)
                val scrollBounds = Rect()
                v.getHitRect(scrollBounds)
                scrollBounds.offset(-scroller.currX, -scroller.currY)
                if (!scrollBounds.contains(ev.x.toInt(), ev.y.toInt())) {
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}