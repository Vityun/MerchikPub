package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.dataLayer.common.VizitShowcaseDataHolder
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class ShowcaseDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private val planogrammId = mutableStateOf(0)

    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        val dialogFullPhoto = DialogFullPhotoR(context)
        dialogFullPhoto.setPhoto(stackPhotoDB)
        comment?.let { dialogFullPhoto.setComment(it) }
        dialogFullPhoto.hideCamera()
        dialogFullPhoto.setClose { dialogFullPhoto.dismiss() }
        dialogFullPhoto.show()
    }

    override fun updateFilters() {
        when (contextUI) {
            ContextUI.SHOWCASE,
            -> {

//                val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
//                val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
//                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
//                )

                try {

                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)

                    val codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()
                    planogrammId.value = dataJsonObject.get("planogrammVizitShowcaseId").asInt
                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    )


                    val filterWpDataDB = ItemFilter(
                        "Адреса",
                        AddressSDB::class,
                        AddressSDBViewModel::class,
                        ModeUI.MULTI_SELECT,
                        "title",
                        "subTitle",
                        "addr_id",
                        "addr_id",
                        mutableListOf(wpDataDB.addr_id.toString()),
                        mutableListOf(wpDataDB.addr_txt),
                        false
                    )


                    val filterImagesTypeListDB = ItemFilter(
                        "Клиент",
                        CustomerSDB::class,
                        CustomerSDBViewModel::class,
                        ModeUI.MULTI_SELECT,
                        "title",
                        "subTitle",
                        "client_id",
                        "client_id",
                        mutableListOf(wpDataDB.client_id),
                        mutableListOf(wpDataDB.client_txt),
                        true
                    )

                    filters = Filters(
                        rangeDataByKey = null,
                        searchText = "",
                        items = mutableListOf(
                            filterWpDataDB,
                            filterImagesTypeListDB
                        )
                    )

                } catch (e: Exception) {
                    Log.e("!!!!!", "err: ${e.message}")
                }
            }

            else -> {}
        }
    }

    override fun getItems(): List<DataItemUI> {
        return try {
            when (contextUI) {
                ContextUI.SHOWCASE
                -> {
//                    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                    val codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()
                    val id = dataJsonObject.get("planogrammVizitShowcaseId").asInt

                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    )
                    val list = mutableListOf(0, 1, 2)
                    val showcaseDataList = RoomManager.SQL_DB.showcaseDao().getByDocTP(
                        wpDataDB!!.client_id, wpDataDB.addr_id, list
                    )

                    val ids = showcaseDataList.map { it.photoId?.toString() }.toTypedArray()

                    val data: List<StackPhotoDB> =
                        RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getByIds2(ids))

                    repository.toItemUIList(StackPhotoDB::class, data, contextUI, 0)
                        .map {
//                            val selected = FilteringDialogDataHolder.instance()
//                                .filters
//                                ?.items
//                                ?.firstOrNull { it.clazz == table }
//                                ?.rightValuesRaw
//                                ?.contains((it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.id.toString())
//                            it.copy(selected = selected == true)

                            val photoId: String = VizitShowcaseDataHolder.getInstance()[id].showcasePhotoId.toString()
                            val justId = (it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.photoServerId.toString()
                            val selected =
                                justId == photoId
                            it.copy(selected = selected)
                        }
                }

                else -> {
                    emptyList()
                }
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.let {
            when (contextUI) {
                ContextUI.SHOWCASE -> {
                    val dataHolder = VizitShowcaseDataHolder.getInstance()
                    dataHolder[planogrammId.value].showcaseId = it.showcase_id.toInt()
                    dataHolder[planogrammId.value].showcasePhotoId = it.photoServerId.toInt()

//                    VizitShowcaseDataHolder.instance().planogrammVizitMap[planogrammId.value] =
//                        it.photoServerId
//                    it.getShowcase_id()
//                    VizitShowcaseDataHolder.instance().photoId = it.id
//                    VizitShowcaseDataHolder.instance().photoServerId = it.photoServerId
                }

                else -> {}
            }

        }
    }

}