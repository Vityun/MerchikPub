package ua.com.merchik.merchik.Utils

import io.realm.Realm
import io.realm.RealmObject
import java.util.concurrent.atomic.AtomicInteger


object PrimaryKeyGenerator {
    private val idMap = mutableMapOf<Class<*>, AtomicInteger>()

    fun <T : RealmObject> nextId(realm: Realm, clazz: Class<T>): Int {
        val counter = idMap.getOrPut(clazz) {
            val maxId = realm.where(clazz)
                .max("id")?.toInt() ?: 0
            AtomicInteger(maxId)
        }
        return counter.incrementAndGet()
    }
}
