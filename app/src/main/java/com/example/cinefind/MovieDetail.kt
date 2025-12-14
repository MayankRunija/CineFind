package com.example.cinefind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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

    var movie by rememberSaveable { mutableStateOf<MovieDetailResponse?>(null) }
    var loading by rememberSaveable { mutableStateOf(true) }
    var error by rememberSaveable { mutableStateOf(false) }
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D0D0D), Color(0xFF1C1C1C), Color(0xFF3A3A3A))
    )

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
        modifier = Modifier.fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> Text("Loading...", fontSize = 20.sp, color = Color.White)
            error -> Text("Failed to load movie", fontSize = 18.sp, color = Color.White)
            movie != null -> MovieDetailUI(movie!!)
        }
    }
}

@Composable
fun MovieDetailUI(movie: MovieDetailResponse) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 80.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = movie.short?.image ?: "Poster not Available",
                    contentDescription = "Movie Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(220.dp)
                        .height(320.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                .background(Color(0xFFFFC107))
                .padding(20.dp),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = movie.short?.name ?: "N/A",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = movie.short?.datePublished ?: "Unknown Release Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Divider()
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = movie.short?.description ?: "No description available.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
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
