package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InlinePriceField(
    val itemId: Long,
    val rowId: String,
    val tovarId: String,
    val beforePromotion: Boolean
)

data class InlinePriceValues @JvmOverloads constructor(
    val price: String,
    val priceMin: String,
    val product: String,
    val discountPercent: Int = 0,
    val promotionName: String = "",
    val priceMax: String = priceMin,
    val averageSalary: Int = 0,
    val linked: Boolean = false
) {
    fun discountCheck() = PromotionPriceCheck(price, priceMax, discountPercent, promotionName)
    val invalidBefore: String? get() = PriceSaveGuard.invalidBeforePrice(price, priceMin, priceMax)
}

data class InlinePriceDraft(val value: String, val prices: InlinePriceValues) {
    val exceededLimit get() = PriceSaveGuard.exceededSalaryLimit(value, prices.averageSalary)
    val needsConfirmation get() = !invalidPrice && (exceededLimit != null || invalidDiscount)
    val invalidPrice: Boolean
        get() = prices.invalidBefore != null
    val invalidDiscount: Boolean
        get() = !invalidPrice && prices.discountCheck().outsideRange()
}

data class InlinePriceError(val field: InlinePriceField, val draft: InlinePriceDraft)

/** Owns only primitive drafts, never Realm objects. All access is on the UI thread. */
class InlinePriceDrafts(
    restored: Map<InlinePriceField, String>,
    private val readPrices: (InlinePriceField) -> InlinePriceValues,
    private val savePrice: (InlinePriceField, String) -> Boolean,
    private val store: (Map<InlinePriceField, String>) -> Unit,
    private val saveConfirmedPrice: (InlinePriceField, String, InlinePriceValues) -> Boolean = { field, value, _ -> savePrice(field, value) },
    private val onInputFinished: (InlinePriceField) -> Unit = {}
) {
    private val _drafts = MutableStateFlow(restored.mapValues { (field, value) -> draft(field, value) })
    val drafts = _drafts.asStateFlow()
    private val pending = restored.keys.toMutableSet()
    // Successful prefixes leave drafts immediately, but still need one completion callback.
    private val edited = restored.keys.toMutableSet()
    private val finished = restored.keys.toMutableSet()
    private val reported = mutableMapOf<InlinePriceField, InlinePriceDraft>()
    private val _notificationVersion = MutableStateFlow(0)
    val notificationVersion = _notificationVersion.asStateFlow()
    private val _focusRequest = MutableStateFlow<InlinePriceField?>(null)
    val focusRequest = _focusRequest.asStateFlow()

    private fun draft(field: InlinePriceField, value: String): InlinePriceDraft {
        val entered = value.ifBlank { "0" }
        val current = readPrices(field)
        val edit = PriceSaveGuard.PriceEdit(current.price, current.priceMin, current.priceMax, entered,
            field.beforePromotion, current.linked)
        return InlinePriceDraft(entered, current.copy(price = edit.price, priceMin = edit.min, priceMax = edit.max))
    }

    fun change(field: InlinePriceField, value: String) {
        val edit = draft(field, value)
        if (edit.prices.linked && _drafts.value.keys.any { it.tovarId == field.tovarId && it != field }) {
            // Both inputs now edit the same price. A deferred edit from the other input must not overwrite it.
            discardProducts(setOf(field.tovarId))
        }
        edited.add(field)
        finished.remove(field)
        reported.remove(field)
        pending.remove(field)
        _drafts.value = _drafts.value + (field to edit)
        persistDrafts()
        // Valid input still saves automatically; invalid prefixes never open a dialog.
        trySave(field)
        revalidate()
    }

    private fun trySave(field: InlinePriceField) {
        val value = _drafts.value[field]?.value ?: return
        val current = draft(field, value)
        _drafts.value = _drafts.value + (field to current)
        // Don't commit prefixes of a price the user may ultimately refuse.
        if ((current.prices.averageSalary > 0 || current.prices.linked) && field !in finished) return
        if (current.invalidPrice || current.needsConfirmation || value.isBlank() || value.endsWith(".")) return
        if (!savePrice(field, value)) {
            // The transaction may reject a price changed by another editor/synchronization.
            _drafts.value = _drafts.value + (field to draft(field, value))
            return
        }
        _drafts.value = _drafts.value - field
        pending.remove(field)
        reported.remove(field)
        persistDrafts()
        notifyFinished(field)
    }

    fun finish(field: InlinePriceField) {
        if (field !in edited && field !in _drafts.value) return
        finished.add(field)
        val value = _drafts.value[field]?.value
        if (value == null) {
            notifyFinished(field)
            revalidate()
            return
        }
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
        (edited + _drafts.value.keys).toList().forEach(::finish)
    }

    fun discardProducts(tovarIds: Set<String>) {
        if (tovarIds.isEmpty()) return
        _drafts.value = _drafts.value.filterKeys { it.tovarId !in tovarIds }
        pending.removeAll { it.tovarId in tovarIds }
        edited.removeAll { it.tovarId in tovarIds }
        finished.removeAll { it.tovarId in tovarIds }
        reported.keys.removeAll { it.tovarId in tovarIds }
        if (_focusRequest.value?.tovarId in tovarIds) _focusRequest.value = null
        persistDrafts()
    }

    private fun notifyFinished(field: InlinePriceField) {
        if (field in finished && field !in _drafts.value && edited.remove(field)) {
            finished.remove(field)
            onInputFinished(field)
        }
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

    fun approveDiscount(error: InlinePriceError) {
        val current = _drafts.value[error.field]?.let { draft(error.field, it.value) } ?: return
        if (current == error.draft && !current.invalidPrice &&
            saveConfirmedPrice(error.field, current.value, current.prices)) {
            _drafts.value = _drafts.value - error.field
            pending.remove(error.field)
            reported.remove(error.field)
            persistDrafts()
            notifyFinished(error.field)
        } else {
            reported.remove(error.field)
            finish(error.field)
        }
        requestNotification()
    }

    fun refuseDiscount(error: InlinePriceError) {
        // Discard only the value the user was asked about, never a newer edit.
        if (_drafts.value[error.field]?.value != error.draft.value) return
        _drafts.value = _drafts.value - error.field
        pending.remove(error.field)
        reported.remove(error.field)
        edited.remove(error.field)
        finished.remove(error.field)
        persistDrafts()
        _focusRequest.value = error.field
    }

    fun focusRestored(field: InlinePriceField) {
        if (_focusRequest.value == field) _focusRequest.value = null
    }

    fun requestNotification() {
        _notificationVersion.value++
    }

    private fun persistDrafts() = store(_drafts.value.mapValues { it.value.value })
}
