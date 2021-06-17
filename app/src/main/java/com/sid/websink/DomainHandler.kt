package com.sid.websink

import android.content.Context
import android.os.Handler
import com.sid.websink.data.DomainOverrideMapping
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request

class DomainHandler {

    var httpClient: OkHttpClient? = null
    var httpClientHandler: Handler? = null
    private var domainMappings: Map<String, String>? = null

    companion object {
        @Volatile
        private var INSTANCE: DomainHandler? = null

        fun getDomainHandler(context: Context): DomainHandler {
            val tempInstance = INSTANCE
            if(tempInstance != null)
                return tempInstance
            INSTANCE = DomainHandler()
            return INSTANCE as DomainHandler
        }
    }

    fun sanitizeDomain(domain: String): String {
        if(domain.length == 0) //Blacklist
            return domain
        var newDomain = domain
        if(domainMappings != null && domainMappings!!.containsKey(newDomain))
            newDomain = domainMappings!![newDomain].toString()
        if(!(newDomain.startsWith("http") || newDomain.startsWith("https")))
            newDomain = "https://$newDomain"
        return newDomain
    }

    fun getMappedDomain(oldDomain: String): String {
        var newDomain: String = oldDomain
        if(isValidDomain(oldDomain) && domainMappings != null && domainMappings?.containsKey(oldDomain) == true) {
            newDomain = domainMappings!![oldDomain].toString()
        }
        return newDomain
    }

    fun getClientHandler(): Handler {
        return httpClientHandler!!
    }
    fun getClientProcess(req: Request): Call {
        return httpClient!!.newCall(req)
    }

    fun setDomainOverrideMappings(domainMappingList: List<DomainOverrideMapping>) {
        domainMappings = domainMappingList.associateBy({it.oldDomain}, {it.newDomain!!})
    }

    fun isValidDomain(domain: String): Boolean {
        val domainRegex = Regex("(?!-)[A-Za-z0-9-]+([\\-\\.]{1}[a-z0-9]+)*\\.[A-Za-z]{2,6}")
        if((domain != null) && domain.isNotEmpty() && domainRegex.containsMatchIn(domain)) {
            return true
        }
        return false
    }
}