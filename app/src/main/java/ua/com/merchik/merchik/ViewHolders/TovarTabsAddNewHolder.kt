package ua.com.merchik.merchik.ViewHolders

import ua.com.merchik.merchik.data.RealmModels.TovarDB


object TovarTabsAddNewHolder {

    private val selectedIds = linkedSetOf<String>()

    @Synchronized
    fun set(ids: List<String>) {
        selectedIds.clear()
        selectedIds.addAll(ids.filter { it.isNotBlank() })
    }

    @Synchronized
    fun peek(): Set<String> {
        return selectedIds.toSet()
    }

    @Synchronized
    fun consume(): Set<String> {
        val result = selectedIds.toSet()
        selectedIds.clear()
        return result
    }

    @Synchronized
    fun clear() {
        selectedIds.clear()
    }
}