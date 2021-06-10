package com.example.websink

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.OkHttpClient
import okhttp3.Request

/*
* Functionalities of WebSink Web Honeypot:
* 1) Domain/URL Override
* 2) Certificate Pinning functionality
* 3) Traffic inspection
* */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val httpClient = getHttpClient()
        val browserView = getBrowserView(R.id.web_sink)
        val inputAddressField = findViewById<EditText>(R.id.url_bar)
        val submitBtn = findViewById<Button>(R.id.submit)
        val domainRegex = Regex("^(((([A-Za-z0-9]+){1,63}\\.)|(([A-Za-z0-9]+(\\-)+[A-Za-z0-9]+){1,63}\\.))+){1,255}$")
        browserView.webViewClient = CustomWebViewClient(applicationContext, httpClient)
        browserView.loadUrl("https://ipchicken.com")
        submitBtn.setOnClickListener {
            val addr = inputAddressField.text.toString()
            val notifyInvalidAddr = Toast.makeText(applicationContext, "Invalid FQDN $addr", Toast.LENGTH_SHORT)
            if(addr.isNotEmpty() && domainRegex.containsMatchIn(addr)) {
                val req = Request.Builder().url(addr).build()
                val resp = httpClient.newCall(req).execute()
                browserView.loadDataWithBaseURL(addr, resp.body().toString(), "text/html", "UTF-8", null)
            } else {
                notifyInvalidAddr.show()
            }

        }
    }

    private fun getHttpClient(): OkHttpClient {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(getInterceptor())
            .build()
        return okHttpClient
    }

    private fun getInterceptor: ChuckerInterceptor {
        return ChuckerInterceptor.Builder(applicationContext)
            .collector(getCollector())
            .maxContentLength(250000L)
            .alwaysReadResponseBody(true)
            .build()
    }

    private fun getCollector(): ChuckerCollector {
        return ChuckerCollector(
            context = this,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.FOREVER
        )
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