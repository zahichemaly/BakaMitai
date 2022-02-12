package com.zc.bakamitai.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.zc.bakamitai.data.models.DownloadsItem
import com.zc.bakamitai.databinding.ItemDownloadBinding

class DownloadLinkAdapter(private val items: List<DownloadsItem>) : RecyclerView.Adapter<DownloadLinkAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(downloadsItem: DownloadsItem) {
            binding.tvTitle.text = downloadsItem.res
            setLink(binding.cvMagnet, downloadsItem.torrent)
            setLink(binding.cvTorrent, downloadsItem.magnet)
            setLink(binding.cvXdcc, downloadsItem.xdcc)
        }

        private fun setLink(cardView: CardView, link: String) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindData(item)
    }

    override fun getItemCount(): Int = items.size
}
