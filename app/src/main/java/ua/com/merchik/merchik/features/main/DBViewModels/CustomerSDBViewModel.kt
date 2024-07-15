package ua.com.merchik.merchik.features.main.DBViewModels

import android.content.Context
import android.text.Html
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.database.realm.tables.AddressRealm
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.UsersRealm
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark
import ua.com.merchik.merchik.features.main.Filters
import ua.com.merchik.merchik.features.main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class CustomerSDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val idResImage: Int?
        get() = R.drawable.merchik

    override val title: String
        get() = "Справочник: Клиенты"

    override val contextUI: ContextUI
        get() = ContextUI.DEFAULT

    override val table: KClass<out DataObjectUI>
        get() = CustomerSDB::class

    override fun getItems(): List<ItemUI> {
        val data = repository.getAllRoom(CustomerSDB::class, contextUI)
        return data
    }

    override fun onClickItem(itemUI: ItemUI, context: Context) {
    }
}