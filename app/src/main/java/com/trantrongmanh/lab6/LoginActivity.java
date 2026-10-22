package com.trantrongmanh.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trantrongmanh.lab6.utils.AnimUtil;
import com.trantrongmanh.lab6.utils.FirebaseAuthHelper;
import com.trantrongmanh.lab6.utils.SimpleTextWatcher;

import java.util.regex.Pattern;

/**
 * Login screen with Firebase Authentication.
 *
 * Demonstrates:
 * - Event programming: OnClickListener, TextWatcher (via error clearing)
 * - Intent: explicit intent to MainActivity / RegisterActivity
 * - Material Design UI: TextInputLayout, MaterialButton, MaterialCardView
 * - Animation: card scale-in, button bounce
 * - Input validation
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuthHelper authHelper;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        authHelper = new FirebaseAuthHelper();

        // Entrance animation for the card
        View cardForm = findViewById(R.id.cardForm);
        AnimUtil.scaleUp(cardForm, 600);
    }

    private void setupListeners() {
        // OnClickListener for login button
        btnLogin.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            attemptLogin();
        });

        // OnClickListener for register text
        TextView tvRegister = findViewById(R.id.tvRegister);
        tvRegister.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // OnClickListener for forgot password
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            AnimUtil.bounce(v);
            attemptPasswordReset();
        });

        // Clear errors on text change (demonstrates TextWatcher event handling)
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
            }
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validation
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

        // Show loading
        setLoading(true);

        // Firebase Auth login
        authHelper.login(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Snackbar.make(btnLogin,
                        getString(R.string.login_failed, errorMessage),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void attemptPasswordReset() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            return;
        }
        authHelper.resetPassword(email, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                Snackbar.make(btnLogin,
                        getString(R.string.password_reset_sent),
                        Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Snackbar.make(btnLogin, errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? getString(R.string.loading) : getString(R.string.btn_login));
    }

    @Override
    public void onBackPressed() {
        // Prevent going back from login to splash
        finishAffinity();
    }
}
