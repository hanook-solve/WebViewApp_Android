package com.webviewapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * JavaScript Interface — JS Bridge
 *
 * Usage from your website's JavaScript:
 *
 *   Android.showToast("Hello from web!")
 *   Android.shareText("Check this out!", "https://example.com")
 *   Android.openDialer("123-456-7890")
 *   Android.openEmail("support@example.com", "Subject", "Body")
 *   Android.openBrowser("https://example.com")
 *   Android.vibrate()
 *   var online = Android.isNetworkAvailable()
 *   var pkg    = Android.getPackageName()
 *   var ver    = Android.getAppVersion()
 */
public class WebAppInterface {

    private final Context     mContext;
    private final MainActivity mActivity;

    WebAppInterface(MainActivity activity) {
        mContext  = activity;
        mActivity = activity;
    }

    /** Show a native Toast message. */
    @JavascriptInterface
    public void showToast(String message) {
        mActivity.runOnMainThread(() ->
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show());
    }

    /** Share text + URL via Android share sheet. */
    @JavascriptInterface
    public void shareText(String subject, String text) {
        mActivity.runOnMainThread(() -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, subject);
            share.putExtra(Intent.EXTRA_TEXT, text);
            mContext.startActivity(Intent.createChooser(share, "Share via"));
        });
    }

    /** Open native phone dialer. */
    @JavascriptInterface
    public void openDialer(String phoneNumber) {
        mActivity.runOnMainThread(() -> {
            Intent dial = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + phoneNumber));
            mContext.startActivity(dial);
        });
    }

    /** Open email client pre-filled. */
    @JavascriptInterface
    public void openEmail(String to, String subject, String body) {
        mActivity.runOnMainThread(() -> {
            Intent email = new Intent(Intent.ACTION_SENDTO,
                    Uri.parse("mailto:" + to));
            email.putExtra(Intent.EXTRA_SUBJECT, subject);
            email.putExtra(Intent.EXTRA_TEXT, body);
            mContext.startActivity(Intent.createChooser(email, "Send Email"));
        });
    }

    /** Open URL in external browser. */
    @JavascriptInterface
    public void openBrowser(String url) {
        mActivity.runOnMainThread(() ->
                mContext.startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
    }

    /** Open Google Maps with a query. */
    @JavascriptInterface
    public void openMaps(String query) {
        mActivity.runOnMainThread(() -> {
            Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, mapUri));
        });
    }

    /** Returns true if device has network access. */
    @JavascriptInterface
    public boolean isNetworkAvailable() {
        return mActivity.isNetworkAvailable();
    }

    /** Returns this app's package name. */
    @JavascriptInterface
    public String getPackageName() {
        return mContext.getPackageName();
    }

    /** Returns app version name from PackageManager. */
    @JavascriptInterface
    public String getAppVersion() {
        try {
            return mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    /** Close/finish the app from JS. */
    @JavascriptInterface
    public void closeApp() {
        mActivity.runOnMainThread(mActivity::finish);
    }
}
