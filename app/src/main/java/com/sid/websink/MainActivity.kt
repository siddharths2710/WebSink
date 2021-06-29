package com.sid.websink

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
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
import com.sid.websink.fragments.add.addPinnerOverrideFragment
import com.sid.websink.fragments.list.ListDomainOverrideFragment
import com.sid.websink.fragments.list.ListPinnerOverrideFragment
import okhttp3.*

/*
* Functionalities of WebSink Web Honeypot:
* 1) Domain/URL Override
* 2) Certificate Pinning functionality (HPKP)
* 3) Traffic inspection
* */
class MainActivity : AppCompatActivity() {

    private var CHECK_PAUSE = false
    private var CHECK_RESTART = false
    private val PERMISSIONS_REQUEST_CODE = 5
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
        browserView.settings.builtInZoomControls = true

        submitBtn.setOnClickListener {
            var addr = domainHandler.getMappedDomain(inputAddressField.text.toString())
            if(addr != null && addr.length > 0 && domainHandler.isValidDomain(addr)) {
                browserView.loadUrl(addr)
            } else {
                val block_page = DomainHandler.getBlockPageAsString(applicationContext)
                val encodedHtml = Base64.encodeToString(block_page.toByteArray(), Base64.NO_PADDING)
                browserView.loadData(encodedHtml, "text/html", "base64")

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
        setDefaultExceptionHandler()
        validatePermissions()
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
        supportFragmentManager.setFragmentResultListener(
            "trusted_fragment",
            this,
            FragmentResultListener { requestKey, result -> recreate() })
    }

    private fun setDefaultExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            var emailBodyStr = "Forwarding customer trace of a WebSandbox failure.\n"
            emailBodyStr = "$emailBodyStr ${e.message} \nTrace: \n${Log.getStackTraceString(e)}"
            val supportEmailIntent = Intent(Intent.ACTION_SEND)
            supportEmailIntent.setType(Intent.ACTION_SENDTO)
            intent.putExtra(Intent.EXTRA_EMAIL, "support.websandboxsink@protonmail.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "WebSandbox: Unexpected Failure");
            intent.putExtra(Intent.EXTRA_TEXT, emailBodyStr)
            startActivity(Intent.createChooser(intent, "Reach out to support for unexpected failure"))
        }
    }

    private fun validatePermissions() {
        var pendingPermissions: MutableList<String> = emptyArray<String>().toMutableList()

        if(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
                pendingPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED)
            pendingPermissions.add(Manifest.permission.INTERNET)
        if(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_NETWORK_STATE)
            != PackageManager.PERMISSION_GRANTED)
            pendingPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
        if(pendingPermissions.size > 0)
            requestPermissions(pendingPermissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)

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
            if(!areFabsVisibile) {
                mMenuFab.extend()
            }
            else {
                mMenuFab.shrink()
            }
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
                certPinnerBuilder.add(pinnerMapping?.domain, "${pinnerMapping?.hashType}/${pinnerMapping.hashVal}")
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
        webSink.settings.javaScriptEnabled = true
        webSink.settings.useWideViewPort = true
        webSink.settings.loadWithOverviewMode = true
        webSink.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSink.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        return webSink
    }

}