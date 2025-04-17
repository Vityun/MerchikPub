package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.WorkPlan
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.WPDataObj
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm
import ua.com.merchik.merchik.database.realm.tables.TovarRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
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

    private val EXAMPLE_ID = "id_1c"
    private val EXAMPLE_IMG_ID = "photo_id"

    override val table: KClass<out DataObjectUI>
        get() = SamplePhotoSDB::class

    override fun getFieldsForCommentsImage(): List<String>? {
        return "nm, about".split(",").map { it.trim() }
    }

    override fun getFieldsForCustomResult(): List<String>? {
        return "$EXAMPLE_ID, $EXAMPLE_IMG_ID".split(",").map { it.trim() }
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        super.onClickItemImage(clickedDataItemUI, context)
        dialog?.setCamera {
            openCamera(null) {
                dialog?.dismiss()
            }
        }
    }

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        try {
            val dialogFullPhoto = DialogFullPhotoR(context)
            dialogFullPhoto.setPhoto(stackPhotoDB)

            // Pika
            comment?.let { dialogFullPhoto.setComment(it) }

            dialogFullPhoto.setCamera {
                openCamera(stackPhotoDB) {
                    dialogFullPhoto.dismiss()
                }
            }

            dialogFullPhoto.setClose { dialogFullPhoto.dismiss() }
            dialogFullPhoto.show()
        } catch (e: Exception) {
            Log.e("ShowcaseAdapter", "Exception e: $e")
        }
    }

    private fun openCamera(stackPhotoDB: StackPhotoDB?, callback: () -> Unit) {
        val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
        val wpDataDB =
            RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowById(dataJsonObject.get("wpDataDBId").asString.toLong()))
        val id  = dataJsonObject.get("optionDBId").asString
        val optionDB =
            RealmManager.INSTANCE.copyFromRealm(OptionsRealm.getOptionById(dataJsonObject.get("optionDBId").asString))
        if (wpDataDB != null && optionDB != null) {
            val typePhotoId = when (contextUI) {
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355 -> 5
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_135809 -> 14
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 28
                else -> {
                    null
                }
            }

            when (contextUI) {
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> {
                    val req = if (stackPhotoDB == null) {
                        TovarRequisites()
                    } else {
                        var reportPrepareDB: ReportPrepareDB? = null
                        val tovarDB = TovarRealm.getById(stackPhotoDB.tovar_id)
                        if (tovarDB != null)
                            reportPrepareDB = ReportPrepareRealm.getReportPrepareByTov(
                                wpDataDB.code_dad2.toString(),
                                stackPhotoDB.tovar_id
                            )

                        if (tovarDB == null || reportPrepareDB == null)
                            TovarRequisites()
                        else
                            TovarRequisites(tovarDB, reportPrepareDB)
                    }

                    req
                        .createDialog(
                            context,
                            wpDataDB,
                            optionDB
                        ) {}
                        .show()

                    callback.invoke()
                }

                ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_135809,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355
                -> {
                    typePhotoId?.let {
                        val workPlan = WorkPlan()
                        val wpDataObj: WPDataObj = workPlan.getKPS(wpDataDB.id)
                        wpDataObj.setPhotoType(it.toString())
                        val makePhoto = MakePhoto()
                        val custom: HashMap<String, Any?> = valueForCustomResult.value
                        // Проверяем и передаем example_id
                        custom[EXAMPLE_ID]?.let {
                            if (it.toString().isNotEmpty()) {
                                MakePhoto.example_id = it.toString()
                            }
                        }

                        // Проверяем и передаем example_img_id
                        custom[EXAMPLE_IMG_ID]?.let {
                            if (it.toString().isNotEmpty()) {
                                MakePhoto.example_img_id = it.toString()
                            }
                        }

                        makePhoto.pressedMakePhotoOldStyle<WpDataDB>(
                            context as Activity,
                            wpDataObj,
                            wpDataDB,
                            optionDB,
                            stackPhotoDB
                        )
                        callback.invoke()
                    }
                }

                else -> {}
            }
        }
    }

    override fun updateFilters() {

        val typePhotoId = when (contextUI) {
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355 -> 5
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_135809 -> 14
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 28
            else -> {
                null
            }
        }

        val itemsFilter = mutableListOf<ItemFilter>()

        typePhotoId?.let {
            val imagesType =
                RealmManager.INSTANCE.copyFromRealm(PhotoTypeRealm.getPhotoTypeById(it))
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
            itemsFilter.add(filterImagesTypeListDB)
        }

        try {
//                AddressSDB addr = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
//                TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));
//                groupText.setText(tradeMarkDB.getNm());

            val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
            val tradeMarkId = dataJsonObject.get("tradeMarkDBId").asString
            val tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(tradeMarkId.toString())

            val filterTradeMarkDB = ItemFilter(
                "Торгова марка",
                TradeMarkDB::class,
                TradeMarkDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "Торгова марка",
                "subTitle",
                "grp_id",
                "iD",
                mutableListOf(tradeMarkDB.id.toString(), "0"),
                mutableListOf(tradeMarkDB.nm, "Все не указанные"),
                true
            )
            itemsFilter.add(filterTradeMarkDB)
        } catch (e: Exception) {
        }

        filters = Filters(
            rangeDataByKey = null,
            searchText = "",
            items = itemsFilter
        )
    }

    override fun getDefaultHideUserFields(): List<String>? {
        return "abbr, grp_id, ID, photo_id, photo_tp, column_name".split(",")
    }

    override fun getItems(): List<DataItemUI> {
        val data = RoomManager.SQL_DB.samplePhotoDao().getPhotoLogActive(1)
        val typePhoto = 35
        return repository.toItemUIList(SamplePhotoSDB::class, data, contextUI, typePhoto)
    }
}