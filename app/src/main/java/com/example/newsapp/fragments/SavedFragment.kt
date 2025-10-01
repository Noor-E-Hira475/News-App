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
import com.example.newsapp.databinding.FragmentSavedBinding
import com.example.newsapp.models.ArticleItem
import com.example.newsapp.models.toArticleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HeadlinesAdapter
    private lateinit var dao: ArticleDAO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dao = ArticleDAO(
            dbManager = DBManager(requireContext()),
            context = requireContext()
        )
        setupRecycler()
        loadSaved()
    }

    private fun setupRecycler() {
        adapter = HeadlinesAdapter(emptyList(),
            onSaveToggle = { item -> handleSaveToggleFromDb(item) },
            onFavToggle = { item -> handleFavToggleFromDb(item) }
        )
        binding.recyclerSaved.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSaved.adapter = adapter
    }

    private fun loadSaved() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbArticles: List<Article> = dao.getAllArticles().filter { it.isSaved == 1 }
            val display = dbArticles.map { it.toArticleItem() }
            withContext(Dispatchers.Main) {
                adapter.updateData(display)
            }
        }
    }

    private fun handleSaveToggleFromDb(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.isSaved == 0) {
                // was saved and user toggled to unsave
                dao.deleteArticleByUrl(item.url)
                // reload list
                loadSaved()
            } else {
                // unlikely: toggled "save" on an already-saved item, maybe update flags
                // Do nothing or update flags if needed
            }
        }
    }

    private fun handleFavToggleFromDb(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.isFavorite == 1) {
                // mark favorite: update DB row (simple approach: insert or update)
                dao.insertArticle(item.title, item.url, item.imageUrl, item.isSaved, 1)
                loadSaved()
            } else {
                // remove favorite: update row or delete; here we delete by url if you want to remove entirely
                dao.deleteArticleByUrl(item.url)
                loadSaved()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
