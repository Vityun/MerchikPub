package ua.com.merchik.merchik.dialogs.features.dialogMessage

data class MessageDialogChoice(
    val key: String,
    val title: String
)

data class MessageDialogSingleChoice(
    val options: List<MessageDialogChoice>,
    val selectedKey: String? = null,
    val onSelected: (String) -> Unit
) {
    val canConfirm: Boolean
        get() = options.any { it.key == selectedKey }

    fun select(key: String) {
        if (options.any { it.key == key }) onSelected(key)
    }
}
