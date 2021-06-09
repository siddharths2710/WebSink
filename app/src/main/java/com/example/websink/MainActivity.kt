package com.example.websink

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val httpClient = getHttpClient()
        val browserView = getBrowserView(R.id.web_sink)
        val inputAddressField = findViewById<EditText>(R.id.url_bar)
        val submitBtn = findViewById<Button>(R.id.submit)
        browserView.webViewClient = CustomWebViewClient(applicationContext, httpClient)
        browserView.loadUrl("https://ipchicken.com")
        submitBtn.setOnClickListener {
            browserView.loadUrl(inputAddressField.text.toString())
        }
    }

    private fun getHttpClient(): OkHttpClient {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                ChuckerInterceptor.Builder(applicationContext)
                    .collector(ChuckerCollector(applicationContext))
                    .maxContentLength(250000L)
                    .alwaysReadResponseBody(true)
                    .build()
            )
            .build()
        return okHttpClient
    }
    private fun getBrowserView(id: Int): WebView {
        val webSink: WebView = findViewById(id)
        webSink.settings.javaScriptEnabled = true;
        webSink.settings.useWideViewPort = true;
        webSink.settings.loadWithOverviewMode = true;
        webSink.settings.cacheMode = WebSettings.LOAD_NO_CACHE;
        return webSink
    }

}