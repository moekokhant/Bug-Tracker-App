package com.example.trackerapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trackerapp.data.local.Issue
import com.example.trackerapp.data.repository.IssueRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IssueViewModel(private val repository: IssueRepository) : ViewModel() {

    val allIssues: StateFlow<List<Issue>> = repository.allIssues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addIssue(title: String, description: String, priority: String) {
        viewModelScope.launch {
            val newIssue = Issue(
                title = title,
                description = description,
                priority = priority,
                status = "Open"
            )
            repository.insert(newIssue)
        }
    }

    fun retrySync() {
        viewModelScope.launch {
            repository.syncWithRemote()
        }
    }
}

class IssueViewModelFactory(private val repository: IssueRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IssueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IssueViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
