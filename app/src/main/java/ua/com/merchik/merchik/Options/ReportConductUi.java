package ua.com.merchik.merchik.Options;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.features.DialogDismissListener;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

/** Owns only visible UI; durable decisions and requests live in ReportConductManager. */
public final class ReportConductUi {
    private static final Map<Activity, Session> SESSIONS = new HashMap<>();

    private static final class Session {
        long dad2;
        int userId;
        String revision;
        Runnable dismiss;
        Object dialogToken;
        LifecycleEventObserver observer;
        boolean conducting;
        boolean resultShowing;
    }

    private ReportConductUi() { }

    public static Activity activity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static boolean canShow(Context context) {
        Activity activity = activity(context);
        return activity instanceof LifecycleOwner && !activity.isFinishing() && !activity.isDestroyed()
                && ((LifecycleOwner) activity).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }

    public static void resume(Activity activity, long dad2) {
        if (!canShow(activity)) return;
        Session session = register(activity, dad2);
        showResult(dad2);
        ReportConductState state = ReportConductManager.get(dad2);
        if (!session.conducting && state != null && state.awaitingDecision()) {
            Options.resumePendingConduct(activity, dad2);
        }
        ReportConductManager.flushPending();
    }

    public static boolean begin(Context context, long dad2) {
        Activity activity = activity(context);
        Session session = SESSIONS.get(activity);
        if (session != null && (session.conducting || session.resultShowing)) return false;
        if (!ReportConductManager.begin(dad2)) {
            Toast.makeText(context, "Команда вже збережена та очікує передачі на сервер.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!canShow(context)) {
            ReportConductManager.log("INFO", dad2, "UI deferred until visit resumes");
            return false;
        }
        session = register(activity, dad2);
        session.conducting = true;
        session.revision = ReportConductManager.get(dad2).revision;
        return true;
    }

    private static Session register(Activity activity, long dad2) {
        Session current = SESSIONS.get(activity);
        if (current != null && current.dad2 == dad2 && current.userId == Globals.getCurrentUserId()) return current;
        if (current != null) stop(activity);
        Session session = new Session();
        session.dad2 = dad2;
        session.userId = Globals.getCurrentUserId();
        session.observer = (owner, event) -> {
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) stop(activity);
        };
        SESSIONS.put(activity, session);
        ((LifecycleOwner) activity).getLifecycle().addObserver(session.observer);
        return session;
    }

    private static void stop(Activity activity) {
        Session session = SESSIONS.remove(activity);
        if (session == null) return;
        ((LifecycleOwner) activity).getLifecycle().removeObserver(session.observer);
        // Remove ownership before dismissing, so lifecycle changes do not cancel the decision.
        if (session.dismiss != null) session.dismiss.run();
        if (session.conducting) ReportConductManager.log("INFO", session.dad2, "UI stopped; decision retained");
    }

    public static boolean canSubmit(Context context, long dad2) {
        Session session = SESSIONS.get(activity(context));
        ReportConductState state = ReportConductManager.get(dad2);
        return canShow(context) && session != null && session.dad2 == dad2
                && session.userId == Globals.getCurrentUserId() && session.conducting
                && state != null && state.revision.equals(session.revision);
    }

    public static void show(Context context, long dad2, DialogData dialog) {
        Session session = SESSIONS.get(activity(context));
        if (session == null || !canShow(context)) return;
        Object token = new Object();
        session.dialogToken = token;
        session.dismiss = dialog::dismiss;
        dialog.setOnDismissAction(() -> dismissed(context, dad2, session, token));
        dialog.show();
        ReportConductManager.log("INFO", dad2, "decision dialog shown");
    }

    public static void show(Context context, long dad2, MessageDialogBuilder dialog) {
        Session session = SESSIONS.get(activity(context));
        if (session == null || !canShow(context)) return;
        Object token = new Object();
        session.dialogToken = token;
        session.dismiss = dialog::dismiss;
        dialog.setOnDismissListener((DialogDismissListener) () -> dismissed(context, dad2, session, token));
        dialog.show();
        ReportConductManager.log("INFO", dad2, "photo upload confirmation shown");
    }

    private static void dismissed(Context context, long dad2, Session expected, Object token) {
        if (SESSIONS.get(activity(context)) != expected || expected.dialogToken != token
                || expected.userId != Globals.getCurrentUserId()) return;
        expected.dismiss = null;
        expected.dialogToken = null;
        expected.conducting = false;
        ReportConductState state = ReportConductManager.get(dad2);
        if (canShow(context) && state != null && state.revision.equals(expected.revision)) {
            ReportConductManager.cancelDecision(dad2);
        }
    }

    public static void release(Context context) {
        Session session = SESSIONS.get(activity(context));
        if (session != null) session.conducting = false;
    }

    public static void closeApprovedDialog(Context context) {
        Session session = SESSIONS.get(activity(context));
        if (session == null) return;
        session.conducting = false;
        Runnable dismiss = session.dismiss;
        session.dismiss = null;
        session.dialogToken = null;
        if (dismiss != null) dismiss.run();
    }

    public static void showResult(long dad2) {
        ReportConductState state = ReportConductManager.get(dad2);
        if (state == null || !state.resultPending) return;
        for (Map.Entry<Activity, Session> entry : SESSIONS.entrySet()) {
            Session session = entry.getValue();
            if (session.dad2 != dad2 || session.userId != Globals.getCurrentUserId()
                    || session.conducting || session.resultShowing || !canShow(entry.getKey())) continue;
            Activity activity = entry.getKey();
            Object token = new Object();
            MessageDialogBuilder dialog = new MessageDialogBuilder(activity)
                    .setTitle("Команда на проведення звіту")
                    .setStatus(state.stage == ReportConductState.Stage.ACCEPTED ? DialogStatus.NORMAL : DialogStatus.ERROR)
                    .setMessage(state.message)
                    .setOnConfirmAction(() -> Unit.INSTANCE);
            dialog.setOnDismissListener((DialogDismissListener) () -> {
                if (SESSIONS.get(activity) != session || session.dialogToken != token
                        || session.userId != Globals.getCurrentUserId()) return;
                session.resultShowing = false;
                session.dismiss = null;
                session.dialogToken = null;
                if (canShow(activity)) ReportConductManager.resultShown(dad2, state.revision);
            });
            session.resultShowing = true;
            session.dialogToken = token;
            session.dismiss = dialog::dismiss;
            dialog.show();
            return;
        }
    }
}
