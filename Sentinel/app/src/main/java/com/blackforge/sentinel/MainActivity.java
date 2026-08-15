package com.blackforge.sentinel;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private WebView web;
    private SpeechRecognizer recognizer;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(), "SentinelAndroid");
        web.loadUrl("file:///android_asset/index.html");
    }

    public class Bridge {
        @JavascriptInterface public void open(String url) {
            runOnUiThread(() -> {
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                catch (Exception ignored) {}
            });
        }

        @JavascriptInterface public void share(String text) {
            runOnUiThread(() -> {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Sentinel incident log");
                i.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(i, "Export incident log"));
            });
        }

        @JavascriptInterface public void listen() {
            runOnUiThread(() -> startListening());
        }
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 117);
            return;
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            sendSpeech("Speech recognition is unavailable on this device.");
            return;
        }
        if (recognizer != null) recognizer.destroy();
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle b) { web.evaluateJavascript("voiceState('LISTENING')", null); }
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float f) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() { web.evaluateJavascript("voiceState('PROCESSING')", null); }
            public void onError(int e) { web.evaluateJavascript("voiceState('TRY AGAIN')", null); }
            public void onResults(Bundle b) {
                ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (r != null && !r.isEmpty()) sendSpeech(r.get(0));
            }
            public void onPartialResults(Bundle b) {}
            public void onEvent(int t, Bundle b) {}
        });
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizer.startListening(i);
    }

    private void sendSpeech(String value) {
        String safe = value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ");
        web.evaluateJavascript("receiveSpeech('" + safe + "')", null);
    }

    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(request, permissions, results);
        if (request == 117 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) startListening();
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        web.destroy();
        super.onDestroy();
    }
}
