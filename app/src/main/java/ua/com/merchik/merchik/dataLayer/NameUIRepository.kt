package ua.com.merchik.merchik.dataLayer

import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB
import ua.com.merchik.merchik.database.realm.RealmManager
import java.util.concurrent.ConcurrentHashMap


class MemoryCache<T> {
    private val cache = ConcurrentHashMap<Long, T>()

    fun put(key: Long, value: T) {
        cache[key] = value
    }

    fun get(key: Long): T? {
        return cache[key]
    }

    fun remove(key: Long) {
        cache.remove(key)
    }

    fun clear() {
        cache.clear()
    }
}

class NameUIRepository(
//    private val databaseRoom: AppDatabase,
//    private val databaseRealm: Realm,
) {
    private val cache = MemoryCache<String>()

    private fun getNameUIById(id: Long): String? {
        return RealmManager.INSTANCE
            .where(SiteObjectsDB::class.java)
            .equalTo("id", id)
            .findFirst()?.let {
                RealmManager.INSTANCE.copyFromRealm(it)?.commentsTranslation
            }
    }

    fun getTranslateString(text: String, translateId: Long?): String {
        if (translateId == null) return text

        cache.get(translateId)?.let {
            return it
        }

        val result = getNameUIById(translateId)

        result?.let {
            cache.put(translateId, it)
        }

        return result ?: text
    }

    fun clearCache() {
        cache.clear()
    }
}