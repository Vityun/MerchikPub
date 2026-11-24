package ua.com.merchik.merchik.features.main.options

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.data.RealmModels.OptionsDB

class VisitCorrectionPlanTest {
    private fun row(id: String, target: String, signal: String, button: Boolean = false, visit: String = "42") =
        OptionsDB().apply {
            codeDad2 = visit
            optionId = id
            optionControlId = target
            optionGroup = if (button) "3161" else "3148"
            isSignal = signal
        }

    @Test fun correctedButtonAndControlsRemainInTheSameBatch() {
        for (id in listOf("157241", "157243", "141361")) {
            val button = row("157242", id, "3", button = true)
            val control = row(id, "138520", "2")
            val otherMinimum = row(id, "158309", "3")
            val plan = VisitCorrectionPlan("42", listOf(button, control, otherMinimum))
            button.isSignal = "2"
            otherMinimum.isSignal = "2"
            assertTrue(plan.includes(button))
            assertTrue(plan.includes(control))
            assertTrue(plan.includes(otherMinimum))
            assertFalse(plan.includes(row("138520", "138521", "2", button = true)))
            assertFalse(plan.includes(row(id, "138520", "3", visit = "43")))
        }
    }

    @Test fun previousWorkControlDoesNotRequireADedicatedButton() {
        val endPrevious = row("156928", "138518", "3")
        val notUnlockedHere = row("157243", "138520", "1")
        val otherVisit = row("157243", "138520", "3", visit = "43")
        val plan = VisitCorrectionPlan("42", listOf(endPrevious, notUnlockedHere, otherVisit))
        assertTrue(plan.includes(endPrevious))
        assertFalse(plan.includes(notUnlockedHere))
        assertFalse(plan.includes(otherVisit))
    }
}
