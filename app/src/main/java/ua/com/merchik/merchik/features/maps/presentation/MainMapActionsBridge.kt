package ua.com.merchik.merchik.features.maps.presentation

import androidx.compose.ui.graphics.Color
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel


class MainMapActionsBridge(
    private val mainVm: MainViewModel,
    private val onDismiss: () -> Unit,
    override val contextUI: ContextUI,
    override val highlightColor: Color
) : MapActionsBridge {
    override fun requestScrollToVisit(stableId: Long) = mainVm.requestScrollToVisit(stableId)
    override fun highlightByAddrId(addrId: String, color: Color) = mainVm.highlightByAddrId(addrId, color)
    override fun dismissHost() = onDismiss()
}
