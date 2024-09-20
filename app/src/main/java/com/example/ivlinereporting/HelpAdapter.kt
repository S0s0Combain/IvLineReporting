package com.example.ivlinereporting

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class HelpAdapter(private val context: Context, private val sections: List<Section>) :
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = sections.size

    override fun getChildrenCount(groupPosition: Int): Int {
        val section = sections[groupPosition]
        return section.items.size
    }

    override fun getGroup(groupPosition: Int): Any = sections[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val section = sections[groupPosition]
        return section.items[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = sections[groupPosition].title
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val item = sections[groupPosition].items[childPosition]
        return when (item) {
            is Item.LayoutItem -> {
                val view = LayoutInflater.from(context).inflate(item.layoutId, parent, false)
                if (item.layoutId == R.layout.about_app_layout) {
                    val aboutAppTextView = view.findViewById<TextView>(R.id.aboutAppTextView)
                    aboutAppTextView.text = "Версия приложения: ${getAppVersion()}"
                }
                if(item.layoutId == R.layout.feedback_layout){
                    val feedbackTextView = view.findViewById<TextView>(R.id.feedbackTextView)
                    feedbackTextView.text = getHelpText()
                    feedbackTextView.movementMethod = LinkMovementMethod.getInstance()
                }
                view
            }
            is Item.SubSection -> {
                val view = LayoutInflater.from(context).inflate(R.layout.sub_section_layout, parent, false)
                val textView = view.findViewById<TextView>(R.id.sub_section_title)
                textView.text = item.title
                view
            }
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    private fun getAppVersion(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }

    private fun getHelpText(): SpannableString {
        val helpText =
            "Если у вас возникли проблемы при использовании приложения, пожалуйста, обратитесь по адресу "
        val emailAddress = "dev.assist@yandex.ru"
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
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "Нет приложения для отправки почты",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color =
                    ContextCompat.getColor(context, android.R.color.holo_blue_light)
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
