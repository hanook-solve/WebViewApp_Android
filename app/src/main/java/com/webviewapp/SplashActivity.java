package com.webviewapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private View    mGlowRing;
    private View    mDivider;
    private View    mDot1, mDot2, mDot3;
    private boolean mAnimationDone = false;
    private boolean mDelayDone     = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Bind views
        View     logo       = findViewById(R.id.splash_logo);
        TextView appName    = findViewById(R.id.splash_app_name);
        TextView tagline    = findViewById(R.id.splash_tagline);
        TextView version    = findViewById(R.id.splash_version);
        View     dotsLayout = findViewById(R.id.splash_dots);
        mGlowRing           = findViewById(R.id.splash_glow);
        mDivider            = findViewById(R.id.splash_divider);
        mDot1               = findViewById(R.id.dot1);
        mDot2               = findViewById(R.id.dot2);
        mDot3               = findViewById(R.id.dot3);

        Handler handler = new Handler(Looper.getMainLooper());

        // ── Step 1: Logo pops in with overshoot (0ms) ──────────
        handler.postDelayed(() -> {
            AnimationSet logoAnim = new AnimationSet(true);
            ScaleAnimation scale = new ScaleAnimation(
                    0f, 1f, 0f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(600);
            AlphaAnimation fade = new AlphaAnimation(0f, 1f);
            fade.setDuration(400);
            logoAnim.addAnimation(scale);
            logoAnim.addAnimation(fade);
            logoAnim.setInterpolator(new OvershootInterpolator(1.5f));
            logoAnim.setFillAfter(true);
            logo.startAnimation(logoAnim);
            logo.setAlpha(1f);
        }, 100);

        // ── Step 2: Glow ring pulses in (400ms) ────────────────
        handler.postDelayed(() -> {
            AlphaAnimation glowAnim = new AlphaAnimation(0f, 0.8f);
            glowAnim.setDuration(500);
            glowAnim.setFillAfter(true);
            mGlowRing.startAnimation(glowAnim);
            mGlowRing.setAlpha(0.8f);
            startGlowPulse();
        }, 400);

        // ── Step 3: App name slides up (700ms) ─────────────────
        handler.postDelayed(() -> {
            AnimationSet nameAnim = new AnimationSet(true);
            TranslateAnimation slide = new TranslateAnimation(
                    0f, 0f, 40f, 0f);
            slide.setDuration(500);
            AlphaAnimation fade = new AlphaAnimation(0f, 1f);
            fade.setDuration(500);
            nameAnim.addAnimation(slide);
            nameAnim.addAnimation(fade);
            nameAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            nameAnim.setFillAfter(true);
            appName.startAnimation(nameAnim);
            appName.setAlpha(1f);
        }, 700);

        // ── Step 4: Gold divider expands (900ms) ───────────────
        handler.postDelayed(() -> {
            mDivider.setAlpha(1f);
            android.view.ViewGroup.LayoutParams params =
                    mDivider.getLayoutParams();
            params.width = 0;
            mDivider.setLayoutParams(params);
            android.animation.ValueAnimator widthAnim =
                    android.animation.ValueAnimator.ofInt(0, 180);
            widthAnim.setDuration(400);
            widthAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            widthAnim.addUpdateListener(animator -> {
                int val = (int) animator.getAnimatedValue();
                android.view.ViewGroup.LayoutParams p =
                        mDivider.getLayoutParams();
                p.width = (int) (val *
                        getResources().getDisplayMetrics().density);
                mDivider.setLayoutParams(p);
            });
            widthAnim.start();
        }, 900);

        // ── Step 5: Tagline fades in (1100ms) ──────────────────
        handler.postDelayed(() -> {
            AlphaAnimation tagAnim = new AlphaAnimation(0f, 1f);
            tagAnim.setDuration(400);
            tagAnim.setFillAfter(true);
            tagline.startAnimation(tagAnim);
            tagline.setAlpha(1f);
        }, 1100);

        // ── Step 6: Loading dots + version appear (1400ms) ─────
        handler.postDelayed(() -> {
            AlphaAnimation dotsAnim = new AlphaAnimation(0f, 1f);
            dotsAnim.setDuration(300);
            dotsAnim.setFillAfter(true);
            dotsLayout.startAnimation(dotsAnim);
            dotsLayout.setAlpha(1f);
            version.setAlpha(1f);
            startDotAnimation();
        }, 1400);

        // ── Step 7: Mark animation done ────────────────────────
        handler.postDelayed(() -> {
            mAnimationDone = true;
            navigateIfReady();
        }, 1800);

        // ── Minimum splash duration ─────────────────────────────
        handler.postDelayed(() -> {
            mDelayDone = true;
            navigateIfReady();
        }, Config.SPLASH_DURATION_MS);
    }

    // ── Glow ring pulse animation ───────────────────────────────
    private void startGlowPulse() {
        AlphaAnimation pulse = new AlphaAnimation(0.3f, 0.9f);
        pulse.setDuration(800);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        mGlowRing.startAnimation(pulse);
    }

    // ── Animated loading dots ───────────────────────────────────
    private void startDotAnimation() {
        animateDot(mDot1, 0);
        animateDot(mDot2, 200);
        animateDot(mDot3, 400);
    }

    private void animateDot(View dot, long delay) {
        AlphaAnimation anim = new AlphaAnimation(0.3f, 1f);
        anim.setDuration(400);
        anim.setStartOffset(delay);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        dot.startAnimation(anim);
    }

    // ── Navigate when both animation and delay are done ─────────
    private void navigateIfReady() {
        if (!mAnimationDone || !mDelayDone) return;
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
        finish();
    }
}
