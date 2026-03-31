package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers


import android.util.Log
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB
import java.util.concurrent.CopyOnWriteArrayList

class ScrollDataHolder private constructor() {
    private val ids: MutableSet<Long> = mutableSetOf()
    private val dad2: MutableList<Long> = mutableListOf()
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
        Log.e("ScrollDataHolder", "listener.invoke(ids.toList()) -")
        listener.invoke(ids.toList())
        Log.e("ScrollDataHolder", "listener.invoke(ids.toList()) +")
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
        Log.e("ScrollDataHolder", "notifyListeners -")
        val snapshot = ids.toList()
        Log.e("ScrollDataHolder", "notifyListeners +")
        for (l in listeners) {
            try {
                l.invoke(snapshot)
            } catch (t: Throwable) {
                // защищаем от падений в коллбэках
                try {
                    Globals.writeToMLOG(
                        "ERROR",
                        "ScrollDataHolder.notifyListeners",
                        "listener exception: $t"
                    )
                } catch (_: Throwable) { /* ignore */
                }
            }
        }
    }

    // -- Оригинальные методы (с вызовом notifyListeners после мутаций) --

    fun init() {
        ids.clear()
        dad2.clear()
        currentIndex = 0
        notifyListeners()
    }

    fun addId(id: Long) {
        ids.add(id)
        notifyListeners()
    }
    fun addIdWithOutNotif(id: Long) {
        ids.add(id)
    }

    fun addDad2(newDad2: Collection<Long>) {
        if (newDad2.isNotEmpty()) {
            dad2.addAll(newDad2)
            notifyListeners()
        }
    }


    fun addIds(newIds: Collection<Long>) {
        if (newIds.isNotEmpty()) {
            ids.addAll(newIds)
            notifyListeners()
        }
    }
    fun addIdsWithOutNotif(newIds: Collection<Long>) {
        if (newIds.isNotEmpty()) {
            ids.addAll(newIds)
        }
    }


    fun addIdsWithClear(newIds: Collection<Long>) {
        if (newIds.isNotEmpty()) {
            ids.clear()
            ids.addAll(newIds)
            notifyListeners()
        }
    }


    @Synchronized
    fun removeIds(idsToRemove: Collection<Long>) {
        if (idsToRemove.isEmpty()) return
        ids.removeAll(idsToRemove.toSet())
        if (currentIndex >= ids.size) currentIndex = 0
        notifyListeners()
    }

    @Synchronized
    fun removeIdsWithOutNotif(idsToRemove: Collection<Long>) {
        if (idsToRemove.isEmpty()) return
        ids.removeAll(idsToRemove.toSet())
        if (currentIndex >= ids.size) currentIndex = 0
    }

    @Synchronized
    fun removeId(id: Long) {
        val removed = ids.remove(id)
        if (!removed) return

        if (currentIndex >= ids.size) {
            currentIndex = 0
        }

        notifyListeners()
    }

    @Synchronized
    fun removeIdWithOutNotif(id: Long) {
        val removed = ids.remove(id)
        if (!removed) return

        if (currentIndex >= ids.size) {
            currentIndex = 0
        }
    }


    fun removeByCodeWpDataId(id: Long) {
        ids.remove(id)
    }

    @Synchronized
    fun getAllSnapshot(): Set<Long> {
        return ids.toSet()
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

    fun setIdsWithOutNotif(newIds: Collection<Long>) {
        ids.clear()
        ids.addAll(newIds)
        currentIndex = 0
    }

    fun getDad2(): List<Long> {
        return dad2
    }


    fun getAll(): List<Long> {
        if (ids.isEmpty()) return emptyList()
        return ids.toList()
    }

}


data class AdditionalWPHolder(
    val id: Long,
    val codeDad2: Long?,
    val adrId: Int?,
    val clientId: Int?,
    val wdDataId: Long?
)
