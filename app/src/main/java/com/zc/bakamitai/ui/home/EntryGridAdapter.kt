package com.zc.bakamitai.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.extensions.setAiredText
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.utils.PreferenceUtil
import com.zc.bakamitai.utils.TimeFormat

class EntryGridAdapter(private val pageListener: PageListener, private val preferenceUtil: PreferenceUtil) :
    RecyclerView.Adapter<EntryGridAdapter.EntryViewHolder>() {

    private val items: MutableList<EntryDto> = mutableListOf()
    private var is12HourFormat = preferenceUtil.getTimeFormat() == TimeFormat.TF_12

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvName)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val tvAired = itemView.findViewById<TextView>(R.id.tvAired)
        private val ivEntry = itemView.findViewById<ImageView>(R.id.ivEntry)

        fun bindData(entryDto: EntryDto) {
            tvName.text = entryDto.name
            tvTime.text = entryDto.getFormattedTime(is12HourFormat)
            tvAired.setAiredText(entryDto.aired)
            Glide.with(itemView)
                .load(entryDto.imageUrl)
                .into(ivEntry)
            itemView.rootView.setOnClickListener {
                pageListener.onPageClicked(entryDto.page)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entry_grid, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val data = items[position]
        holder.bindData(data)
    }

    fun updateSettings() {
        is12HourFormat = preferenceUtil.getTimeFormat() == TimeFormat.TF_12
        notifyDataSetChanged()
    }

    fun addItems(items: List<EntryDto>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
