package com.trantrongmanh.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.trantrongmanh.lab6.utils.AnimUtil;
import com.trantrongmanh.lab6.utils.FirebaseAuthHelper;
import com.trantrongmanh.lab6.utils.SimpleTextWatcher;

import java.util.regex.Pattern;

/**
 * Register screen with Firebase Authentication.
 *
 * Demonstrates:
 * - Event programming: OnClickListener, TextWatcher
 * - Intent: explicit intent to LoginActivity / MainActivity
 * - Material Design UI components
 * - Input validation with real-time error clearing
 * - Firebase: createUserWithEmailAndPassword, profile update
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuthHelper authHelper;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        authHelper = new FirebaseAuthHelper();

        View cardForm = findViewById(R.id.cardForm);
        AnimUtil.scaleUp(cardForm, 600);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            attemptRegister();
        });

        TextView tvLoginHere = findViewById(R.id.tvLoginHere);
        tvLoginHere.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            finish(); // Go back to login
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // TextWatcher: clear errors on input change
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilName.setError(null);
            }
        });
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
            }
        });
        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                tilConfirmPassword.setError(null);
            }
        });
        etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilConfirmPassword.setError(null);
            }
        });
    }

    private void attemptRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validation
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.err_empty_name));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.err_empty_email));
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.err_empty_password));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.err_short_password));
            return;
        }
        if (TextUtils.isEmpty(confirm)) {
            tilConfirmPassword.setError(getString(R.string.err_empty_confirm));
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirmPassword.setError(getString(R.string.err_password_mismatch));
            return;
        }

        setLoading(true);

        // Firebase register
        authHelper.register(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Update display name in Firebase profile
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            setLoading(false);
                            Snackbar.make(btnRegister,
                                    getString(R.string.register_success),
                                    Snackbar.LENGTH_SHORT).show();
                            // Navigate to MainActivity
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .postDelayed(() -> {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 1000);
                        });
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Snackbar.make(btnRegister,
                        getString(R.string.register_failed, errorMessage),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? getString(R.string.loading) : getString(R.string.btn_register));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
