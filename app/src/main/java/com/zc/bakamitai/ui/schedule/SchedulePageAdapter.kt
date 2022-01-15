package com.zc.bakamitai.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.ScheduleDto

class SchedulePageAdapter : RecyclerView.Adapter<SchedulePageAdapter.ViewHolder>() {

    private val items: MutableList<ScheduleDto> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rvEntries = itemView.findViewById<RecyclerView>(R.id.rvEntries)

        fun bindData(data: ScheduleDto) {
            val layoutManager = GridLayoutManager(itemView.context, 3)
            val adapter = ScheduleEntryAdapter(data.entries)
            rvEntries.layoutManager = layoutManager
            rvEntries.adapter = adapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        holder.bindData(data)
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): ScheduleDto? {
        return items.getOrNull(position)
    }

    fun addItems(items: List<ScheduleDto>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }
}
