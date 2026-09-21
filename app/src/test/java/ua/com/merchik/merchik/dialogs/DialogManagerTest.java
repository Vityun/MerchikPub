package ua.com.merchik.merchik.dialogs;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DialogManagerTest {
    @Test
    public void repeatedRegistrationDoesNotRetainDuplicateAfterUnregister() throws Exception {
        DialogData dialog = new DialogData();
        Field field = DialogManager.class.getDeclaredField("activeDialogs");
        field.setAccessible(true);
        List<?> activeDialogs = (List<?>) field.get(null);
        int initialCount = activeDialogs.size();
        try {
            DialogManager.register(dialog);
            DialogManager.register(dialog);
            assertEquals(initialCount + 1, activeDialogs.size());
            DialogManager.unregister(dialog);
            assertEquals(initialCount, activeDialogs.size());
            assertFalse(activeDialogs.contains(dialog));
        } finally {
            DialogManager.unregister(dialog);
        }
    }
}
