package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.network.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: NewsRepository) : ViewModel() {
    private val _searchState = MutableStateFlow<ResultState<NewsResponse>>(ResultState.Idle)
    val searchState: StateFlow<ResultState<NewsResponse>> = _searchState

    fun searchNews(query: String) {
        viewModelScope.launch {
            _searchState.value = ResultState.Loading
            _searchState.value = repository.searchNews(query)
        }
    }

}