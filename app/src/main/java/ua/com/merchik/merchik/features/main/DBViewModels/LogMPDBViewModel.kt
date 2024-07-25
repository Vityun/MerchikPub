package ua.com.merchik.merchik.features.main.DBViewModels

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.join
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogMap
import ua.com.merchik.merchik.features.main.Filters
import ua.com.merchik.merchik.features.main.MainViewModel
import ua.com.merchik.merchik.features.main.RangeDate
import java.time.LocalDate
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class LogMPDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val title: String
        get() = "Історія місцеположення"

    override val subTitle: String
        get() = "Подзаголовок Подзаг Подво Подво Подзаголовок njh"

    override val contextUI: ContextUI
        get() = ContextUI.MULTI_SELECT

    override val table: KClass<out DataObjectUI>
        get() = LogMPDB::class

    override fun getItems(): List<ItemUI> {
        var startTime = System.currentTimeMillis()
        var endTime = System.currentTimeMillis()

        try {
            val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

            val validTime = 1800

            startTime = if ((wpDataDB.getVisit_start_dt() > 0))
                wpDataDB.getVisit_start_dt() - validTime
            else
                (System.currentTimeMillis() / 1000) - validTime

            endTime =
                if (wpDataDB.getVisit_end_dt() > 0) wpDataDB.getVisit_end_dt() else System.currentTimeMillis() / 1000

        } catch (e: Exception) {}

        val logMPDBUI = repository
            .getByRangeDateRealmDataObjectUI(LogMPDB::class, "CoordTime", startTime*1000, endTime*1000, contextUI)
        val addressSDB = repository.getAllRoom(AddressSDB::class, contextUI)

        return logMPDBUI.join(addressSDB, "address = addr_id: nm")
    }

    override fun onClickItem(itemUI: ItemUI, context: Context) {
        val activity = (context as? AppCompatActivity) ?: return

        val logMPDB = (itemUI.rawObj.firstOrNull { it is LogMPDB } as? LogMPDB)

        val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

        val addressSDB = RoomManager.SQL_DB.addressDao().getById(wpDataDB.addr_id)

        if (logMPDB != null) {
            val dialogMap = DialogMap(
                activity,
                "",
                addressSDB?.locationXd ?: 0f,
                addressSDB?.locationYd ?: 0f,
                "Місцеположення ТТ",
                logMPDB.CoordX,
                logMPDB.CoordY,
                "Ваше місцеположення"
            )
            dialogMap.setData("", "Місцеположення")
            dialogMap.show()
        }
    }
}