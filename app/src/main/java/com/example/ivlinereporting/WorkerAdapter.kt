package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class WorkerAdapter(private val workers: List<String>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {
    private var filteredWorkers = workers

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredWorkers = workers.filter { it.contains(query, ignoreCase = true) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        holder.bind(filteredWorkers[position])
    }

    override fun getItemCount(): Int = filteredWorkers.size

    inner class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(worker: String) {
            textView.text = worker
            itemView.setOnClickListener { onItemClick(worker) }
        }
    }
}