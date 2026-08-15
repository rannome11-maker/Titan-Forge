package com.blackforge.realm

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import io.aatricks.llmedge.LLMEdge
import io.aatricks.llmedge.model.ModelSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RealmActivity : Activity() {
    private lateinit var web: WebView
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var edge: LLMEdge
    private val model = ModelSpec.huggingFace(
        repoId = "Qwen/Qwen3-4B-GGUF",
        filename = "Qwen3-4B-Q4_K_M.gguf",
        preferSystemDownloader = true,
    )

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        edge = LLMEdge.create(this, scope)
        web = WebView(this)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(), "RealmAndroid")
        web.loadUrl("file:///android_asset/index.html")
        setContentView(web)
    }

    private fun js(code: String) = runOnUiThread { web.evaluateJavascript(code, null) }

    inner class Bridge {
        @JavascriptInterface fun toast(message: String) = runOnUiThread {
            Toast.makeText(this@RealmActivity, message, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface fun prepareModel() {
            js("modelStatus('downloading','Android Download Manager is preparing Qwen3-4B…')")
            scope.launch {
                try {
                    val file = withContext(Dispatchers.IO) { edge.models.prefetch(model) }
                    getSharedPreferences("realm", MODE_PRIVATE).edit().putString("model", file.absolutePath).apply()
                    js("modelStatus('ready',${JSONObject.quote("Ready • ${file.name}")})")
                } catch (t: Throwable) {
                    js("modelStatus('error',${JSONObject.quote(t.message ?: "Model download failed")})")
                }
            }
        }

        @JavascriptInterface fun checkModel() {
            val ready = getSharedPreferences("realm", MODE_PRIVATE).getString("model", null)
            js(if (ready != null) "modelStatus('ready','Qwen3-4B is installed')" else "modelStatus('missing','Model not downloaded yet')")
        }

        @JavascriptInterface fun generate(requestId: String, prompt: String) {
            scope.launch {
                try {
                    val answer = withContext(Dispatchers.IO) {
                        edge.text.generate(prompt = "$prompt\n/no_think", model = model, maxTokens = 512, batchSize = 8)
                    }
                    js("nativeReply(${JSONObject.quote(requestId)},true,${JSONObject.quote(answer)})")
                } catch (t: Throwable) {
                    js("nativeReply(${JSONObject.quote(requestId)},false,${JSONObject.quote(t.message ?: "Local inference failed")})")
                }
            }
        }
    }

    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
    override fun onDestroy() { runCatching { edge.close() }; scope.cancel(); web.destroy(); super.onDestroy() }
}
