package com.zc.bakamitai.data.network

import android.content.Context
import com.google.gson.GsonBuilder
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

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            Timber.i(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private const val CACHE_SIZE = (10 * 1024 * 1024).toLong()

    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private fun getOkHttpClient(context: Context): OkHttpClient {
        val cache = Cache(context.applicationContext.cacheDir, CACHE_SIZE)
        return OkHttpClient().newBuilder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(ParamInterceptor())
            .addInterceptor(OfflineInterceptor(context, 30))
            .addInterceptor(OnlineInterceptor())
            .cache(cache)
            .build()
    }

    fun getRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.Api.BASE_URL)
            .client(getOkHttpClient(context))
            .addConverterFactory(JsoupConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
