package com.zc.bakamitai.ui.bookmarks

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zc.bakamitai.R
import com.zc.bakamitai.data.room.entities.Bookmark
import com.zc.bakamitai.listeners.PageListener

class BookmarkAdapter(private val pageListener: PageListener) : RecyclerView.Adapter<BookmarkAdapter.EntryViewHolder>() {

    private val items: MutableList<Bookmark> = mutableListOf()

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvName)
        private val ivEntry = itemView.findViewById<ImageView>(R.id.ivEntry)

        fun bindData(bookmark: Bookmark) {
            tvName.text = bookmark.name
            Glide.with(itemView)
                .load(bookmark.imageUrl)
                .into(ivEntry)
            itemView.rootView.setOnClickListener {
                pageListener.onPageClicked(bookmark.page)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val data = items[position]
        holder.bindData(data)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<Bookmark>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearItems() {
        this.items.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
