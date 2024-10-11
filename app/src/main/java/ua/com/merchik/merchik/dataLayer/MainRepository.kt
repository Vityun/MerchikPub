package ua.com.merchik.merchik.dataLayer

import android.util.Log
import com.google.gson.Gson
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.BonusSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB
import ua.com.merchik.merchik.data.Database.Room.SettingsUISDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.Main.SettingsUI
import kotlin.reflect.KClass

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

    private fun getSettingsUI(clazz: Class<*>, contextUI: ContextUI?) =
        try {
            Gson().fromJson(RoomManager.SQL_DB.settingsUIDao()
                .getTableByContext(clazz.simpleName, contextUI?.name)?.settingsJson, SettingsUI::class.java)
        }catch (e: Exception) {
            null
        }

    fun <T: DataObjectUI> getSortingFields(klass: KClass<T>, contextUI: ContextUI?) =
        getSettingsUI(klass.java, contextUI)?.sortFields ?: emptyList()

    fun <T: DataObjectUI> getSettingsItemList(klass: KClass<T>, contextUI: ContextUI?, defaultHideUserFields: List<String>?): List<SettingsItemUI> {

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
            obj.getIdResImage()?.let {
                fields.add("id_res_image")
            }
            jsonObject.keys().forEach { key -> fields.add(key) }
            val hideUserFields = (getSettingsUI(obj::class.java, contextUI)?.hideFields ?: defaultHideUserFields)?.map{ it.trim() }
            val hidedFieldsOnUI = obj.getHidedFieldsOnUI().split(",").map { it.trim() }

            return fields
                .filter { !hidedFieldsOnUI.contains(it) }
                .map {
                    SettingsItemUI(
                        it,
                        when (it) {
                            "column_name" -> "Назва реквізитів"
                            "id_res_image" -> "Зображення"
                            else -> nameUIRepository.getTranslateString(it, obj.getFieldTranslateId(it))
                        },
                        hideUserFields?.contains(it) != true,
                        0
                    )
                }
        } ?: return emptyList()
    }

    fun <T: DataObjectUI> saveSettingsUI(klass: KClass<T>, settingsUI: SettingsUI, contextUI: ContextUI?){
        val contextTAG = contextUI?.name ?: ContextUI.DEFAULT.name

        val settingsUISDB = RoomManager.SQL_DB.settingsUIDao()
            .getTableByContext(klass.java.simpleName, contextTAG) ?: SettingsUISDB()

        settingsUISDB.contextTAG = contextTAG
        settingsUISDB.tableDB = klass.java.simpleName
        settingsUISDB.settingsJson = Gson().toJson(settingsUI)

        RoomManager.SQL_DB.settingsUIDao().insert(settingsUISDB)
    }

    fun <T: RealmObject> getAllRealm(kClass: KClass<T>, contextUI: ContextUI?, typePhoto: Int?): List<DataItemUI> {
        return getAllRealmDataObjectUI(kClass).toItemUI(kClass, contextUI, typePhoto)
    }


    fun <T: RealmObject> getByRangeDateRealmDataObjectUI(
        kClass: KClass<T>,
        fieldName: String,
        startTime: Long,
        endTime: Long,
        contextUI: ContextUI?,
        typePhoto: Int?
    ): List<DataItemUI> {
        var query = RealmManager.INSTANCE.where( kClass.java )
        query = query.greaterThanOrEqualTo("CoordTime", startTime)
        query = query.and().lessThanOrEqualTo("CoordTime", endTime)
        val results = query
            .sort(fieldName, Sort.DESCENDING) // Сортировка по убыванию
            .notEqualTo("CoordX", 0.0)
            .notEqualTo("CoordY", 0.0)
            .findAllAsync()

        return RealmManager.INSTANCE.copyFromRealm(results)
            .filter { it is DataObjectUI }
            .map { it as DataObjectUI }
            .toItemUI(kClass, contextUI, typePhoto)
    }


    fun <T: RealmObject> getAllRealmDataObjectUI(kClass: KClass<T>): List<DataObjectUI> {
        return RealmManager.INSTANCE
            .copyFromRealm(RealmManager.INSTANCE
                .where(kClass.java)
                .findAllAsync())
            .filter { it is DataObjectUI }
            .map { it as DataObjectUI }
    }

    fun <T: DataObjectUI> getAllRoom(kClass: KClass<T>, contextUI: ContextUI?, typePhoto: Int?): List<DataItemUI> {
        return getAllRoomDataObjectUI(kClass).toItemUI(kClass, contextUI, typePhoto)
    }

    fun <T: DataObjectUI> getAllRoomDataObjectUI(kClass: KClass<T>): List<DataObjectUI> {
        val roomManager = RoomManager.SQL_DB
        return when (kClass) {
            PlanogrammSDB::class-> roomManager.planogrammDao().all
            CustomerSDB::class -> roomManager.customerDao().all
            UsersSDB::class -> roomManager.usersDao().all2
            AddressSDB::class -> roomManager.addressDao().all
            else -> { return emptyList() }
        }
    }

    fun <T: DataObjectUI>toItemUIList(kClass: KClass<T>, data: List<DataObjectUI>, contextUI: ContextUI?, typePhoto: Int?): List<DataItemUI> {
        return data.map { it.toItemUI(nameUIRepository, getSettingsUI(kClass.java, contextUI)?.hideFields?.joinToString { "," }, typePhoto) }
    }

    private fun <T: DataObjectUI> List<T>.toItemUI(kClass: KClass<*>, contextUI: ContextUI?, typePhoto: Int?): List<DataItemUI> {
        return this.map { (it as DataObjectUI).toItemUI(nameUIRepository, getSettingsUI(kClass.java, contextUI)?.hideFields?.joinToString { "," }, typePhoto) }
    }

}

