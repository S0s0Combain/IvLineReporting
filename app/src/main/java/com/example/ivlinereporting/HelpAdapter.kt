package com.example.ivlinereporting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

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
                LayoutInflater.from(context).inflate(item.layoutId, parent, false)
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
}
