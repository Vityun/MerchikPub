package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionControlEndAnotherWork<T> extends OptionControl {
    public int OPTION_CONTROL_TASK_ANSWER_ID = 156928;

    public boolean signal = true;

    private Date date;
    private int userId;
    private long codeDad2;

    public OptionControlEndAnotherWork(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                WpDataDB wpDataDB = (WpDataDB) document;

                date = wpDataDB.getDt();
                userId = wpDataDB.getUser_id();
                codeDad2 = wpDataDB.getCode_dad2();
            }
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionControlEndAnotherWork/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        // ---  Получаем данные с БД для подальшей обработке
        RealmResults<WpDataDB> wp = WpDataRealm.getWpData();
        wp = wp.where()
                .equalTo("dt", date)
                .equalTo("user_id", userId)
                .notEqualTo("code_dad2", codeDad2)
                .findAll();
        List<WpDataDB> wpDataDB = RealmManager.INSTANCE.copyFromRealm(wp);

        // ---  Создаём место куда будем писать ошибки
        List<WpDataDB> result = new ArrayList<>();

        // ---  В цикле пробегаем по всему отобранному Плану работ
        for (WpDataDB item : wpDataDB) {
            if (item.getVisit_start_dt() > 0 && item.getVisit_end_dt() == 0) {
                result.add(item);

                // Dialog massage
                spannableStringBuilder
                        .append("Вы еще не закончили (не указали время окончания) ПРЕДЫДУЩУЮ работу!")
                        .append(": ")
                        .append(createLinkedString(item.getAddr_txt() + item.getClient_txt(), item))
                        .append("\n");
            }
        }

        // ---  Отображаем ОБЫЧНОЕ сообщение. Не развёрнутое.
        if (wpDataDB.size() == 0) {
            massageToUser = "Нет данных для анализа окончания ПРЕДЫДУЩИХ работ.";
            signal = false;
//            unlockCodeResultListener.onUnlockCodeFailure();
            unlockCodeResultListener.onUnlockCodeSuccess();
        } else if (result.size() == 0) {
            massageToUser = "Замечаний по указанию времени начала/окончания ПРЕДЫДУЩИХ работ нет.";
            signal = false;
//            unlockCodeResultListener.onUnlockCodeFailure();
            unlockCodeResultListener.onUnlockCodeSuccess();
        } else {
            massageToUser = "Вы еще не закончили (не указали время окончания) ПРЕДЫДУЩУЮ работу!";
            signal = true;
//            unlockCodeResultListener.onUnlockCodeSuccess();
            unlockCodeResultListener.onUnlockCodeFailure();
        }

        setIsBlockOption(signal);
        Log.d("test", "spannableStringBuilder: " + spannableStringBuilder);
        Log.d("test", "massageToUser: " + massageToUser);
    }

    public boolean isSignal() {
        return signal;
    }

    private SpannableString createLinkedString(String msg, WpDataDB wpDataDB) {
        SpannableString res = new SpannableString(msg);

        try {
            long otchetId;
            int action = wpDataDB.getAction();
            if (action == 1 || action == 94) {
                otchetId = wpDataDB.getDoc_num_otchet_id();
            } else {
                otchetId = wpDataDB.getDoc_num_1c_id();
            }

            Data D = new Data(
                    wpDataDB.getId(),
                    wpDataDB.getAddr_txt(),
                    wpDataDB.getClient_txt(),
                    wpDataDB.getUser_txt(),
                    wpDataDB.getDt(),  //+TODO CHANGE DATE
                    otchetId,
                    "",
                    R.mipmap.merchik);

            WorkPlan workPlan = new WorkPlan();
            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());


            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(context, DetailedReportActivity.class);
                    intent.putExtra("WpDataDB_ID", wpDataDB.getId());
//                    intent.putExtra("dataFromWP", D);
//                    intent.putExtra("rowWP", wpDataDB);
//                    intent.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            int count = msg.length();
            res.setSpan(clickableSpan, 0, count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlEndAnotherWork/createLinkedString/Exception", "Exception e: " + e);
        }
        return res;
    }


}
