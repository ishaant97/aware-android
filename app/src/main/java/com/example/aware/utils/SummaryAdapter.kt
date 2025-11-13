package com.example.aware.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aware.R

class SummaryAdapter(private val items: List<SummaryItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_CATEGORY = 0
    private val TYPE_ENTRY = 1

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SummaryItem.Category -> TYPE_CATEGORY
            is SummaryItem.Entry -> TYPE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CATEGORY) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_summary_category, parent, false)
            CategoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_summary_entry, parent, false)
            EntryViewHolder(view)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SummaryItem.Category -> (holder as CategoryViewHolder).bind(item)
            is SummaryItem.Entry -> (holder as EntryViewHolder).bind(item)
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SummaryItem.Category) {
            itemView.findViewById<TextView>(R.id.categoryTitle).text = item.title
        }
    }

    class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SummaryItem.Entry) {
            itemView.findViewById<TextView>(R.id.entryText).text = item.text
        }
    }
}
