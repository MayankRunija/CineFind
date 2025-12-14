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
import com.google.gson.annotations.SerializedName


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
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D0D0D), Color(0xFF1C1C1C), Color(0xFF3A3A3A))
    )

    LaunchedEffect(imdbId) {
        val json = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://www.omdbapi.com/?i=$imdbId&apikey=3530cde3")
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
            error -> Text("Failed to load movie", fontSize = 18.sp, color = Color.Red)
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
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = movie.poster,
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
                .padding(horizontal = 16.dp),
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
                    text = movie.title ?: "N/A",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Released: ${movie.released ?: "Unknown"}",
                    style = MaterialTheme.typography.titleMedium
                )

                Divider()

                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = movie.plot ?: "No description available",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                Divider()

                Text(text = "IMDb Rating: ${movie.imdbRating ?: "N/A"}",fontWeight = FontWeight.SemiBold)
                Text(text = "Genre: ${movie.genre ?: "N/A"}",fontWeight = FontWeight.SemiBold)
                Text(text = "Director: ${movie.director ?: "N/A"}",fontWeight = FontWeight.SemiBold)
                Text(text = "Actors: ${movie.actors ?: "N/A"}",fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


data class MovieDetailResponse(

    @SerializedName("Title")
    val title: String?,

    @SerializedName("Year")
    val year: String?,

    @SerializedName("Released")
    val released: String?,

    @SerializedName("Plot")
    val plot: String?,

    @SerializedName("Poster")
    val poster: String?,

    @SerializedName("Genre")
    val genre: String?,

    @SerializedName("Director")
    val director: String?,

    @SerializedName("Actors")
    val actors: String?,

    @SerializedName("imdbRating")
    val imdbRating: String?
)
