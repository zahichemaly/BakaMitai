package com.zc.bakamitai.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zc.bakamitai.data.enums.LinkType
import com.zc.bakamitai.data.models.dtos.DownloadDto
import com.zc.bakamitai.databinding.ItemDownloadBinding

class DownloadLinkAdapter(
    private val items: List<DownloadDto>,
    private val listener: Listener
) : RecyclerView.Adapter<DownloadLinkAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(downloadDto: DownloadDto) {
            binding.tvTitle.text = downloadDto.getFormattedResolution()
            binding.cvMagnet.setOnClickListener {
                listener.onLinkClicked(downloadDto.magnet, LinkType.Magnet)
            }
            binding.cvTorrent.setOnClickListener {
                listener.onLinkClicked(downloadDto.torrent, LinkType.Torrent)
            }
            binding.cvXdcc.setOnClickListener {
                listener.onLinkClicked(downloadDto.getXdccLink(), LinkType.Xdcc)
            }
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

    interface Listener {
        fun onLinkClicked(link: String, linkType: LinkType)
    }
}
