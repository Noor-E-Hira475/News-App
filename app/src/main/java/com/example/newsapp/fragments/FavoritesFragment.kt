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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dao = ArticleDAO(DBManager(requireContext()))
        setupRecycler()
        loadFavorites()
    }

    private fun setupRecycler() {
        adapter = HeadlinesAdapter(emptyList(),
            onSaveToggle = { item -> handleSaveToggleFromDb(item) },
            onFavToggle = { item -> handleFavToggleFromDb(item) }
        )
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFavorites.adapter = adapter
    }

    private fun loadFavorites() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbArticles: List<Article> = dao.getAllArticles().filter { it.isFavorite == 1 }
            val display = dbArticles.map { it.toArticleItem() }
            withContext(Dispatchers.Main) {
                adapter.updateData(display)
            }
        }
    }

    private fun handleSaveToggleFromDb(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.isSaved == 0) {
                dao.deleteArticleByUrl(item.url)
                loadFavorites()
            } else {
                // update if needed
            }
        }
    }

    private fun handleFavToggleFromDb(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.isFavorite == 0) {
                // unfavorite -> delete or update
                dao.deleteArticleByUrl(item.url)
                loadFavorites()
            } else {
                // mark favorite (insert/update)
                dao.insertArticle(item.title, item.url, item.imageUrl, item.isSaved, 1)
                loadFavorites()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
