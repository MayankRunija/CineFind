package com.example.cinefind

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.foundation.shape.RoundedCornerShape
import okhttp3.OkHttpClient
import okhttp3.Request

class SearchList : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val movieName = intent.getStringExtra("movie_name") ?: ""
        setContent {
            SearchListView(movieName)
        }
    }
}

@Composable
fun SearchListView(movieName: String) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var canLoadMore by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D0D0D), Color(0xFF1C1C1C), Color(0xFF3A3A3A))
    )

    // Initial load
    LaunchedEffect(movieName) {
        loading = true
        page = 1
        movies = emptyList()
        canLoadMore = true
        error = false

        val result = fetchMovies(movieName, page)
        if (result != null && result.response == "True") {
            movies = result.search
        } else {
            error = true
        }
        loading = false
    }

    // Scroll-based pagination
    LaunchedEffect(listState) {
        while (true) {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            if (
                lastVisibleItem >= movies.size - 1 &&
                canLoadMore &&
                !isLoadingMore &&
                !loading
            ) {
                isLoadingMore = true
                page += 1
                val result = fetchMovies(movieName, page)
                if (result != null && result.search.isNotEmpty()) {
                    movies = movies + result.search
                } else {
                    canLoadMore = false
                }
                isLoadingMore = false
            }
            delay(200) // Small delay to prevent busy looping
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        Text(
            text = "Showing results for \"$movieName\"",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", fontSize = 20.sp, color = Color.White)
                }
            }

            error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Movies not found !!", fontSize = 18.sp, color = Color.Red)
                }
            }

            else -> {
                LazyColumn(state = listState) {
                    items(movies) { movie ->
                        MovieItem(movie) {
                            val intent = Intent(context, MovieDetail::class.java)
                            intent.putExtra("imdb_id", movie.imdbId)
                            context.startActivity(intent)
                        }
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Loading more...", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFC107))
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Year • ${movie.year}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

suspend fun fetchMovies(movieName: String, page: Int): MovieResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://www.omdbapi.com/?apikey=3530cde3&s=$movieName&page=$page")
                .build()
            val json = client.newCall(request).execute().body?.string()
            Gson().fromJson(json, MovieResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

// 🔹 Models
data class MovieResponse(
    @SerializedName("Search") val search: List<Movie> = emptyList(),
    @SerializedName("totalResults") val totalResults: String? = null,
    @SerializedName("Response") val response: String? = null,
    @SerializedName("Error") val error: String? = null
)

data class Movie(
    @SerializedName("Title") val title: String,
    @SerializedName("Year") val year: String,
    @SerializedName("imdbID") val imdbId: String
)
