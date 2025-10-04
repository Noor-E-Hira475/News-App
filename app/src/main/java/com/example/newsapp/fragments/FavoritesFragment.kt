package com.example.newsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.adapters.HeadlinesAdapter
import com.example.newsapp.database.Article
import com.example.newsapp.database.ArticleDAO
import com.example.newsapp.database.DBManager
import com.example.newsapp.databinding.FragmentFavoritesBinding
import com.example.newsapp.models.ArticleItem
import com.example.newsapp.models.toArticleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HeadlinesAdapter
    private lateinit var dao: ArticleDAO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dao = ArticleDAO(
            dbManager = DBManager(requireContext()),
            context = requireContext()
        )
        setupRecycler()
        loadFavorites()
    }

    private fun setupRecycler() {
        adapter = HeadlinesAdapter(
            emptyList(),
            onSaveToggle = { }, // Save button hidden here
            onFavToggle = { item -> handleFavToggleFromDb(item) },
            showSaveButton = false,
            showFavButton = true
        )
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFavorites.adapter = adapter
    }

    private fun loadFavorites() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbArticles: List<Article> = dao.getFavoriteArticles()
            val display = dbArticles.map { it.toArticleItem() }
            withContext(Dispatchers.Main) {
                adapter.updateData(display)
            }
        }
    }

    private fun handleFavToggleFromDb(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Update DB: mark as not favorite
            dao.updateArticleFlags(item.url, isFavorite = 0)

            // Remove from RecyclerView immediately
            withContext(Dispatchers.Main) {
                adapter.removeItemByUrl(item.url)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
