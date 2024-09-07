package com.example.ivlinereporting

import HelpAdapter
import HelpItem
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HelpActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var menuHandler: MenuHandler
    private lateinit var expandableListView: ExpandableListView
    private lateinit var helpAdapter: HelpAdapter
    private lateinit var listDataHeader: List<String>
    private lateinit var listDataChild: HashMap<String, List<HelpItem>>
    private var currentExpandedPosition: Pair<Int, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navView)
        menuHandler =
            MenuHandler(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        navigationView.setNavigationItemSelectedListener { item ->
            menuHandler.onNavigationItemSelected(item)
        }
        val user = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("login", null)
        val loginTextView = navigationView.getHeaderView(0).findViewById<TextView>(R.id.loginTextView)
        loginTextView.text = user ?: "Неизвестный пользователь"

        expandableListView = findViewById(R.id.expandableListView)
        prepareListData()
        helpAdapter = HelpAdapter(this, listDataHeader, listDataChild)
        expandableListView.setAdapter(helpAdapter)

        expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val helpItem = listDataChild[listDataHeader[groupPosition]]?.get(childPosition)
            val layoutResId = helpItem?.layoutResId
            Log.d("MyLogTag", "Child clicked at group: $groupPosition, child: $childPosition")
            if (layoutResId != null) {
                Log.d("MyLogTag", "Inflating layout: $layoutResId")
                val flatPosition = expandableListView.getFlatListPosition(
                    ExpandableListView.getPackedPositionForChild(
                        groupPosition,
                        childPosition
                    )
                )
                Log.d("MyLogTag", "Flat position: $flatPosition")
                val childView =
                    expandableListView.getChildAt(flatPosition - expandableListView.firstVisiblePosition)
                val detailContainer = childView?.findViewById<FrameLayout>(R.id.detailContainer)

                if (currentExpandedPosition == Pair(groupPosition, childPosition)) {
                    detailContainer?.visibility =
                        if (detailContainer?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    currentExpandedPosition = null
                } else {
                    for (i in 0 until expandableListView.count) {
                        val groupView = expandableListView.getChildAt(i)
                        val groupDetailContainer =
                            groupView?.findViewById<FrameLayout>(R.id.detailContainer)
                        groupDetailContainer?.visibility = View.GONE
                    }

                    detailContainer?.removeAllViews()
                    val detailView = LayoutInflater.from(this).inflate(layoutResId, null)
                    detailContainer?.addView(detailView)
                    detailContainer?.visibility = View.VISIBLE
                    currentExpandedPosition = Pair(groupPosition, childPosition)
                }
            }
            true
        }
    }

    private fun prepareListData() {
        listDataHeader =
            listOf(
                "О приложении",
                "Обратная связь",
                "Главная страница",
                "Ввод данных",
                "Отправка данных"
            )
        listDataChild = HashMap()

        val mainPage = listOf(
            HelpItem("Обзор главной страницы.", R.layout.main_review_help),
            HelpItem("Как выбрать вид отчета.", R.layout.type_choosing_help)
        )

        val dataInput = listOf(
            HelpItem("Как ввести данные о выполненной работе.", R.layout.input_work_help),
            HelpItem("Как ввести данные об отработанных часах.", R.layout.input_working_hours_help),
            HelpItem("Как ввести данные об использованной технике.", R.layout.input_technique_help)
        )

        listDataChild[listDataHeader[0]] = listOf(HelpItem("Имя приложения: ${getString(R.string.app_name)}\nВерсия: ${getAppVersion()}\nРазработчик: Кудринский Артем", 0))
        listDataChild[listDataHeader[1]] = listOf(HelpItem(getHelpText(), 0))
        listDataChild[listDataHeader[2]] = mainPage
        listDataChild[listDataHeader[3]] = dataInput
        listDataChild[listDataHeader[4]] = listOf()
    }

    private fun getAppVersion(): String {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionName
    }

    private fun getHelpText(): SpannableString {
        val helpText =
            "Если у вас возникли проблемы при использовании приложения, пожалуйста, обратитесь по адресу "
        val emailAddress = "dev.assist@.yandex.ru"
        val fullText = "$helpText$emailAddress"

        val spannableString = SpannableString(fullText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                    putExtra(Intent.EXTRA_SUBJECT, "Помощь по приложению")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Здравствуйте, \n\nУ меня возникли проблемы с использованием приложения..."
                    )
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@HelpActivity,
                        "Нет приложения для отправки почты",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color =
                    ContextCompat.getColor(this@HelpActivity, android.R.color.holo_blue_light)
            }
        }

        spannableString.setSpan(
            clickableSpan,
            helpText.length,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }
}