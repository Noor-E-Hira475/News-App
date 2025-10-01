package com.example.newsapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.databinding.ItemHeadlineBinding
import com.example.newsapp.models.ArticleItem

class HeadlinesAdapter(
    private var items: List<ArticleItem>,
    private val onSaveToggle: (ArticleItem) -> Unit,
    private val onFavToggle: (ArticleItem) -> Unit
) : RecyclerView.Adapter<HeadlinesAdapter.HeadlineViewHolder>() {

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

        b.tvHeadline.text = item.title

        // Load image: if imageUrl looks like a file path, Glide will load it too.
        Glide.with(b.imgNews.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.placeholder) // add placeholder drawable
            .into(b.imgNews)

        // Open article URL in browser on item click
        holder.itemView.setOnClickListener {
            if (item.url.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, item.url.toUri())
                holder.itemView.context.startActivity(intent)
            }
        }

        // Set icon colors according to state
        setSaveIconColor(b, item.isSaved == 1)
        setFavIconColor(b, item.isFavorite == 1)

        // Save toggle: adapter updates UI & state and notifies fragment via callback
        b.btnSave.setOnClickListener {
            item.isSaved = if (item.isSaved == 1) 0 else 1
            setSaveIconColor(b, item.isSaved == 1)
            onSaveToggle(item)
        }

        // Favorite toggle
        b.btnFavorite.setOnClickListener {
            item.isFavorite = if (item.isFavorite == 1) 0 else 1
            setFavIconColor(b, item.isFavorite == 1)
            onFavToggle(item)
        }
    }

    private fun setSaveIconColor(b: ItemHeadlineBinding, isSaved: Boolean) {
        val color = if (isSaved) R.color.green else android.R.color.white
        b.btnSave.setColorFilter(ContextCompat.getColor(b.root.context, color))
    }

    private fun setFavIconColor(b: ItemHeadlineBinding, isFav: Boolean) {
        val color = if (isFav) R.color.green else android.R.color.white
        b.btnFavorite.setColorFilter(ContextCompat.getColor(b.root.context, color))
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ArticleItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
