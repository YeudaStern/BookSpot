package com.example.bookspot;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GoogleBooksResponse {

    @SerializedName("items")
    private List<BookItem> items;

    public List<BookItem> getItems() {
        return items;
    }

    public static class BookItem {
        @SerializedName("id")
        private String id;

        @SerializedName("volumeInfo")
        private VolumeInfo volumeInfo;

        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
    }

    public static class VolumeInfo {
        @SerializedName("title")
        private String title;

        @SerializedName("authors")
        private List<String> authors;

        @SerializedName("description")
        private String description;

        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        @SerializedName("averageRating")
        private Double averageRating;

        @SerializedName("publishedDate")
        private String publishedDate;

        @SerializedName("pageCount")
        private Integer pageCount;

        @SerializedName("publisher")
        private String publisher;

        @SerializedName("categories")
        private List<String> categories;

        public String getTitle() { return title != null ? title : "Untitled"; }
        public List<String> getAuthors() { return authors; }
        public String getDescription() { return description != null ? description : "No description available."; }
        public ImageLinks getImageLinks() { return imageLinks; }
        public Double getAverageRating() { return averageRating != null ? averageRating : 0.0; }
        public String getPublishedDate() { return publishedDate; }
        public Integer getPageCount() { return pageCount != null ? pageCount : 0; }
        public String getPublisher() { return publisher != null ? publisher : "Unknown Publisher"; }
        public List<String> getCategories() { return categories; }
    }

    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;

        public String getThumbnail() {
            if (thumbnail != null && thumbnail.startsWith("http://")) {
                return thumbnail.replace("http://", "https://");
            }
            return thumbnail;
        }
    }
}