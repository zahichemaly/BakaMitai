package com.zc.bakamitai.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.util.*

class ParamInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url()

        val newUrl = url.newBuilder()
            .addQueryParameter("tz", TimeZone.getDefault().id)
            .build()
        val builder = request.newBuilder().url(newUrl)
        val newRequest = builder.build()

        return chain.proceed(newRequest)
    }
}
