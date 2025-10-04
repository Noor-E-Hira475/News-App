package com.example.newsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.adapters.HeadlinesAdapter
import com.example.newsapp.database.Article as DbArticle
import com.example.newsapp.database.ArticleDAO
import com.example.newsapp.database.DBManager
import com.example.newsapp.databinding.FragmentHeadlinesBinding
import com.example.newsapp.models.ApiArticle
import com.example.newsapp.network.ResultState
import com.example.newsapp.models.ArticleItem
import com.example.newsapp.models.toArticleItem
import com.example.newsapp.utils.ImageUtils
import com.example.newsapp.viewmodels.HeadlinesViewModel
import com.example.newsapp.viewmodelfactory.HeadlinesViewModelFactory
import com.example.newsapp.api.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HeadlinesFragment : Fragment() {

    private var _binding: FragmentHeadlinesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HeadlinesAdapter
    private lateinit var dao: ArticleDAO

    private val apiKey = "06ee18642b634a62a2c6db84293f9794"

    // ViewModel (same as before)
    private val viewModel: HeadlinesViewModel by viewModels {
        HeadlinesViewModelFactory(NewsRepository(apiKey))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeadlinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dao = ArticleDAO(
            dbManager = DBManager(requireContext()),
            context = requireContext()
        )

        setupRecyclerView()
        viewModel.fetchHeadlines()
        observeHeadlines()
    }

    private fun setupRecyclerView() {
        adapter = HeadlinesAdapter(
            items = emptyList(),
            onSaveToggle = { item -> handleSaveToggle(item) },
            onFavToggle = { item -> handleFavToggle(item) }
        )

        binding.recyclerHeadlines.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHeadlines.adapter = adapter
    }

    private fun observeHeadlines() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.headlinesState.collectLatest { state ->
                when (state) {
                    is ResultState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.errorText.visibility = View.GONE
                        adapter.updateData(emptyList())
                    }
                    is ResultState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorText.visibility = View.GONE

                        // Convert ApiArticle → ArticleItem
                        val items = state.data.articles.map(ApiArticle::toArticleItem)
                        adapter.updateData(items)
                    }
                    is ResultState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorText.apply {
                            visibility = View.VISIBLE
                            text = state.message
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    /** Toggle Save button **/
    private fun handleSaveToggle(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.isSaved == 1) {
                // Save
                val localPath = if (item.imageUrl.startsWith("http")) {
                    ImageUtils.saveImageLocally(
                        requireContext(),
                        item.imageUrl,
                        "article_${System.currentTimeMillis()}"
                    ) ?: ""
                } else item.imageUrl

                val dbArticle = DbArticle(
                    title = item.title,
                    url = item.url,
                    imagePath = localPath,
                    isSaved = 1,
                    isFavorite = item.isFavorite
                )
                dao.insertArticle(
                    dbArticle.title,
                    dbArticle.url,
                    dbArticle.imagePath,
                    dbArticle.isSaved,
                    dbArticle.isFavorite
                )
            } else {
                // Unsave
                dao.deleteArticleByUrl(item.url)
            }
        }
    }

    /** Toggle Favorite button **/
    private fun handleFavToggle(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.isFavorite == 1) {
                val localPath = if (item.imageUrl.startsWith("http")) {
                    ImageUtils.saveImageLocally(
                        requireContext(),
                        item.imageUrl,
                        "article_${System.currentTimeMillis()}"
                    ) ?: ""
                } else item.imageUrl

                dao.insertArticle(
                    item.title,
                    item.url,
                    localPath,
                    item.isSaved,
                    1
                )
            } else {
                dao.deleteArticleByUrl(item.url)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
