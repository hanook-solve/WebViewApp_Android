package com.webviewapp;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  NativeWeb — Central Configuration
 *  Change everything about your app from this ONE file.
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class Config {

    // ─────────────────────────────────────────────────────────────
    //  🌐  WEBSITE
    // ─────────────────────────────────────────────────────────────
    /** The URL your app loads on launch. Change this ONE line. */
    public static final String START_URL = "https://hanook-solve.github.io/-webview-test/";

    /** Force all navigation inside this domain to stay in-app.
     *  Set to "" to allow ALL URLs to open inside the WebView. */
    public static final String ALLOWED_DOMAIN = "hanook-solve.github.io";

    // ─────────────────────────────────────────────────────────────
    //  📱  APP IDENTITY
    // ─────────────────────────────────────────────────────────────
    public static final String APP_NAME        = "NativeWeb";
    public static final String APP_VERSION     = "1.0.0";
    public static final String COMPANY_NAME    = "Your Company";
    public static final String PLAY_STORE_URL  =
            "https://play.google.com/store/apps/details?id=com.webviewapp";

    // ─────────────────────────────────────────────────────────────
    //  💰  ADMOB   (replace with your own IDs from AdMob console)
    // ─────────────────────────────────────────────────────────────
    /** Set to false to hide all ads (e.g. premium / paid build). */
    public static final boolean ADS_ENABLED = true;

    /** Banner ad unit ID */
    public static final String ADMOB_BANNER_ID =
            "ca-app-pub-3940256099942544/6300978111"; // ← test ID

    /** Interstitial ad unit ID */
    public static final String ADMOB_INTERSTITIAL_ID =
            "ca-app-pub-3940256099942544/1033173712"; // ← test ID

    /** Show interstitial every N page loads (0 = disabled) */
    public static final int INTERSTITIAL_EVERY_N_PAGES = 5;

    // ─────────────────────────────────────────────────────────────
    //  🔔  FIREBASE / FCM
    // ─────────────────────────────────────────────────────────────
    /** Default FCM topic all devices subscribe to on first launch. */
    public static final String FCM_DEFAULT_TOPIC = "all_users";

    // ─────────────────────────────────────────────────────────────
    //  ⏱  SPLASH SCREEN
    // ─────────────────────────────────────────────────────────────
    /** Minimum duration the splash screen is shown (ms). */
    public static final long SPLASH_DURATION_MS = 3000;

    // ─────────────────────────────────────────────────────────────
    //  🗄  CACHING
    // ─────────────────────────────────────────────────────────────
    /** Cache size for the WebView (bytes). Default = 50 MB. */
    public static final long WEBVIEW_CACHE_SIZE = 50L * 1024 * 1024;

    /** User-Agent suffix appended to the WebView UA string. */
    public static final String USER_AGENT_SUFFIX = " NativeWeb/1.0";

    // ─────────────────────────────────────────────────────────────
    //  📤  SHARING
    // ─────────────────────────────────────────────────────────────
    public static final String SHARE_SUBJECT = "Check this out!";

    // ─────────────────────────────────────────────────────────────
    //  ℹ️  ABOUT DIALOG
    // ─────────────────────────────────────────────────────────────
    public static final String ABOUT_TEXT =
            "NativeWeb converts any website into a\n"
          + "powerful native Android app.\n\n"
          + "© 2025 " + COMPANY_NAME + "\n"
          + "All rights reserved.";
}
