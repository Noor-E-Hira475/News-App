package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.network.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HeadlinesViewModel(private val repository: NewsRepository) : ViewModel() {

    private val _headlinesState = MutableStateFlow<ResultState<NewsResponse>>(ResultState.Loading)
    val headlinesState: StateFlow<ResultState<NewsResponse>> = _headlinesState

    fun fetchHeadlines() {
        viewModelScope.launch {
            _headlinesState.value = ResultState.Loading
            _headlinesState.value = repository.getHeadlines()
        }
    }
}

