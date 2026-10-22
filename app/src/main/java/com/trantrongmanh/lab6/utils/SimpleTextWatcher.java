package com.trantrongmanh.lab6.utils;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Convenience implementation of TextWatcher with empty default methods.
 * Subclasses only need to override the methods they care about.
 * Demonstrates: event programming pattern (TextWatcher for input validation).
 */
public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Override in subclass
    }

    @Override
    public void afterTextChanged(Editable s) {
        // No-op
    }
}
