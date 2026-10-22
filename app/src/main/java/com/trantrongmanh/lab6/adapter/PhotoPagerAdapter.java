package com.trantrongmanh.lab6.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trantrongmanh.lab6.R;
import com.trantrongmanh.lab6.model.Photo;

import java.util.List;

/**
 * Adapter for ViewPager2 in PhotoDetailActivity - enables swiping between photos.
 */
public class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PageViewHolder> {

    private final Context context;
    private final List<Photo> photoList;

    public PhotoPagerAdapter(Context context, List<Photo> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo_full, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        Photo photo = photoList.get(position);

        holder.tvTitle.setText(photo.getTitle());
        holder.tvDescription.setText(photo.getDescription());
        holder.tvCategory.setText(photo.getCategory());

        Glide.with(context)
                .load(photo.getImageUrl())
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivPhotoFull);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhotoFull;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvCategory;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhotoFull = itemView.findViewById(R.id.ivPhotoFull);
            tvTitle = itemView.findViewById(R.id.tvDetailTitle);
            tvDescription = itemView.findViewById(R.id.tvDetailDescription);
            tvCategory = itemView.findViewById(R.id.tvDetailCategory);
        }
    }
}
