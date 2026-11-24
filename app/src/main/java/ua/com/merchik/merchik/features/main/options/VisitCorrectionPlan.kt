package ua.com.merchik.merchik.features.main.options

import ua.com.merchik.merchik.Global.OptionUnlockPolicy
import ua.com.merchik.merchik.data.RealmModels.OptionsDB

/** Freeze the groups before the first check changes any of their persisted signals. */
internal class VisitCorrectionPlan(private val dad2: String, rows: List<OptionsDB>) {
    private val groups = rows.asSequence()
        .filter { it.codeDad2 == dad2 && it.isSignal == OptionUnlockPolicy.SIGNAL_UNLOCKED }
        .mapNotNull(::controlId).toSet()

    fun includes(row: OptionsDB) = row.codeDad2 == dad2 && controlId(row) in groups

    private fun controlId(row: OptionsDB) =
        OptionUnlockPolicy.controlId(row.optionId, row.optionControlId, row.optionGroup)
}
