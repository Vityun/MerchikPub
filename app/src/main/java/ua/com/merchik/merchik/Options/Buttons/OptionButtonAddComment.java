package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.ViewHolders.TextViewClickAdapter;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class OptionButtonAddComment<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_COMMENT_ID = 132623;

    private WpDataDB wpDataDB;

    public OptionButtonAddComment(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        if (document instanceof WpDataDB) {
            this.wpDataDB = WpDataRealm.getWpDataRowByDad2Id(((WpDataDB) document).getCode_dad2());
        }
    }

    private void executeOption() {

        String text = "Ваш комментарий";
        if (wpDataDB.user_comment != null && wpDataDB.user_comment.length() > 0){
            text = wpDataDB.user_comment;
        }

        showDefaultDialog(text);
    }

    private void showDefaultDialog(String text){
        DialogData dialog = new DialogData(context);
        dialog.setTitle("Внесите комментарий");
        dialog.setText("Внесите комментарий к отчёту и нажмите 'Сохранить'");
        dialog.setOperation(DialogData.Operations.TEXT, text, null, null);

        if (wpDataDB.getClient_id().equals("14874")){
            dialog.setAdditionalOperation(new TextViewClickAdapter(getStringsTetaMarket(), new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    Toast.makeText(dialog.context, "Ви обрали: " + data, Toast.LENGTH_LONG).show();
                    dialog.setEditTextText((String) data);
                }
            }), new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        }


        dialog.setOk("Сохранить", ()->{
            String comment = dialog.getOperationResult();

            if (comment != null && comment.length() > 1){
                final Long curTime = System.currentTimeMillis()/1000;
                final long minute = 360;
                if (wpDataDB.user_comment_dt_update == 0 || ((curTime - wpDataDB.user_comment_dt_update) < minute)) {
                    RealmManager.INSTANCE.executeTransaction(realm -> {
                        wpDataDB.setDt_update(System.currentTimeMillis() / 1000);
                        wpDataDB.user_comment = comment;
                        wpDataDB.user_comment_author_id = wpDataDB.getUser_id();
                        wpDataDB.user_comment_dt_update = System.currentTimeMillis() / 1000;
                        wpDataDB.startUpdate = true;

                        realm.copyToRealmOrUpdate(realm.copyFromRealm(wpDataDB));
                    });
                    Toast.makeText(dialog.context, "Комментарий: '" + comment + "' сохранён", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(dialog.context, "Разрешенное время для комментария (1ч) закончилось! Прошло " + (curTime - wpDataDB.user_comment_dt_update)/60 + " минут", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(dialog.context, "Комментарий НЕ сохранён. Заполните корректно поле для комментария!", Toast.LENGTH_LONG).show();
            }
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    private List<String> getStringsTetaMarket(){
        List<String> comments = new ArrayList<>();
        comments.add("Всі товари в наявності, більше 5 шт на кожне місце викладки.");
        comments.add("Деякі товари відсутні на складі.");
        comments.add("Всі товари в наявності, але менше 5 шт на кожне місце викладки. Персонал магазину не дозволив виставити по 5 шт. Номер розпорядження офісу керуючому магазину повідомив (ла) - товар зі складу все одно не винесли.");
        comments.add("На початок візиту на полиці менше 5шт на кожному місці викладки стартових пакетів, після звернення до персоналу товар зі складу винесли.");
        comments.add("Інша інформація щодо наявності товару, кількості місць викладки, тощо. Заповнити самостійно в довільній формі.");

        return comments;
    }


}
