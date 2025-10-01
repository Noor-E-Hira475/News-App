package com.example.newsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.adapters.HeadlinesAdapter
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.database.Article as DbArticle
import com.example.newsapp.database.ArticleDAO
import com.example.newsapp.database.DBManager
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.models.ApiArticle
import com.example.newsapp.models.ArticleItem
import com.example.newsapp.models.toArticleItem
import com.example.newsapp.network.ResultState
import com.example.newsapp.utils.ImageUtils
import com.example.newsapp.viewmodels.SearchViewModel
import com.example.newsapp.viewmodelfactory.SearchViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var newsAdapter: HeadlinesAdapter
    private lateinit var dao: ArticleDAO

    private val apiKey = "06ee18642b634a62a2c6db84293f9794"

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(NewsRepository(apiKey))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init DAO
        dao = ArticleDAO(
            dbManager = DBManager(requireContext()),
            context = requireContext()
        )

        setupRecyclerView()
        setupSearchView()
        observeSearchResults()
    }

    /** Setup RecyclerView **/
    private fun setupRecyclerView() {
        newsAdapter = HeadlinesAdapter(
            items = emptyList(),
            onSaveToggle = { item -> handleSaveToggle(item) },
            onFavToggle = { item -> handleFavToggle(item) }
        )

        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = newsAdapter
    }

    /** Setup search input **/
    private fun setupSearchView() {
        binding.searchViewNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchNews(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    /** Observe search results **/
    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchState.collectLatest { state ->
                when (state) {
                    is ResultState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorText.visibility = View.GONE
                        newsAdapter.updateData(emptyList())
                    }
                    is ResultState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is ResultState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorText.visibility = View.GONE
                        val items = state.data.articles.map(ApiArticle::toArticleItem)
                        newsAdapter.updateData(items)
                    }
                    is ResultState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.errorText.apply {
                            visibility = View.VISIBLE
                            text = state.message
                        }
                    }
                }
            }
        }
    }

    /** Save toggle **/
    private fun handleSaveToggle(item: ArticleItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.isSaved == 1) {
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
                dao.deleteArticleByUrl(item.url)
            }
        }
    }

    /** Favorite toggle **/
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