fun List<BonusSDB>.getBonusText(): Pair<String, Float> {
    val baseZP = 15000
    var result = ""
    var sumPrem = 0f
    this.forEach {
        val themeComment = ThemeRealm.getThemeById(it.themeId.toString()).comment
        val bonus = Math.round((it.percent.toFloatOrNull() ?: 0f) * baseZP)
        sumPrem += bonus
        result += "\n- $bonus грн. $themeComment"
    }
    return result to sumPrem
}

fun List<DataItemUI>.join(rightTable: List<DataItemUI>, query: String): List<DataItemUI> {
    val keyLeft = query.split(":")[0].trim().split("=")[0].trim()
    val keyRight = query.split(":")[0].trim().split("=")[1].trim()
    val expFields = query.split(":")[1].replace(" ", "").split(",")

    return this.map { itemLeftUI ->
        val joinedFields: MutableList<FieldValue> = mutableListOf()
        var itemRightUI: DataItemUI? = null
        itemLeftUI.fields.firstOrNull { it.key.equals(keyLeft, true) }?.let { fieldLeftUI ->
            itemRightUI = rightTable.firstOrNull { it.fields.firstOrNull { it.key.equals(keyRight, true) }?.value?.value == fieldLeftUI.value.value }
            expFields.forEach { expField ->
                itemRightUI?.fields?.firstOrNull { it.key.equals(expField, true) }?.let { fieldRightUI ->
                    joinedFields.add(
                        FieldValue(
                            "${fieldLeftUI.key}_${fieldRightUI.key}",
                            fieldRightUI.field,
                            fieldRightUI.value,
                        )
                    )
                }
            }
        }

        val newFields = mutableListOf<FieldValue>()
        newFields.addAll(itemLeftUI.fields)
        newFields.addAll(joinedFields)

        val rawObj = mutableListOf<DataObjectUI>()
        rawObj.addAll(itemLeftUI.rawObj)
        itemRightUI?.let {
            rawObj.addAll(it.rawObj)
        }

        itemLeftUI.copy(rawObj = rawObj, fields = newFields)
    }
}