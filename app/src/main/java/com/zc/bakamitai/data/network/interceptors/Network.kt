package com.zc.bakamitai.data.network.interceptors

import android.content.Context
import android.net.ConnectivityManager
import com.zc.bakamitai.BakaApplication

fun isNetworkAvailable(): Boolean {
    var isConnected = false
    val connectivityManager = BakaApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    if (activeNetwork != null && activeNetwork.isConnected)
        isConnected = true
    return isConnected
}
