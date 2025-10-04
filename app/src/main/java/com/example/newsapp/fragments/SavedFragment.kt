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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        adapter = HeadlinesAdapter(
            emptyList(),
            onSaveToggle = { item -> handleSaveToggleFromDb(item) },
            onFavToggle = { item -> handleFavToggleFromDb(item) },
            showSaveButton = true,   // show Save button in this fragment
            showFavButton = false    // hide Favorite button here
        )
        binding.recyclerSaved.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSaved.adapter = adapter
    }

    private fun loadSaved() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbArticles: List<Article> = dao.getSavedArticles()
            val display = dbArticles.map { it.toArticleItem() }
            withContext(Dispatchers.Main) {
                adapter.updateData(display)
            }
        }
    }

    private fun handleSaveToggleFromDb(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            // DB update with new state (adapter already toggled item.isSaved)
            dao.updateArticleFlags(item.url, isSaved = item.isSaved)

            // If now un-saved → remove from UI
            if (item.isSaved == 0) {
                withContext(Dispatchers.Main) {
                    adapter.removeItemByUrl(item.url)
                }
            }
        }
    }

    private fun handleFavToggleFromDb(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            // DB update (adapter already toggled item.isFavorite)
            dao.updateArticleFlags(item.url, isFavorite = item.isFavorite)

            // Just refresh UI to reflect icon color change
            withContext(Dispatchers.Main) {
                adapter.updateItem(item)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
