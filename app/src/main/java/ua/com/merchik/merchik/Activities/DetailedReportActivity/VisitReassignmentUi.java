package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.app.Activity;
import android.widget.Toast;

import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.VisitReassignmentStore;
import ua.com.merchik.merchik.dialogs.DialogData;

public final class VisitReassignmentUi {
    private static final String MESSAGE = "Це відвідування вже переназначено іншому виконавцю. "
            + "Воно більше не відображатиметься у вашому плані робіт.";
    private static Notice pendingNotice;

    private static final class Notice {
        final int user;
        final long id;
        final long dad2;

        Notice(Activity report, int user) {
            this.user = user;
            id = report.getIntent().getLongExtra("WpDataDB_ID", 0);
            dad2 = report.getIntent().getLongExtra("WpDataDB_DAD2", 0);
        }

        boolean claim() {
            return VisitReassignmentStore.get().claimNotification(user, id, dad2);
        }
    }

    private VisitReassignmentUi() { }

    public static void close(Activity report, int user, boolean notify) {
        if (notify && user == Globals.getCurrentUserId()) {
            Notice notice = new Notice(report, user);
            if (report.isTaskRoot()) {
                if (notice.claim()) Toast.makeText(report, MESSAGE, Toast.LENGTH_LONG).show();
            } else {
                pendingNotice = notice;
            }
        }
        report.finish();
    }

    public static void onActivityResumed(Activity activity) {
        Notice notice = pendingNotice;
        if (notice == null || activity.isFinishing() || activity.isDestroyed()) return;
        if (notice.user != Globals.getCurrentUserId()) {
            pendingNotice = null;
            return;
        }
        // The report must be finished before displaying a dialog on its parent screen.
        activity.getWindow().getDecorView().post(() -> {
            if (pendingNotice != notice || activity.isFinishing() || activity.isDestroyed()
                    || MyApplication.getCurrentActivity() != activity) return;
            if (notice.user != Globals.getCurrentUserId()) {
                pendingNotice = null;
                return;
            }
            pendingNotice = null;
            if (!notice.claim()) return;
            DialogData dialog = new DialogData(activity);
            dialog.setTitle("Відвідування переназначено");
            dialog.setText(MESSAGE);
            dialog.setClose(dialog::dismiss);
            dialog.setOk("Зрозуміло", dialog::dismiss);
            dialog.show();
        });
    }
}
