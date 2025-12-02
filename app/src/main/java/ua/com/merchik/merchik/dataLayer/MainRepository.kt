package ua.com.merchik.merchik.dataLayer

import android.util.Log
import com.google.gson.Gson
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.BonusSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSPlanSDB
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.Database.Room.SettingsUISDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.Database.Room.VacancySDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.Main.SettingsUI
import ua.com.merchik.merchik.features.main.Main.SortingField
import kotlin.reflect.KClass

fun <T : RealmObject> RealmResults<T>.toFlow(): Flow<RealmResults<T>> = callbackFlow {
    val listener = RealmChangeListener<RealmResults<T>> { results ->
        trySend(results).isSuccess
    }
    addChangeListener(listener)
    awaitClose { removeChangeListener(listener) }
}


inline fun <reified T : RealmObject> Realm.hasData(): Boolean {
    return where(T::class.java).findFirst() != null
}


class MainRepository(
//    private val databaseRoom: AppDatabase,
//    private val databaseRealm: Realm,
    val nameUIRepository: NameUIRepository
) {

    private fun getSettingsUI(clazz: Class<*>, contextUI: ContextUI?) =
        try {
            val json = RoomManager.SQL_DB.settingsUIDao()
                .getTableByContext(clazz.simpleName, contextUI?.name)?.settingsJson

            Gson().fromJson(json, SettingsUI::class.java)

        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "MainRepository.getSettingsUI", "error: ${e.message}")

            null
        }

    fun <T : DataObjectUI> getSortingFields(klass: KClass<T>, contextUI: ContextUI?) =
        getSettingsUI(klass.java, contextUI)?.sortFields ?: emptyList()

    fun <T : DataObjectUI> getSettingsItemList(
        klass: KClass<T>,
        contextUI: ContextUI?,
        defaultHideUserFields: List<String>?
    ): List<SettingsItemUI> {

        val item = (klass.java.newInstance() as? RealmObject)?.let {
            (RealmManager.INSTANCE
                .where(it::class.java)
                .findFirst()?.let {
                    RealmManager.INSTANCE
                        .copyFromRealm(it)
                } as? DataObjectUI)
        } ?: run {
            val roomManager = RoomManager.SQL_DB
            runCatching {
                when (klass) {
                    PlanogrammSDB::class -> roomManager.planogrammDao().all.firstOrNull() as DataObjectUI
                    CustomerSDB::class -> roomManager.customerDao().all.firstOrNull() as DataObjectUI
                    UsersSDB::class -> roomManager.usersDao().all2.firstOrNull() as DataObjectUI
                    VacancySDB::class -> roomManager.vacancyDao().all.firstOrNull() as DataObjectUI
                    SamplePhotoSDB::class -> roomManager.samplePhotoDao().all.firstOrNull() as DataObjectUI
                    AddressSDB::class -> roomManager.addressDao().all.firstOrNull() as DataObjectUI
                    SMSPlanSDB::class -> roomManager.smsPlanDao().all.firstOrNull() as DataObjectUI
                    else -> null
                }
            }.getOrNull()
        }

        item?.let { obj ->
            val jsonObject = JSONObject(Gson().toJson(obj))
            val fields = mutableListOf<String>()
            fields.add("column_name")
            fields.add("group_header")
            obj.getIdResImage()?.let {
                fields.add("id_res_image")
            }
            jsonObject.keys().forEach { key -> fields.add(key) }

            // 👇 сначала забираем сохранённые настройки (если есть)
            val settingsUI = getSettingsUI(obj::class.java, contextUI)

            // базовый список скрытых (или дефолтный)
            val baseHide = settingsUI?.hideFields ?: defaultHideUserFields

            // 👉 если пользователь ещё ничего не настраивал (settingsUI == null),
            //    НЕ скрываем group_header, он по умолчанию включён
            val hideUserFields: List<String>? = if (settingsUI == null) {
                baseHide
                    .orEmpty()
                    .filterNot {
                        it.equals(
                            "group_header",
                            ignoreCase = true
                        )
                    } // <- убираем из скрытых
                    .map { it.trim() }
            } else {
                baseHide
                    ?.map { it.trim() }
            }


            val hidedFieldsOnUI = obj.getHidedFieldsOnUI().split(",").map { it.trim() }

            return fields
                .filter { field ->
                    hidedFieldsOnUI.none { it == field }
                }
                .map { key ->
                    SettingsItemUI(
                        key,
                        when (key) {
                            "column_name" -> "Назва реквізитів"
                            "id_res_image" -> "Зображення"
                            "group_header" -> "Заголовок групи"
                            else -> nameUIRepository.getTranslateString(
                                key,
                                obj.getFieldTranslateId(key)
                            )
                        },
                        // 👇 логика видимости оставлена как была
                        hideUserFields?.contains(key) != true,
                        0
                    )
                }
        } ?: return emptyList()
    }

    fun <T : DataObjectUI> saveSettingsUI(
        klass: KClass<T>,
        settingsUI: SettingsUI,
        contextUI: ContextUI?
    ) {
//        Log.e("!!!!!!TEST!!!!!!","saveSettingsUI: start")

        val contextTAG = contextUI?.name ?: ContextUI.DEFAULT.name

        val settingsUISDB = RoomManager.SQL_DB.settingsUIDao()
            .getTableByContext(klass.java.simpleName, contextTAG) ?: SettingsUISDB()

        settingsUISDB.contextTAG = contextTAG
        settingsUISDB.tableDB = klass.java.simpleName
        settingsUISDB.settingsJson = Gson().toJson(settingsUI)

        RoomManager.SQL_DB.settingsUIDao().insert(settingsUISDB)
//        Log.e("!!!!!!TEST!!!!!!","saveSettingsUI: finish")
    }

    fun <T : DataObjectUI> getSortingFields(
        klass: KClass<T>,
        contextUI: ContextUI?,
        defaultSortKeys: List<String>? = null
    ): List<SortingField> {
        // 1) если в настройках уже есть сортировка — отдаем её
        getSettingsUI(klass.java, contextUI)?.sortFields?.let { saved ->
            if (!saved.isNullOrEmpty()) return saved
        }

        // 2) иначе пробуем собрать из дефолтных ключей
        if (defaultSortKeys.isNullOrEmpty()) return emptyList()

        // достанем "пример" объекта (как в getSettingsItemList), чтобы получить корректные title'ы
        val sample: DataObjectUI? =
            (klass.java.newInstance() as? RealmObject)?.let {
                (RealmManager.INSTANCE
                    .where(it::class.java)
                    .findFirst()?.let { found ->
                        RealmManager.INSTANCE.copyFromRealm(found)
                    } as? DataObjectUI)
            } ?: run {
                val room = RoomManager.SQL_DB
                runCatching {
                    when (klass) {
                        PlanogrammSDB::class -> room.planogrammDao().all.firstOrNull() as DataObjectUI
                        CustomerSDB::class -> room.customerDao().all.firstOrNull() as DataObjectUI
                        UsersSDB::class -> room.usersDao().all2.firstOrNull() as DataObjectUI
                        VacancySDB::class -> room.vacancyDao().all.firstOrNull() as DataObjectUI
                        SamplePhotoSDB::class -> room.samplePhotoDao().all.firstOrNull() as DataObjectUI
                        AddressSDB::class -> room.addressDao().all.firstOrNull() as DataObjectUI
                        SMSPlanSDB::class -> room.smsPlanDao().all.firstOrNull() as DataObjectUI
                        else -> null
                    }
                }.getOrNull()
            }

        // Собираем SortingField’ы по порядку ключей
        return defaultSortKeys
            .mapNotNull { rawKey ->
                val key = rawKey.trim().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                SortingField(
                    key = key,
                    title = sample?.let {
                        nameUIRepository.getTranslateString(
                            key,
                            it.getFieldTranslateId(key)
                        )
                    } ?: key,
                    order = 1
                )
            }
    }


    suspend fun hasUserSorting(
        table: KClass<out DataObjectUI>,
        contextUI: ContextUI?
    ): Boolean {
        val settings = getSettingsUI(
            table.java,
            contextUI
        )   // тот же способ, как ты сейчас грузишь SettingsUI
        return settings?.sortFields?.isNotEmpty() == true
    }


    fun <T : RealmObject> getAllRealm(
        kClass: KClass<T>,
        contextUI: ContextUI?,
        typePhoto: Int?
    ): List<DataItemUI> {
        Log.e("!!!!!!TEST!!!!!!", "getAllRealm: start")
        return getAllRealmDataObjectUI(kClass).toItemUI(kClass, contextUI, typePhoto)
    }


    fun <T : RealmObject> getByRangeDateRealmDataObjectUI(
        kClass: KClass<T>,
        fieldName: String,
        startTime: Long,
        endTime: Long,
        contextUI: ContextUI?,
        typePhoto: Int?
    ): List<DataItemUI> {
        var query = RealmManager.INSTANCE.where(kClass.java)
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


    fun <T : RealmObject> getAllRealmDataObjectUI(kClass: KClass<T>): List<DataObjectUI> {
        Log.e("!!!!!!TEST!!!!!!", "getAllRealmDataObjectUI: start")
        return RealmManager.INSTANCE
            .copyFromRealm(
                RealmManager.INSTANCE
                    .where(kClass.java)
                    .findAllAsync()
            )
            .filter { it is DataObjectUI }
            .map { it as DataObjectUI }
    }

    fun <T : DataObjectUI> getAllRoom(
        kClass: KClass<T>,
        contextUI: ContextUI?,
        typePhoto: Int?
    ): List<DataItemUI> {
        return getAllRoomDataObjectUI(kClass).toItemUI(kClass, contextUI, typePhoto)
    }

    fun <T : DataObjectUI> getAllRoomDataObjectUI(kClass: KClass<T>): List<DataObjectUI> {
        val roomManager = RoomManager.SQL_DB
        return when (kClass) {
            PlanogrammSDB::class -> roomManager.planogrammDao().all
            CustomerSDB::class -> roomManager.customerDao().all
            UsersSDB::class -> roomManager.usersDao().all2
            VacancySDB::class -> roomManager.vacancyDao().all
            SamplePhotoSDB::class -> roomManager.samplePhotoDao().all
            AddressSDB::class -> roomManager.addressDao().all
            OpinionSDB::class -> roomManager.opinionDao().all
            PlanogrammVizitShowcaseSDB::class -> roomManager.planogrammVizitShowcaseDao().all
            SMSPlanSDB::class -> roomManager.smsPlanDao().all
            else -> {
                return emptyList()
            }
        }
    }

    fun <T : DataObjectUI> toItemUIList(
        kClass: KClass<T>,
        data: List<DataObjectUI>,
        contextUI: ContextUI?,
        typePhoto: Int?,
        groupingKeys: List<String> = emptyList()
    ): List<DataItemUI> {
//        Log.e("!!!!!!TEST!!!!!!","getItems: end 0?")
//        Globals.writeToMLOG("INFO","MainRepository.toItemUIList","data size: ${data.size}")
        return data.map {
//            Globals.writeToMLOG("INFO","MainRepository.toItemUIList","data.map: $it")
            it.toItemUI(
                nameUIRepository,
                getSettingsUI(kClass.java, contextUI)?.hideFields?.joinToString { "," },
                typePhoto,
                groupingKeys
            )
        }
    }

    private fun <T : DataObjectUI> List<T>.toItemUI(
        kClass: KClass<*>,
        contextUI: ContextUI?,
        typePhoto: Int?
    ): List<DataItemUI> {
        Log.e("!!!!!!TEST!!!!!!", "getItems: end 1?")
//        Globals.writeToMLOG("INFO","MainRepository.toItemUI","toItemUI: $this" )
        return this.map {
            (it as DataObjectUI).toItemUI(
                nameUIRepository,
                getSettingsUI(kClass.java, contextUI)?.hideFields?.joinToString { "," },
                typePhoto
            )
        }
    }

}

fun List<BonusSDB>.getBonusText(): Pair<String, Float> {
    val baseZP = Globals.getAverageSalary()
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
            itemRightUI = rightTable.firstOrNull {
                it.fields.firstOrNull {
                    it.key.equals(
                        keyRight,
                        true
                    )
                }?.value?.value == fieldLeftUI.value.value
            }
            expFields.forEach { expField ->
                itemRightUI?.fields?.firstOrNull { it.key.equals(expField, true) }
                    ?.let { fieldRightUI ->
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