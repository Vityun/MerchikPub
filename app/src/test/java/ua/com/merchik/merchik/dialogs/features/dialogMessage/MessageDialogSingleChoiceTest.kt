package ua.com.merchik.merchik.dialogs.features.dialogMessage

import org.junit.Assert.*
import org.junit.Test

class MessageDialogSingleChoiceTest {
    private val options = listOf(
        MessageDialogChoice("available", "Available"),
        MessageDialogChoice("unavailable", "Unavailable")
    )

    @Test
    fun nothingIsSelectedByDefaultAndConfirmIsDisabled() {
        val choice = MessageDialogSingleChoice(options, onSelected = {})

        assertNull(choice.selectedKey)
        assertFalse(choice.canConfirm)
    }

    @Test
    fun eachAnswerEnablesConfirmIncludingNegativeAnswer() {
        for (option in options) {
            val choice = MessageDialogSingleChoice(options, option.key, {})

            assertTrue(choice.canConfirm)
            assertEquals(1, choice.options.count { it.key == choice.selectedKey })
        }
    }

    @Test
    fun changingSelectionReplacesPreviousAnswerWithoutConfirming() {
        var choice = MessageDialogSingleChoice(options, onSelected = {})
        val selectedKeys = mutableListOf<String>()
        choice = choice.copy(onSelected = { key ->
            selectedKeys.add(key)
            choice = choice.copy(selectedKey = key)
        })

        choice.select("available")
        choice.select("unavailable")

        assertEquals(listOf("available", "unavailable"), selectedKeys)
        assertEquals("unavailable", choice.selectedKey)
        assertEquals(1, choice.options.count { it.key == choice.selectedKey })
        assertTrue(choice.canConfirm)
    }

    @Test
    fun selectingCurrentAnswerDoesNotClearIt() {
        var selected: String? = "available"
        val choice = MessageDialogSingleChoice(options, selected) { selected = it }

        choice.select("available")

        assertEquals("available", selected)
        assertTrue(choice.canConfirm)
    }

    @Test
    fun unknownKeyDoesNotSelectOrEnableConfirm() {
        var calls = 0
        val choice = MessageDialogSingleChoice(options, "missing") { calls++ }

        choice.select("missing")

        assertEquals(0, calls)
        assertFalse(choice.canConfirm)
    }

    @Test
    fun emptyOptionsOrRemovedSelectionDisableConfirm() {
        val selected = MessageDialogSingleChoice(options, "available", {})

        assertFalse(selected.copy(options = emptyList()).canConfirm)
        assertFalse(selected.copy(options = options.takeLast(1)).canConfirm)
    }

    @Test
    fun translatedTitleDoesNotChangeSelection() {
        val choice = MessageDialogSingleChoice(options, "available", {})
        val translated = choice.copy(options = options.map { it.copy(title = "Translated ${it.title}") })

        assertEquals(choice.selectedKey, translated.selectedKey)
        assertTrue(translated.canConfirm)
    }
}
