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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.lang.IllegalArgumentException

/*
* Functionalities of WebSink Web Honeypot:
* 1) Domain/URL Override
* 2) Certificate Pinning functionality
* 3) Traffic inspection
* */
class MainActivity : AppCompatActivity() {

    private lateinit var httpClient: OkHttpClient
    private lateinit var submitBtn: Button
    private lateinit var browserView: WebView
    private lateinit var inputAddressField: EditText
    private lateinit var mMenuFab: ExtendedFloatingActionButton
    private lateinit var mInspectFab: FloatingActionButton
    private lateinit var mPinFab: FloatingActionButton
    private lateinit var mOverrideFab: FloatingActionButton
    private lateinit var mInspectText: TextView
    private lateinit var mPinText: TextView
    private lateinit var mOverrideText: TextView
    private var areFabsVisibile: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureMenuBehaviourInit()
        httpClient = getHttpClient()
        browserView = getBrowserView(R.id.web_sink)
        inputAddressField = findViewById<EditText>(R.id.url_bar)
        submitBtn = findViewById<Button>(R.id.submit)
        browserView.webViewClient = CustomWebViewClient(applicationContext, httpClient)
        browserView.loadUrl("https://ipchicken.com")

        submitBtn.setOnClickListener {
            val addr = inputAddressField.text.toString()
            val notifyInvalidAddr = Toast.makeText(applicationContext, "Invalid URL $addr", Toast.LENGTH_SHORT)
            if(addr.isNotEmpty() && isValidFqdn(addr)) {
                val req = Request.Builder().url(addr).build()
                try {
                    val resp = httpClient.newCall(req).execute()
                    browserView.loadDataWithBaseURL(addr, resp.body().toString(), "text/html", "UTF-8", null)
                } catch (e: IllegalArgumentException) {
                    //notifyInvalidAddr.show()
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                notifyInvalidAddr.show()
            }
        }

    }
    private fun configureMenuBehaviourInit() {
        mMenuFab = findViewById(R.id.menu_efab)
        mInspectFab = findViewById(R.id.inspect_fab)
        mPinFab = findViewById(R.id.pin_fab)
        mOverrideFab = findViewById(R.id.override_fab)
        mInspectText = findViewById(R.id.inspect_text)
        mOverrideText = findViewById(R.id.override_text)
        mPinText = findViewById(R.id.pin_text)

        mMenuFab.shrink()
        toggleVisibility()

        mMenuFab.setOnClickListener {
            if(!areFabsVisibile) {
                mMenuFab.extend()
                toggleVisibility()
                areFabsVisibile = true
            } else {
                mMenuFab.shrink()
                toggleVisibility()
                areFabsVisibile = false
            }
        }

        mInspectFab.setOnClickListener {
            //Inspect traffic
            startActivity(Chucker.getLaunchIntent(applicationContext))
        }

        mPinFab.setOnClickListener {
            Toast.makeText(applicationContext, "Cert Pin", Toast.LENGTH_LONG)
        }

        mOverrideFab.setOnClickListener {
            Toast.makeText(applicationContext, "Domain Override", Toast.LENGTH_SHORT)
        }

    }

    private fun toggleVisibility() {
        val isVisibleObj = if(areFabsVisibile) View.VISIBLE else View.GONE
        mInspectFab.visibility = isVisibleObj
        mPinFab.visibility = isVisibleObj
        mOverrideFab.visibility = isVisibleObj
        mInspectText.visibility = isVisibleObj
        mPinText.visibility = isVisibleObj
        mOverrideText.visibility = isVisibleObj
    }

    private fun isValidFqdn(domain: String): Boolean {
        //val domainRegex = Regex("^(((([A-Za-z0-9]+){1,63}\\.)|(([A-Za-z0-9]+(\\-)+[A-Za-z0-9]+){1,63}\\.))+){1,255}$")
        return true
    }

    private fun getHttpClient(): OkHttpClient {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(getInterceptor())
            .build()
        return okHttpClient
    }

    private fun getInterceptor(): ChuckerInterceptor {
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