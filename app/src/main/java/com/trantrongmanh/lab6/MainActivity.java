package com.trantrongmanh.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.trantrongmanh.lab6.adapter.PhotoAdapter;
import com.trantrongmanh.lab6.model.Photo;
import com.trantrongmanh.lab6.utils.AnimUtil;
import com.trantrongmanh.lab6.utils.FirebaseAuthHelper;
import com.trantrongmanh.lab6.utils.PhotoStore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Main gallery screen - the heart of the photo management app.
 *
 * Demonstrates:
 * - Options Menu: SearchView, Sort submenu, Filter submenu, View toggle, Settings
 * - Context Menu: View, Share, Favorite/Unfavorite, Set as Wallpaper, Delete
 * - SubMenu: within Sort and Filter items
 * - Navigation bar: NavigationView drawer (Home, Favorites, Profile, Settings, Logout)
 * - Event programming: OnClickListener, OnContextItemSelected, OnQueryTextListener,
 *   OnNavigationItemSelected, OnRefreshListener
 * - Intent: explicit (PhotoDetailActivity, AddPhotoActivity, ProfileActivity, LoginActivity),
 *   implicit (share via ACTION_SEND)
 * - Material Design: Toolbar, FAB, NavigationView, Snackbar, SwipeRefreshLayout
 * - RecyclerView with GridLayoutManager (grid/list toggle)
 * - Animations: FAB scale, item stagger
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_PHOTO = 1001;

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private MaterialToolbar toolbar;
    private RecyclerView rvPhotos;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAdd;
    private View emptyState;

    // Data
    private PhotoAdapter adapter;
    private PhotoStore photoStore;
    private List<Photo> currentList;
    private boolean isGridView = true;
    private boolean showFavoritesOnly = false;
    private String currentFilter = "All";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        photoStore = PhotoStore.getInstance();

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFAB();
        updateNavigationHeader();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.toolbar);
        rvPhotos = findViewById(R.id.rvPhotos);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabAdd = findViewById(R.id.fabAdd);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void setupNavigationDrawer() {
        // Navigation item selection listener (event programming)
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFavoritesOnly = false;
                toolbar.setTitle(R.string.gallery_title);
                loadPhotos();
            } else if (id == R.id.nav_favorites) {
                showFavoritesOnly = true;
                toolbar.setTitle(R.string.favorites_title);
                loadPhotos();
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.nav_settings) {
                Snackbar.make(rvPhotos, R.string.menu_settings, Snackbar.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                confirmLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void updateNavigationHeader() {
        View headerView = navView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.navUserName);
        TextView tvEmail = headerView.findViewById(R.id.navUserEmail);
        CircleImageView ivAvatar = headerView.findViewById(R.id.navAvatar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvName.setText(user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                    ? user.getDisplayName() : "Người dùng");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa có email");
        }
    }

    private void setupRecyclerView() {
        currentList = photoStore.getAllPhotos();
        adapter = new PhotoAdapter(this, currentList, new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(Photo photo, int position) {
                // Explicit Intent to PhotoDetailActivity
                Intent intent = new Intent(MainActivity.this, PhotoDetailActivity.class);
                intent.putExtra("photo_id", photo.getId());
                intent.putExtra("position", position);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }

            @Override
            public void onFavoriteClick(Photo photo, int position) {
                photoStore.toggleFavorite(photo.getId());
                loadPhotos();
                Snackbar.make(rvPhotos,
                        photo.isFavorite() ? R.string.removed_from_favorites : R.string.added_to_favorites,
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        // GridLayoutManager with 2 columns (grid view)
        rvPhotos.setLayoutManager(new GridLayoutManager(this, 2));
        rvPhotos.setAdapter(adapter);

        // Staggered entrance animation
        rvPhotos.post(() -> {
            for (int i = 0; i < rvPhotos.getChildCount(); i++) {
                View child = rvPhotos.getChildAt(i);
                AnimUtil.slideInFromRight(child, 400, i * 50L);
            }
        });
    }

    private void setupSwipeRefresh() {
        // OnRefreshListener (event programming)
        swipeRefresh.setOnRefreshListener(() -> {
            // Simulate refresh
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                loadPhotos();
                swipeRefresh.setRefreshing(false);
                Snackbar.make(rvPhotos, "Đã làm mới thư viện", Snackbar.LENGTH_SHORT).show();
            }, 1000);
        });
    }

    private void setupFAB() {
        // OnClickListener (event programming)
        fabAdd.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            // Explicit Intent to AddPhotoActivity
            Intent intent = new Intent(MainActivity.this, AddPhotoActivity.class);
            startActivityForResult(intent, REQUEST_ADD_PHOTO);
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
        });
    }

    /**
     * Load photos based on current filters: favorites-only, category filter, search query.
     */
    private void loadPhotos() {
        List<Photo> list;

        if (showFavoritesOnly) {
            list = photoStore.getFavoritePhotos();
        } else if (searchQuery != null && !searchQuery.isEmpty()) {
            list = photoStore.searchPhotos(searchQuery);
        } else if (!"All".equals(currentFilter)) {
            list = photoStore.getPhotosByCategory(currentFilter);
        } else {
            list = photoStore.getAllPhotos();
        }

        currentList = list;
        adapter.updateData(list);

        // Show/hide empty state
        if (list.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            TextView tvEmpty = findViewById(R.id.tvEmptyText);
            tvEmpty.setText(showFavoritesOnly
                    ? R.string.empty_favorites : R.string.empty_gallery);
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

    // ─── Options Menu ───────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        // Setup SearchView (event programming: OnQueryTextListener)
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                loadPhotos();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Toolbar hamburger -> open drawer
        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        // Sort options (SubMenu)
        if (id == R.id.sort_date) {
            currentList = photoStore.getSortedPhotos("date", false);
            adapter.updateData(currentList);
            Snackbar.make(rvPhotos, R.string.menu_sort_date, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.sort_name) {
            currentList = photoStore.getSortedPhotos("name", true);
            adapter.updateData(currentList);
            Snackbar.make(rvPhotos, R.string.menu_sort_name, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.sort_favorite) {
            currentList = photoStore.getSortedPhotos("favorite", false);
            adapter.updateData(currentList);
            Snackbar.make(rvPhotos, R.string.menu_sort_favorite, Snackbar.LENGTH_SHORT).show();
            return true;
        }

        // Filter options (SubMenu)
        if (id == R.id.filter_all) {
                currentFilter = "All";
                showFavoritesOnly = false;
                loadPhotos();
                return true;
            }
            if (id == R.id.filter_nature) {
                currentFilter = "Thiên nhiên";
                showFavoritesOnly = false;
                loadPhotos();
                return true;
            }
            if (id == R.id.filter_urban) {
                currentFilter = "Đô thị";
                showFavoritesOnly = false;
                loadPhotos();
                return true;
            }

        // View toggle (SubMenu)
        if (id == R.id.view_grid) {
            isGridView = true;
            rvPhotos.setLayoutManager(new GridLayoutManager(this, 2));
            Snackbar.make(rvPhotos, R.string.menu_view_grid, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.view_list) {
            isGridView = false;
            rvPhotos.setLayoutManager(new GridLayoutManager(this, 1));
            Snackbar.make(rvPhotos, R.string.menu_view_list, Snackbar.LENGTH_SHORT).show();
            return true;
        }

        // Settings
        if (id == R.id.action_settings) {
            Snackbar.make(rvPhotos, R.string.menu_settings, Snackbar.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ─── Context Menu ───────────────────────────────────────────

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = adapter.getContextMenuPosition();
        Photo photo = adapter.getPhotoAt(position);
        if (photo == null) return false;

        int id = item.getItemId();

        if (id == R.id.ctx_view) {
            // Explicit Intent to PhotoDetailActivity
            Intent intent = new Intent(this, PhotoDetailActivity.class);
            intent.putExtra("photo_id", photo.getId());
            intent.putExtra("position", position);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return true;
        }

        if (id == R.id.ctx_share) {
            // Implicit Intent: ACTION_SEND for sharing
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, photo.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Xem ảnh này: " + photo.getTitle() + "\n" + photo.getImageUrl());
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
            return true;
        }

        if (id == R.id.ctx_favorite) {
            photoStore.toggleFavorite(photo.getId());
            loadPhotos();
            Snackbar.make(rvPhotos,
                    photo.isFavorite() ? R.string.removed_from_favorites : R.string.added_to_favorites,
                    Snackbar.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.ctx_wallpaper) {
            Snackbar.make(rvPhotos, "Đã đặt làm hình nền (demo)", Snackbar.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.ctx_delete) {
            confirmDelete(photo);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void confirmDelete(Photo photo) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    photoStore.deletePhoto(photo.getId());
                    loadPhotos();
                    Snackbar.make(rvPhotos, R.string.photo_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    // ─── Logout ────────────────────────────────────────────────

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    FirebaseAuthHelper authHelper = new FirebaseAuthHelper();
                    authHelper.logout(this, LoginActivity.class);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    // ─── Activity Result from AddPhotoActivity ───────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_PHOTO && resultCode == RESULT_OK) {
            loadPhotos();
            Snackbar.make(rvPhotos, R.string.photo_added, Snackbar.LENGTH_SHORT).show();
        }
    }

    // ─── Back press handling (close drawer first) ──────────────

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from other activities
        loadPhotos();
        updateNavigationHeader();
    }
}
