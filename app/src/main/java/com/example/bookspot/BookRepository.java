package com.example.bookspot;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookRepository {

    private static final String API_KEY = "AIzaSyAaH5ZheW9Vo7N6dik-8dMfUnDtNzkgVss";

    public interface OnSearchCompleteListener {
        void onComplete();
    }

    private final Application application;
    private final BookDao bookDao;
    private final LiveData<List<Book>> allBooks;
    private final LiveData<List<Book>> favoriteBooks;
    private final BookApiService apiService;

    private boolean isRequestInProgress = false;

    public BookRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        bookDao = db.bookDao();
        allBooks = bookDao.getAllBooks();
        favoriteBooks = bookDao.getFavoriteBooks();
        apiService = RetrofitClient.getApiService();
    }

    public LiveData<List<Book>> getAllBooks() {
        return allBooks;
    }

    public LiveData<List<Book>> getFavoriteBooks() {
        return favoriteBooks;
    }

    // מתודה זו מבטיחה שמירה מיידית ב-Room גם אם הספר הגיע כרגע מהרשת
    public void toggleFavorite(Book book) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            bookDao.insertBook(book);
        });
    }

    public void updateBook(Book book) {
        AppDatabase.databaseWriteExecutor.execute(() -> bookDao.updateBook(book));
    }

    public void fetchBooksFromApi(String query, OnSearchCompleteListener listener) {
        if (isRequestInProgress) {
            if (listener != null) listener.onComplete();
            return;
        }

        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : "Technology";
        isRequestInProgress = true;

        apiService.searchBooks(cleanQuery, 20, "books", API_KEY).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {
                isRequestInProgress = false;

                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                    List<GoogleBooksResponse.BookItem> items = response.body().getItems();
                    if (items.isEmpty()) {
                        showToast("No results found for: " + cleanQuery);
                        finishLoading(listener);
                        return;
                    }

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        Set<String> favoriteIds = new HashSet<>(bookDao.getFavoriteBookIdsSync());
                        List<Book> newSearchResults = new ArrayList<>();
                        int orderIndex = 0;

                        for (GoogleBooksResponse.BookItem item : items) {
                            String id = item.getId() != null ? item.getId() : String.valueOf(System.currentTimeMillis() + Math.random());
                            GoogleBooksResponse.VolumeInfo info = item.getVolumeInfo();

                            String title = info != null ? info.getTitle() : "Untitled";

                            String author = "Unknown Author";
                            if (info != null && info.getAuthors() != null && !info.getAuthors().isEmpty()) {
                                author = String.join(", ", info.getAuthors());
                            }

                            String description = info != null ? info.getDescription() : "No description available.";

                            String imageUrl = null;
                            if (info != null && info.getImageLinks() != null) {
                                imageUrl = info.getImageLinks().getThumbnail();
                            }

                            double rating = info != null ? info.getAverageRating() : 0.0;
                            int pageCount = info != null ? info.getPageCount() : 0;
                            String publisher = info != null ? info.getPublisher() : "Unknown Publisher";

                            int publishYear = 0;
                            if (info != null && info.getPublishedDate() != null && info.getPublishedDate().length() >= 4) {
                                try {
                                    publishYear = Integer.parseInt(info.getPublishedDate().substring(0, 4));
                                } catch (NumberFormatException ignored) {}
                            }

                            String subjects = "";
                            if (info != null && info.getCategories() != null && !info.getCategories().isEmpty()) {
                                subjects = String.join(", ", info.getCategories());
                            }

                            // האם הספר סומן בעבר כמועדף
                            boolean isFav = favoriteIds.contains(id);

                            newSearchResults.add(new Book(
                                    id, title, author, description, imageUrl, rating, isFav, orderIndex++,
                                    publishYear, pageCount, publisher, subjects
                            ));
                        }

                        // שמירה/עדכון ספרים ללא דריסת סדר או הדבקת ספרים ישנים
                        bookDao.insertBooks(newSearchResults);

                        // החזרת רשימת החיפוש הנוכחית ישירות ל-UI
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (listener != null) {
                                if (listener instanceof OnBooksLoadedListener) {
                                    ((OnBooksLoadedListener) listener).onBooksLoaded(newSearchResults);
                                }
                                listener.onComplete();
                            }
                        });
                    });
                } else {
                    if (response.code() == 429) {
                        showToast("Google rate limit (429). Please wait a moment.");
                    } else {
                        showToast("Server response: " + response.code());
                    }
                    finishLoading(listener);
                }
            }

            @Override
            public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                isRequestInProgress = false;
                showToast("Network failed: " + t.getMessage());
                finishLoading(listener);
            }
        });
    }

    public interface OnBooksLoadedListener extends OnSearchCompleteListener {
        void onBooksLoaded(List<Book> books);
    }
    private void finishLoading(OnSearchCompleteListener listener) {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(listener::onComplete);
        }
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
        );
    }
}