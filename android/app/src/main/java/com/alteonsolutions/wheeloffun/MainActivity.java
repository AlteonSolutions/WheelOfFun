package com.alteonsolutions.wheeloffun;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "WheelOfFun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on while playing.
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Throwable ignored) {}

        try {
            WebView webView = new WebView(this);
            webView.setBackgroundColor(0xFF0d0907);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView v, int code, String desc, String url) {
                    Log.e(TAG, "WebView error " + code + " on " + url + ": " + desc);
                }
            });
            webView.setWebChromeClient(new WebChromeClient());

            WebSettings ws = webView.getSettings();
            ws.setJavaScriptEnabled(true);
            ws.setDomStorageEnabled(true);
            ws.setMediaPlaybackRequiresUserGesture(false);
            ws.setAllowFileAccess(true);
            ws.setAllowContentAccess(true);
            // Required so file:// origin can load the inlined data: URI audio
            // and read its own resources without same-origin restrictions.
            ws.setAllowFileAccessFromFileURLs(true);
            ws.setAllowUniversalAccessFromFileURLs(true);

            setContentView(webView);
            webView.loadUrl("file:///android_asset/index.html");

            enterImmersive();
        } catch (Throwable t) {
            // Anything fatal at startup paints itself on-screen so we can see
            // what actually failed instead of a silent crash.
            Log.e(TAG, "Startup failed", t);
            showError(t);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersive();
    }

    private void showError(Throwable t) {
        try {
            TextView tv = new TextView(this);
            tv.setText("Wheel of Fun failed to launch:\n\n" + t.getClass().getSimpleName()
                + "\n" + (t.getMessage() == null ? "" : t.getMessage())
                + "\n\n" + Log.getStackTraceString(t));
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(0xFF0d0907);
            tv.setPadding(48, 96, 48, 48);
            tv.setTextSize(11);
            tv.setGravity(Gravity.TOP | Gravity.START);
            setContentView(tv);
        } catch (Throwable ignored) {}
    }

    private void enterImmersive() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(false);
                WindowInsetsController c = getWindow().getInsetsController();
                if (c != null) {
                    c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
        } catch (Throwable ignored) {}
    }
}
