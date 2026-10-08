package ua.com.merchik.merchik.dialogs.features.dialogMessage

import org.junit.Assert.assertEquals
import org.junit.Test

class DialogStatusTest {
    @Test
    fun informationStatusIsCaseInsensitive() {
        assertEquals(DialogStatus.INFO, DialogStatus.fromString("info"))
        assertEquals(DialogStatus.INFO, DialogStatus.fromString("INFO"))
    }

    @Test
    fun existingStatusMappingsArePreserved() {
        assertEquals(DialogStatus.ALERT, DialogStatus.fromString("alert"))
        assertEquals(DialogStatus.ERROR, DialogStatus.fromString("danger"))
        assertEquals(DialogStatus.NORMAL, DialogStatus.fromString("ok"))
    }

    @Test
    fun missingOrUnknownStatusRemainsEmpty() {
        assertEquals(DialogStatus.EMPTY, DialogStatus.fromString(null))
        assertEquals(DialogStatus.EMPTY, DialogStatus.fromString(""))
        assertEquals(DialogStatus.EMPTY, DialogStatus.fromString("unknown"))
    }
}
