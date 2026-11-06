package ua.com.merchik.merchik.features.main.options

import android.content.Intent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel

object PriceIssueCorrection {
    const val PRODUCT_IDS = "price_issue_product_ids"

    @JvmStatic
    fun open(view: View, dad2: Long, clientId: String?, ids: List<String>, subtitle: String, onReturn: Runnable) {
        val host = Globals.unwrap(view.context) as? ComponentActivity ?: return
        if (ids.isEmpty() || host.isFinishing || host.isDestroyed) return
        val intent = Intent(host, FeaturesActivity::class.java).apply {
            putExtra("viewModel", TovarDBViewModel::class.java.name)
            putExtra("contextUI", ContextUI.TOVAR_FROM_OPTION_175121.name)
            putExtra("dataJson", Gson().toJson(JSONObject().put("codeDad2", dad2.toString()).put("clientId", clientId)))
            putStringArrayListExtra(PRODUCT_IDS, ArrayList(ids))
            putExtra("title", "Товари")
            putExtra("subTitle", subtitle)
        }
        host.startActivity(intent)
        host.lifecycle.addObserver(object : DefaultLifecycleObserver {
            private var left = false
            override fun onPause(owner: LifecycleOwner) { left = true }
            override fun onResume(owner: LifecycleOwner) {
                if (!left) return
                owner.lifecycle.removeObserver(this)
                if (view.isAttachedToWindow) onReturn.run()
            }
        })
    }
}
