package com.example.bookspot;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView ivDetailCover;
    private ImageButton btnBack;
    private ImageButton btnDetailFavorite;
    private TextView tvDetailTitle;
    private TextView tvDetailAuthor;
    private TextView tvDetailRating;
    private TextView tvDetailPageCount;
    private TextView tvDetailYear;
    private TextView tvDetailPublisher;
    private TextView tvDetailCategories;
    private TextView tvDetailDescription;

    private AppDatabase db;
    private Book currentBook;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        db = AppDatabase.getDatabase(this);
        bookId = getIntent().getStringExtra("book_id");

        initViews();
        loadBookDetails();
    }

    private void initViews() {
        ivDetailCover = findViewById(R.id.ivDetailCover);
        btnBack = findViewById(R.id.btnBack);
        btnDetailFavorite = findViewById(R.id.btnDetailFavorite);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailAuthor = findViewById(R.id.tvDetailAuthor);
        tvDetailRating = findViewById(R.id.tvDetailRating);
        tvDetailPageCount = findViewById(R.id.tvDetailPageCount);
        tvDetailYear = findViewById(R.id.tvDetailYear);
        tvDetailPublisher = findViewById(R.id.tvDetailPublisher);
        tvDetailCategories = findViewById(R.id.tvDetailCategories);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);

        btnBack.setOnClickListener(v -> finish());

        btnDetailFavorite.setOnClickListener(v -> {
            if (currentBook != null) {
                toggleFavorite();
            }
        });
    }

    private void loadBookDetails() {
        if (TextUtils.isEmpty(bookId)) {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentBook = db.bookDao().getBookById(bookId);
            if (currentBook != null) {
                runOnUiThread(this::bindBookData);
            }
        });
    }

    private void bindBookData() {
        tvDetailTitle.setText(currentBook.getTitle());
        tvDetailAuthor.setText(currentBook.getAuthor());

        tvDetailRating.setText(currentBook.getRating() > 0 ? (currentBook.getRating() + " ★") : "N/A");
        tvDetailPageCount.setText(currentBook.getPageCount() > 0 ? String.valueOf(currentBook.getPageCount()) : "-");
        tvDetailYear.setText(currentBook.getPublishYear() > 0 ? String.valueOf(currentBook.getPublishYear()) : "-");

        tvDetailPublisher.setText(!TextUtils.isEmpty(currentBook.getPublisher()) ? ("Publisher: " + currentBook.getPublisher()) : "Publisher: Unknown");
        tvDetailCategories.setText(!TextUtils.isEmpty(currentBook.getSubjects()) ? ("Category: " + currentBook.getSubjects()) : "Category: General");

        tvDetailDescription.setText(!TextUtils.isEmpty(currentBook.getDescription()) ? currentBook.getDescription() : "No detailed description available for this book.");

        if (!TextUtils.isEmpty(currentBook.getCoverImageUrl())) {
            Glide.with(this)
                    .load(currentBook.getCoverImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivDetailCover);
        }

        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (currentBook != null) {
            btnDetailFavorite.setImageResource(
                    currentBook.isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
            );
        }
    }

    private void toggleFavorite() {
        boolean newState = !currentBook.isFavorite();
        currentBook.setFavorite(newState);
        updateFavoriteIcon();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (newState) {
                db.bookDao().insertBook(currentBook);
            } else {
                db.bookDao().deleteBook(currentBook);
            }
        });
    }
}