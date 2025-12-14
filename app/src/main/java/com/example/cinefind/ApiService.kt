package com.example.cinefind

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {

    @GET("search")
    suspend fun searchMovies(
        @Query("q") movieName: String
    ): MovieResponse

    @GET("search")
    suspend fun getMovieDetail(
        @Query("tt") imdbId: String
    ): MovieDetailResponse
}

object RetrofitInstance {

    private const val BASE_URL = "https://imdb.iamidiotareyoutoo.com/"

    val api: MovieApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MovieApi::class.java)
    }
}
