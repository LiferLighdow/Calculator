package com.liferlighdow.calculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class NotebookAdapter(private val notebooks: List<File>, private val onItemClick: (File) -> Unit) :
    RecyclerView.Adapter<NotebookAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvNotebookName)
        val tvInfo: TextView = view.findViewById(R.id.tvNotebookInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notebook, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = notebooks[position]
        holder.tvName.text = file.nameWithoutExtension
        val lastModified = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(file.lastModified())
        holder.tvInfo.text = "Last modified: $lastModified"
        holder.itemView.setOnClickListener { onItemClick(file) }
    }

    override fun getItemCount() = notebooks.size
}
