package com.zc.bakamitai.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.EntryDto
import de.hdodenhof.circleimageview.CircleImageView

class EntryAdapter : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    private val items: MutableList<EntryDto> = mutableListOf()

    class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvName)
        private val tvEpisode = itemView.findViewById<TextView>(R.id.tvEpisode)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        private val ivEntry = itemView.findViewById<CircleImageView>(R.id.ivEntry)

        fun bindData(entryDto: EntryDto) {
            tvName.text = entryDto.name
            tvEpisode.text = entryDto.episode
            tvDate.text = entryDto.getFormattedDate()
            Glide.with(itemView)
                .load(entryDto.imageUrl)
                .into(ivEntry)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val data = items[position]
        holder.bindData(data)
    }

    fun addItems(items: List<EntryDto>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
