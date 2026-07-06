package com.example.trackerapp.data.repository

import com.example.trackerapp.data.local.Issue
import com.example.trackerapp.data.local.IssueDao
import com.example.trackerapp.data.remote.IssueApiService
import kotlinx.coroutines.flow.Flow

class IssueRepository(
    private val issueDao: IssueDao,
    private val apiService: IssueApiService
) {
    val allIssues: Flow<List<Issue>> = issueDao.getAllIssues()

    suspend fun insert(issue: Issue) {
        issueDao.insertIssue(issue)
        syncWithRemote()
    }

    suspend fun update(issue: Issue) {
        issueDao.updateIssue(issue)
        syncWithRemote()
    }

    suspend fun delete(issue: Issue) {
        issueDao.deleteIssue(issue)
    }

    suspend fun syncWithRemote() {
        val unsynced = issueDao.getUnsyncedIssues()
        unsynced.forEach { issue ->
            try {
                // Implementing a basic retry mechanism (max 3 attempts could be added here)
                val response = apiService.syncIssue(issue)
                if (response.isSuccessful) {
                    issueDao.updateIssue(issue.copy(isSynced = true))
                }
            } catch (e: Exception) {
                // Log error or notify UI
                // In a real app, you might use WorkManager for persistent retries
            }
        }
    }
}
