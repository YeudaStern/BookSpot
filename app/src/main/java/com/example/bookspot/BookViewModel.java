package com.example.bookspot;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BookViewModel extends AndroidViewModel {

    private final BookRepository repository;
    private final LiveData<List<Book>> allBooks;
    private final LiveData<List<Book>> favoriteBooks;

    public BookViewModel(@NonNull Application application) {
        super(application);
        repository = new BookRepository(application);
        allBooks = repository.getAllBooks();
        favoriteBooks = repository.getFavoriteBooks();
    }

    public LiveData<List<Book>> getAllBooks() {
        return allBooks;
    }

    public LiveData<List<Book>> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void searchBooks(String query, BookRepository.OnSearchCompleteListener listener) {
        repository.fetchBooksFromApi(query, listener);
    }

    public void toggleFavorite(Book book) {
        book.setFavorite(!book.isFavorite());
        repository.updateBook(book);
    }
}