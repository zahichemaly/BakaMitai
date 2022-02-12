package com.zc.bakamitai.ui.details

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.ShowDownloadDto
import com.zc.bakamitai.databinding.ItemDownloadDetailsBinding
import com.zc.bakamitai.databinding.ItemDownloadSummaryBinding

class DownloadsAdapter(private val listener: DownloadLinkAdapter.Listener) : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {

    private val items: MutableList<ShowDownloadDto> = mutableListOf()

    abstract inner class ViewHolder(open val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        open fun bindData(position: Int, showDownloadDto: ShowDownloadDto) {
            binding.root.setOnClickListener {
                expandOrCollapse(position, showDownloadDto)
            }
        }
    }

    inner class ParentViewHolder(override val binding: ItemDownloadSummaryBinding) : ViewHolder(binding) {

        override fun bindData(position: Int, showDownloadDto: ShowDownloadDto) {
            super.bindData(position, showDownloadDto)
            val episodeText = binding.root.context.getString(R.string.episode_, showDownloadDto.getFormattedEpisode())
            binding.tvTitle.text = episodeText
        }
    }

    inner class ChildViewHolder(override val binding: ItemDownloadDetailsBinding) : ViewHolder(binding) {

        override fun bindData(position: Int, showDownloadDto: ShowDownloadDto) {
            super.bindData(position, showDownloadDto)
            val adapter = DownloadLinkAdapter(showDownloadDto.downloads, listener)
            val episodeText = binding.root.context.getString(R.string.episode_, showDownloadDto.getFormattedEpisode())
            binding.tvTitle.text = episodeText
            binding.rvDownloads.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
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

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return if (item.isExpanded) CHILD
        else PARENT
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindData(position, item)
    }

    override fun getItemCount(): Int = items.size

    private fun expandOrCollapse(position: Int, showDownloadDto: ShowDownloadDto) {
        showDownloadDto.isExpanded = !showDownloadDto.isExpanded
        notifyItemChanged(position)
    }

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
