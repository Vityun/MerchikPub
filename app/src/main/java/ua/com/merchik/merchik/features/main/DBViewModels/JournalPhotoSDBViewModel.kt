package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogAdapter
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.dialogs.features.InfoDialogBuilder
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.InfoDialog
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.RangeDate
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class JournalPhotoSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {


    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        super.onClickItemImage(clickedDataItemUI, context)
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        super.onClickItem(itemUI, context)
        val imageFields = itemUI.rawObj[0].getFieldsImageOnUI().split(",")
        val jsonObject = JSONObject(Gson().toJson(itemUI.rawObj[0]))
        var photoLogData: StackPhotoDB? = null

        imageFields.getOrNull(0)?.takeIf { it.isNotEmpty() }?.let { fieldKey ->
            RealmManager.getPhotoById(null, jsonObject.get(fieldKey.trim()).toString())
                ?.let { photo ->
                    photoLogData = photo
                }
        }
        InfoDialogBuilder(context)
            .setTitle("Детальна інформація")
            .setMessage(PhotoLogAdapter.photoData(photoLogData))
            .show()
    }

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        try {
            val dialogFullPhoto = DialogFullPhotoR(context)
            dialogFullPhoto.setPhoto(stackPhotoDB)

            // Pika
            comment?.let { dialogFullPhoto.setComment(it) }


            dialogFullPhoto.setClose { dialogFullPhoto.dismiss() }
            dialogFullPhoto.show()
        } catch (e: Exception) {
            Log.e("ShowcaseAdapter", "Exception e: $e")
        }
    }

    override fun updateFilters() {

        try {

            val typePhoto = RealmManager.INSTANCE.copyFromRealm(PhotoTypeRealm.getPhotoType())

            val typePhotoFilter = ItemFilter(
                "Тип фото",
                ImagesTypeListDB::class,
                ImagesTypeListDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "title",
                "subTitle",
                "photo_type",
                "id",
                typePhoto.map { it.id.toString() },
                typePhoto.map { it.nm },
                true
            )

            val client = RoomManager.SQL_DB.customerDao().all

            val clientFilter = ItemFilter(
                "Клієнт",
                CustomerSDB::class,
                CustomerSDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "Додати Клієнта",
                "## sub title",
                "client_id",
                "id",
                client.map { it.id },
                client.map { it.nm },
                enabled = true
            )

            val adress = RoomManager.SQL_DB.addressDao().all

            val adressFilter = ItemFilter(
                "Адреса",
                AddressSDB::class,
                AddressSDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "## ADRESS",
                "## sub title",
                "addr_id",
                "id",
                adress.map { it.id.toString() },
                adress.map { it.nm },
                enabled = true
            )

            val users = RoomManager.SQL_DB.usersDao().all2

            val userFilter = ItemFilter(
                "Виконавець",
                UsersSDB::class,
                UsersSDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "## USER",
                "## sub title",
                "user_id",
                "id",
                users.map { it.id.toString() },
                users.map { it.fio },
                contextUI == ContextUI.WP_DATA_IN_CONTAINER
            )


            filters = Filters(
                searchText = "",
                items = mutableListOf(typePhotoFilter,
                    clientFilter,
                    adressFilter,
                    userFilter),
                rangeDataByKey = null
//                    RangeDate(
//                    key = "dt",
//                    start = rangeDataStart.value,
//                    end = rangeDataEnd.value,
//                    enabled = true
//                )
            )
        } catch (e: Exception) {
            Log.e("!", "error: ${e.message}")
            filters
        }
    }


    override fun getDefaultHideUserFields(): List<String> {
        return ("approve, code_dad2, dviUpload, errorTime, dvi, upload_status, " +
                "get_on_server, id, markUpload, object_id, photo_hash, photo_num, " +
                "premiyaUpload, specialCol, comment, commentUpload, upload_time, upload_to_server, vpi, " +
                "dt, photoServerURL, showcase_id, time_event, tovar_id, photo_typeTxt, code_iza, " +
                "example_id, example_img_id, planogram_id, planogram_img_id, gp, premiya, photoServerId," +
                "img_src_id").split(",")
    }

    override suspend fun getItems(): List<DataItemUI> {
        val data = RealmManager.INSTANCE.copyFromRealm(RealmManager.getStackPhoto())
        return repository.toItemUIList(SamplePhotoSDB::class, data, contextUI, null)
    }
}