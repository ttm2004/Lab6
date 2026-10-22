package com.trantrongmanh.lab6;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trantrongmanh.lab6.model.Photo;
import com.trantrongmanh.lab6.utils.AnimUtil;
import com.trantrongmanh.lab6.utils.PhotoStore;
import com.trantrongmanh.lab6.utils.SimpleTextWatcher;

/**
 * Add Photo screen - allows adding a new photo via URL or picking from device gallery.
 *
 * Demonstrates:
 * - Event programming: OnClickListener, TextWatcher, ActivityResultLauncher
 * - Intent: implicit (ACTION_PICK / GET_CONTENT for gallery image selection)
 * - Material Design: TextInputLayout, MaterialButton, AutoCompleteTextView
 * - Glide: image loading from URL and content URI
 * - Input validation
 */
public class AddPhotoActivity extends AppCompatActivity {

    private TextInputLayout tilTitle, tilDescription, tilUrl;
    private TextInputEditText etTitle, etDescription, etUrl;
    private AutoCompleteTextView actvCategory;
    private ImageView ivPreview;
    private MaterialButton btnAddPhoto, btnPickGallery;

    private String selectedImageUrl = null;

    // ActivityResultLauncher for picking image from gallery (modern approach)
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImageUrl = imageUri.toString();
                        Glide.with(this).load(imageUri).into(ivPreview);
                        // Auto-fill URL field with URI
                        etUrl.setText(selectedImageUrl);
                        AnimUtil.scaleUp(ivPreview, 400);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        initViews();
        setupCategoryDropdown();
        setupListeners();
    }

    private void initViews() {
        tilTitle = findViewById(R.id.tilTitle);
        tilDescription = findViewById(R.id.tilDescription);
        tilUrl = findViewById(R.id.tilUrl);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etUrl = findViewById(R.id.etUrl);
        actvCategory = findViewById(R.id.actvCategory);
        ivPreview = findViewById(R.id.ivPreview);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnPickGallery = findViewById(R.id.btnPickGallery);

        // Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Thiên nhiên", "Đô thị", "Động vật", "Ẩm thực", "Con người", "Du lịch", "Nghệ thuật"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
        actvCategory.setText("Thiên nhiên", false);
    }

    private void setupListeners() {
        // Add photo button (OnClickListener - event programming)
        btnAddPhoto.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            attemptAddPhoto();
        });

        // Pick from gallery (OnClickListener - event programming, implicit Intent)
        btnPickGallery.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryLauncher.launch(galleryIntent);
        });

        // URL text watcher - preview image as user types
        etUrl.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    selectedImageUrl = url;
                    Glide.with(AddPhotoActivity.this)
                            .load(url)
                            .placeholder(R.drawable.ic_photo_placeholder)
                            .error(R.drawable.ic_photo_placeholder)
                            .into(ivPreview);
                }
                tilUrl.setError(null);
            }
        });

        // Clear errors on text change
        etTitle.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilTitle.setError(null);
            }
        });
    }

    private void attemptAddPhoto() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String url = etUrl.getText() != null ? etUrl.getText().toString().trim() : "";
        String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "Nature";

        // Validation
        if (TextUtils.isEmpty(title)) {
            tilTitle.setError("Please enter a title");
            return;
        }
        if (TextUtils.isEmpty(url)) {
            tilUrl.setError("Please enter an image URL or pick from gallery");
            return;
        }

        // Create photo and add to store
        Photo photo = new Photo(null, title, description.isEmpty() ? "Chưa có mô tả" : description,
                url, category, System.currentTimeMillis(), false);
        PhotoStore.getInstance().addPhoto(photo);

        // Return result to MainActivity
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        Snackbar.make(btnAddPhoto, R.string.photo_added, Snackbar.LENGTH_SHORT).show();

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            finish();
            overridePendingTransition(R.anim.slide_out_bottom, R.anim.fade_in);
        }, 800);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_out_bottom, R.anim.fade_in);
    }
}
