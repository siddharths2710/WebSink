package com.sid.websink

import android.content.Context
import android.webkit.*
import okhttp3.*

class CustomWebViewClient(appContext: Context, okHttpClient: OkHttpClient): WebViewClient() {

    private val ctx: Context = appContext
    private var httpClient: OkHttpClient = okHttpClient

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        var internalReqBuilder: Request.Builder = Request.Builder()
        if (request != null) {
            internalReqBuilder.url(request.url.toString())
            for((key, value) in request.requestHeaders) {
                internalReqBuilder.addHeader(key, value)
            }
        }

        var response: Response = httpClient.newCall(internalReqBuilder.build()).execute()
        return super.shouldInterceptRequest(view, request)
    }


}