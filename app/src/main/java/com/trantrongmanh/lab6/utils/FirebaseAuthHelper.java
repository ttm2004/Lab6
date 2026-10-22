package com.trantrongmanh.lab6.utils;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Helper class wrapping Firebase Authentication operations.
 * Provides callbacks for async results so Activities can update the UI.
 */
public class FirebaseAuthHelper {

    private final FirebaseAuth mAuth;

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Check if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Get the current logged-in user, or null.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Sign in with email and password.
     *
     * @param email    user email
     * @param password user password
     * @param callback invoked on success or failure
     */
    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Create a new account with email and password.
     */
    public void register(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Send password reset email.
     */
    public void resetPassword(String email, AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Sign out the current user and redirect to login.
     */
    public void logout(Context context, Class<?> loginActivityClass) {
        mAuth.signOut();
        Intent intent = new Intent(context, loginActivityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Callback interface for auth operations.
     */
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }
}
