package com.sid.websink

import android.content.Context
import android.webkit.*
import com.sid.websink.data.DomainOverrideMapping
import com.sid.websink.data.DomainOverrideViewModel
import okhttp3.*

class CustomWebViewClient(appContext: Context): WebViewClient() {

    private val ctx: Context = appContext
    private var domainHandler = DomainHandler.getDomainHandler(appContext)


    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val internalReqBuilder: Request.Builder = Request.Builder()
        var domain = request?.url.toString()
        if (request != null && domainHandler.isValidDomain(domain)) {
            domain = domainHandler.getMappedDomain(domain)
            internalReqBuilder.url(domainHandler.sanitizeDomain(domain))
            for((key, value) in request.requestHeaders) {
                internalReqBuilder.addHeader(key, value)
            }
            domainHandler.getClientProcess(internalReqBuilder.build()).execute()
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }



}