package com.example.ivlinereporting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class SubSectionAdapter(private val context: Context, private val subItems: List<Item.LayoutItem>) :
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = subItems.size

    override fun getChildrenCount(groupPosition: Int): Int = 1

    override fun getGroup(groupPosition: Int): Any = subItems[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = subItems[groupPosition]

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
        textView.text = "SubItem ${groupPosition + 1}"
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val layoutId = subItems[groupPosition].layoutId
        return LayoutInflater.from(context).inflate(layoutId, parent, false)
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
