package com.example.bookspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageButton btnSearch;
    private ImageButton btnVoiceSearch;
    private ImageButton btnToggleLayout;
    private ImageButton btnToggleTheme;
    private ImageButton btnFilterFavorites;
    private ImageButton btnProfile;
    private LinearLayout layoutLoadingAnimation;
    private ImageView ivAnimatedBook;
    private RecyclerView rvBooks;

    private Button chipTech, chipFiction, chipScience, chipHistory, chipPsychology;

    private BookAdapter bookAdapter;
    private BookRepository bookRepository;
    private AppDatabase db;
    private SharedPreferences preferences;

    private boolean isGridMode = false;
    private boolean isShowingFavoritesOnly = false;
    private List<Book> currentBookList = new ArrayList<>();

    // ה-Launcher לקבלת תוצאת זיהוי הדיבור
    private final ActivityResultLauncher<Intent> voiceSearchLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        etSearch.setText(spokenText);
                        performSearch(spokenText);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        int targetMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);
        bookRepository = new BookRepository(getApplication());

        initViews();
        setupRecyclerView();
        setupObservers();
        setupListeners();
        setupChips();

        if (savedInstanceState != null && savedInstanceState.containsKey("saved_books")) {
            @SuppressWarnings("unchecked")
            ArrayList<Book> restored = (ArrayList<Book>) savedInstanceState.getSerializable("saved_books");
            if (restored != null && !restored.isEmpty()) {
                currentBookList = restored;
                bookAdapter.setBooks(currentBookList);
                return;
            }
        }

        performSearch("Technology");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentBookList != null && !currentBookList.isEmpty()) {
            outState.putSerializable("saved_books", new ArrayList<>(currentBookList));
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnVoiceSearch = findViewById(R.id.btnVoiceSearch);
        btnToggleLayout = findViewById(R.id.btnToggleLayout);
        btnToggleTheme = findViewById(R.id.btnToggleTheme);
        btnFilterFavorites = findViewById(R.id.btnFilterFavorites);
        btnProfile = findViewById(R.id.btnProfile);
        layoutLoadingAnimation = findViewById(R.id.layoutLoadingAnimation);
        ivAnimatedBook = findViewById(R.id.ivAnimatedBook);
        rvBooks = findViewById(R.id.rvBooks);

        chipTech = findViewById(R.id.chipTech);
        chipFiction = findViewById(R.id.chipFiction);
        chipScience = findViewById(R.id.chipScience);
        chipHistory = findViewById(R.id.chipHistory);
        chipPsychology = findViewById(R.id.chipPsychology);

        btnToggleTheme.setImageResource(R.drawable.ic_mode_night);
        btnFilterFavorites.setImageResource(R.drawable.ic_star_border);
        btnToggleLayout.setImageResource(R.drawable.ic_grid_view);
        btnProfile.setImageResource(R.drawable.ic_user_avatar);
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(currentBookList, new BookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("book_id", book.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Book book) {
                toggleFavorite(book);
            }
        });

        bookAdapter.setGridMode(isGridMode);
        rvBooks.setLayoutManager(isGridMode ? new GridLayoutManager(this, 2) : new LinearLayoutManager(this));
        rvBooks.setAdapter(bookAdapter);
    }

    private void setupObservers() {
        // מאזינים רק למועדפים כשמצב סינון פעיל
        bookRepository.getFavoriteBooks().observe(this, favorites -> {
            if (favorites != null && isShowingFavoritesOnly) {
                bookAdapter.setBooks(favorites);
            }
        });
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> executeSearchFromInput());

        btnVoiceSearch.setOnClickListener(v -> promptSpeechInput());

        btnToggleLayout.setOnClickListener(v -> {
            isGridMode = !isGridMode;
            bookAdapter.setGridMode(isGridMode);
            rvBooks.setLayoutManager(isGridMode ? new GridLayoutManager(MainActivity.this, 2) : new LinearLayoutManager(MainActivity.this));
        });

        btnToggleTheme.setOnClickListener(v -> {
            boolean currentMode = preferences.getBoolean("dark_mode", false);
            boolean newMode = !currentMode;
            preferences.edit().putBoolean("dark_mode", newMode).apply();
            AppCompatDelegate.setDefaultNightMode(
                    newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // כפתור מועדפים עליון - מתג מעבר בין רשימה מלאה למועדפים
        btnFilterFavorites.setOnClickListener(v -> {
            isShowingFavoritesOnly = !isShowingFavoritesOnly;
            if (isShowingFavoritesOnly) {
                btnFilterFavorites.setImageResource(R.drawable.ic_star_filled);
                loadFavoritesOnly();
            } else {
                btnFilterFavorites.setImageResource(R.drawable.ic_star_border);
                bookAdapter.setBooks(currentBookList);
            }
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the book title or author...");
        try {
            voiceSearchLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice search is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFavoritesOnly() {
        showLoading(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Book> favs = db.bookDao().getAllFavoriteBooksSync();
            runOnUiThread(() -> {
                showLoading(false);
                if (favs != null && !favs.isEmpty()) {
                    bookAdapter.setBooks(favs);
                } else {
                    bookAdapter.setBooks(new ArrayList<>());
                    Toast.makeText(MainActivity.this, "No favorites yet", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void toggleFavorite(Book book) {
        boolean newState = !book.isFavorite();
        book.setFavorite(newState);
        bookAdapter.notifyDataSetChanged();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.bookDao().insertBook(book);

            if (isShowingFavoritesOnly) {
                List<Book> updatedFavs = db.bookDao().getAllFavoriteBooksSync();
                runOnUiThread(() -> bookAdapter.setBooks(updatedFavs));
            }
        });
    }

    private void setupChips() {
        View.OnClickListener chipListener = v -> {
            Button btn = (Button) v;
            String term = btn.getText().toString();
            etSearch.setText(term);
            performSearch(term);
        };

        chipTech.setOnClickListener(chipListener);
        chipFiction.setOnClickListener(chipListener);
        chipScience.setOnClickListener(chipListener);
        chipHistory.setOnClickListener(chipListener);
        chipPsychology.setOnClickListener(chipListener);
    }

    private void executeSearchFromInput() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(query)) {
            isShowingFavoritesOnly = false;
            btnFilterFavorites.setImageResource(R.drawable.ic_star_border);
            performSearch(query);
        } else {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            rvBooks.setVisibility(View.GONE);
            layoutLoadingAnimation.setVisibility(View.VISIBLE);

            RotateAnimation rotate = new RotateAnimation(
                    -15, 15,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(450);
            rotate.setRepeatMode(Animation.REVERSE);
            rotate.setRepeatCount(Animation.INFINITE);
            ivAnimatedBook.startAnimation(rotate);
        } else {
            ivAnimatedBook.clearAnimation();
            layoutLoadingAnimation.setVisibility(View.GONE);
            rvBooks.setVisibility(View.VISIBLE);
        }
    }

    private void performSearch(String query) {
        isShowingFavoritesOnly = false;
        btnFilterFavorites.setImageResource(R.drawable.ic_star_border);
        showLoading(true);

        bookRepository.fetchBooksFromApi(query, new BookRepository.OnBooksLoadedListener() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                currentBookList = books;
                bookAdapter.setBooks(books);
                rvBooks.scrollToPosition(0);
            }

            @Override
            public void onComplete() {
                runOnUiThread(() -> showLoading(false));
            }
        });
    }
}