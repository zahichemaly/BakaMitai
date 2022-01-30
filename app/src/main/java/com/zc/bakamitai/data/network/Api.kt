package com.zc.bakamitai.data.network

import com.google.gson.GsonBuilder
import com.zc.bakamitai.BakaApplication
import com.zc.bakamitai.data.Constants
import com.zc.bakamitai.data.network.interceptors.OfflineInterceptor
import com.zc.bakamitai.data.network.interceptors.OnlineInterceptor
import com.zc.bakamitai.data.network.interceptors.ParamInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

object Api {

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.i(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private const val cacheSize = (10 * 1024 * 1024).toLong()
    private val cache = Cache(BakaApplication.getContext().cacheDir, cacheSize)

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(ParamInterceptor())
        .addInterceptor(OfflineInterceptor(30))
        .addInterceptor(OnlineInterceptor())
        .cache(cache)
        .build()

    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.Api.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(JsoupConverterFactory)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

}
