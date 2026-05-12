package ua.com.merchik.merchik.dataLayer.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.LaunchOrigin

@Stable
data class ContextMenuController(
    val open: (ContextMenuUiState) -> Unit,
    val close: () -> Unit
)

@Immutable
data class ContextMenuPayload(
    val selectedItems: List<DataItemUI>,
    val allItems: List<DataItemUI>? = null,
    val deckId: String? = null,
    val origin: LaunchOrigin? = null
) {
    init {
        require(selectedItems.isNotEmpty()) { "ContextMenuPayload.items cannot be empty" }
    }

    val isMulti: Boolean get() = selectedItems.size > 1
    val firstItem: DataItemUI get() = selectedItems.first()
}

@Immutable
data class ContextMenuHeaderUi(
    val visible: Boolean = true,
    val title: String? = null,
    val rows: List<ContextMenuHeaderRow> = emptyList()
)

@Immutable
data class ContextMenuHeaderRow(
    val label: String,
    val value: String,
    val valueHighlighted: Boolean = true
)

enum class SubmenuPresentation {
    INLINE_EXPAND, // раскрыть внутри текущего меню
    OVERLAY,       // открыть поверх текущего меню
    REPLACE        // заменить текущее содержимое меню
}

sealed interface MenuLeading {
    data object None : MenuLeading
    data class Checkbox(val checked: Boolean) : MenuLeading
    data class Text(val value: String) : MenuLeading
    data class DrawableIcon(@DrawableRes val resId: Int) : MenuLeading
    data class BadgeText(val value: String) : MenuLeading
}

@Immutable
sealed interface ContextMenuEntry {
    val id: String

    @Immutable
    data class Action(
        override val id: String,
        val actionId: String,
        val title: String,
        val leading: MenuLeading,
        val enabled: Boolean = true,
        val visible: Boolean = true,
        val closeMenuOnClick: Boolean = true
    ) : ContextMenuEntry

    @Immutable
    data class Divider(
        override val id: String
    ) : ContextMenuEntry

    data class Submenu(
        override val id: String,
        val title: String,
        val leading: MenuLeading,
        val enabled: Boolean = true,
        val visible: Boolean = true,
        val items: List<ContextMenuEntry>,
        val presentation: SubmenuPresentation = SubmenuPresentation.INLINE_EXPAND,
        val expandedByDefault: Boolean = false,
        val headerOverride: ContextMenuHeaderUi? = null
    ) : ContextMenuEntry
}

@Immutable
data class ContextMenuUiState(
    val payload: ContextMenuPayload,
    val header: ContextMenuHeaderUi? = null,
    val entries: List<ContextMenuEntry> = emptyList()
)

@Immutable
data class ContextMenuActionEvent(
    val actionId: String,
    val payload: ContextMenuPayload
)

object ContextMenuActionIds {
    const val EDIT = "edit"
    const val OPEN_ORDER = "open_order"
    const val OPEN_UFMD_SELECTOR = "open_ufmd_selector"
    const val MARK = "mark"
    const val MARK_ALL = "mark_all"
    const val UNMARK = "unmark"
    const val UNMARK_ALL = "unmark_all"
    const val INVERT = "invert"
    const val COLLAPSE_LIST = "collapse_list"
    const val EXPAND_LIST = "expand_list"
    const val ACCEPT = "accept"
    const val REJECT = "reject"
    const val CLOSE = "close"
    const val OPEN_SMS_PLAN = "open_sms_plan"
    const val ADDITIONAL_ADD = "additional_add_add"
    const val ADDITIONAL_DEL = "additional_add_del"
    const val ADDITIONAL_DEL_ADDRESS = "additional_add_addrss"
    const val ADDITIONAL_DEL_CLIENT = "additional_add_client"

    const val REJECT_WORK = "reject_work"
    const val REJECT_CLIENT = "reject_client"
    const val REJECT_ADDRESS = "reject_address"
    const val ASK_MORE_MONEY = "ask_more_money"

    const val LIST_SETTINGS = "list_settings"

    const val TOVAR_PRICE = "tovar_price"
    const val TOVAR_FACE = "tovar_face"
    const val TOVAR_EXPIRE_LEFT = "tovar_expire_left"
    const val TOVAR_AMOUNT = "tovar_amount"
    const val TOVAR_UP = "tovar_up"
    const val TOVAR_DT_EXPIRE = "tovar_dt_expire"
    const val TOVAR_OBOROTVED_NUM = "tovar_oborotved_num"
    const val TOVAR_ERROR_ID = "tovar_error_id"
    const val TOVAR_AKCIYA_ID = "tovar_akciya_id"
    const val TOVAR_AKCIYA = "tovar_akciya"
    const val TOVAR_NOTES = "tovar_notes"

