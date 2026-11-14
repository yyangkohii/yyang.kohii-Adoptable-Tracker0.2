package com.yyangkohii.adoptabletracker;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import androidx.webkit.WebViewAssetLoader;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 4207;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private WebViewAssetLoader assetLoader;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) {
                    if ("yyangkohii.carrd.co".equalsIgnoreCase(uri.getHost())) {
                        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                        return true;
                    }
                }
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = params.createIntent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try { startActivityForResult(intent, FILE_CHOOSER_REQUEST); }
                catch (Exception e) { filePathCallback = null; Toast.makeText(MainActivity.this, "No file picker is available.", Toast.LENGTH_LONG).show(); return false; }
                return true;
            }
        });
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    private class AndroidBridge {
        @JavascriptInterface public void saveFile(String filename, String mimeType, String base64) {
            runOnUiThread(() -> {
                try {
                    String safeName = filename == null ? "download" : filename.replaceAll("[^A-Za-z0-9._-]", "_");
                    byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Downloads.DISPLAY_NAME, safeName);
                        values.put(MediaStore.Downloads.MIME_TYPE, mimeType == null ? "application/octet-stream" : mimeType);
                        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Adoptable Tracker");
                        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        if (uri == null) throw new Exception("Could not create download file");
                        try (OutputStream out = getContentResolver().openOutputStream(uri)) { if (out == null) throw new Exception("Could not open download file"); out.write(bytes); }
                        Toast.makeText(MainActivity.this, "Saved to Downloads/Adoptable Tracker", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Saving files requires Android 10 or newer in this starter build.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) { Toast.makeText(MainActivity.this, "File save failed: " + e.getMessage(), Toast.LENGTH_LONG).show(); }
            });
        }
        @JavascriptInterface public void printPage() {
            runOnUiThread(() -> {
                try {
                    PrintManager manager = (PrintManager) getSystemService(PRINT_SERVICE);
                    manager.print("Adoptable Tracker Receipt", webView.createPrintDocumentAdapter("Adoptable Tracker Receipt"), new PrintAttributes.Builder().build());
                } catch (Exception e) { Toast.makeText(MainActivity.this, "Printing is unavailable on this device.", Toast.LENGTH_LONG).show(); }
            });
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (filePathCallback != null) filePathCallback.onReceiveValue(null);
        if (webView != null) { webView.removeJavascriptInterface("AndroidBridge"); webView.destroy(); }
        super.onDestroy();
    }
}
