package com.trantrongmanh.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.trantrongmanh.lab6.utils.AnimUtil;
import com.trantrongmanh.lab6.utils.FirebaseAuthHelper;
import com.trantrongmanh.lab6.utils.PhotoStore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * User Profile screen showing Firebase user information.
 *
 * Demonstrates:
 * - Firebase: FirebaseUser.getDisplayName/getEmail/getUid/isEmailVerified
 * - Event programming: OnClickListener for edit/logout
 * - Intent: explicit Intent back to LoginActivity on logout
 * - Material Design: CircleImageView, MaterialCardView stats, MaterialButton
 * - Animation: staggered entrance
 * - PhotoStore integration for stats (total photos, favorites)
 */
public class ProfileActivity extends AppCompatActivity {

    private CircleImageView ivAvatar;
    private TextView tvDisplayName, tvEmail, tvUid, tvEmailVerified, tvLastSignIn;
    private TextView tvTotalPhotos, tvFavCount, tvMemberYear;
    private MaterialButton btnEditProfile, btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupToolbar();
        loadUserInfo();
        loadStats();
        setupListeners();
        applyAnimations();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvEmail = findViewById(R.id.tvEmail);
        tvUid = findViewById(R.id.tvUid);
        tvEmailVerified = findViewById(R.id.tvEmailVerified);
        tvLastSignIn = findViewById(R.id.tvLastSignIn);
        tvTotalPhotos = findViewById(R.id.tvTotalPhotos);
        tvFavCount = findViewById(R.id.tvFavCount);
        tvMemberYear = findViewById(R.id.tvMemberYear);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Not logged in, redirect
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Display name
        String name = user.getDisplayName();
        tvDisplayName.setText(name != null && !name.isEmpty() ? name : "Người dùng");

        // Email
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa có email");

        // User ID (truncated for display)
        String uid = user.getUid();
        tvUid.setText(uid.length() > 20 ? uid.substring(0, 20) + "..." : uid);

        // Email verified
        boolean verified = user.isEmailVerified();
        tvEmailVerified.setText(verified ? "Đã xác thực" : "Chưa xác thực");
        tvEmailVerified.setTextColor(verified ? getColor(R.color.success) : getColor(R.color.error));

        // Last sign in date
        if (user.getMetadata() != null && user.getMetadata().getLastSignInTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvLastSignIn.setText(sdf.format(new Date(user.getMetadata().getLastSignInTimestamp())));
        } else {
            tvLastSignIn.setText("Vừa xong");
        }
    }

    private void loadStats() {
        PhotoStore store = PhotoStore.getInstance();
        tvTotalPhotos.setText(String.valueOf(store.getTotalCount()));
        tvFavCount.setText(String.valueOf(store.getFavoriteCount()));

        // Show member year
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getMetadata() != null && user.getMetadata().getCreationTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
            tvMemberYear.setText(sdf.format(new Date(user.getMetadata().getCreationTimestamp())));
        } else {
            tvMemberYear.setText("--");
        }
    }

    private void setupListeners() {
        // Edit profile (demonstrates updating Firebase profile)
        btnEditProfile.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                // Toggle display name as a demo edit
                String currentName = user.getDisplayName();
                String newName = (currentName == null || currentName.isEmpty()) ? "Người dùng" : currentName;
                UserProfileChangeRequest updates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName + " ✓")
                        .build();
                user.updateProfile(updates)
                        .addOnSuccessListener(unused -> {
                            loadUserInfo();
                            Snackbar.make(v, "Cập nhật hồ sơ thành công", Snackbar.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Snackbar.make(v, "Cập nhật thất bại: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
            }
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            confirmLogout();
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Có", (d, w) -> {
                    FirebaseAuthHelper helper = new FirebaseAuthHelper();
                    helper.logout(this, LoginActivity.class);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void applyAnimations() {
        AnimUtil.fadeIn(findViewById(R.id.headerSection), 500);
        AnimUtil.slideInFromRight(findViewById(R.id.statsSection), 500, 200);
        AnimUtil.fadeIn(btnEditProfile, 500);
        AnimUtil.fadeIn(btnLogout, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
