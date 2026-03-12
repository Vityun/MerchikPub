package ua.com.merchik.merchik.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

fun observeInternetState(context: Context): Flow<Boolean> = callbackFlow {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isInternetAvailable(): Boolean {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    trySend(isInternetAvailable())

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(isInternetAvailable())
        }

        override fun onLost(network: Network) {
            trySend(isInternetAvailable())
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            trySend(isInternetAvailable())
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    cm.registerNetworkCallback(request, callback)

    awaitClose {
        cm.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()