package com.example.cinefind

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// ---------------- ACTIVITY ----------------

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

// ---------------- COMPOSABLE ----------------

@Composable
fun SearchListView(movieName: String) {

    val context = LocalContext.current
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(movieName) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://imdb.iamidiotareyoutoo.com/search?q=$movieName")
            .build()

        withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val json = response.body?.string()

            json?.let {
                val result = Gson().fromJson(it, MovieResponse::class.java)
                movies = result.description
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Searching for: $movieName",
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        } else {
            LazyColumn {
                items(movies) { movie ->
                    MovieItem(movie) {
                        val intent = Intent(context, MovieDetail::class.java)
                        intent.putExtra("imdb_id", movie.imdbId)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

// ---------------- MOVIE ITEM ----------------

@Composable
fun MovieItem(movie: Movie, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = movie.title,
            fontSize = 18.sp,

        )
        Text(
            text = "Year: ${movie.year ?: "N/A"}",
            fontSize = 14.sp,
        )
    }
}

// ---------------- DATA MODELS ----------------

data class MovieResponse(
    val description: List<Movie>
)

data class Movie(
    @SerializedName("#TITLE") val title: String,
    @SerializedName("#YEAR") val year: Int?,
    @SerializedName("#IMDB_ID") val imdbId: String,
)
