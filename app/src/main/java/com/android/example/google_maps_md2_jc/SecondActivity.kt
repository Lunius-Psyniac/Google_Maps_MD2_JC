package com.android.example.google_maps_md2_jc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class Spell(
    val name: String,
    val description: String
)

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
    var dataList by remember { mutableStateOf<List<Spell>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
            TopAppBar(
                title = { Text("Spell Reader") },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Map") },
                                onClick = {
                                    menuExpanded = false
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                errorMessage != null -> Text(text = errorMessage!!, modifier = Modifier.padding(16.dp))
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(dataList) { spell ->
                        ListItem(spell = spell)
                    }
                }
            }
        }
    }
}

@Composable
fun ListItem(spell: Spell) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = spell.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spell.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

suspend fun fetchData(): List<Spell> {
    return withContext(Dispatchers.IO) {
        val url = URL("https://hp-api.onrender.com/api/spells")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(response)
        List(jsonArray.length()) { i ->
            val jsonObject = jsonArray.getJSONObject(i)
            Spell(
                name = jsonObject.getString("name"),
                description = jsonObject.getString("description")
            )
        }
    }
}
