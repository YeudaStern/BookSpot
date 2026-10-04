package com.example.bookspot;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "books")
public class Book implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String author;
    private String description;
    private String coverImageUrl;
    private double rating;
    private boolean isFavorite;
    private int searchOrder;
    private int publishYear;
    private int pageCount;
    private String publisher;
    private String subjects;

    public Book(@NonNull String id, String title, String author, String description,
                String coverImageUrl, double rating, boolean isFavorite, int searchOrder,
                int publishYear, int pageCount, String publisher, String subjects) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.rating = rating;
        this.isFavorite = isFavorite;
        this.searchOrder = searchOrder;
        this.publishYear = publishYear;
        this.pageCount = pageCount;
        this.publisher = publisher;
        this.subjects = subjects;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public int getSearchOrder() { return searchOrder; }
    public void setSearchOrder(int searchOrder) { this.searchOrder = searchOrder; }

    public int getPublishYear() { return publishYear; }
    public void setPublishYear(int publishYear) { this.publishYear = publishYear; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }
}