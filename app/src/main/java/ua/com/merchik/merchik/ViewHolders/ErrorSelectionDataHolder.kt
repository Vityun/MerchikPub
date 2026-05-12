package ua.com.merchik.merchik.ViewHolders


object ErrorSelectionDataHolder {
    var selectedId: String? = null
    var selectedName: String? = null

    fun set(id: String?, name: String?) {
        selectedId = id
        selectedName = name
    }

    fun clear() {
        selectedId = null
        selectedName = null
    }
}