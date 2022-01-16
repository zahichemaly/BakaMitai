package com.zc.bakamitai.data.network

import com.google.gson.GsonBuilder
import com.zc.bakamitai.data.network.interceptors.ParamInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

object Api {

    const val BASE_URL = "https://subsplease.org/"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.i(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(ParamInterceptor())
        .build()

    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(JsoupConverterFactory)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

}
