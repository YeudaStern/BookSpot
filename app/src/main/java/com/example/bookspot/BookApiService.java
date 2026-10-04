package com.example.bookspot;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BookApiService {

    @GET("books/v1/volumes")
    Call<GoogleBooksResponse> searchBooks(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("printType") String printType,
            @Query("key") String apiKey
    );
}