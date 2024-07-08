package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
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

    override val contextUI: ContextUI
        get() = ContextUI.DEFAULT

    override val table: KClass<out DataObjectUI>
        get() = LogMPDB::class

    override fun getItems(): List<ItemUI> {
        val logMPDBUI = repository.getAllRealm(LogMPDB::class, contextUI)
        val addressSDB = repository.getAllRoom(AddressSDB::class, contextUI)
        return logMPDBUI.join(addressSDB, "address = addr_id: nm")
    }

    override fun getFilters(): Filters {
        return Filters(
            RangeDate("CoordTime", LocalDate.now(), LocalDate.now()),
            ""
        )
    }

    override fun onClickItemImage(itemUI: ItemUI, activity: AppCompatActivity) {
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