    const val ADDITIONAL_CONTENT_DELETE_EXTRA = "additional_content_delete_extra"
    const val ADDITIONAL_CONTENT_ADD_PPA = "additional_content_add_ppa"
    const val ADDITIONAL_CONTENT_ADD_ALL = "additional_content_add_all"
    const val ADDITIONAL_CONTENT_ADD_ONE = "additional_content_add_one"
}

@Immutable
data class ContextMenuActionPreset(
    val actionId: String,
    val title: String,
    val leading: MenuLeading = MenuLeading.DrawableIcon(R.drawable.ic_37),
    val enabled: Boolean = true,
    val closeMenuOnClick: Boolean = true
) {
    fun toEntry(
        id: String = actionId,
        title: String = this.title,
        leading: MenuLeading = this.leading,
        enabled: Boolean = this.enabled,
        visible: Boolean = true,
        closeMenuOnClick: Boolean = this.closeMenuOnClick
    ): ContextMenuEntry.Action {
        return ContextMenuEntry.Action(
            id = id,
            actionId = actionId,
            title = title,
            leading = leading,
            enabled = enabled,
            visible = visible,
            closeMenuOnClick = closeMenuOnClick
        )
    }
}

object ContextMenuPresets {
    val Add = ContextMenuActionPreset(
        actionId = "add",
        title = "Додати",
        leading = MenuLeading.Text("+"),
        enabled = false
    )

    val Edit = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.EDIT,
        title = "Змінити",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_24)
    )

    val Delete = ContextMenuActionPreset(
        actionId = "delete",
        title = "Видалити",
        leading = MenuLeading.Text("🗑"),
        enabled = false
    )

    val OpenOrder = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.OPEN_ORDER,
        title = "Інформація про це замовлення",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_37)
    )

    val OpenUfmdSelector = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.OPEN_UFMD_SELECTOR,
        title = "Додатковий заробіток",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_37)
    )

    val Mark = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.MARK,
        title = "Позначити",
        leading = MenuLeading.Checkbox(true)
    )

    val MarkAll = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.MARK_ALL,
        title = "Позначити усі",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_select_all)
    )

    val Unmark = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.UNMARK,
        title = "Зняти позначку",
        leading = MenuLeading.Checkbox(false)
    )

    val UnmarkAll = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.UNMARK_ALL,
        title = "Зняти всi позначки",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_unselect_all)
    )

    val Invert = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.INVERT,
        title = "Інвертувати",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_39)
    )

    val CollapseList = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.COLLAPSE_LIST,
        title = "Згорнути список",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_arrow_up_1)
    )

    val ExpandList = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.EXPAND_LIST,
        title = "Розгорнути список",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_arrow_down_1)
    )

    val OpenSmsPlan = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.OPEN_SMS_PLAN,
        title = "Переглянути журнал заявок",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_37)
    )

    val Close = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.CLOSE,
        title = "Закрити",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_letter_x)
    )

    val RejectWork = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.REJECT_WORK,
        title = "Відмовитися від роботи",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_37)
    )

    val RejectClient = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.REJECT_CLIENT,
        title = "Відмовитися від клієнта",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_37)
    )

    val RejectAddress = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.REJECT_ADDRESS,
        title = "Відмовитися від адреси",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_37)
    )

    val AskMoreMoney = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.ASK_MORE_MONEY,
        title = "Запросити більшу оплату",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_37)
    )

    val AdditionalWorkAdd = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.ADDITIONAL_ADD,
        title = "Обрати дiю",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_21)
    )

    val AdditionalWorkDell = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.ADDITIONAL_DEL,
        title = "Не показувати вiзит",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_times_circle_regular)
    )

    val AdditionalWorkDellClient = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.ADDITIONAL_DEL_CLIENT,
        title = "Не показувати клієнта",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_times_circle_regular)
    )

    val AdditionalWorkDellAddress = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.ADDITIONAL_DEL_ADDRESS,
        title = "Не показувати адресу",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_times_circle_regular)
    )

    val ListSettings = ContextMenuActionPreset(
        actionId = ContextMenuActionIds.LIST_SETTINGS,
        title = "Налаштування списку",
        leading = MenuLeading.DrawableIcon(R.drawable.ic_sort_down)
    )

}

inline fun <reified T : DataObjectUI> DataItemUI.rawAs(): T? =
    rawObj.firstOrNull { it is T } as? T

fun DataItemUI.fieldValueOrNull(key: String): String? =
    rawFields.firstOrNull { it.key.equals(key, ignoreCase = true) }
        ?.value
        ?.rawValue
        ?.toString()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }