package ua.com.merchik.merchik.Options;

import static ua.com.merchik.merchik.Global.UnlockCode.UnlockCodeMode.CODE_DAD_2_AND_OPTION;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportViewModel;
import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Controls.OptionControlAdditionalRequirementsMark;
import ua.com.merchik.merchik.Options.Controls.OptionControlEndAnotherWork;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogManager;

/**
 * 31.05.2022
 * Класс OptionControl занимается сбором данных и отписанием пользователю информации о проблеммах
 * которые возникли при обработке опции.
 */
public class OptionControl<T> {

    public Context context;
    private DialogData dialog;
    private String optionMessagePrefix = "";
    private Runnable correctionCheck;
    public T document;
    public OptionsDB optionDB;
    public WpDataDB wpDataDB;
    public OptionMassageType msgType;
    public Options.NNKMode nnkMode;

    public Clicks.clickStatusMsg clickStatusMsg;

    public UnlockCodeResultListener unlockCodeResultListener;

    public interface UnlockCodeResultListener {
        void onUnlockCodeSuccess();

        void onUnlockCodeFailure();
    }

    public String massageToUser = "";    // Для быстрого сообщения   // НЕ ЮЗАЙ БОЛЬШЕ ЕГО
    public StringBuilder stringBuilderMsg = new StringBuilder();
    public SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    public boolean notCloseSpannableStringBuilderDialog = false;    // Делает так что при клике на текст диалог не будет закрываться
    private boolean block;

    protected void logOptionError(String stage, Throwable error) {
        logOptionError(getClass().getSimpleName() + "/" + stage, optionDB, nnkMode, error);
    }

    public static String describeOption(OptionsDB option) {
        if (option == null) return "option=null";
        try {
            return "rowId=" + option.getID() + ", dad2=" + option.getCodeDad2()
                    + ", optionId=" + option.getOptionId() + ", controlId=" + option.getOptionControlId()
                    + ", blockPns=" + option.getBlockPns() + ", isSignal=" + option.getIsSignal();
        } catch (RuntimeException e) {
            // A deleted/closed Realm object must not hide the original exception.
            return "optionContextUnavailable=" + e;
        }
    }

    public static void logOptionError(String source, OptionsDB option, Options.NNKMode mode, Throwable error) {
        String message = describeOption(option) + ", mode=" + mode + "\n" + Log.getStackTraceString(error);
        try {
            Globals.writeToMLOG("ERROR", source, message);
        } catch (RuntimeException loggingError) {
            Log.e(source, message, loggingError);
        }
    }

