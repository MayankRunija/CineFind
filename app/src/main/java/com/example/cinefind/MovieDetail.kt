package com.example.cinefind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import coil.compose.AsyncImage


class MovieDetail : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val imdbId = intent.getStringExtra("imdb_id") ?: ""
            MovieDetailView(imdbId)
        }
    }
}

@Composable
fun MovieDetailView(imdbId: String) {

    var movie by remember { mutableStateOf<MovieDetailResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(imdbId) {

        val json = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://imdb.iamidiotareyoutoo.com/search?tt=$imdbId")
                    .build()

                client.newCall(request).execute().body?.string()
            } catch (e: Exception) {
                null
            }
        }

        if (json != null) {
            movie = Gson().fromJson(json, MovieDetailResponse::class.java)
        } else {
            error = true
        }

        loading = false
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> Text("Loading...", fontSize = 20.sp)
            error -> Text("Failed to load movie", fontSize = 18.sp)
            movie != null -> MovieDetailUI(movie!!)
        }
    }
}

@Composable
fun MovieDetailUI(movie: MovieDetailResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = movie.short?.image,
            contentDescription = "Movie Poster",
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = movie.short?.name ?: "N/A",
            fontSize = 26.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Release Date: ${movie.short?.datePublished ?: "N/A"}",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = movie.short?.description ?: "No description available",
            fontSize = 16.sp
        )
    }
}

data class MovieDetailResponse(
    val short: ShortMovie?
)

data class ShortMovie(
    val name: String?,
    val image: String?,
    val description: String?,
    val datePublished: String?
)
