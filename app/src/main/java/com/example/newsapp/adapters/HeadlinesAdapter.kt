package com.example.newsapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.databinding.ItemHeadlineBinding
import com.example.newsapp.models.ArticleItem

class HeadlinesAdapter(
    items: List<ArticleItem>,
    private val onSaveToggle: (ArticleItem) -> Unit,
    private val onFavToggle: (ArticleItem) -> Unit,
    private val showSaveButton: Boolean = true,
    private val showFavButton: Boolean = true
) : RecyclerView.Adapter<HeadlinesAdapter.HeadlineViewHolder>() {

    private var items: MutableList<ArticleItem> = items.toMutableList()

    inner class HeadlineViewHolder(val binding: ItemHeadlineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadlineViewHolder {
        val binding = ItemHeadlineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HeadlineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeadlineViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        // ---------- Title ----------
        b.tvHeadline.text = item.title

        // ---------- Image ----------
        Glide.with(b.imgNews.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.placeholder)
            .into(b.imgNews)

        // ---------- Open in Browser ----------
        holder.itemView.setOnClickListener {
            if (item.url.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, item.url.toUri())
                holder.itemView.context.startActivity(intent)
            }
        }

        // ---------- Save button ----------
        if (showSaveButton) {
            b.btnSave.visibility = View.VISIBLE
            setSaveIconColor(b, item.isSaved == 1)

            b.btnSave.setOnClickListener {
                // Toggle locally
                item.isSaved = if (item.isSaved == 1) 0 else 1
                setSaveIconColor(b, item.isSaved == 1)
                // Callback to fragment (to update DB)
                onSaveToggle(item)
            }
        } else {
            b.btnSave.visibility = View.GONE
        }

        // ---------- Favorite button ----------
        if (showFavButton) {
            b.btnFavorite.visibility = View.VISIBLE
            setFavIconColor(b, item.isFavorite == 1)

            b.btnFavorite.setOnClickListener {
                // Toggle locally
                item.isFavorite = if (item.isFavorite == 1) 0 else 1
                setFavIconColor(b, item.isFavorite == 1)
                // Callback to fragment (to update DB/remove if needed)
                onFavToggle(item)
            }
        } else {
            b.btnFavorite.visibility = View.GONE
        }
    }

    private fun setSaveIconColor(b: ItemHeadlineBinding, isSaved: Boolean) {
        val colorRes = if (isSaved) R.color.green else android.R.color.white
        b.btnSave.setColorFilter(ContextCompat.getColor(b.root.context, colorRes))
    }

    private fun setFavIconColor(b: ItemHeadlineBinding, isFav: Boolean) {
        val colorRes = if (isFav) R.color.green else android.R.color.white
        b.btnFavorite.setColorFilter(ContextCompat.getColor(b.root.context, colorRes))
    }

    override fun getItemCount(): Int = items.size

    // ---------- Utility Functions ----------
    fun updateData(newItems: List<ArticleItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItemByUrl(url: String) {
        val index = items.indexOfFirst { it.url == url }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun removeItem(item: ArticleItem) {
        removeItemByUrl(item.url)
    }

    fun updateItem(updated: ArticleItem) {
        val index = items.indexOfFirst { it.url == updated.url }
        if (index != -1) {
            items[index] = updated
            notifyItemChanged(index)
        }
    }

}
