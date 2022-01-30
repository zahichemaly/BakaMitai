package com.zc.bakamitai.data.network.interceptors

import com.zc.bakamitai.data.Constants
import okhttp3.Interceptor
import okhttp3.Response

class OfflineInterceptor(maxDays: Int) : Interceptor {
    private val maxStale = maxDays * 24 * 60 * 60

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isNetworkAvailable()) {
            request = request.newBuilder()
                .header(Constants.Api.CACHE_CONTROL, "public, only-if-cached, max-stale=$maxStale")
                .removeHeader(Constants.Api.PRAGMA)
                .build()
        }
        return chain.proceed(request)
    }
}
