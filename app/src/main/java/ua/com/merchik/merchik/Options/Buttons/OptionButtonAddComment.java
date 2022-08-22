package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;
import android.widget.Toast;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class OptionButtonAddComment<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_COMMENT_ID = 132623;

    private WpDataDB wpDataDB;

    public OptionButtonAddComment(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
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

        DialogData dialog = new DialogData(context);
        dialog.setTitle("Внесите комментарий");
        dialog.setText("Внесите комментарий к отчёту и нажмите 'Сохранить'");
        dialog.setOperation(DialogData.Operations.TEXT, text, null, null);
        dialog.setCancel("Сохранить", ()->{
            String comment = dialog.getOperationResult();

            if (comment != null && comment.length() > 1){
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    wpDataDB.setDt_update(System.currentTimeMillis()/1000);
                    wpDataDB.user_comment = comment;
                    wpDataDB.user_comment_author_id = wpDataDB.getUser_id();
                    wpDataDB.user_comment_dt_update = System.currentTimeMillis()/1000;
                    wpDataDB.startUpdate = true;

                    realm.copyToRealmOrUpdate(wpDataDB);
                });
                Toast.makeText(dialog.context, "Комментарий: '" + comment + "' сохранён", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }else {
                Toast.makeText(dialog.context, "Комментарий НЕ сохранён. Заполните корректно поле для комментария!", Toast.LENGTH_LONG).show();
            }
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }


}
