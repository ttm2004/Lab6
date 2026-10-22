package com.trantrongmanh.lab6.utils;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Utility class providing reusable animations for UI elements.
 * Demonstrates creating animations programmatically in Java code.
 */
public class AnimUtil {

    /**
     * Fade-in animation for views.
     */
    public static void fadeIn(View view, long duration) {
        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(duration);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        view.startAnimation(anim);
    }

    /**
     * Fade-out animation for views.
     */
    public static void fadeOut(View view, long duration) {
        AlphaAnimation anim = new AlphaAnimation(1f, 0f);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    /**
     * Scale-up animation with overshoot effect (useful for FABs, buttons).
     */
    public static void scaleUp(View view, long duration) {
        ScaleAnimation anim = new ScaleAnimation(
                0.5f, 1f, 0.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        anim.setInterpolator(new OvershootInterpolator());
        view.startAnimation(anim);
    }

    /**
     * Slide in from bottom with fade.
     */
    public static void slideInFromBottom(View view, long duration, long offset) {
        AnimationSet set = new AnimationSet(true);
        set.setInterpolator(new AccelerateDecelerateInterpolator());

        TranslateAnimation slide = new TranslateAnimation(
                0, 0,
                view.getHeight() + 200, 0);
        slide.setDuration(duration);
        slide.setStartOffset(offset);

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(duration);
        fade.setStartOffset(offset);

        set.addAnimation(slide);
        set.addAnimation(fade);
        view.startAnimation(set);
    }

    /**
     * Slide in from right (used for staggered list items).
     */
    public static void slideInFromRight(View view, long duration, long offset) {
        AnimationSet set = new AnimationSet(true);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.setStartOffset(offset);

        TranslateAnimation slide = new TranslateAnimation(
                200, 0, 0, 0);
        slide.setDuration(duration);

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(duration);

        set.addAnimation(slide);
        set.addAnimation(fade);
        view.startAnimation(set);
    }

    /**
     * Bounce animation (click feedback for buttons).
     */
    public static void bounce(View view) {
        ScaleAnimation anim = new ScaleAnimation(
                1f, 0.9f, 1f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(100);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        view.startAnimation(anim);
    }

    /**
     * Rotate animation (used for loading indicators, refresh icons).
     */
    public static void rotate(View view, long duration) {
        RotateAnimation anim = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        view.startAnimation(anim);
    }

    /**
     * Pop-in animation combining scale + fade + slight rotation.
     */
    public static void popIn(View view, long duration) {
        AnimationSet set = new AnimationSet(true);
        set.setInterpolator(new OvershootInterpolator(2f));
        set.setDuration(duration);

        ScaleAnimation scale = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        set.addAnimation(scale);

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        set.addAnimation(fade);

        view.startAnimation(set);
    }
}
