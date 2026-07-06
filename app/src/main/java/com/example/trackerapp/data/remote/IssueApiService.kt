package com.example.trackerapp.data.remote

import com.example.trackerapp.data.local.Issue
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IssueApiService {
    @GET("issues")
    suspend fun getIssues(): List<Issue>

    @POST("issues/sync")
    suspend fun syncIssue(@Body issue: Issue): Response<Unit>
}
