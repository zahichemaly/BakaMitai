package com.zc.bakamitai.ui.details

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.bakamitai.data.models.dtos.ShowDownloadDto
import com.zc.bakamitai.databinding.ItemDownloadDetailsBinding
import com.zc.bakamitai.databinding.ItemDownloadSummaryBinding

class DownloadsAdapter : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {

    private val items: MutableList<ShowDownloadDto> = mutableListOf()

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindData(showDownloadDto: ShowDownloadDto)
    }

    class ParentViewHolder(val binding: ItemDownloadSummaryBinding) : ViewHolder(binding.root) {

        override fun bindData(showDownloadDto: ShowDownloadDto) {
            binding.tvTitle.text = showDownloadDto.getFormattedEpisode()
        }
    }

    class ChildViewHolder(val binding: ItemDownloadDetailsBinding) : ViewHolder(binding.root) {

        override fun bindData(showDownloadDto: ShowDownloadDto) {
            val adapter = DownloadLinkAdapter(showDownloadDto.downloads)
            binding.rvDownloads.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvDownloads.adapter = adapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            PARENT -> {
                val binding = ItemDownloadSummaryBinding.inflate(layoutInflater, parent, false)
                ParentViewHolder(binding)
            }
            else -> {
                val binding = ItemDownloadDetailsBinding.inflate(layoutInflater, parent, false)
                ChildViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = PARENT

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindData(item)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<ShowDownloadDto>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    companion object {
        const val PARENT = 1
        const val CHILD = 2
    }
}
