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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.withContext
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
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D0D0D), Color(0xFF1C1C1C), Color(0xFF3A3A3A))
    )

    LaunchedEffect(movieName) {
        val json = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://imdb.iamidiotareyoutoo.com/search?q=$movieName")
                    .build()

                client.newCall(request).execute().body?.string()
            } catch (e: Exception) {
                null
            }
        }

        if (json != null) {
            val result = Gson().fromJson(json, MovieResponse::class.java)
            movies = result.description
        } else {
            error = true
        }

        loading = false
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Text(
            text = "Showing results for $movieName",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        when {

            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text("Loading...", fontSize = 20.sp, color = Color.White)
            }
            error ->  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text("Failed to load movie", fontSize = 18.sp, color = Color.Red)
            }
            else -> LazyColumn {
                    items(movies) { movie -> MovieItem(movie) {
                        val intent = Intent(context, MovieDetail::class.java)
                        intent.putExtra("imdb_id", movie.imdbId)
                        context.startActivity(intent)
                    }
                    }
                }

        }
    }
}

@Composable
fun MovieItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFC107))
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Year • ${movie.year ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


data class MovieResponse(
    val description: List<Movie>
)

data class Movie(
    @SerializedName("#TITLE") val title: String,
    @SerializedName("#YEAR") val year: Int?,
    @SerializedName("#IMDB_ID") val imdbId: String,
)