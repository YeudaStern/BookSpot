package com.example.bookspot;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    // ה-API Key שעבד לך מצוין בדפדפן
    private static final String API_KEY = "AIzaSyAaH5ZheW9Vo7N6dik-8dMfUnDtNzkgVss";
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface BookCallback {
        void onSuccess(List<Book> books);
        void onFailure(String error);
    }

    public static void searchBooks(String query, BookCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : "Technology";
                String encodedQuery = URLEncoder.encode(cleanQuery, StandardCharsets.UTF_8.name());

                // בניית ה-URL המדויק עם ה-Key כמו שעבד בדפדפן
                String urlString = BASE_URL + encodedQuery + "&maxResults=20&printType=books&key=" + API_KEY;

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // הגדרת Header של User-Agent
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                InputStream stream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                if (stream == null) {
                    postFailure(callback, "Server returned response code: " + responseCode);
                    return;
                }

                reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                if (responseCode == 429) {
                    postFailure(callback, "Rate limit (429). Please wait a moment.");
                    return;
                } else if (responseCode != HttpURLConnection.HTTP_OK) {
                    postFailure(callback, "Error (" + responseCode + "): " + response.toString());
                    return;
                }

                List<Book> books = parseBooksJson(response.toString());
                mainHandler.post(() -> callback.onSuccess(books));

            } catch (Exception e) {
                postFailure(callback, e.getMessage() != null ? e.getMessage() : "Network error");
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (Exception ignored) {}
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static void postFailure(BookCallback callback, String error) {
        mainHandler.post(() -> callback.onFailure(error));
    }

    private static List<Book> parseBooksJson(String jsonStr) {
        List<Book> bookList = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonStr);
            if (!root.has("items")) {
                return bookList;
            }

            JSONArray items = root.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String id = item.optString("id", String.valueOf(System.currentTimeMillis() + i));

                JSONObject volumeInfo = item.optJSONObject("volumeInfo");
                if (volumeInfo == null) continue;

                String title = volumeInfo.optString("title", "No Title");

                StringBuilder authorsBuilder = new StringBuilder();
                if (volumeInfo.has("authors")) {
                    JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                    for (int j = 0; j < authorsArray.length(); j++) {
                        if (j > 0) authorsBuilder.append(", ");
                        authorsBuilder.append(authorsArray.getString(j));
                    }
                } else {
                    authorsBuilder.append("Unknown Author");
                }
                String author = authorsBuilder.toString();

                String description = volumeInfo.optString("description", "No description available.");

                String coverImageUrl = "";
                if (volumeInfo.has("imageLinks")) {
                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                    coverImageUrl = imageLinks.optString("thumbnail", "");
                    if (coverImageUrl.startsWith("http://")) {
                        coverImageUrl = coverImageUrl.replace("http://", "https://");
                    }
                }

                double rating = volumeInfo.optDouble("averageRating", 4.0);
                boolean isFavorite = false;
                int searchOrder = i;

                int publishYear = 0;
                String publishedDate = volumeInfo.optString("publishedDate", "");
                if (publishedDate.length() >= 4) {
                    try {
                        publishYear = Integer.parseInt(publishedDate.substring(0, 4));
                    } catch (NumberFormatException ignored) {}
                }

                int pageCount = volumeInfo.optInt("pageCount", 0);
                String publisher = volumeInfo.optString("publisher", "Unknown Publisher");

                StringBuilder subjectsBuilder = new StringBuilder();
                if (volumeInfo.has("categories")) {
                    JSONArray categoriesArray = volumeInfo.getJSONArray("categories");
                    for (int k = 0; k < categoriesArray.length(); k++) {
                        if (k > 0) subjectsBuilder.append(", ");
                        subjectsBuilder.append(categoriesArray.getString(k));
                    }
                }
                String subjects = subjectsBuilder.toString();

                Book book = new Book(
                        id,
                        title,
                        author,
                        description,
                        coverImageUrl,
                        rating,
                        isFavorite,
                        searchOrder,
                        publishYear,
                        pageCount,
                        publisher,
                        subjects
                );
                bookList.add(book);
            }
        } catch (Exception ignored) {}
        return bookList;
    }
}