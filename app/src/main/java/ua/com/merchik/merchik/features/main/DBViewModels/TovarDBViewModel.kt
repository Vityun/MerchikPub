package ua.com.merchik.merchik.features.main.DBViewModels

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.join
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogMap
import ua.com.merchik.merchik.features.main.Filters
import ua.com.merchik.merchik.features.main.MainViewModel
import ua.com.merchik.merchik.features.main.RangeDate
import java.time.LocalDate
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class TovarDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val title: String
        get() = "Історія місцеположення"

    override val subTitle: String
        get() = "Подзаголовок Подзаг Подво Подво Подзаголовок njh"

    override val idResImage: Int
        get() = R.drawable.ic_caution

    override val contextUI: ContextUI
        get() = ContextUI.ONE_SELECT

    override val table: KClass<out DataObjectUI>
        get() = TovarDB::class

    override fun getItems(): List<ItemUI> {
        return repository.getAllRealm(TovarDB::class, contextUI)
    }

    override fun onSelectedItemsUI(itemsUI: List<ItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.getiD()?.let {
            AchievementDataHolder.instance().tovarId = it.toInt()
        }
    }

}