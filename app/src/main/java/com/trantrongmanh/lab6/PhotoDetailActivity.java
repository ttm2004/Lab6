package com.trantrongmanh.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.trantrongmanh.lab6.adapter.PhotoPagerAdapter;
import com.trantrongmanh.lab6.model.Photo;
import com.trantrongmanh.lab6.utils.AnimUtil;
import com.trantrongmanh.lab6.utils.PhotoStore;

import java.util.List;

/**
 * Full-screen photo viewer with swipe navigation.
 *
 * Demonstrates:
 * - ViewPager2: swipe between photos
 * - Event programming: OnPageChangeCallback, OnClickListener
 * - Intent: receives photo_id via extras (explicit), share via implicit (ACTION_SEND)
 * - Material Design: Toolbar, AlertDialog, Snackbar
 * - Animation: page transition
 */
public class PhotoDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private PhotoPagerAdapter pagerAdapter;
    private List<Photo> photoList;
    private PhotoStore photoStore;
    private TextView tvCounter;
    private ImageButton btnFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        photoStore = PhotoStore.getInstance();

        // Get photo_id and position from Intent
        String photoId = getIntent().getStringExtra("photo_id");
        int initialPosition = getIntent().getIntExtra("position", 0);

        // Load all photos (or filtered list depending on what was shown)
        photoList = photoStore.getAllPhotos();

        initViews();
        setupViewPager(initialPosition);
        setupListeners();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }
        viewPager = findViewById(R.id.viewPager);
        tvCounter = findViewById(R.id.tvCounter);
        btnFavorite = findViewById(R.id.btnFavorite);
    }

    private void setupViewPager(int initialPosition) {
        pagerAdapter = new PhotoPagerAdapter(this, photoList);
        viewPager.setAdapter(pagerAdapter);

        // Set initial position
        if (initialPosition >= 0 && initialPosition < photoList.size()) {
            viewPager.setCurrentItem(initialPosition, false);
        }

        // Update counter and favorite icon on page change (event programming)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
            }
        });

        // Set initial state
        updateUI(initialPosition);
    }

    private void updateUI(int position) {
        if (position >= 0 && position < photoList.size()) {
            Photo photo = photoList.get(position);
            tvCounter.setText((position + 1) + " / " + photoList.size());

            // Update toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(photo.getTitle());
            }

            // Update favorite icon
            btnFavorite.setImageResource(
                    photo.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }
    }

    private void setupListeners() {
        // Favorite toggle (OnClickListener - event programming)
        btnFavorite.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos >= 0 && pos < photoList.size()) {
                Photo photo = photoList.get(pos);
                photoStore.toggleFavorite(photo.getId());
                updateUI(pos);
                Snackbar.make(viewPager,
                        photo.isFavorite() ? R.string.removed_from_favorites : R.string.added_to_favorites,
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        // Share (OnClickListener - event programming, implicit Intent)
        ImageButton btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos >= 0 && pos < photoList.size()) {
                Photo photo = photoList.get(pos);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, photo.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "Xem ảnh này: " + photo.getTitle() + "\n" + photo.getImageUrl());
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
            }
        });

        // Delete (OnClickListener - event programming, AlertDialog)
        ImageButton btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_delete_title)
                    .setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        int pos = viewPager.getCurrentItem();
                        if (pos >= 0 && pos < photoList.size()) {
                            Photo photo = photoList.get(pos);
                            photoStore.deletePhoto(photo.getId());
                            photoList.remove(pos);
                            pagerAdapter.notifyItemRemoved(pos);
                            if (photoList.isEmpty()) {
                                Snackbar.make(viewPager, R.string.photo_deleted, Snackbar.LENGTH_SHORT).show();
                                finish();
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            } else {
                                updateUI(viewPager.getCurrentItem());
                                Snackbar.make(viewPager, R.string.photo_deleted, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
