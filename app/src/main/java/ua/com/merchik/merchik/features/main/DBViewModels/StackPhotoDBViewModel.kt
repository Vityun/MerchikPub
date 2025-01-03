package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class StackPhotoDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class

    override fun updateFilters() {
        when (contextUI) {
            ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
            ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354,
            ContextUI.STACK_PHOTO_FROM_OPTION_158605
            -> {
                val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
                val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                )

                val filterWpDataDB = ItemFilter(
                    "Відвідування",
                    WpDataDB::class,
                    WpDataDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "code_dad2",
                    "code_dad2",
                    mutableListOf(wpDataDB.code_dad2.toString()),
                    mutableListOf(wpDataDB.code_dad2.toString()),
                    false
                )

                val typePhotoId = when (contextUI) {
                    ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT -> 14
                    ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> 0
                    ContextUI.STACK_PHOTO_FROM_OPTION_158605 -> 40
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 26
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354 -> 42
                    else -> 0 // Резервное значение, не должно использоваться
                }

                val imagesType =
                    RealmManager.INSTANCE.copyFromRealm(PhotoTypeRealm.getPhotoTypeById(typePhotoId))
                val filterImagesTypeListDB = ItemFilter(
                    "Тип фото",
                    ImagesTypeListDB::class,
                    ImagesTypeListDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "photo_type",
                    "id",
                    mutableListOf(imagesType.id.toString()),
                    mutableListOf(imagesType.nm),
                    true
                )

                filters = Filters(
//            rangeDataByKey = RangeDate("dt", LocalDate.now().minusYears(55), LocalDate.now(), false),
                    rangeDataByKey = null,
                    searchText = "",
                    items = mutableListOf(
                        filterWpDataDB,
                        filterImagesTypeListDB
                    )
                )
            }

            else -> {}
        }
    }

    override fun getItems(): List<DataItemUI> {
        return try {
            when (contextUI) {
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354,
                ContextUI.STACK_PHOTO_FROM_OPTION_158605
                -> {
                    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    )

                    val typePhoto = when (contextUI) {
                        ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT -> 14
                        ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> 0
                        ContextUI.STACK_PHOTO_FROM_OPTION_158605 -> 40
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 26
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354 -> 42
                        else -> 0 // Резервное значение, не должно использоваться
                    }


//                    val data = when (typePhoto) {
//                        10, 31 -> {
//                            RealmManager.INSTANCE.copyFromRealm(
//                                StackPhotoRealm.getPhotoByAddrCustomer(
//                                    wpDataDB.addr_id,
//                                    wpDataDB.client_id,
//                                    typePhoto
//                                )
//                            )
//                        }
//
//                        else -> {
//                            RealmManager.INSTANCE.copyFromRealm(
//                                StackPhotoRealm.getPhotosByDAD2(
//                                    codeDad2,
//                                    typePhoto
//                                )
//                            )
//                        }
//                    }

                    val data = RealmManager.INSTANCE.copyFromRealm(
                                StackPhotoRealm.getPhotosByDAD2(
                                    codeDad2,
                                    typePhoto
                                ))
                    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")

                    for (i in data) {
                        Log.e("~~~~~~~~~~~","~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                        Log.e("!!!!!!!!!!!", "id: ${i.id}")
                        Log.e("!!!!!!!!!!!", "PhotoServerId: ${i.getPhotoServerId()}")
                        Log.e("!!!!!!!!!!!", "photo_user_id: ${i.photo_user_id}")
                        Log.e("!!!!!!!!!!!", "photo_type: ${i.photo_type}")
                        Log.e("!!!!!!!!!!!", "time: ${format.format(i.dt * 1000)}")
                        Log.e("!!!!!!!!!!!", "photo_num: ${i.photo_num}")

                    }
                    Log.e("!!!!!!!!!!!", "data: ${data.size}")

                    repository.toItemUIList(StackPhotoDB::class, data, contextUI, typePhoto)
                        .map {
                            val photoId =
                                if (contextUI == ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT) AchievementDataHolder.instance().photoToId
                                else AchievementDataHolder.instance().photoAfterId
                            val selected =
                                (it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.id == photoId
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
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT -> {
                    AchievementDataHolder.instance().photoToId = it.id
                    AchievementDataHolder.instance().photoToURI = it.photo_num
                    AchievementDataHolder.instance().photoHashTo = it.photo_hash
                }

                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> {
                    AchievementDataHolder.instance().photoAfterId = it.id
                    AchievementDataHolder.instance().photoAfterURI = it.photo_num
                    AchievementDataHolder.instance().photoHashAfter = it.photo_hash
                }

                else -> {}
            }

        }
    }

}