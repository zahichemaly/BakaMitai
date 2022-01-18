package com.zc.bakamitai.data.network.interceptors

import com.zc.bakamitai.data.network.Api
import okhttp3.Interceptor
import okhttp3.Response

class OfflineInterceptor(private val maxDays: Int) : Interceptor {
    private val maxStale = maxDays * 24 * 60 * 60

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isNetworkAvailable()) {
            request = request.newBuilder()
                .header(Api.Headers.CACHE_CONTROL, "public, only-if-cached, max-stale=$maxStale")
                .removeHeader(Api.Headers.PRAGMA)
                .build()
        }
        return chain.proceed(request)
    }
}
