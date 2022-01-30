package com.zc.bakamitai.data.network.interceptors

import com.zc.bakamitai.data.Constants
import okhttp3.Interceptor
import okhttp3.Response

class OnlineInterceptor : Interceptor {
    private val maxAge = 60

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return response.newBuilder()
            .header(Constants.Api.CACHE_CONTROL, "public, max-age=$maxAge")
            .removeHeader(Constants.Api.PRAGMA)
            .build()
    }
}
