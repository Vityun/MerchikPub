package ua.com.merchik.merchik.features.maps.presentation

import androidx.compose.ui.graphics.Color
import ua.com.merchik.merchik.dataLayer.ContextUI


interface MapActionsBridge {
    val contextUI: ContextUI
    val highlightColor: Color
    val addressSelectionMode: Boolean
        get() = false
    fun requestScrollToVisit(stableId: Long)
    fun highlightByAddrId(addrId: String, color: Color)
    fun selectAddressFromMap(stableId: Long?, addressId: String?, addressName: String?) = Unit
    fun dismissHost()
}
