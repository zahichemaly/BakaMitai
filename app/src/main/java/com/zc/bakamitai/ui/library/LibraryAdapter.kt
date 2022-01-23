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

class LibraryAdapter(private val pageListener: PageListener) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    private val items = mutableListOf<ShowDto>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvTitle)

        fun bindData(showDto: ShowDto) {
            tvName.text = showDto.title
            itemView.rootView.setOnClickListener {
                pageListener.onPageClicked(showDto.link)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (viewType == HEADER) R.layout.item_header
        else R.layout.item_library
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindData(item)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<ShowDto>) {
        val ordered = items.sortedBy { it.title }
//        ordered.forEach {
//            val firstLetter = it.link[0].uppercaseChar()
//        }
        this.items.clear()
        this.items.addAll(ordered)
        notifyDataSetChanged()
    }

    class Header {
        val index: Char = 'A'
        val isSet: Boolean = false
    }

    companion object {
        const val ITEM = 1
        const val HEADER = 2
    }
}
