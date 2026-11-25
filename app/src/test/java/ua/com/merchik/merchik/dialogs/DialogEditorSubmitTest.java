package ua.com.merchik.merchik.dialogs;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DialogEditorSubmitTest {
    private final AtomicInteger clicks = new AtomicInteger();

    @Test public void imeDoneClicksConfirmAndOtherImeActionsDoNot() {
        assertFalse(ime(EditorInfo.IME_ACTION_NEXT, true));
        assertTrue(ime(EditorInfo.IME_ACTION_DONE, true));
        assertEquals(1, clicks.get());
    }

    @Test public void unavailableConfirmCannotBeActivatedFromKeyboard() {
        assertTrue(ime(EditorInfo.IME_ACTION_DONE, false));
        assertTrue(DialogData.handleEditorSubmit(EditorInfo.IME_NULL, KeyEvent.KEYCODE_ENTER,
                KeyEvent.ACTION_UP, false, false, clicks::incrementAndGet));
        assertEquals(0, clicks.get());
    }

    @Test public void bothEnterKeysSubmitOnceOnReleaseEvenAfterKeyRepeat() {
        for (int key : new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER}) {
            assertTrue(key(key, KeyEvent.ACTION_DOWN, false));
            assertTrue(key(key, KeyEvent.ACTION_DOWN, false));
            assertTrue(key(key, KeyEvent.ACTION_UP, false));
        }
        assertEquals(2, clicks.get());
    }

    @Test public void canceledEnterAndUnrelatedKeysDoNotSubmit() {
        assertTrue(key(KeyEvent.KEYCODE_ENTER, KeyEvent.ACTION_UP, true));
        assertFalse(key(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_UP, false));
        assertEquals(0, clicks.get());
    }

    private boolean ime(int action, boolean available) {
        return DialogData.handleEditorSubmit(action, KeyEvent.KEYCODE_UNKNOWN, -1, false,
                available, clicks::incrementAndGet);
    }

    private boolean key(int code, int action, boolean canceled) {
        return DialogData.handleEditorSubmit(EditorInfo.IME_NULL, code, action, canceled,
                true, clicks::incrementAndGet);
    }
}
