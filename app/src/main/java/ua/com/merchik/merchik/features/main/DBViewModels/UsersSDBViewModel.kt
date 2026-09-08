package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.com.merchik.merchik.Utils.ValidatorEKL
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.EKL.EKLDataHolder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class UsersSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private val _uiItemsHeader = MutableStateFlow<List<DataItemUI>>(emptyList())
    val uiItemsHeader: StateFlow<List<DataItemUI>>
        get() = _uiItemsHeader.asStateFlow()

    override val table: KClass<out DataObjectUI>
        get() = UsersSDB::class

    override fun getDefaultHideUserFields(): List<String>? {
        return when (contextUI) {
            ContextUI.USERS_SDB_FROM_EKL ->
                ("client_id, work_addr_id, tel2, teritorial_id, regional_id, instructor_id, supervisor_id, nach_otd_proizv_id, zamestitel_id").split(",")

            else -> null
        }
    }

    override suspend fun getItemsHeader(): List<DataItemUI> {
        return uiItemsHeader.value
    }

    private fun readInputParameters(): JsonObject? {
        val raw = dataJson?.takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            Gson().fromJson(raw, JsonObject::class.java)
        }.onFailure {
            Log.w("UsersSDBViewModel", "Invalid dataJson; using fallback parameters")
        }.getOrNull()
    }

    private fun JsonObject?.stringOrNull(key: String): String? {
        val value = this?.get(key)
            ?.takeIf { it.isJsonPrimitive }
            ?.asJsonPrimitive
            ?: return null
        if (!value.isString && !value.isNumber) return null
        return value.asString.trim().takeIf { it.isNotEmpty() }
    }


    override fun updateFilters() {

        try {

            val dataJsonObject = readInputParameters()

            val addrId = dataJsonObject.stringOrNull("addr_id")?.toIntOrNull()
                ?: EKLDataHolder.instance().usersPTTWorkAddressId

            val wpClientId = dataJsonObject.stringOrNull("wpDataClientId")
                ?: EKLDataHolder.instance().usersPTTWPClientId

            val wpPttUserId = dataJsonObject.stringOrNull("wpDataPttUserId")?.toIntOrNull()
                ?: EKLDataHolder.instance().usersPTTWPPttUserId

            val wpDataUserId = dataJsonObject.stringOrNull("wpDataUserId")?.toIntOrNull()
                ?: EKLDataHolder.instance().usersPTTWPDataUserId

            val wpDataTime = dataJsonObject.stringOrNull("wpDataTime")?.toLongOrNull()
                ?: EKLDataHolder.instance().usersPTTWPDataTime

            if (addrId != null && wpClientId != null && wpPttUserId != null
                && wpDataTime != null && wpDataUserId != null
            ) {

                if (EKLDataHolder.instance().usersPTTWorkAddressId == null)
                    EKLDataHolder.instance().usersPTTWorkAddressId = addrId
                if (EKLDataHolder.instance().usersPTTWPClientId == null)
                    EKLDataHolder.instance().usersPTTWPClientId = wpClientId
                if (EKLDataHolder.instance().usersPTTWPPttUserId == null)
                    EKLDataHolder.instance().usersPTTWPPttUserId = wpPttUserId
                if (EKLDataHolder.instance().usersPTTWPDataUserId == null)
                    EKLDataHolder.instance().usersPTTWPDataUserId = wpDataUserId
                if (EKLDataHolder.instance().usersPTTWPDataTime == null)
                    EKLDataHolder.instance().usersPTTWPDataTime = wpDataTime

                val address = RoomManager.SQL_DB.addressDao().getById(addrId)

//            val tovarGroupClientSDB = RoomManager.SQL_DB.tovarGroupClientDao()
//                .getAllBy(wpClientId, address.tpId)

//
//            val ids: List<Int> = tovarGroupClientSDB?.map { it.tovarGrpId } ?: emptyList()
//
//             = ids

                Log.e("updateFilters", "addrId: $addrId")

                val addressSDB = ItemFilter(
                    "Адреса місця роботи",
                    AddressSDB::class,
                    AddressSDBViewModel::class,
                    ModeUI.ONE_SELECT,
                    "Address",
                    "subTitle",
                    "work_addr_id",
                    "id",
                    mutableListOf(address?.id.toString()),
                    mutableListOf(address?.nm ?: ""),
                    false
                )


//            Validator2EKL.bla(wpId)

                val control = ValidatorEKL.controlEKL()
                Log.e("ValidatorEKL", ">>> ${control.message} + ${control.result}")


                val data = RoomManager.SQL_DB.usersDao().getPTT(addrId)

                if (data.isNullOrEmpty()) {
                    val header = AdditionalRequirementsDB::class.java.newInstance()
                    header.notes =
                        "Системе не удалось найти представителей торговой точки (птт) у которых вы можете подписать  электронно-контрольный лист (экл). \n" +
                                "Для того что бы просмотреть список всех ТПП зарегистрированных на данной Торговой точке (ТТ) нажмите на кнопку обновить \n" +
                                "Если нужный вам птт в этом списке отсутствует нажмите кнопку + для того что бы зарегистрировать нового представителей торговой точки"
                    _uiItemsHeader.value = repository.toItemUIList(
                        AdditionalRequirementsDB::class,
                        listOf(header),
                        contextUI,
                        null
                    )
                        .map {
                            val selected =
                                (it.rawObj.firstOrNull { it is AdditionalRequirementsDB } as? AdditionalRequirementsDB)?.id == AchievementDataHolder.instance().requirementClientId
                            it.copy(selected = selected)
                        }

                }

                val filterUsersSDB = ItemFilter(
                    "Доп. фильтр",
                    UsersSDB::class,
                    UsersSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "Спiвробiтник",
                    "subTitle",
                    "fio",
                    "fio",
                    data.map { it.fio.toString() },
                    data.map { it.fio },
                    true
                )

                filters = Filters(
                    rangeDataByKey = null,
                    searchText = "",
                    items = mutableListOf(
                        filterUsersSDB,
                        addressSDB,
                    )
                )
                if (filters?.items?.firstOrNull { it.clazz == table }?.rightValuesRaw == null)
                    Log.e("updateFilters", "!!!!!!!! isNull !!!!!!!!!")
            } else {
                Log.e("updateFilters", "!!!!!!!! isEmpty !!!!!!!!!")
            }
        } catch (e: Exception) {
            super.updateFilters()
        }
    }

    override suspend fun getItems(): List<DataItemUI> {

        val dataJsonObject = readInputParameters()

        val addrId = dataJsonObject.stringOrNull("addr_id")?.toIntOrNull()
            ?: EKLDataHolder.instance().usersPTTWorkAddressId

        val usersSDBList: List<UsersSDB> = if (addrId != null) {
            RoomManager.SQL_DB.usersDao().getPTT(addrId)
        } else {
            RoomManager.SQL_DB.usersDao().all2
        }

        val data = repository.toItemUIList(TradeMarkDB::class, usersSDBList, contextUI, null)
            .map {
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains((it.rawObj.firstOrNull { it is UsersSDB } as? UsersSDB)?.fio.toString())
                it.copy(selected = selected == true)
            }
        Log.e("getItems", "data.sise: ${data.size}")

        return data
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        Log.e("onSelectedItemsUI", "+ !!!!!!!!!!!!!!!!!!!!!!")
        (itemsUI.first().rawObj.firstOrNull { it is UsersSDB } as? UsersSDB)?.let {
            Log.e("onSelectedItemsUI", "+ 1")
            EKLDataHolder.instance().usersPTTName = it.fio
            EKLDataHolder.instance().usersPTTid = it.id
            EKLDataHolder.instance().usersPTTNumberTel1 = it.tel
            EKLDataHolder.instance().usersPTTNumberTel2 = it.tel2
            EKLDataHolder.instance().usersPTTOtdelId = it.otdelId

            Log.e("onSelectedItemsUI", "it.fio: ${it.fio}")
            Log.e("onSelectedItemsUI", "usersPTTName: ${EKLDataHolder.instance().usersPTTName}")
        }


        FilteringDialogDataHolder.instance().filters.apply {
            this?.let { filters ->
                filters.items = filters.items.map { itemFilter ->
                    if (itemFilter.clazz == table) {
                        val rightValuesRaw = mutableListOf<String>()
                        val rightValuesUI = mutableListOf<String>()
                        itemsUI.forEach {
                            (it.rawObj.firstOrNull() as? UsersSDB)?.let {
                                Log.e("onSelectedItemsUI", "+ 2")
                                EKLDataHolder.instance().usersPTTName = it.fio
                                EKLDataHolder.instance().usersPTTid = it.id
                                Log.e("onSelectedItemsUI", "1 it.fio: ${it.fio}")
                                Log.e(
                                    "onSelectedItemsUI",
                                    "1 usersPTTName: ${EKLDataHolder.instance().usersPTTName}"
                                )

                                rightValuesRaw.add(it.fio.toString())
                                rightValuesUI.add(it.fio)
                            }
                        }
                        itemFilter.copy(
                            rightValuesRaw = rightValuesRaw,
                            rightValuesUI = rightValuesUI
                        )
                    } else {
                        itemFilter
                    }
                }
            }
        }
    }

}
