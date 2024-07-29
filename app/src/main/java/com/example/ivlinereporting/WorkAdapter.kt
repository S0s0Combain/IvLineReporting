package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkAdapter(
    private val works: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<WorkAdapter.WorkViewHolder>() {
    private var filteredWork = works

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredWork = works.filter { it.contains(query, ignoreCase = true) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return WorkViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkViewHolder, position: Int) {
        holder.bind(filteredWork[position])
    }

    override fun getItemCount(): Int = filteredWork.size

    inner class WorkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(works: String) {
            textView.text = works
            itemView.setOnClickListener { onItemClick(works) }
        }
    }
}