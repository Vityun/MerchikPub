package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers

import android.util.Log
import androidx.collection.mutableLongListOf
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.DaoInterfaces.WPDataAdditionalDao
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters


import java.util.concurrent.CopyOnWriteArrayList

class ScrollDataHolder private constructor() {
    private val ids: MutableList<Long> = mutableListOf()
    private var currentIndex = 0

    // Потокобезопасный список слушателей (каждый получает актуальную копию ids)
    private val listeners = CopyOnWriteArrayList<(List<Long>) -> Unit>()

    companion object {
        @Volatile
        private var instance: ScrollDataHolder? = null

        fun instance(): ScrollDataHolder {
            return instance ?: synchronized(this) {
                instance ?: ScrollDataHolder().also { instance = it }
            }
        }
    }

    // -- Подписка на изменения списка ids --

    /**
     * Добавляет слушатель, который будет вызван при каждом изменении внутреннего списка ids.
     * Возвращает лямбду для удобной отписки: val remove = addOnIdsChangedListener { ... }; remove()
     */
    fun addOnIdsChangedListener(listener: (List<Long>) -> Unit): () -> Unit {
        listeners.add(listener)
        // сразу отправим текущее состояние
        Log.e("ScrollDataHolder","listener.invoke(ids.toList()) -")
        listener.invoke(ids.toList())
        Log.e("ScrollDataHolder","listener.invoke(ids.toList()) +")
        return {
            listeners.remove(listener)
        }
    }

    /**
     * Удалить слушатель (альтернативный метод)
     */
    fun removeOnIdsChangedListener(listener: (List<Long>) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        Log.e("ScrollDataHolder","notifyListeners -")
        val snapshot = ids.toList()
        Log.e("ScrollDataHolder","notifyListeners +")
        for (l in listeners) {
            try {
                l.invoke(snapshot)
            } catch (t: Throwable) {
                // защищаем от падений в коллбэках
                try {
                    Globals.writeToMLOG("ERROR", "ScrollDataHolder.notifyListeners", "listener exception: $t")
                } catch (_: Throwable) { /* ignore */ }
            }
        }
    }

    // -- Оригинальные методы (с вызовом notifyListeners после мутаций) --

    fun init() {
        ids.clear()
        currentIndex = 0
        notifyListeners()
    }

    fun addId(id: Long) {
        ids.add(id)
        notifyListeners()
    }

    fun addIds(newIds: Collection<Long>) {
        if (newIds.isNotEmpty()) {
            ids.addAll(newIds)
            notifyListeners()
        }
    }

    fun removeId(id: Long) {
        val index = ids.indexOf(id)
        if (index >= 0) {
            ids.removeAt(index)
            if (currentIndex > index) {
                currentIndex-- // сдвигаем указатель назад, чтобы не пропустить следующий элемент
            }
            if (currentIndex >= ids.size) {
                currentIndex = 0 // защита от выхода за границу
            }
            notifyListeners()
        }
    }

    fun removeByCodeDad2(codeDad2: Long) {
        // Получаем все WPDataAdditional с заданным codeDad2
        val items = SQL_DB.wpDataAdditionalDao().getByCodeDad2(codeDad2).blockingGet() // для Single в RxJava

        if (items.isEmpty()) return

        var removedSomething = false
        items.forEach { wpDataAdditional ->
            val index = ids.indexOf(wpDataAdditional.ID)
            if (index >= 0) {
                ids.removeAt(index)
                removedSomething = true
                // корректируем currentIndex
                if (currentIndex > index) {
                    currentIndex--
                }
            }
        }

        if (currentIndex >= ids.size) {
            currentIndex = 0
        }

        if (removedSomething) {
            notifyListeners()
        }
    }


    /**
     * Удобный метод: заменить весь список сразу.
     */
    fun setIds(newIds: Collection<Long>) {
        ids.clear()
        ids.addAll(newIds)
        currentIndex = 0
        notifyListeners()
    }

    fun getIds(): List<Long> {
        if (ids.isEmpty()) return emptyList()
        val wpDataAdditionalList = SQL_DB.wpDataAdditionalDao().getByIds(ids)
        val listIdAdditionalCode2: MutableList<Long> = mutableListOf()
        for (item in wpDataAdditionalList) {
            if (item.codeDad2 != 0L)
                listIdAdditionalCode2.add(item.codeDad2)
        }
        val listId: MutableList<Long> = RealmManager.getWpDataIdsByAdditionalIds(listIdAdditionalCode2)
        return listId.toList()
    }

    fun hasId(id: Long): Boolean {
        return ids.contains(id)
    }

    fun getNext(): Long? {
        if (ids.isEmpty()) return null

        // Берем следующий ID
        val id = ids[currentIndex]
        currentIndex = (currentIndex + 1) % ids.size

        // Получаем WPDataAdditional
        val wpDataAdditional = SQL_DB.wpDataAdditionalDao().getByIdSync(id) ?: return null

        // По codeDad2 получаем WpDataDB
        return RealmManager.getWorkPlanRowByCodeDad2(wpDataAdditional.codeDad2).id
    }
}


data class AdditionalWPHolder(
    val id: Long,
    val codeDad2: Long?,
    val adrId: Int?,
    val clientId: Int?,
    val wdDataId: Long?
)
