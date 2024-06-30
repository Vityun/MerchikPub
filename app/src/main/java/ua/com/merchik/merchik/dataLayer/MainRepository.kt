package ua.com.merchik.merchik.dataLayer

import android.util.Log
import com.google.gson.Gson
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB
import ua.com.merchik.merchik.data.Database.Room.SettingsUISDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun <T : RealmObject> RealmResults<T>.toFlow(): Flow<RealmResults<T>> = callbackFlow {
    val listener = RealmChangeListener<RealmResults<T>> { results ->
        trySend(results).isSuccess
    }
    addChangeListener(listener)
    awaitClose { removeChangeListener(listener) }
}

class MainRepository(
//    private val databaseRoom: AppDatabase,
//    private val databaseRealm: Realm,
    val nameUIRepository: NameUIRepository
) {

    private fun getHideUserFields(clazz: Class<*>, contextUI: ContextUI?) =
        RoomManager.SQL_DB.settingsUIDao().getTableByContext(clazz.simpleName, contextUI?.name)?.settingsJson

    fun <T: DataObjectUI> getSettingsItemList(klass: KClass<T>, contextUI: ContextUI?): List<SettingsItemUI> {

        val item = (klass.java.newInstance() as? RealmObject)?.let {
            (RealmManager.INSTANCE
                .where(it::class.java)
                .findFirst()?.let {
                    RealmManager.INSTANCE
                        .copyFromRealm(it)
                } as? DataObjectUI)
        } ?: run {
            val roomManager = RoomManager.SQL_DB
            when (klass) {
                PlanogrammSDB::class-> roomManager.planogrammDao().all.first() as DataObjectUI
                CustomerSDB::class -> roomManager.customerDao().all.first() as DataObjectUI
                UsersSDB::class -> roomManager.usersDao().all2.first() as DataObjectUI
                AddressSDB::class -> roomManager.addressDao().all.first() as DataObjectUI
                else -> { null }
            }
        }

        item?.let { obj ->
            val jsonObject = JSONObject(Gson().toJson(obj))
            val fields = mutableListOf<String>()
            fields.add("column_name")
            jsonObject.keys().forEach { key -> fields.add(key) }
            val hideUserFields = getHideUserFields(obj::class.java, contextUI)
            val hidedFieldsOnUI = obj.getHidedFieldsOnUI()

            return fields
                .filter { !hidedFieldsOnUI.contains(it) }
                .map {
                    SettingsItemUI(
                        it,
                        if (it == "column_name") "Назва реквізитів" else nameUIRepository.getTranslateString(it, obj.getTranslateId(it)),
                        hideUserFields?.contains(it) != true
                    )
                }
        } ?: return emptyList()
    }

    fun <T: DataObjectUI> saveSettingsUI(klass: KClass<T>, settingsItemUI: List<SettingsItemUI>, contextUI: ContextUI?){
        val contextTAG = contextUI?.name ?: ContextUI.DEFAULT.name

        val settingsUISDB = RoomManager.SQL_DB.settingsUIDao()
            .getTableByContext(klass.java.simpleName, contextTAG) ?: SettingsUISDB()

        settingsUISDB.contextTAG = contextTAG
        settingsUISDB.tableDB = klass.java.simpleName
        settingsUISDB.settingsJson = settingsItemUI.filter { !it.isEnabled }.map { it.key }.toString()

        RoomManager.SQL_DB.settingsUIDao().insert(settingsUISDB)
    }

    fun <T: RealmObject> getAllRealm(kClass: KClass<T>, contextUI: ContextUI?): List<ItemUI> {
        return RealmManager.INSTANCE
            .copyFromRealm(RealmManager.INSTANCE
                .where(kClass.java)
                .findAllAsync())
            .filter { it is DataObjectUI }
            .map { (it as DataObjectUI).toItemUI(nameUIRepository, getHideUserFields(kClass.java, contextUI)) }
    }

    fun <T: DataObjectUI> getAllRoom(kClass: KClass<T>, contextUI: ContextUI?): List<ItemUI> {
        val roomManager = RoomManager.SQL_DB
        return when (kClass) {
            PlanogrammSDB::class-> roomManager.planogrammDao().all
            CustomerSDB::class -> roomManager.customerDao().all
            UsersSDB::class -> roomManager.usersDao().all2
            AddressSDB::class -> roomManager.addressDao().all
            else -> { return emptyList() }
        }.map { it.toItemUI(nameUIRepository, getHideUserFields(kClass.java, contextUI)) }
    }

}