    public void showOptionMassage(String msg) {
        try {
            optionMessagePrefix = msg;
            dialog = new DialogData(context);
            unlockCode();
            if (msgType != null && msgType.type != null) {
                switch (msgType.type) {
                    case STRING:
                        Log.e("OptionControl", "(original) Massage to Log: " + stringBuilderMsg);
                        break;

                    case TOAST:
                        Log.e("OptionControl", "(toast) Massage to Log: " + massageToUser);
                        Toast.makeText(context, massageToUser, Toast.LENGTH_LONG).show();
                        break;

                    case DIALOG:
                        Log.e("OptionControl", "(dialog) Massage to Log: " + stringBuilderMsg);

                        if (block && this instanceof OptionControlAdditionalRequirementsMark) {
                            dialog.setVideoLesson(context, true, new Integer[]{10407}, null, null);
                        }
                        if (block && this instanceof OptionControlEndAnotherWork) {
                            dialog.setVideoLessonById(context, 87);
                        }

//                        String optionTitle = "Опция: (" + optionDB.getOptionControlId() + ")\n" + optionDB.getOptionControlTxt();
                        String optionTitle = "Опция: (" + optionDB.getOptionId() + ")\n" + optionDB.getOptionTxt();


                        CharSequence heading = optionMessageHeading();
                        WpDataDB visit = document instanceof WpDataDB ? (WpDataDB) document : wpDataDB;
                        boolean correctionList = correctionCheck != null && visit != null
                                && "1".equals(optionDB.getIsSignal())
                                && (firstAction(heading) != null || firstAction(spannableStringBuilder) != null);
                        if (!correctionList && stringBuilderMsg.toString().length() > 1 && heading.length() == 0) {
                            dialog.setTitle(optionTitle);
                            dialog.setText(OptionMessageLinks.style(context, msg + stringBuilderMsg));
                            if (block) {
                                dialog.setDialogIco();
                            } else {
                                dialog.setDialogNormalIco();
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        } else if (correctionList || spannableStringBuilder.length() > 1) {
//                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            if (block) {
                                dialog.setDialogIco();
                            }
                            setSpannableOptionText(heading, msg, correctionList);
                            dialog.setClose(dialog::dismiss);
                            if (correctionList) dialog.setPassiveDismissEnabled(false);
// Важно! Нужно разрешить клики по ссылкам (если DialogData использует TextView)
//                            if (dialog.getTextView() != null) {
//                                dialog.getTextView().setMovementMethod(LinkMovementMethod.getInstance());
//                            }
                            dialog.show();
                            if (correctionList) observeCorrectionList(dialog, visit.getCode_dad2());
                        } else if (massageToUser.length() > 1) {  // НЕ ЮЗАЙ ЭТО
//                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            dialog.setText(OptionMessageLinks.style(context, massageToUser));
                            if (block) {
                                dialog.setDialogIco();
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }
                        break;
                }
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERR", "OptionControl.showOptionMassage", "STACK: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void setSpannableOptionText(CharSequence heading, String prefix, boolean correctionList) {
        SpannableStringBuilder text = new SpannableStringBuilder("Всього знайдено зауважень щодо " )
                .append(heading)
                .append(prefix == null ? "" : prefix).append(spannableStringBuilder);
        DialogData message = dialog;
        message.setText(OptionMessageLinks.style(context, text),
                correctionList || notCloseSpannableStringBuilderDialog ? () -> {} : message::dismiss);
        if (correctionList) {
            ClickableSpan action = firstAction(text);
            if (action == null) message.hideCancel();
            else message.setCancel("Виправити зауваження", () -> action.onClick(message.getMessageView()));
        }
    }

    private static ClickableSpan firstAction(CharSequence text) {
        if (!(text instanceof Spanned)) return null;
        Spanned styled = (Spanned) text;
        ClickableSpan first = null;
        for (ClickableSpan action : styled.getSpans(0, styled.length(), ClickableSpan.class)) {
            if (first == null || styled.getSpanStart(action) < styled.getSpanStart(first)) first = action;
        }
        return first;
    }

    protected final void enableCorrectionList(Runnable check) {
        correctionCheck = check;
    }

    private void observeCorrectionList(DialogData message, long dad2) {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable refresh = () -> refreshCorrectionList(message);
        Runnable onChanged = () -> {
            handler.removeCallbacks(refresh);
            handler.post(refresh);
        };
        List<Runnable> subscriptions = new ArrayList<>(3);
        message.setOnDismissAction(() -> {
            handler.removeCallbacks(refresh);
            for (Runnable unsubscribe : subscriptions) unsubscribe.run();
            subscriptions.clear();
        });
        subscriptions.add(observeRows(ReportPrepareRealm.getReportPrepareByDad2(dad2), onChanged));
        subscriptions.add(observeRows(StackPhotoRealm.getPhotosByDAD2AllTypes(dad2), onChanged));
        if (context instanceof LifecycleOwner) {
            Lifecycle lifecycle = ((LifecycleOwner) context).getLifecycle();
            DefaultLifecycleObserver observer = new DefaultLifecycleObserver() {
                private boolean paused;

                @Override public void onPause(LifecycleOwner owner) { paused = true; }
                @Override public void onResume(LifecycleOwner owner) {
                    if (!paused) return;
                    paused = false;
                    onChanged.run();
                }
                @Override public void onDestroy(LifecycleOwner owner) { message.dismiss(); }
            };
            lifecycle.addObserver(observer);
            subscriptions.add(() -> lifecycle.removeObserver(observer));
        }
    }

    private static <E extends RealmModel> Runnable observeRows(RealmResults<E> rows, Runnable onChanged) {
        RealmChangeListener<RealmResults<E>> listener = ignored -> onChanged.run();
        rows.addChangeListener(listener);
        return () -> {
            if (rows.isValid()) rows.removeChangeListener(listener);
        };
    }

    private void refreshCorrectionList(DialogData message) {
        if (dialog != message || !message.isDialogShow()) return;
        try {
            stringBuilderMsg.setLength(0);
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            setIsBlockOption(false);
            correctionCheck.run();
            if ("2".equals(optionDB.getIsSignal())) {
                message.dismiss();
            } else if (spannableStringBuilder.length() > 0) {
                if (block) message.setDialogIco();
                else message.setDialogNormalIco();
                setSpannableOptionText(optionMessageHeading(), block ? optionMessagePrefix : "", true);
            }
        } catch (RuntimeException e) {
            logOptionError("refreshCorrectionList", e);
        }
    }

    protected CharSequence optionMessageHeading() { return ""; }

    public void unlockCode() {
        dialog.setUnlockMode(() -> {
            WpDataDB wpDataDB1 = WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(optionDB.getCodeDad2()));
            showUnlockCodeDialogInMainThread(wpDataDB1, isBlockOption());
        });
        dialog.setImgBtnCall(context);
        if (block)
            dialog.setCancel("Виправити зауваження", () -> {
                Log.e("optionNotConduct", "optionDB: " + new Gson().toJson(optionDB));
                DetailedReportViewModel viewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(DetailedReportViewModel.class);
                viewModel.postScrollToId(optionDB);
                DialogManager.dismissAll();
            });
        else
            dialog.setCancel("Закрити", DialogManager::dismissAll);
    }


    public void setIsBlockOption(boolean block) {
        this.block = block;
    }

    public boolean isBlockOption() {
        return block;
    }

    public boolean isBlockOption2() {
        if (block) {
            unlockCodeResultListener.onUnlockCodeFailure();
        } else {
            unlockCodeResultListener.onUnlockCodeSuccess();
        }
        return block;
    }

    protected double calculatePenalty(WpDataDB optionWpDataDB) {
        return calculatePenalty(optionWpDataDB, 1.00);
    }

    protected double calculatePenalty(WpDataDB optionWpDataDB, double multiplier) {
        if (optionWpDataDB == null) {
            String optionId = optionDB != null ? optionDB.getOptionId() : "null";
            String codeDad2 = optionDB != null ? optionDB.getCodeDad2() : "null";
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControl/calculatePenalty",
                    "wpDataDB is null, optionId=" + optionId + ", codeDad2=" + codeDad2
            );
            return 0.00;
        }

        return optionWpDataDB.getCash_zakaz() * Globals.OPTION_CONTROL_PENALTY_RATE * multiplier;
    }


    public void showUnlockCodeDialogInMainThread(WpDataDB wpDataDB, boolean signal) {
        new UnlockCode().showDialogUnlockCode(context, wpDataDB, optionDB, CODE_DAD_2_AND_OPTION, new Clicks.clickStatusMsg() {
            @Override
            public void onSuccess(String data) {
                setIsBlockOption(false);
                unlockCodeResultListener.onUnlockCodeSuccess();
                dialog.dismiss();
                Toast.makeText(context, "Код прийнято", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                setIsBlockOption(signal);
                unlockCodeResultListener.onUnlockCodeFailure();
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionDB != null) {
                        optionDB.setBlockPns("1");
                        optionDB.setIsSignal("1");
                        realm.insertOrUpdate(optionDB);
                    }
                });
                Toast.makeText(context, "Код розблокування НЕ прийнято", Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean checkUnlockCode(OptionsDB optionDB) {
        try {
            // 12.08.24. Виктор. Для будущих поколений.
            // Обращайте внимание - сохраняет ли опция контроля сигнал. Был случай когда не было
            // сохранения/обновления сигнала в БД изза чего мерчер НЕ БЛОКИРОВАЛО то что должно было блокировать.
            if (optionDB == null) return false;
            if ("1".equals(optionDB.getIsSignal())) {
                if (new UnlockCode().applyStoredUnlockCode(optionDB)) {
                    stringBuilderMsg.append("\n\nАле виконавцю видано код розблокування!");
                    spannableStringBuilder.append("\n\nАле виконавцю видано код розблокування!");
                    setIsBlockOption(false);
                    return true;
                }
                if ("1".equals(optionDB.getBlockPns())) {
                    return false;
                }
            }
            setIsBlockOption(false);
            return true;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControl/checkUnlockCode", "Exception e: " + e);
            return false;
        }
    }
}
