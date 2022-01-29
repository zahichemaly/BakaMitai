package com.zc.bakamitai.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.ScheduleDto
import com.zc.bakamitai.extensions.toDayOfWeekNumber
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.utils.FirstDayOfWeek
import com.zc.bakamitai.utils.PreferenceUtil

class SchedulePageAdapter(private val pageListener: PageListener, private val preferenceUtil: PreferenceUtil) :
    RecyclerView.Adapter<SchedulePageAdapter.ViewHolder>() {

    private val items: MutableList<ScheduleDto> = mutableListOf()
    private var startsMonday = preferenceUtil.getFirstDayOfWeek() == FirstDayOfWeek.Monday

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rvEntries = itemView.findViewById<RecyclerView>(R.id.rvEntries)

        fun bindData(data: ScheduleDto) {
            val layoutManager = GridLayoutManager(itemView.context, 3)
            val entries = data.entries.sortedBy { it.time }
            val adapter = ScheduleEntryAdapter(entries, pageListener, preferenceUtil)
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

    fun updateSettings() {
        startsMonday = preferenceUtil.getFirstDayOfWeek() == FirstDayOfWeek.Monday
        addItems(this.items)
    }

    fun addItems(items: List<ScheduleDto>) {
        val sorted = items.sortedBy { it.day.toDayOfWeekNumber(startsMonday) }
        this.items.clear()
        this.items.addAll(sorted)
        notifyDataSetChanged()
    }
}
