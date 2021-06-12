package com.sid.websink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException

/*
* Functionalities of WebSink Web Honeypot:
* 1) Domain/URL Override
* 2) Certificate Pinning functionality (HPKP)
* 3) Traffic inspection
* */
class MainActivity : AppCompatActivity() {

    private lateinit var httpClient: OkHttpClient
    private lateinit var httpClientHandler: Handler
    private lateinit var chuckerIntent: Intent
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

        configureInit()
        configureMenuBehaviourInit()
        browserView.loadUrl("https://ipchicken.com")

        submitBtn.setOnClickListener {
            val addr = inputAddressField.text.toString()
            if(addr.isNotEmpty() && isValidFqdn(addr)) {
                val req = Request.Builder().url(addr).build()
                try {
                    val callObj = httpClient.newCall(req)
                    callObj.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                if(!response.isSuccessful) throw IOException("Unexpected code $response")
                                httpClientHandler.post {
                                    browserView.loadDataWithBaseURL(addr, response.body().toString(),
                                                "text/html", "UTF-8", null)
                                }
                            }
                        }
                    })
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("onCreate", "addr: $addr")
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(applicationContext, "Invalid URL $addr", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun configureInit() {
        configureActionBar()
        httpClient = getHttpClient()
        httpClientHandler = Handler(Looper.myLooper()!!)
        chuckerIntent = Chucker.getLaunchIntent(applicationContext)
        browserView = getBrowserView(R.id.web_sink)
        inputAddressField = findViewById<EditText>(R.id.url_bar)
        submitBtn = findViewById<Button>(R.id.submit)
        browserView.webViewClient = CustomWebViewClient(applicationContext, httpClient)
    }

    private fun configureActionBar() {
        setupActionBarWithNavController(findNavController(R.id.navPinFragment))
        setupActionBarWithNavController(findNavController(R.id.navOverrideFragment))
        actionBar?.setDisplayHomeAsUpEnabled(true)
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
            if(!areFabsVisibile)
                mMenuFab.extend()
            else
                mMenuFab.shrink()
            areFabsVisibile = !areFabsVisibile
            toggleVisibility()
        }

        mInspectFab.setOnClickListener {
            //Inspect traffic
            startActivity(chuckerIntent)
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
        mInspectText.visibility = isVisibleObj
        mPinText.visibility = isVisibleObj
        mOverrideText.visibility = isVisibleObj
        if(areFabsVisibile) {
            mInspectFab.show()
            mPinFab.show()
            mOverrideFab.show()
        } else {
            mInspectFab.hide()
            mPinFab.hide()
            mOverrideFab.hide()
        }
    }

    private fun isValidFqdn(domain: String): Boolean {
        val domainRegex = Regex("^(((([A-Za-z0-9]+){1,63}\\.)|(([A-Za-z0-9]+(\\-)+[A-Za-z0-9]+){1,63}\\.))+){1,255}$")
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