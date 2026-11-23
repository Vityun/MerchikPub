package ua.com.merchik.merchik.dialogs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import kotlin.Unit;
import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Utils.MockLocationGuard;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

public final class MockLocationWarning {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final WeakHashMap<Activity, WeakReference<MessageDialogBuilder>> dialogs = new WeakHashMap<>();

    private MockLocationWarning() {}

    public static void refresh(Context context) {
        // onResume and background option checks can both arrive before the Activity is RESUMED.
        MAIN.post(() -> {
            Activity source = context instanceof Activity ? (Activity) context : null;
            Activity activity = resumed(source) ? source : MyApplication.getCurrentActivity();
            if (!resumed(activity)) return;
            WeakReference<MessageDialogBuilder> ref = dialogs.get(activity);
            MessageDialogBuilder previous = ref == null ? null : ref.get();
            if (!MockLocationGuard.isActive(activity)) {
                if (previous != null && previous.isShowing()) previous.dismiss();
                return;
            }
            if (previous != null && previous.isShowing()) return;
            MessageDialogBuilder dialog = new MessageDialogBuilder(activity)
                    .setTitle("Фіктивне місцеположення")
                    .setStatus(DialogStatus.ERROR)
                    .setMessage("У вас увімкнено фіктивне місцеположення. Вимкніть його, інакше інформацію буде передано керівництву.</b></b>"
                            + MockLocationGuard.MESSAGE + "</b></b>" + MockLocationGuard.INSTRUCTIONS)
                    .setShowCloseButton(true)
                    .setOnConfirmAction("Виправити", () -> {
                        try {
                            activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                        } catch (ActivityNotFoundException | SecurityException e) {
                            Toast.makeText(activity, MockLocationGuard.INSTRUCTIONS, Toast.LENGTH_LONG).show();
                        }
                        return Unit.INSTANCE;
                    });
            dialogs.put(activity, new WeakReference<>(dialog));
            dialog.show();
        });
    }

    private static boolean resumed(Activity activity) {
        return activity instanceof LifecycleOwner && !activity.isFinishing() && !activity.isDestroyed()
                && ((LifecycleOwner) activity).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }
}
