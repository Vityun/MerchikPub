package ua.com.merchik.merchik.dialogs;

import java.util.ArrayList;
import java.util.List;

public class DialogManager {
    private static final List<DialogData> activeDialogs = new ArrayList<>();

    public static void register(DialogData dialog) {
        synchronized (activeDialogs) {
            activeDialogs.add(dialog);
        }
    }

    public static void unregister(DialogData dialog) {
        synchronized (activeDialogs) {
            activeDialogs.remove(dialog);
        }
    }

    public static void dismissAll() {
        synchronized (activeDialogs) {
            for (DialogData dialog : new ArrayList<>(activeDialogs)) {
                if (dialog != null && dialog.isDialogShow()) {
                    dialog.dismiss();
                }
            }
            activeDialogs.clear();
        }
    }
}
