package com.zc.bakamitai.ui.library

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.ShowDto
import com.zc.bakamitai.listeners.PageListener
import java.util.*

class LibraryAdapter(private val pageListener: PageListener) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    private val items = mutableListOf<ShowDto>()

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindData(showDto: ShowDto)
    }

    inner class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvTitle)

        override fun bindData(showDto: ShowDto) {
            tvName.text = showDto.title
            itemView.rootView.setOnClickListener {
                pageListener.onPageClicked(showDto.link)
            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        private val tvIndex = itemView.findViewById<TextView>(R.id.tvIndex)

        override fun bindData(showDto: ShowDto) {
            tvIndex.text = showDto.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (viewType == HEADER) R.layout.item_header
        else R.layout.item_library
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return if (viewType == HEADER) HeaderViewHolder(view)
        else ItemViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        val item = this.items[position]
        return if (item.isHeader) HEADER
        else ITEM
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindData(item)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<ShowDto>) {
        val sortedItems = getSortedItems(items)
        this.items.clear()
        this.items.addAll(sortedItems)
        notifyDataSetChanged()
    }

    private fun getSortedItems(items: List<ShowDto>): List<ShowDto> {
        val map: SortedMap<String, MutableList<ShowDto>> = sortedMapOf()
        items.forEach { showDto ->
            val firstLetter = showDto.link[0].uppercaseChar().toString()
            if (map.containsKey(firstLetter)) {
                //get existing list by alphabet and append item
                map[firstLetter]?.add(showDto)
            } else {
                val list = mutableListOf<ShowDto>()
                list.add(showDto)
                map[firstLetter] = list
            }
        }
        val newItems = mutableListOf<ShowDto>()
        map.forEach { kv ->
            //sort
            kv.value?.sortBy { it.title }
            newItems.add(ShowDto(title = kv.key, isHeader = true))
            newItems.addAll(kv.value)
        }
        return newItems
    }

    companion object {
        const val ITEM = 1
        const val HEADER = 2
    }
}
