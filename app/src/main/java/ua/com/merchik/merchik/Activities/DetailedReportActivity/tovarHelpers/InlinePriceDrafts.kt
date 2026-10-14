package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InlinePriceField(
    val itemId: Long,
    val rowId: String,
    val tovarId: String,
    val beforePromotion: Boolean
)

data class InlinePriceValues(val price: String, val priceMin: String, val product: String)

data class InlinePriceDraft(val value: String, val prices: InlinePriceValues) {
    val invalidPrice: Boolean
        get() = PriceSaveGuard.isPriceAboveBeforePromotion(prices.price, prices.priceMin)
}

data class InlinePriceError(val field: InlinePriceField, val draft: InlinePriceDraft)

/** Owns only primitive drafts, never Realm objects. All access is on the UI thread. */
class InlinePriceDrafts(
    restored: Map<InlinePriceField, String>,
    private val readPrices: (InlinePriceField) -> InlinePriceValues,
    private val savePrice: (InlinePriceField, String) -> Boolean,
    private val store: (Map<InlinePriceField, String>) -> Unit
) {
    private val _drafts = MutableStateFlow(restored.mapValues { (field, value) -> draft(field, value) })
    val drafts = _drafts.asStateFlow()
    private val pending = restored.keys.toMutableSet()
    private val reported = mutableMapOf<InlinePriceField, InlinePriceDraft>()
    private val _notificationVersion = MutableStateFlow(0)
    val notificationVersion = _notificationVersion.asStateFlow()

    private fun draft(field: InlinePriceField, value: String): InlinePriceDraft {
        val entered = value.ifBlank { "0" }
        val current = readPrices(field)
        val prices = if (field.beforePromotion) current.copy(priceMin = entered)
        else current.copy(price = entered)
        return InlinePriceDraft(entered, prices)
    }

    fun change(field: InlinePriceField, value: String) {
        reported.remove(field)
        pending.remove(field)
        _drafts.value = _drafts.value + (field to draft(field, value))
        persistDrafts()
        // Valid input still saves automatically; invalid prefixes never open a dialog.
        trySave(field)
        revalidate()
    }

    private fun trySave(field: InlinePriceField) {
        val value = _drafts.value[field]?.value ?: return
        val current = draft(field, value)
        _drafts.value = _drafts.value + (field to current)
        if (current.invalidPrice || value.isBlank() || value.endsWith(".")) return
        if (!savePrice(field, value)) {
            // The transaction may reject a price changed by another editor/synchronization.
            _drafts.value = _drafts.value + (field to draft(field, value))
            return
        }
        _drafts.value = _drafts.value - field
        pending.remove(field)
        reported.remove(field)
        persistDrafts()
    }

    fun finish(field: InlinePriceField) {
        val value = _drafts.value[field]?.value ?: return
        if (value.endsWith(".") && value.length > 1) {
            _drafts.value = _drafts.value + (field to draft(field, value.dropLast(1)))
        }
        trySave(field)
        revalidate()
        val remaining = _drafts.value[field]
        if (remaining != null && reported[field] != remaining) pending.add(field)
        persistDrafts()
        requestNotification()
    }

    fun finishAll() {
        _drafts.value.keys.toList().forEach(::finish)
    }

    fun revalidate() {
        _drafts.value = _drafts.value.mapValues { (field, value) -> draft(field, value.value) }
    }

    fun nextError(): InlinePriceError? {
        // Re-read database values before showing an error deferred during backgrounding.
        pending.toList().forEach { field ->
            trySave(field)
            val value = _drafts.value[field]
            if (value == null || reported[field] == value) {
                pending.remove(field)
            } else {
                return InlinePriceError(field, value)
            }
        }
        return null
    }

    fun markShown(error: InlinePriceError) {
        reported[error.field] = error.draft
        pending.remove(error.field)
    }

    fun requestNotification() {
        _notificationVersion.value++
    }

    private fun persistDrafts() = store(_drafts.value.mapValues { it.value.value })
}
