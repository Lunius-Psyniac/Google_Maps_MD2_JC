package com.android.example.google_maps_md2_jc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApiListScreen(onNavigateBack = {
                startActivity(Intent(this, MainActivity::class.java))
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiListScreen(onNavigateBack: () -> Unit) {
    var dataList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            dataList = fetchData()
        } catch (e: Exception) {
            errorMessage = "Failed to fetch data"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("API List") }, actions = {
                TextButton(onClick = onNavigateBack) {
                    Text("Back to Map")
                }
            })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                errorMessage != null -> Text(text = errorMessage!!, modifier = Modifier.padding(16.dp))
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(dataList) { item ->
                        ListItem(title = item)
                    }
                }
            }
        }
    }
}

@Composable
fun ListItem(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(text = title, modifier = Modifier.padding(16.dp))
    }
}

suspend fun fetchData(): List<String> {
    return withContext(Dispatchers.IO) {
        val url = URL("https://jsonplaceholder.typicode.com/posts")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(response)
        List(10) { i -> jsonArray.getJSONObject(i).getString("title") }
    }
}
