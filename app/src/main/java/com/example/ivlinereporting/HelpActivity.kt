package com.example.ivlinereporting

import HelpAdapter
import HelpItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ExpandableListView
import android.widget.FrameLayout
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
        menuHandler = MenuHandler(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        navigationView.setNavigationItemSelectedListener { item ->
            menuHandler.onNavigationItemSelected(item)
        }
        expandableListView = findViewById(R.id.expandableListView)
        prepareListData()
        helpAdapter = HelpAdapter(this, listDataHeader, listDataChild)
        expandableListView.setAdapter(helpAdapter)

        expandableListView.setOnGroupExpandListener { groupPosition ->
            for (i in 0 until expandableListView.count) {
                val groupView = expandableListView.getChildAt(i)
                val groupDetailContainer = groupView?.findViewById<FrameLayout>(R.id.detailContainer)
                groupDetailContainer?.visibility = View.GONE
            }
        }

        expandableListView.setOnGroupCollapseListener { groupPosition ->
            for (i in 0 until expandableListView.count) {
                val groupView = expandableListView.getChildAt(i)
                val groupDetailContainer = groupView?.findViewById<FrameLayout>(R.id.detailContainer)
                groupDetailContainer?.visibility = View.GONE
            }
        }

        expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val helpItem = listDataChild[listDataHeader[groupPosition]]?.get(childPosition)
            val layoutResId = helpItem?.layoutResId
            if (layoutResId != null) {
                val flatPosition = expandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition))
                val childView = expandableListView.getChildAt(flatPosition - expandableListView.firstVisiblePosition)
                val detailContainer = childView?.findViewById<FrameLayout>(R.id.detailContainer)

                if (currentExpandedPosition == Pair(groupPosition, childPosition)) {
                    detailContainer?.visibility = if (detailContainer?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    currentExpandedPosition = null
                } else {
                    for (i in 0 until expandableListView.count) {
                        val groupView = expandableListView.getChildAt(i)
                        val groupDetailContainer = groupView?.findViewById<FrameLayout>(R.id.detailContainer)
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
            listOf("Главная страница", "Ввод данных", "Отправка данных", "Настройки")
        listDataChild = HashMap()

        val mainPage = listOf(
            HelpItem("Обзор главной страницы.", R.layout.main_review_help),
            HelpItem("Как выбрать вид отчета.", R.layout.type_choosing_help)
        )

        val dataInput = listOf(HelpItem("Как ввести данные о выполненной работе.", R.layout.input_work_help),
            HelpItem("Как ввести данные об отработанных часах.", R.layout.input_working_hours_help),
            HelpItem("Как ввести данные об использованной технике.", R.layout.input_technique_help)
            )

        val settings = listOf(HelpItem("Как настроить тему приложения.", R.layout.theme_settings_help))

        listDataChild[listDataHeader[0]] = mainPage
        listDataChild[listDataHeader[1]] = dataInput

        listDataChild[listDataHeader[3]] = settings
    }
}