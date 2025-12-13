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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
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
                    model = movie.short?.image,
                    contentDescription = "Movie Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(220.dp)
                        .height(320.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Movie Name
                Text(
                    text = movie.short?.name ?: "N/A",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Release Date
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = movie.short?.datePublished ?: "Unknown Release Date"
                        )
                    }
                )

                Divider()

                // Overview
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
