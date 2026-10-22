package com.trantrongmanh.lab6.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trantrongmanh.lab6.R;
import com.trantrongmanh.lab6.model.Photo;
import com.trantrongmanh.lab6.utils.AnimUtil;

import java.util.List;

/**
 * Adapter for displaying photos in a RecyclerView grid (gallery view).
 * Supports click and long-click listeners, favorite toggle, and image loading via Glide.
 * Registers ContextMenu on each item for long-press actions.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context context;
    private List<Photo> photoList;
    private final OnPhotoClickListener listener;

    /** Position of the item that was long-pressed (for ContextMenu handling). */
    private int contextMenuPosition = -1;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo, int position);
        void onFavoriteClick(Photo photo, int position);
    }

    public PhotoAdapter(Context context, List<Photo> photoList, OnPhotoClickListener listener) {
        this.context = context;
        this.photoList = photoList;
        this.listener = listener;
    }

    public void updateData(List<Photo> newList) {
        this.photoList = newList;
        notifyDataSetChanged();
    }

    public int getContextMenuPosition() {
        return contextMenuPosition;
    }

    public Photo getPhotoAt(int position) {
        if (position >= 0 && position < photoList.size()) {
            return photoList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photoList.get(position);

        holder.tvTitle.setText(photo.getTitle());
        holder.tvCategory.setText(photo.getCategory());

        // Load image with Glide
        Glide.with(context)
                .load(photo.getImageUrl())
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.ivPhoto);

        // Favorite icon state
        holder.btnFavorite.setImageResource(
                photo.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            listener.onPhotoClick(photo, holder.getBindingAdapterPosition());
        });

        // Long click: track position, return false to let ContextMenu show
        holder.itemView.setOnLongClickListener(v -> {
            contextMenuPosition = holder.getBindingAdapterPosition();
            return false;
        });

        holder.btnFavorite.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            listener.onFavoriteClick(photo, holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public Photo getItem(int position) {
        return photoList.get(position);
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvTitle;
        TextView tvCategory;
        ImageButton btnFavorite;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            // Register for ContextMenu - menu is inflated in the Activity's onCreateContextMenu
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                MenuInflater inflater = new MenuInflater(itemView.getContext());
                inflater.inflate(R.menu.context_menu, menu);
            });
        }
    }
}
