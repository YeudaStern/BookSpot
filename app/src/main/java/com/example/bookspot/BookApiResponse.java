package com.example.bookspot;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookApiResponse {

    @SerializedName("docs")
    private List<BookDoc> docs;

    public List<BookDoc> getDocs() {
        return docs;
    }

    public static class BookDoc {
        @SerializedName("key")
        private String key;

        @SerializedName("title")
        private String title;

        @SerializedName("author_name")
        private List<String> authorNames;

        @SerializedName("cover_i")
        private Integer coverId;

        @SerializedName("ratings_average")
        private Double ratingsAverage;

        @SerializedName("first_publish_year")
        private Integer firstPublishYear;

        @SerializedName("number_of_pages_median")
        private Integer pageCount;

        @SerializedName("publisher")
        private List<String> publishers;

        @SerializedName("subject")
        private List<String> subjects;

        public String getKey() { return key; }
        public String getTitle() { return title; }
        public List<String> getAuthorNames() { return authorNames; }
        public Integer getCoverId() { return coverId; }
        public Double getRatingsAverage() { return ratingsAverage; }
        public Integer getFirstPublishYear() { return firstPublishYear; }
        public Integer getPageCount() { return pageCount; }
        public List<String> getPublishers() { return publishers; }
        public List<String> getSubjects() { return subjects; }
    }
}