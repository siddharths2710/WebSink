package com.sid.websink

import android.content.Context
import android.webkit.*
import com.sid.websink.data.ContentTypeParser
import com.sid.websink.data.DomainOverrideMapping
import com.sid.websink.data.DomainOverrideViewModel
import okhttp3.*

class CustomWebViewClient(appContext: Context): WebViewClient() {

    private var domainHandler = DomainHandler.getDomainHandler(appContext)

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val httpResponse = domainHandler.getClientProcess(getInternalRequestBuilder(request).build()).execute()
        val contentType = httpResponse.header("Content-Type")
        if(contentType != null) {
            val inputStream = httpResponse.body()?.byteStream()
            val mimeType = ContentTypeParser.getMimeType(contentType)
            val charset = ContentTypeParser.getCharset(contentType)
            return WebResourceResponse(mimeType, charset, inputStream)
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        //domainHandler.getClientProcess(getInternalRequestBuilder(request).build()).execute()
        return super.shouldOverrideUrlLoading(view, request)
    }

    private fun getInternalRequestBuilder(request: WebResourceRequest?): Request.Builder {
        val builder: Request.Builder = Request.Builder()
        var domain = request?.url?.toString()
        if (request != null && domain != null && domainHandler.isValidDomain(domain!!)) {
            domain = domainHandler.getMappedDomain(domain!!)
            builder.url(domainHandler.sanitizeDomain(domain))
            if(request.requestHeaders != null) {
                for((key, value) in request.requestHeaders) {
                    builder.addHeader(key, value)
                }
            }
        }
        return builder
    }

}