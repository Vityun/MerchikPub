package ua.com.merchik.merchik.data

import android.os.Parcelable
import android.view.View
import java.util.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class Data(
    var id: Long,
    var addr: String?,
    var cust: String?,
    var merc: String?,
    var date: Date,
    var otchetId: Long,
    @IgnoredOnParcel
    var optionsSignals: View? = null,
    var optionsSignalsString: String? = null,
    var images: Int,
) : Parcelable