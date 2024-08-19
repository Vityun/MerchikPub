package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class StackPhotoDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class

    override fun getItems(): List<DataItemUI> {
        return try
        {
            when (contextUI) {
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> {
                    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)

                    val typePhoto = if (contextUI == ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT) 14 else 0

                    val data = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhotosByDAD2(codeDad2, typePhoto))

                    repository.toItemUIList(StackPhotoDB::class, data, contextUI, typePhoto)
                        .map {
                            val photoId =
                                if (contextUI == ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT) AchievementDataHolder.instance().photoToId
                                else AchievementDataHolder.instance().photoAfterId
                            val selected = (it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.id == photoId
                            it.copy(selected = selected)
                        }
                }
                else -> { emptyList() }
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
                }
                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> {
                    AchievementDataHolder.instance().photoAfterId = it.id
                    AchievementDataHolder.instance().photoAfterURI = it.photo_num
                }
                else ->{}
            }

        }
    }

}