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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sid.websink.data.DomainOverrideViewModel
import com.sid.websink.data.PinnerViewModel
import com.sid.websink.fragments.list.ListDomainOverrideFragment
import com.sid.websink.fragments.list.ListPinnerOverrideFragment
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

    private lateinit var domainHandler: DomainHandler
    private lateinit var chuckerIntent: Intent
    private lateinit var listDomainOverrideFragment: Fragment
    private lateinit var listPinnerOverrideFragment: Fragment
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
    private lateinit var mPinnerViewModel: PinnerViewModel
    private lateinit var mDomainOverrideViewModel: DomainOverrideViewModel
    private var areFabsVisibile: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureInit()
        configureMenuBehaviourInit()
        browserView.loadUrl("https://ipchicken.com")

        submitBtn.setOnClickListener {
            var addr = inputAddressField.text.toString()
            if(addr.isNotEmpty() && domainHandler.isValidDomain(addr)) {
                addr = domainHandler.sanitizeDomain(addr)
                val req = Request.Builder().url(addr).build()
                try {
                    val callObj = domainHandler.getClientProcess(req)
                    callObj.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                if(!response.isSuccessful) throw IOException("Unexpected code $response")
                                domainHandler.getClientHandler().post {
                                    /*browserView.loadDataWithBaseURL(addr, response.body().toString(),
                                    "text/html", "UTF-8", null)*/
                                    browserView.loadUrl(addr)
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

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container_layout, fragment)
        transaction.addToBackStack(fragment.toString())
        transaction.commitAllowingStateLoss()
    }

    private fun configureInit() {
        configureActionBar()
        domainHandler = DomainHandler.getDomainHandler(applicationContext)
        inputAddressField = findViewById<EditText>(R.id.url_bar)
        submitBtn = findViewById<Button>(R.id.submit)
        listDomainOverrideFragment = ListDomainOverrideFragment()
        listPinnerOverrideFragment = ListPinnerOverrideFragment()
        mPinnerViewModel = ViewModelProvider(this).get(PinnerViewModel::class.java)
        mDomainOverrideViewModel =  ViewModelProvider(this).get(DomainOverrideViewModel::class.java)

        domainHandler.httpClient = getHttpClient()
        domainHandler.httpClientHandler = Handler(Looper.myLooper()!!)
        chuckerIntent = Chucker.getLaunchIntent(applicationContext)
        browserView = getBrowserView(R.id.web_sink)
        browserView.webViewClient = getCustomWebViewClient()
    }


    private fun configureActionBar() {
        //setupActionBarWithNavController(findNavController(R.id.listDomainOverrideFragment))
        //setupActionBarWithNavController(findNavController(R.id.listPinnerOverrideFragment))
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
            Toast.makeText(baseContext, "Cert Pin", Toast.LENGTH_LONG)
            loadFragment(listPinnerOverrideFragment)
        }

        mOverrideFab.setOnClickListener {
            Toast.makeText(baseContext, "Domain Override", Toast.LENGTH_SHORT)
            loadFragment(listDomainOverrideFragment)
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


    private fun getCustomWebViewClient(): CustomWebViewClient {
        val customWebViewClient = CustomWebViewClient(applicationContext)
        mDomainOverrideViewModel.getAll.observe(this, Observer { domainMappingList ->
            domainHandler.setDomainOverrideMappings(domainMappingList)
        })
        return customWebViewClient
    }
    private fun getHttpClient(): OkHttpClient {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(getInterceptor())
            .certificatePinner(getCertPinner())
            .build()
        return okHttpClient
    }

    private fun getCertPinner(): CertificatePinner {
        val certPinnerBuilder = CertificatePinner.Builder()
        mPinnerViewModel.getAll.observe(this, Observer { pinnerMappingList ->
            for(pinnerMapping in pinnerMappingList) {
                certPinnerBuilder.add(pinnerMapping?.domain, "$pinnerMapping?.hashType/$pinnerMapping.hashVal")
            }
        })
        return certPinnerBuilder.build()
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