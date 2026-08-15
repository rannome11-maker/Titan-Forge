package com.blackforge.realm;

import android.app.*;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;

public class MainActivity extends Activity {
  private WebView web;
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    web = new WebView(this);
    WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient());
    web.addJavascriptInterface(new Object(){ @JavascriptInterface public void toast(String x){ runOnUiThread(()->Toast.makeText(MainActivity.this,x,Toast.LENGTH_SHORT).show()); } },"RealmAndroid");
    web.loadUrl("file:///android_asset/index.html"); setContentView(web);
  }
  @Override public void onBackPressed(){ if(web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
