package ua.com.merchik.merchik.features.main.Main


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.LaunchOrigin
import ua.com.merchik.merchik.dataLayer.ModeUI
import kotlin.reflect.KClass

fun launchFeaturesActivity(
    launcher: ActivityResultLauncher<Intent>,
    context: Context,
    viewModelClass: KClass<out MainViewModel>,
    dataJson: String? = null,
    modeUI: ModeUI = ModeUI.DEFAULT,
    contextUI: ContextUI = ContextUI.DEFAULT,
    title: String? = "##title",
    subTitle: String? = "##subTitle",
    idResImage: Int? = null,
    typeWindow: String? = null,
//    options: ActivityOptionsCompat? = null
    origin: LaunchOrigin? = null

) {
    val intent = Intent(context, FeaturesActivity::class.java).apply {
        putExtra("viewModel", viewModelClass.qualifiedName)
        putExtra("dataJson", dataJson)
        putExtra("modeUI", modeUI.name)
        putExtra("contextUI", contextUI.name)
        putExtra("title", title)
        putExtra("subTitle", subTitle)
        putExtra("idResImage", idResImage ?: 0)
        putExtra("typeWindow", typeWindow)

        putExtra("launch_origin_left", origin?.x ?: -1)
        putExtra("launch_origin_top", origin?.y ?: -1)
        putExtra("launch_origin_width", origin?.width ?: -1)
        putExtra("launch_origin_height", origin?.height ?: -1)
    }

//    if (options != null) {
//        launcher.launch(intent, options)
//    } else {
        launcher.launch(intent)
//    }
}
