package com.expressphonepro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private WebView webView;
    private LinearLayout splashLayout;
    private LinearLayout errorLayout;
    private static final String APP_URL = "https://express-phone-pro.vercel.app/";

    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST  = 200;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#0D0D1A"));
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_main);

        splashLayout = findViewById(R.id.splash_layout);
        errorLayout  = findViewById(R.id.error_layout);
        webView      = findViewById(R.id.webview);

        requestAllPermissions();
        setupWebView();

        new Handler().postDelayed(() -> {
            if (isNetworkAvailable()) {
                splashLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(APP_URL);
            } else {
                splashLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
            }
        }, 2500);

        Button retryBtn = findViewById(R.id.retry_btn);
        retryBtn.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                errorLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(APP_URL);
            }
        });
    }

    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            java.util.List<String> perms = new java.util.ArrayList<>();

            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
                perms.add(android.Manifest.permission.CAMERA);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED)
                    perms.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                    perms.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                    perms.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!perms.isEmpty())
                requestPermissions(perms.toArray(new String[0]), STORAGE_PERMISSION_REQUEST);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 12; Samsung) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36 " +
            "ExpressPhonePro/1.0"
        );

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // ✅ الحل الرئيسي: معالجة التنزيل مباشرة
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                    String contentDisposition, String mimetype, long contentLength) {

                // استخراج اسم الملف
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "backup_" + System.currentTimeMillis() + ".json";
                }

                // للـ blob URLs نحتاج معالجة خاصة
                if (url.startsWith("blob:") || url.startsWith("data:")) {
                    handleBlobDownload(url, fileName);
                    return;
                }

                // للروابط العادية نستخدم DownloadManager
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimetype);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.addRequestHeader("Cookie",
                        CookieManager.getInstance().getCookie(url));
                    request.setDescription("جارٍ تنزيل النسخة الاحتياطية...");
                    request.setTitle(fileName);
                    request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, fileName);

                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);

                    Toast.makeText(MainActivity.this,
                        "✅ جارٍ الحفظ في Downloads: " + fileName,
                        Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                        "⚠️ خطأ في التنزيل: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("blob:") || url.startsWith("data:")) return false;
                if (url.contains("express-phone-pro.vercel.app") || url.contains("vercel.app"))
                    return false;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // إصلاح viewport
                webView.evaluateJavascript(
                    "(function(){" +
                    "var m=document.querySelector('meta[name=viewport]');" +
                    "if(!m){m=document.createElement('meta');m.name='viewport';document.head.appendChild(m);}" +
                    "m.content='width=device-width,initial-scale=1.0,maximum-scale=1.0';})();", null);

                // ✅ اعتراض روابط التنزيل من الصفحة (blob و data URLs)
                webView.evaluateJavascript(
                    "(function(){" +
                    // اعتراض النقر على أي رابط تنزيل
                    "document.addEventListener('click', function(e){" +
                    "  var a = e.target.closest('a[download], a[href]');" +
                    "  if(a){" +
                    "    var href = a.href || '';" +
                    "    if(href.startsWith('blob:') || href.startsWith('data:')){" +
                    "      e.preventDefault();" +
                    "      window.AndroidBridge.downloadBlob(href, a.download||'backup.json');" +
                    "    }" +
                    "  }" +
                    "}, true);" +
                    // override window.print
                    "window.print=function(){window.AndroidBridge&&window.AndroidBridge.printPage();};" +
                    "})();", null);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                    ValueCallback<Uri[]> filePathCallback2,
                    FileChooserParams fileChooserParams) {

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                    filePathCallback = null;
                }
                filePathCallback = filePathCallback2;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    new String[]{"application/json", "text/plain",
                                 "application/octet-stream", "*/*"});

                try {
                    startActivityForResult(
                        Intent.createChooser(intent, "اختر ملف النسخة الاحتياطية"),
                        FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                    GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        // JavaScript Bridge للتنزيل والطباعة
        webView.addJavascriptInterface(new Object() {

            @android.webkit.JavascriptInterface
            public void downloadBlob(String blobUrl, String fileName) {
                // نطلب من الصفحة تحويل الـ blob إلى base64
                runOnUiThread(() -> {
                    String safeFileName = fileName.isEmpty() ?
                        "backup_" + System.currentTimeMillis() + ".json" : fileName;

                    webView.evaluateJavascript(
                        "(function(){" +
                        "  var xhr = new XMLHttpRequest();" +
                        "  xhr.open('GET', '" + blobUrl + "', true);" +
                        "  xhr.responseType = 'blob';" +
                        "  xhr.onload = function(){" +
                        "    var reader = new FileReader();" +
                        "    reader.onloadend = function(){" +
                        "      window.AndroidBridge.saveBase64File(reader.result, '" + safeFileName + "');" +
                        "    };" +
                        "    reader.readAsDataURL(xhr.response);" +
                        "  };" +
                        "  xhr.send();" +
                        "})();", null);
                });
            }

            @android.webkit.JavascriptInterface
            public void saveBase64File(String base64Data, String fileName) {
                try {
                    // استخراج البيانات من base64
                    String base64 = base64Data.contains(",") ?
                        base64Data.split(",")[1] : base64Data;
                    byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);

                    // حفظ في Downloads
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsDir.exists()) downloadsDir.mkdirs();

                    File file = new File(downloadsDir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bytes);
                    fos.close();

                    // إشعار بنجاح الحفظ
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "✅ تم الحفظ في Downloads:\n" + fileName,
                        Toast.LENGTH_LONG).show());

                    // تحديث مكتبة الوسائط
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)));

                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "❌ فشل الحفظ: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
                }
            }

            @android.webkit.JavascriptInterface
            public void printPage() {
                runOnUiThread(() -> {
                    PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                    PrintDocumentAdapter adapter =
                        webView.createPrintDocumentAdapter("Express Phone Pro");
                    pm.print("Express Phone Pro", adapter,
                        new PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .build());
                });
            }

        }, "AndroidBridge");
    }

    private void handleBlobDownload(String url, String fileName) {
        runOnUiThread(() -> {
            webView.evaluateJavascript(
                "window.AndroidBridge.downloadBlob('" + url + "', '" + fileName + "');",
                null);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onResume() { super.onResume(); webView.onResume(); }
    @Override protected void onPause()  { super.onPause();  webView.onPause();  }
}
