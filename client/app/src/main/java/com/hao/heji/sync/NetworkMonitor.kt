import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils

/**
 * NetworkMonitor
 * 网络变化监听
 */
class NetworkMonitor(private val context: Context, private val listener: (Boolean) -> Unit) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            LogUtils.d("NetworkMonitor", "Network is available", network)
            if (isNetworkAvailable()) {
                listener(true)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            LogUtils.d("NetworkMonitor", "Network is lost", network)
            if (!isNetworkAvailable()) {
                listener(false)
            }
        }
    }

    fun startNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun stopNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork: Network? = connectivityManager.activeNetwork
        val networkCapabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
