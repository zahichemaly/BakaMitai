package com.zc.bakamitai.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.prefs.PreferenceUtil
import com.zc.bakamitai.prefs.TimeFormat

class ScheduleEntryAdapter(
    private val items: List<EntryDto>,
    private val pageListener: PageListener,
    preferenceUtil: PreferenceUtil
) :
    RecyclerView.Adapter<ScheduleEntryAdapter.EntryViewHolder>() {

    private val is12HourFormat = preferenceUtil.getTimeFormat() == TimeFormat.TF_12

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvName)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val ivEntry = itemView.findViewById<ImageView>(R.id.ivEntry)

        fun bindData(entryDto: EntryDto) {
            tvName.text = entryDto.name
            tvTime.text = entryDto.getFormattedTime(is12HourFormat)
            Glide.with(itemView)
                .load(entryDto.imageUrl)
                .into(ivEntry)
            itemView.rootView.setOnClickListener {
                pageListener.onPageClicked(entryDto.page)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val data = items[position]
        holder.bindData(data)
    }

    override fun getItemCount(): Int = items.size
}
