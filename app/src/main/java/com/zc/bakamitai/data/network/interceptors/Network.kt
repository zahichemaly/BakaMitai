package com.zc.bakamitai.data.network.interceptors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

@Suppress("DEPRECATION")
fun isNetworkAvailable(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        } else {
            activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }
