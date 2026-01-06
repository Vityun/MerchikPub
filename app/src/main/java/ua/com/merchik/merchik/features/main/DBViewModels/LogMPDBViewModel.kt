package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Options.Buttons.OptionButtonHistoryMP
import ua.com.merchik.merchik.Options.Options
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm
import ua.com.merchik.merchik.features.main.Main.MainEvent
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.componentsUI.CardItemsData
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class LogMPDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = LogMPDB::class



    override fun onClickAdditionalContent() {
        super.onClickAdditionalContent()
        val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)
        if (context != null && wpDataDB != null) {
            OptionButtonHistoryMP(
                context!!,
                wpDataDB,
                null,
                null,
                Options.NNKMode.MAKE,
                null,
                ::updateContent
            )
        }
    }

    override suspend fun getItems(): List<DataItemUI> {
        var startTime = System.currentTimeMillis()
        var endTime = System.currentTimeMillis()

        val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

        val validTime = 1800

        startTime = if ((wpDataDB.visit_start_dt > 0) && wpDataDB.visit_end_dt > 0)
            wpDataDB.visit_start_dt - validTime
        else
            (System.currentTimeMillis() / 1000) - validTime

        endTime =
            if (wpDataDB.visit_end_dt > 0) wpDataDB.visit_end_dt else (System.currentTimeMillis() / 1000)


        val logMPDBUI = repository
            .getByRangeDateRealmDataObjectUI(
                LogMPDB::class,
                "CoordTime",
                LogMPRealm.normalizeToMillis(startTime),
                LogMPRealm.normalizeToMillis(endTime),
                contextUI,
                null
            )
//        val addressSDB = repository.getAllRoom(AddressSDB::class, contextUI, null)
//        return logMPDBUI.join(addressSDB, "address = addr_id: nm")
        return logMPDBUI.addWpDataFieldsMandatory(wpDataDB)
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
//        val activity = (context as? AppCompatActivity) ?: return
//
//        val logMPDB = (itemUI.rawObj.firstOrNull { it is LogMPDB } as? LogMPDB)
//
//        val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)
//
//        val addressSDB = RoomManager.SQL_DB.addressDao().getById(wpDataDB.addr_id)
//
//        if (logMPDB != null) {
//            val dialogMap = DialogMap(
//                activity,
//                "",
//                addressSDB?.locationXd ?: 0f,
//                addressSDB?.locationYd ?: 0f,
//                "Місцеположення ТТ",
//                logMPDB.CoordX,
//                logMPDB.CoordY,
//                "Ваше місцеположення"
//            )
//            dialogMap.setData("", "Місцеположення")
//            dialogMap.show()
//        }
        super.onClickItem(itemUI, context)
        viewModelScope.launch {
            Log.e("!!!!!", "onClickItem+++++++++")
            _events.emit(
                MainEvent.ShowCardItemsDialog(
                    cardItemsData = CardItemsData(
                        dateItemUI = itemUI,
                        title = "Данные МП"
                    )
                )
            )
        }

    }
}

//fun List<DataItemUI>.addWpDataFieldsMandatory(wpDataDB: WpDataDB?): List<DataItemUI> {
//    val txt = wpDataDB?.addr_txt ?: ""
//    val xdRaw = wpDataDB?.addr_location_xd ?: 0
//    val ydRaw = wpDataDB?.addr_location_yd ?: 0
//
//    val addrTxtField = FieldValue(
//        key = "addr_txt",
//        field = TextField(rawValue = txt, value = txt),
//        value = TextField(rawValue = txt, value = txt)
//    )
//
//    val addrXdField = FieldValue(
//        key = "addr_location_xd",
//        field = TextField(rawValue = xdRaw, value = xdRaw.toString()),
//        value = TextField(rawValue = xdRaw, value = xdRaw.toString())
//    )
//
//    val addrYdField = FieldValue(
//        key = "addr_location_yd",
//        field = TextField(rawValue = ydRaw, value = ydRaw.toString()),
//        value = TextField(rawValue = ydRaw, value = ydRaw.toString())
//    )
//
//    val toAdd = listOf(addrTxtField, addrXdField, addrYdField)
//    val toAddKeysLower = toAdd.map { it.key.lowercase() }.toSet()
//
//    return this.map { item ->
//        // fields: удаляем старые по ключам (case-insensitive) и добавляем новые в конец
//        val newFields = item.fields
//            .filterNot { it.key.lowercase() in toAddKeysLower }
//            .toMutableList()
//            .apply { addAll(toAdd) }
//
//        // rawFields: то же самое
//        val newRawFields = item.rawFields
//            .filterNot { it.key.lowercase() in toAddKeysLower }
//            .toMutableList()
//            .apply { addAll(toAdd) }
//
//        // rawObj оставляем как есть (если нужно модифицировать rawObj — см. вариант 2)
//        item.copy(
//            fields = newFields,
//            rawFields = newRawFields
//        )
//    }
//}


fun List<DataItemUI>.addWpDataFieldsMandatory(wpDataDB: WpDataDB?): List<DataItemUI> {
    // защитный случай: если wpDataDB == null — всё равно добавим "пустые" значения
    val txt = wpDataDB?.addr_txt ?: ""
    val xdRaw = wpDataDB?.addr_location_xd
    val ydRaw = wpDataDB?.addr_location_yd

    // подготовим TextField для каждого поля
    val addrTxtField = FieldValue(
        key = "log_addr_txt",
        field = TextField(rawValue = txt, value = txt),
        value = TextField(rawValue = txt, value = txt)
    )

    val xdStr = xdRaw?.toString() ?: "0"
    val addrXdField = FieldValue(
        key = "log_addr_location_xd",
        field = TextField(rawValue = xdRaw ?: 0, value = xdStr),
        value = TextField(rawValue = xdRaw ?: 0, value = xdStr)
    )

    val ydStr = ydRaw?.toString() ?: "0"
    val addrYdField = FieldValue(
        key = "log_addr_location_yd",
        field = TextField(rawValue = ydRaw ?: 0, value = ydStr),
        value = TextField(rawValue = ydRaw ?: 0, value = ydStr)
    )

    val toAdd = listOf(addrTxtField, addrXdField, addrYdField)

    return this.map { item ->
        // удаляем старые поля с теми же ключами (чтобы не дублировать), затем добавляем новые
        val filtered = item.fields.filter { existing ->
            toAdd.none { add -> add.key.equals(existing.key, ignoreCase = true) }
        }.toMutableList()

        filtered.addAll(toAdd)

        // если нужно — также добавить rawObj из wpDataDB (в твоей логике ранее это возможно)
        val newRawObj = item.rawObj.toMutableList()
        // если wpDataDB у тебя хранится как DataObjectUI, можно добавить; здесь пропущу, тк тип WpDataDB отличается

        item.copy(rawFields = filtered)
    }
}

