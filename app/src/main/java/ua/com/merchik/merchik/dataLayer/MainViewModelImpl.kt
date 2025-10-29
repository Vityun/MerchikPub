package ua.com.merchik.merchik.dataLayer


import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm
import ua.com.merchik.merchik.features.main.DBViewModels.addWpDataFieldsMandatory
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(
    application = application,
    repository = repository,
    nameUIRepository = nameUIRepository,
    savedStateHandle = savedStateHandle
) {

    override val table: KClass<out DataObjectUI>
        get() = LogMPDB::class

    override suspend fun getItems(): List<DataItemUI> {
        var startTime = System.currentTimeMillis()
        var endTime = System.currentTimeMillis()


        val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

        wpDataDB.addr_txt
        wpDataDB.addr_location_xd
        wpDataDB.addr_location_yd
        val validTime = 1800

        startTime = if ((wpDataDB.visit_start_dt > 0) && wpDataDB.visit_end_dt > 0)
            wpDataDB.visit_start_dt - validTime
        else
            (System.currentTimeMillis() / 1000) - validTime

        endTime =
            if (wpDataDB.visit_end_dt > 0) wpDataDB.visit_end_dt else System.currentTimeMillis() / 1000


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
}
