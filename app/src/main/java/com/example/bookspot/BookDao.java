package com.example.bookspot;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBook(Book book);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBooks(List<Book> books);

    @Update
    void updateBook(Book book);

    @Delete
    void deleteBook(Book book);

    @Query("DELETE FROM books WHERE id = :bookId")
    void deleteBookById(String bookId);

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    Book getBookById(String bookId);

    // כל הספרים הממוינים לפי סדר החיפוש
    @Query("SELECT * FROM books ORDER BY searchOrder ASC")
    LiveData<List<Book>> getAllBooks();

    // שליפת המועדפים כ-LiveData (השם המדויק ש-BookRepository קורא לו)
    @Query("SELECT * FROM books WHERE isFavorite = 1")
    LiveData<List<Book>> getFavoriteBooks();

    // שליפת מועדפים מסונכרנת (לשימוש ישיר ברקע)
    @Query("SELECT * FROM books WHERE isFavorite = 1")
    List<Book> getAllFavoriteBooksSync();

    // רשימת ה-IDs של כל המועדפים
    @Query("SELECT id FROM books WHERE isFavorite = 1")
    List<String> getFavoriteBookIdsSync();

    // מחיקת ספרים שאינם מועדפים בעת חיפוש חדש
    @Query("DELETE FROM books WHERE isFavorite = 0")
    void clearSearchResults();

    // איפוס סדר המועדפים שלא יתנגשו במיון של החיפוש החדש
    @Query("UPDATE books SET searchOrder = -1 WHERE isFavorite = 1")
    void resetFavoritesSearchOrder();
}