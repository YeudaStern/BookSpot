package com.example.bookspot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book);
        void onFavoriteClick(Book book);
    }

    private List<Book> books = new ArrayList<>();
    private final OnBookClickListener listener;
    private boolean isGridMode = false;

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        if (books != null) {
            this.books = books;
        }
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setGridMode(boolean isGridMode) {
        this.isGridMode = isGridMode;
        notifyDataSetChanged();
    }

    public void setBooks(List<Book> newBooks) {
        this.books = (newBooks != null) ? new ArrayList<>(newBooks) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (books != null && position < books.size() && books.get(position).getId() != null) {
            return books.get(position).getId().hashCode();
        }
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? 1 : 0;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == 1) ? R.layout.item_book_grid : R.layout.item_book;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);

        if (holder.tvBookTitle != null) {
            holder.tvBookTitle.setText(book.getTitle());
        }

        if (holder.tvBookAuthor != null) {
            holder.tvBookAuthor.setText(book.getAuthor());
        }

        if (holder.ivBookCover != null) {
            String imageUrl = book.getCoverImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.ivBookCover);
            } else {
                Glide.with(holder.itemView.getContext()).clear(holder.ivBookCover);
                holder.ivBookCover.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        if (holder.ivFavorite != null) {
            // עדכון המראה לפי מצב המועדף
            holder.ivFavorite.setImageResource(
                    book.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_border
            );

            // לחיצה ייעודית על הכוכב
            holder.ivFavorite.setOnClickListener(v -> {
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction(() ->
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                ).start();

                if (listener != null) {
                    listener.onFavoriteClick(book);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle;
        TextView tvBookAuthor;
        ImageView ivFavorite;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}