package com.webviewapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // ─── Views ───────────────────────────────────────────────────
    private WebView            mWebView;
    private ProgressBar        mProgressBar;
    private SwipeRefreshLayout mSwipeRefresh;
    private FrameLayout        mOfflineLayout;
    private AdView             mBannerAdView;

    // ─── State ───────────────────────────────────────────────────
    private String             mCurrentUrl        = Config.START_URL;
    private boolean            mIsOffline         = false;
    private int                mPageLoadCount     = 0;
    private boolean            mBackPressedOnce   = false;

    // ─── Ads ─────────────────────────────────────────────────────
    private InterstitialAd     mInterstitialAd;

    // ─── File upload ─────────────────────────────────────────────
    private ValueCallback<Uri[]> mFilePathCallback;
    private Uri                  mCameraImageUri;

    private final ActivityResultLauncher<Intent> mFileChooserLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (mFilePathCallback == null) return;
                        Uri[] results = null;
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data == null || data.getData() == null) {
                                // Camera capture
                                if (mCameraImageUri != null) {
                                    results = new Uri[]{ mCameraImageUri };
                                }
                            } else {
                                // Gallery / file pick
                                String dataString = data.getDataString();
                                if (dataString != null) {
                                    results = new Uri[]{ Uri.parse(dataString) };
                                }
                            }
                        }
                        mFilePathCallback.onReceiveValue(results);
                        mFilePathCallback = null;
                    }
            );

    // ─────────────────────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────────────────────

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto dark-mode
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        setContentView(R.layout.activity_main);

        bindViews();
        setupWebView();
        setupSwipeRefresh();
        setupAds();
        setupFCM();
        handleNotificationIntent(getIntent());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Hide title completely ← ADD THESE
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mWebView.loadUrl(Config.START_URL);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        if (mBannerAdView != null) mBannerAdView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
        if (mBannerAdView != null) mBannerAdView.pause();
    }

    @Override
    protected void onDestroy() {
        if (mBannerAdView != null) mBannerAdView.destroy();
        mWebView.destroy();
        super.onDestroy();
    }

    // ─────────────────────────────────────────────────────────────
    //  View Binding
    // ─────────────────────────────────────────────────────────────

    private void bindViews() {
        mWebView       = findViewById(R.id.webview);
        mProgressBar   = findViewById(R.id.progress_bar);
        mSwipeRefresh  = findViewById(R.id.swipe_refresh);
        mOfflineLayout = findViewById(R.id.offline_layout);
        mBannerAdView  = findViewById(R.id.banner_ad_view);

        // "Try Again" button on offline page
        findViewById(R.id.btn_retry).setOnClickListener(v -> {
            mOfflineLayout.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            mWebView.reload();
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  WebView Setup
    // ─────────────────────────────────────────────────────────────

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setupWebView() {
        WebSettings settings = mWebView.getSettings();

        // Core
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // Performance boost
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setEnableSmoothTransition(true);

        // Viewport
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Media
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        // Encoding
        settings.setDefaultTextEncodingName("UTF-8");

        // Hardware acceleration
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // User agent
        String ua = settings.getUserAgentString()
                + Config.USER_AGENT_SUFFIX;
        settings.setUserAgentString(ua);

        // Cookies
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(mWebView, true);

        // JS Bridge
        mWebView.addJavascriptInterface(
                new WebAppInterface(this), "Android");

        // Clients
        mWebView.setWebViewClient(buildWebViewClient());
        mWebView.setWebChromeClient(buildWebChromeClient());

        // Scroll performance
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Download listener
        mWebView.setDownloadListener((url, userAgent,
                                      contentDisposition, mimeType, contentLength) -> {
            try {
                DownloadManager.Request request =
                        new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.addRequestHeader("Cookie",
                        CookieManager.getInstance().getCookie(url));
                request.setDescription("Downloading...");
                request.setTitle(getFileNameFromUrl(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        getFileNameFromUrl(url));
                DownloadManager dm = (DownloadManager)
                        getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(MainActivity.this,
                            "Downloading " + getFileNameFromUrl(url),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        // Long press image download
        mWebView.setOnLongClickListener(v -> {
            WebView.HitTestResult result = mWebView.getHitTestResult();
            if (result.getType() == WebView.HitTestResult.IMAGE_TYPE
                    || result.getType() ==
                    WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                String imageUrl = result.getExtra();
                if (imageUrl != null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Download Image")
                            .setMessage("Download this image?")
                            .setPositiveButton("Download",
                                    (dialog, which) -> {
                                        DownloadManager.Request request =
                                                new DownloadManager.Request(
                                                        Uri.parse(imageUrl));
                                        request.setDestinationInExternalPublicDir(
                                                Environment.DIRECTORY_DOWNLOADS,
                                                getFileNameFromUrl(imageUrl));
                                        request.setNotificationVisibility(
                                                DownloadManager.Request
                                                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        DownloadManager dm = (DownloadManager)
                                                getSystemService(
                                                        Context.DOWNLOAD_SERVICE);
                                        if (dm != null) dm.enqueue(request);
                                    })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
            return false;
        });
    }

    private WebViewClient buildWebViewClient() {
        return new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                    WebResourceRequest request) {
                String url = request.getUrl().toString();

                // Stay inside allowed domain
                if (Config.ALLOWED_DOMAIN.isEmpty()
                        || url.contains(Config.ALLOWED_DOMAIN)) {
                    return false; // let WebView handle it
                }

                // External URL → open in browser
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (ActivityNotFoundException ignored) {}
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mCurrentUrl = url;
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(0);
                mIsOffline = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefresh.setRefreshing(false);
                CookieManager.getInstance().flush();

                // Count page loads for interstitial timing
                if (Config.ADS_ENABLED && Config.INTERSTITIAL_EVERY_N_PAGES > 0) {
                    mPageLoadCount++;
                    if (mPageLoadCount % Config.INTERSTITIAL_EVERY_N_PAGES == 0) {
                        showInterstitial();
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view,
                    WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    showOfflinePage();
                }
            }
        };
    }

    private WebChromeClient buildWebChromeClient() {
        return new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
               /* if (getSupportActionBar() != null && title != null) {
                    getSupportActionBar().setTitle(title);
                }*/
            }

            // ── File chooser for <input type="file"> ──
            @Override
            public boolean onShowFileChooser(WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;
                openFileChooser(fileChooserParams);
                return true;
            }
        };
    }

    // ─────────────────────────────────────────────────────────────
    //  File Upload
    // ─────────────────────────────────────────────────────────────

    private void openFileChooser(WebChromeClient.FileChooserParams params) {
        Intent contentIntent = params.createIntent();

        // Camera capture option
        mCameraImageUri = null;
        try {
            mCameraImageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new android.content.ContentValues());
        } catch (Exception ignored) {}

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);

        Intent chooser = Intent.createChooser(contentIntent, "Choose file");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                new Intent[]{ cameraIntent });
        mFileChooserLauncher.launch(chooser);
    }

    // ─────────────────────────────────────────────────────────────
    //  Offline / Error page
    // ─────────────────────────────────────────────────────────────

    private void showOfflinePage() {
        mIsOffline = true;
        mSwipeRefresh.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        mOfflineLayout.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────
    //  Swipe-to-Refresh
    // ─────────────────────────────────────────────────────────────

    private void setupSwipeRefresh() {
        mSwipeRefresh.setColorSchemeResources(
                R.color.colorPrimary, R.color.colorSecondary);
        mSwipeRefresh.setOnRefreshListener(() -> {
            mOfflineLayout.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            mWebView.reload();
        });
        // Only allow refresh when page is scrolled to very top
        mWebView.getViewTreeObserver()
                .addOnScrollChangedListener(() -> {
                    int scrollY = mWebView.getScrollY();
                    mSwipeRefresh.setEnabled(scrollY == 0);
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  Back button
    // ─────────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }

        // Double-back-to-exit
        if (mBackPressedOnce) {
            super.onBackPressed();
            return;
        }
        mBackPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper())
                .postDelayed(() -> mBackPressedOnce = false, 2000);
    }

    // ─────────────────────────────────────────────────────────────
    //  Options menu
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            shareCurrentUrl();
        } else if (id == R.id.action_refresh) {
            mWebView.reload();
        } else if (id == R.id.action_home) {
            mWebView.loadUrl(Config.START_URL);
        } else if (id == R.id.action_rate) {
            openPlayStore();
        } else if (id == R.id.action_about) {
            showAboutDialog();
        } else if (id == R.id.action_open_browser) {
            openInBrowser();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  Share / Rate / About
    // ─────────────────────────────────────────────────────────────

    private void shareCurrentUrl() {
        String url = mWebView.getUrl();
        if (url == null) url = Config.START_URL;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, Config.SHARE_SUBJECT);
        share.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(share, "Share via"));
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Config.PLAY_STORE_URL)));
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About " + Config.APP_NAME)
                .setMessage(Config.ABOUT_TEXT)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openInBrowser() {
        String url = mWebView.getUrl();
        if (url == null) url = Config.START_URL;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    // ─────────────────────────────────────────────────────────────
    //  Ads
    // ─────────────────────────────────────────────────────────────

    private void setupAds() {
        if (!Config.ADS_ENABLED) {
            if (mBannerAdView != null) mBannerAdView.setVisibility(View.GONE);
            return;
        }

        MobileAds.initialize(this, initStatus -> {});

        // Banner
        if (mBannerAdView != null) {
            AdRequest bannerRequest = new AdRequest.Builder().build();
            mBannerAdView.loadAd(bannerRequest);
        }

        // Preload interstitial
        loadInterstitial();
    }

    private void loadInterstitial() {
        if (!Config.ADS_ENABLED || Config.INTERSTITIAL_EVERY_N_PAGES <= 0) return;
        InterstitialAd.load(this,
                Config.ADMOB_INTERSTITIAL_ID,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        mInterstitialAd = ad;
                        mInterstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        mInterstitialAd = null;
                                        loadInterstitial(); // Reload
                                    }
                                });
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError err) {
                        mInterstitialAd = null;
                    }
                });
    }

    private void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  FCM
    // ─────────────────────────────────────────────────────────────

    private void setupFCM() {
        // Subscribe to topic
        FirebaseMessaging.getInstance()
                .subscribeToTopic(Config.FCM_DEFAULT_TOPIC)
                .addOnCompleteListener(task -> {

                });


    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null) return;
        String url = intent.getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            mWebView.loadUrl(url);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Network check
    // ─────────────────────────────────────────────────────────────

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities nc =
                cm.getNetworkCapabilities(cm.getActiveNetwork());
        return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private String getFileNameFromUrl(String url) {
        try {
            // Remove query parameters
            String cleanUrl = url;
            if (url.contains("?")) {
                cleanUrl = url.substring(0, url.indexOf("?"));
            }
            // Remove fragments
            if (cleanUrl.contains("#")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.indexOf("#"));
            }
            // Get filename
            String[] parts = cleanUrl.split("/");
            String name = parts[parts.length - 1];
            // If no filename found use timestamp
            if (name.isEmpty() || !name.contains(".")) {
                name = "download_" + System.currentTimeMillis();
            }
            return name;
        } catch (Exception e) {
            return "download_" + System.currentTimeMillis();
        }
    }


    /** Called by website via Android.showToast("Hello!") */
    public void runOnMainThread(Runnable r) {
        runOnUiThread(r);
    }
}
