package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class SamplePhotoSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = SamplePhotoSDB::class

    override fun updateFilters() {
        when (contextUI) {
            ContextUI.SAMPLE_PHOTO_FROM_OST_TOVARA -> {
                val typePhotoId = 4
                val imagesType = RealmManager.INSTANCE.copyFromRealm(PhotoTypeRealm.getPhotoTypeById(typePhotoId))
                val filterImagesTypeListDB = ItemFilter(
                    "Тип фото",
                    ImagesTypeListDB::class,
                    ImagesTypeListDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "photo_tp",
                    "id",
                    mutableListOf(imagesType.id.toString()),
                    mutableListOf(imagesType.nm),
                    true
                )


//                AddressSDB addr = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
//                TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));
//                groupText.setText(tradeMarkDB.getNm());

                val tradeMarkId = Gson().fromJson(dataJson, String::class.java)
                val tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(tradeMarkId.toString())

                val filterTradeMarDB = ItemFilter(
                    "Торгова марка",
                    TradeMarkDB::class,
                    TradeMarkDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "Торгова марка",
                    "subTitle",
                    "grp_id",
                    "iD",
                    mutableListOf(tradeMarkDB.id.toString()),
                    mutableListOf(tradeMarkDB.nm),
                    true
                )

                filters = Filters(
                    rangeDataByKey = null,
                    searchText = "",
                    items = mutableListOf(
                        filterImagesTypeListDB,
                        filterTradeMarDB
                    )
                )
            }
            else -> {}
        }
    }

    override fun getDefaultHideUserFields(): List<String>? {
        return "abbr, grp_id, ID, photo_id, photo_tp, column_name".split(",")
    }

    override fun getItems(): List<DataItemUI> {
        val data = RoomManager.SQL_DB.samplePhotoDao().getPhotoLogActive(1)
        return repository.toItemUIList(TovarDB::class, data, contextUI, 35)
    }
}