package com.example.trackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackerapp.data.local.AppDatabase
import com.example.trackerapp.data.local.Issue
import com.example.trackerapp.data.remote.IssueApiService
import com.example.trackerapp.data.repository.IssueRepository
import com.example.trackerapp.ui.IssueViewModel
import com.example.trackerapp.ui.IssueViewModelFactory
import com.example.trackerapp.ui.theme.TrackerAppTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual DI for demonstration
        val database = AppDatabase.getDatabase(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Placeholder URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(IssueApiService::class.java)
        val repository = IssueRepository(database.issueDao(), apiService)
        val viewModel: IssueViewModel by viewModels { IssueViewModelFactory(repository) }

        enableEdgeToEdge()
        setContent {
            TrackerAppTheme {
                BugTrackerApp(viewModel)
            }
        }
    }
}

@Composable
fun BugTrackerApp(viewModel: IssueViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    val issues by viewModel.allIssues.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Issue")
            }
        },
        topBar = {
            Row(modifier = Modifier.padding(16.dp).padding(top = 32.dp)) {
                Text(
                    text = "Bug Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.retrySync() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (showAddDialog) {
                AddIssueForm(
                    onAdd = { title, desc, priority ->
                        viewModel.addIssue(title, desc, priority)
                        showAddDialog = false
                    },
                    onCancel = { showAddDialog = false }
                )
            }

            IssueList(issues = issues)
        }
    }
}

@Composable
fun IssueList(issues: List<Issue>) {
    LazyColumn {
        items(issues) { issue ->
            IssueItem(issue)
        }
    }
}

@Composable
fun IssueItem(issue: Issue) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = issue.title, style = MaterialTheme.typography.titleLarge)
            Text(text = issue.description)
            Text(
                text = "Priority: ${issue.priority} | Status: ${issue.status}",
                style = MaterialTheme.typography.bodySmall
            )
            if (!issue.isSynced) {
                Text(
                    text = "Pending Sync...",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun AddIssueForm(onAdd: (String, String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        TextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
        Row {
            Button(onClick = { onAdd(title, desc, priority) }) { Text("Add") }
            Button(onClick = onCancel) { Text("Cancel") }
        }
    }
}
