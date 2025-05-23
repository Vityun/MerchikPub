package ua.com.merchik.merchik.dialogs.EKL;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;
import static ua.com.merchik.merchik.Globals.HELPDESK_PHONE_NUMBER;
import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.toolbar_menus.internetStatus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.ErrorData;
import ua.com.merchik.merchik.ServerExchange.ErrorParams;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ViewHolders.AutoTextUsersViewHolder;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDBDat.UserSDBJoin;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.DataEKL;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogVideo;
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.features.main.DBViewModels.UsersSDBViewModel;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class DialogEKL {

    private Context context;
    private Dialog dialog;

    public static Clicks.OnUpdateUI onUpdateUI;

    public ImageButton close, help, videoHelp, call, addSotr, refresh;
    private TextView title;
    private Button buttonSend, buttonCheck, buttonSend2, buttonSend3;
    private EditText editText;

    private TextView sotr;
    private AutoCompleteTextView tel;

    //---------------------------
    private WpDataDB wp;
    private static UserSDBJoin user;
    private EKL_SDB ekl = new EKL_SDB();


    private Boolean enterCode = false;
    boolean isKeyboardShowing = true;
    private Integer element_id;
    private String telType = "";
    private String telephone = "";
    private int cnt = 0;

    private LoadingDialogWithPercent loadingDialog;
    private ProgressViewModel progress;

    private AutoTextUsersViewHolder adapterUser;
    //---------------------------

    private List<UserSDBJoin> allUsersLJoinTovGrps;

    public DialogEKL(Context context, WpDataDB wp) {
        this.context = context;
        this.wp = wp;

        try {
            dialog = new Dialog(context);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setContentView(R.layout.dialog_ekl);
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
            int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.70);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

            close = dialog.findViewById(R.id.imageButtonClose);
            help = dialog.findViewById(R.id.imageButtonLesson);
            videoHelp = dialog.findViewById(R.id.imageButtonVideoLesson);
            call = dialog.findViewById(R.id.imageButtonCall);
            addSotr = dialog.findViewById(R.id.add_sotr);
            refresh = dialog.findViewById(R.id.refresh);

            buttonSend = dialog.findViewById(R.id.buttonSend3);
            buttonSend2 = dialog.findViewById(R.id.buttonSend2);
            buttonSend3 = dialog.findViewById(R.id.buttonSend);

            buttonCheck = dialog.findViewById(R.id.buttonCheck);
            editText = dialog.findViewById(R.id.editText);
            title = dialog.findViewById(R.id.title);

            sotr = dialog.findViewById(R.id.spinnerSotr);
            tel = dialog.findViewById(R.id.spinnerTel);


            showData();
        } catch (Exception e) {
            Log.e("test", "test: " + e);
            Globals.writeToMLOG("ERROR", "DialogEKL/EXCEPTION/1", "Exception e: " + e);
        }
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public void setTitle(String title) {
        this.title.setVisibility(View.VISIBLE);
        if (title != null && !title.equals("")) {
            this.title.setText(title);
        } else {
            this.title.setVisibility(View.GONE);
        }
    }

    public void setLesson(Context context, boolean visualise, int objectId) {
        if (visualise) help.setVisibility(View.VISIBLE);
        SiteObjectsDB data = RealmManager.getLesson(objectId);

        help.setOnClickListener(v -> {
            if (data != null) {
                DialogData dialogLesson = new DialogData(context);
                dialogLesson.setTitle("Подсказка");
                dialogLesson.setText(data.getComments());
                dialogLesson.show();
            } else {
                Toast.makeText(context, "Для этой странички урок ещё не создан.", Toast.LENGTH_LONG).show();
            }
        });

        help.setOnLongClickListener(v -> {
            if (data != null) {
                Toast.makeText(context, data.getNm(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Для этой странички урок ещё не создан.", Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    public void setVideoLesson(Context context, boolean visualise, int objectId, DialogData.DialogClickListener clickListener) {
        Log.e("setVideoLesson", "click0 Oid: " + objectId);
        try {
            if (visualise) {
                videoHelp.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    videoHelp.getBackground().setTint(Color.RED);
                } else {
                    videoHelp.setBackgroundColor(Color.RED);
                }
                videoHelp.setColorFilter(Color.WHITE);
            }

            SiteObjectsDB object = RealmManager.getLesson(objectId);
            Log.e("setVideoLesson", "object: " + object.getID());

            SiteHintsDB data = null;
            try {
                if (object.getLessonId() != null) {
                    data = RealmManager.getVideoLesson(Integer.parseInt(object.getLessonId()));
                } else {
                    Log.e("setVideoLesson", "getLessonId=null");
                }
            } catch (Exception e) {
                Log.e("setVideoLesson", "Exception e: " + e);
                Globals.writeToMLOG("ERROR", "DialogEKL/EXCEPTION/2", "Exception e: " + e);
            }


            SiteHintsDB finalData = data;
            videoHelp.setOnClickListener(v -> {

                Log.e("setVideoLesson", "click");

                if (finalData != null) {
                    Log.e("setVideoLesson", "click1");
                    if (clickListener == null) {

                        String s = finalData.getUrl();
                        Log.e("setVideoLesson", "click2.URL: " + s);
                        s = s.replace("http://www.youtube.com/", "http://www.youtube.com/embed/");
                        Log.e("setVideoLesson", "click2.replace.URL: " + s);
                        s = s.replace("watch?v=", "");
                        Log.e("setVideoLesson", "click2.replace.URL: " + s);

                        // Отображаем видео
                        // Samsung A6 Galaxy
                        DialogVideo video = new DialogVideo(context);
//                    video.setMerchikIco();
                        video.setTitle("" + finalData.getNm());
                        video.setClose(() -> {
                            Log.e("DialogVideo", "click X");
                            video.dismiss();
                        });
                        video.setVideoLesson(context, true, 0, () -> {
                            Log.e("DialogVideo", "click Video");
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalData.getUrl())));
                        }, null);
                        video.setVideo("<html><body><iframe width=\"700\" height=\"600\" src=\"" + s + "\"></iframe></body></html>");

                        video.show();

                    } else {
                        Log.e("setVideoLesson", "click3");
                        // Переходим по ссылке
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalData.getUrl()))); // Запускаем стартовый ролик - презентацию
                        clickListener.clicked();
                    }
                } else {
                    Log.e("setVideoLesson", "click4");
                    Toast.makeText(context, "Для этой странички Видеоурок ещё не создан.", Toast.LENGTH_LONG).show();
                }


            });


            videoHelp.setOnLongClickListener(v -> {
                if (finalData != null) {
                    Toast.makeText(context, finalData.getNm(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Для этой странички Видеоурок ещё не создан.", Toast.LENGTH_LONG).show();
                }
                return true;
            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogEKL/EXCEPTION/3", "Exception e: " + e);
        }


    }

    public void setImgBtnCall(Context context) {

        Log.e("setImgBtnCall", "i`m here");

        call.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            call.getBackground().setTint(context.getResources().getColor(R.color.greenCol));
        } else {
            call.setBackgroundColor(Color.GREEN);
        }
        call.setColorFilter(Color.WHITE);
        call.setOnClickListener((v -> {
            Globals.telephoneCall(context, HELPDESK_PHONE_NUMBER);
        }));

        Log.e("setImgBtnCall", "and here");
    }


    //==============================================================================================

    public void setData(WpDataDB wp) {
        this.wp = wp;
    }


    private void showData() {
        try {
            EKLDataHolder.Companion.instance().init();

            setAddSotr();   // Установка иконочки добавления/редактирования сотрудника

            Log.e("##############", "getTheme_grp: " + wp.getTheme_grp());
            Log.e("##############", "getDoc_num_grp: " + wp.getDoc_num_grp());


            int id = wp.getAddr_id();
//            List<UserSDBJoin> data = SQL_DB.usersDao().getAllUsersLJoinTovGrps(id);
            allUsersLJoinTovGrps = SQL_DB.usersDao().getAllUsersLJoinTovGrps(id);
            Log.e("!!!!!!!!!", "allUsersLJoinTovGrps: " + allUsersLJoinTovGrps);

            AdditionalRequirementsDB additionalRequirementsDB = AdditionalRequirementsRealm.getADByClientAdr(String.valueOf(wp.getAddr_id()), wp.getClient_id());

//            if (additionalRequirementsDB == null && wp.getClient_id().equals("9128")){
//                additionalRequirementsDB = RealmManager.INSTANCE.copyFromRealm(AdditionalRequirementsRealm.getADByClient(wp.getClient_id()));
//                int us = Integer.parseInt(additionalRequirementsDB.userId);
//                data = SQL_DB.usersDao().getUserLJoinTovGrps(us);
//            }

//            if (additionalRequirementsDB == null && wp.getClient_id().equals("14156")){
//                additionalRequirementsDB = RealmManager.INSTANCE.copyFromRealm(AdditionalRequirementsRealm.getADByClient(wp.getClient_id()));
//                int us = Integer.parseInt(additionalRequirementsDB.userId);
//                data = SQL_DB.usersDao().getUserLJoinTovGrps(us);
//            }

            if (additionalRequirementsDB == null) {
                AdditionalRequirementsDB test = AdditionalRequirementsRealm.getADByClient(wp.getClient_id());
                if (test != null) {
                    additionalRequirementsDB = RealmManager.INSTANCE.copyFromRealm(test);
                    int us = Integer.parseInt(additionalRequirementsDB.userId);
                    allUsersLJoinTovGrps = SQL_DB.usersDao().getUserLJoinTovGrps(us);
                }
            }
            Log.e("!!!!!!!!@@@@", "+");


            Log.e("DialogEKL", "showData/data: " + allUsersLJoinTovGrps);
            Log.e("DialogEKL", "showData/data.size: " + allUsersLJoinTovGrps.size());

            adapterUser = new AutoTextUsersViewHolder(
                    context,
                    android.R.layout.simple_dropdown_item_1line,
                    allUsersLJoinTovGrps
            );
//            sotr.setDropDownHeight(9 * sotr.getLineHeight());
            sotr.setText(underLineText("Виберіть ТПП (Представника Торгової точки)", Color.GRAY));


            adapterUser.setAdditionalInformation(AutoTextUsersViewHolder.AutoTextUserEnum.DEPARTMENT);

//            sotr.setHint("Выберите ПТТ (Представителя Торговой Точки)");

            onUpdateUI = () -> {

                if (EKLDataHolder.Companion.instance().getUsersPTTid() != null && EKLDataHolder.Companion.instance().getUsersPTTName() != null) {
                    if ((EKLDataHolder.Companion.instance().getUsersPTTOtdelId() == null ||
                            EKLDataHolder.Companion.instance().getUsersPTTOtdelId() == 0) && (context instanceof Activity)) {
                        sotr.setText(underLineText("Виберіть ТПП (Представника Торгової точки)", Color.GRAY));
                        new MessageDialogBuilder(unwrap(context))
                                .setTitle("У ПТТ не вказано відділу")
                                .setStatus(DialogStatus.ALERT)
                                .setMessage("У обраного вами представника торгової точки (ПТТ) не вказано відділ, тому він не може підписувати ЕКЛ, виберіть іншого ПТТ, або додайте відділ для цього представника")
                                .setOnConfirmAction("Редагувати ПТТ", () -> {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(editingPTTLinkVersion2()));
                                    context.startActivity(browserIntent);
                                    return Unit.INSTANCE; // Для совместимости с Kotlin
                                })
                                .setOnCancelAction("Змiнити ПТТ", () -> {
                                    startUFMD();
                                    return Unit.INSTANCE; // Для совместимости с Kotlin
                                })
                                .show();

                        tel.setVisibility(View.GONE);
                    } else {

                        sotr.setText(underLineText(EKLDataHolder.Companion.instance().getUsersPTTName() == null ?
                                "Виберіть ТПП (Представника Торгової точки)" : EKLDataHolder.Companion.instance().getUsersPTTName(), Color.BLACK));
                        int targetId = EKLDataHolder.Companion.instance().getUsersPTTid(); // Искомый ID
                        Log.e("onUpdateUI", "targetId: " + targetId);
                        Log.e("onUpdateUI", "targetId: " + sotr.getText());

                        UserSDBJoin res = null;

                        for (UserSDBJoin user : allUsersLJoinTovGrps) {
                            Log.e("onUpdateUI", "user.id: " + user.id);
                            if (user.id == targetId) {
                                res = user;
                                Log.e("onUpdateUI", "1.+");
                                break;
                            }
                        }
                        Log.e("onUpdateUI", "2");

                        Globals.userEKLId = res.id;

                        try {
                            if (res.nm == null) {
                                res.nm = "Отдел не определён";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Globals.writeToMLOG("ERROR", "DialogEKL/EXCEPTION/5", "Exception e: " + e);
                        }
                        user = res;

                        Log.e("onUpdateUI", "3");

                        enterCode = true;
//                    // Установка телефонов
//                    Log.e("onUpdateUI", "4");
                        setTel();
                    }
                }
            };

            if (EKLDataHolder.Companion.instance().getUsersPTTName() == null)
                sotr.setText(underLineText(
                        "Виберіть ТПП (Представника Торгової точки)", Color.GRAY));
            else if (Globals.userEKLId != null && Globals.userEKLId != 0) {
                for (UserSDBJoin item : allUsersLJoinTovGrps) {
                    if (item.id.equals(Globals.userEKLId)) {

                        Globals.writeToMLOG("INFO", "DialogEKL/showData/UserSDBJoin", "item.fio: " + item.fio);
                        Globals.writeToMLOG("INFO", "DialogEKL/showData/UserSDBJoin", "item.nm: " + item.nm);

                        try {
                            if (item.nm == null) {
                                item.nm = "Отдел не определён";
                            }

                            sotr.setText(item.fio + "(" + item.nm + ")");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Globals.writeToMLOG("INFO", "DialogEKL/showData/UserSDBJoin", "Exception e: " + e);
                            sotr.setText(item.fio);
                        }
                    }
                }
            }
//            }

            tel.setHint("Выберите телефон");
            tel.setInputType(0);    // Запрещаю изменять номер


            sotr.setOnClickListener(arg0 -> {
                Log.e("!!!!!!!", "onUpdateUI: " + EKLDataHolder.Companion.instance().getUsersPTTName());
                startUFMD();

            });


            sotr.setOnLongClickListener((v) -> {
//                Log.e("DialogEKL", "setOnLongClickListener");
//                sotr.showDropDown();
//                Globals.showKeyboard(context);
                startUFMD();
                return true;
            });


            refresh.setOnClickListener(v -> {
                updatePTTClientList();
            });
//

            YoYo.with(Techniques.Bounce)
                    .duration(750)
                    .repeat(2)
                    .playOn(buttonSend3);
            YoYo.with(Techniques.Bounce)
                    .duration(750)
                    .repeat(2)
                    .delay(1700)
                    .playOn(buttonSend2);

//            buttonSend.setBackgroundResource(R.drawable.bg_temp);
            buttonSend.setOnClickListener(v -> {
                if (EKLDataHolder.Companion.instance().getUsersPTTNumberTel1() != null
                        && !EKLDataHolder.Companion.instance().getUsersPTTNumberTel1().isEmpty()
                        && EKLDataHolder.Companion.instance().getUsersPTTOtdelId() != null
                        && EKLDataHolder.Companion.instance().getUsersPTTOtdelId() != 0)
                    pressButtonSend();
                else
                    Toast.makeText(context, "Вы не выбрали ПТТ", Toast.LENGTH_LONG).show();
            });

            buttonSend2.setOnClickListener(v -> {
                if (EKLDataHolder.Companion.instance().getUsersPTTNumberTel1() != null &&
                        !EKLDataHolder.Companion.instance().getUsersPTTNumberTel1().isEmpty()
                        && EKLDataHolder.Companion.instance().getUsersPTTOtdelId() != null
                        && EKLDataHolder.Companion.instance().getUsersPTTOtdelId() != 0)
                    sendEKL(v.getContext(), "telegram");
                else
                    Toast.makeText(context, "Вы не выбрали ПТТ", Toast.LENGTH_LONG).show();
            });

            buttonSend3.setOnClickListener(v -> {
                if (EKLDataHolder.Companion.instance().getUsersPTTNumberTel1() != null &&
                        !EKLDataHolder.Companion.instance().getUsersPTTNumberTel1().isEmpty()
                        && EKLDataHolder.Companion.instance().getUsersPTTOtdelId() != null
                        && EKLDataHolder.Companion.instance().getUsersPTTOtdelId() != 0)
                    sendEKL(v.getContext(), "viber");
                else
                    Toast.makeText(context, "Вы не выбрали ПТТ", Toast.LENGTH_LONG).show();
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.e("DialogEKL", "afterTextChanged: " + s);
                    if (s.length() > 4 && user != null) {
                        buttonCheck.setBackgroundResource(R.drawable.bg_temp);
                    } else {
                        buttonCheck.setBackgroundResource(R.drawable.button_bg_inactive);
                    }
                }
            });

            buttonCheck.setBackgroundResource(R.drawable.button_bg_inactive);
            buttonCheck.setOnClickListener(v -> {

                String editTextCode = editText.getText().toString();
                Log.e("DialogEKL", "buttonCheck/editText text: " + editTextCode);
                Log.e("DialogEKL", "buttonCheck/editText text LENGHT: " + editTextCode.length());
                Log.e("DialogEKL", "buttonCheck/ekl: " + ekl.id);
                Globals.writeToMLOG("INFO", "DialogEKL.sendEKL", "editTextCode: " + editTextCode);

                if (user == null)
                    return;

                // Проверка на наличие кода и правильное его
                if (editTextCode.length() >= 4 && editTextCode.length() < 6) {
                    // Ничего не делаем, продолжаем работу
                } else {
                    Toast.makeText(context, "Внесите правильно код в соответствующее поле", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Проверка внесенного Сотрудника
                if (sotr.getText().toString().equals("")) {
                    Log.e("DialogEKL", "Check 1");
                    Toast.makeText(context, "Выберите Сотрудника", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Проверка наличия ЭКЛ у этого сотрудника
                if (SQL_DB.eklDao().getByPTTId(user.id) == null) {
                    Log.e("DialogEKL", "Check 2");
                    Toast.makeText(context, "Такой код у ПТТ " + user.fio + " не зафиксирован", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Сохранение ВПИ для данного ЭКЛ-а
                if (ekl != null && ekl.id != null && ekl.id > 0) {
                    ekl.vpi = System.currentTimeMillis();
                    SQL_DB.eklDao().insertAll(Collections.singletonList(ekl));
                }


                // START
                Toast.makeText(context, "Дождитесь сообщения об окончании работы", Toast.LENGTH_LONG).show();


                // РАБОТА
                Log.e("DialogEKL", "LOOP.POS1");
                responseCheckEKLCode(Collections.singletonList(ekl), new ExchangeInterface.ExchangeResponseInterfaceSingle() {
                    @Override
                    public <T> void onSuccess(T data) {
                        Log.e("DialogEKL", "LOOP.POS2");
                        updateEKLData((EKLCheckData) data);
                    }

                    @Override
                    public void onFailure(ErrorData errorData) {
                        String subTitle = "";
                        if (errorData.getErrorMessage() != null && !errorData.getErrorMessage().isEmpty())
                            subTitle = "Відповідь від сервера";
                        new MessageDialogBuilder(unwrap(context))
                                .setTitle(errorData.getErrorParams() != null && errorData.getErrorParams().getTitle() != null ?
                                        errorData.getErrorParams().getTitle() : "Виникла помилка")
                                .setSubTitle(subTitle)
                                .setStatus(DialogStatus.ERROR)
                                .setMessage(errorData.getErrorMessage() != null ? errorData.getErrorMessage() : "Помилка не визначена")
                                .setOnCancelAction(() -> Unit.INSTANCE
                                )
                                .show();
                    }


                }, Globals.AppWorkMode.ONLINE, false);

            });

        } catch (Exception e) {
            Log.e("EKL", "Exception e: " + e);
        }
    }

    private void startUFMD() {
        Log.e("ValidatorEKL", "startUFMD wp ptt: " + wp.ptt_user_id);
        Log.e("ValidatorEKL", "startUFMD wp dad2: " + wp.getCode_dad2());
        Log.e("ValidatorEKL", "startUFMD wp client_id: " + wp.getClient_id());

        int addrId = wp.getAddr_id();
        List<UsersSDB> usersSDBList = RoomManager.SQL_DB.usersDao().getPTT(addrId);
        if (usersSDBList.isEmpty()) {
            new MessageDialogBuilder(unwrap(context))
                    .setTitle("Не найден подходящий ПТТ")
                    .setSubTitle("Можно скачать список всех ПТТ для данной точки")
                    .setStatus(DialogStatus.ALERT)
                    .setMessage(
                            "Системе не удалось найти представителей торговой точки (птт) у которых вы можете подписать электронно-контрольный лист (экл).\n" +
                                    "Для того что бы просмотреть список всех ПТТ зарегистрированных на данной Торговой точке (ТТ) нажмите на кнопку 'Обновить' \n" +
                                    "Если нужный вам птт в этом списке отсутствует нажмите кнопку + для того что бы зарегистрировать нового представителя торговой точки")
                    .setOnCancelAction(() -> Unit.INSTANCE)
                    .setOnConfirmAction("Обновить", () -> {
                        updatePTTClientList();
                        return Unit.INSTANCE;
                    })
                    .show();
        } else {
            usersSDBList.clear();
            Intent intent = new Intent(context, FeaturesActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("viewModel", UsersSDBViewModel.class.getCanonicalName());
            bundle.putString("contextUI", ContextUI.USERS_SDB_FROM_EKL.toString());
            bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
//                                            JsonObject dataJson = new JsonObject(newPttList);
//                                            bundle.putString("dataJson", jsonString);
//        bundle.putString("dataJson", new Gson().toJson(wp.getAddr_id()));
//        try {
//            bundle.putString("dataJson", new Gson().toJson(
//                    new JSONObject()
//                            .put("addr_id", wp.getAddr_id())
//                            .put("wpDataDBId", String.valueOf(wp.getId()))));
////                            .put("codeDad2", wp.getId()))
//
//        } catch (Exception ignored) {
//        }
            JsonObject dataJson = new JsonObject();
            dataJson.addProperty("addr_id", wp.getAddr_id());
//        dataJson.addProperty("wpDataDBId", String.valueOf(wp.getId()));
            dataJson.addProperty("wpDataClientId", wp.getClient_id());
            dataJson.addProperty("wpDataPttUserId", addrId);
            dataJson.addProperty("wpDataUserId", wp.getUser_id());
            dataJson.addProperty("wpDataTime", wp.getDt().getTime());
            bundle.putString("dataJson", new Gson().toJson(dataJson));

            bundle.putString("title", "Список ПТТ за адресою: " + wp.getAddr_txt());
            bundle.putString("subTitle", "Виберіть ТПП (Представника Торгової точки) якому ви відправите код для підтвердження факту виконаних робіт з даної ТТ та Вашої присутності");
            intent.putExtras(bundle);
            ActivityCompat.startActivityForResult(unwrap(context), intent, NEED_UPDATE_UI_REQUEST, null);

            EKLDataHolder.Companion.instance().init();
        }
    }

    private void sendEKL(Context context, String telType) {
        if (internetStatus == 1)
            responseSendPTTEKLCode(telType, new ExchangeInterface.ExchangeResponseInterfaceSingle() {
                @Override
                public <T> void onSuccess(T data) {
                    try {
                        enterCode = false;
                        EKLRespData resp = (EKLRespData) data;

                        Gson gson = new Gson();
                        String json = gson.toJson(resp);
                        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                        Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/onResponse/onSuccess", "convertedObject: " + convertedObject);
                        Log.e("DialogEKL", "sendStartEKL/onResponse: " + convertedObject);

                        // Создание в БД нового ЭКЛ-а
                        EKL_SDB ekl_sdb = new EKL_SDB();
                        ekl_sdb.id = resp.requestId;
                        ekl_sdb.userId = wp.getUser_id();    // App User
                        ekl_sdb.sotrId = user.id;   // PTT
                        ekl_sdb.clientId = wp.getClient_id();
                        ekl_sdb.addressId = wp.getAddr_id();
                        ekl_sdb.dad2 = wp.getCode_dad2();
                        ekl_sdb.department = user.otdelId != null ? user.otdelId : 0;   // Добавлен отдел, при отправке ЭКЛ
                        ekl_sdb.state = true;
                        ekl_sdb.eklHashCode = resp.codeHash;
                        ekl_sdb.vpi = System.currentTimeMillis() / 1000;

                        // Запись ЭКЛ-а для текущего окна
                        ekl = ekl_sdb;

                        Log.e("DialogEKL", "ekl_sdb: " + ekl_sdb.id);
                        Log.e("DialogEKL", "ekl_sdb: " + ekl_sdb.dad2);

                        Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/onResponse/onSuccess/Хочу_понимать_какой_экл_я_сохраняю", "ekl_sdb: " + new Gson().toJson(ekl_sdb));

                        // Запись ЭКЛ-а в базу данных
                        SQL_DB.eklDao().insertAll(Collections.singletonList(ekl_sdb));

//                                Toast.makeText(context, "Код отправлен. Ответ: " + convertedObject, Toast.LENGTH_LONG).show();

                        Toast.makeText(context, "Код Представителю Торговой Точки отправлен", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/onResponse/onSuccess/Exception", "Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(ErrorData errorData) {
                    String subTitle = "";
                    if (errorData.getErrorMessage() != null && !errorData.getErrorMessage().isEmpty())
                        subTitle = "Відповідь від сервера";

                    if (errorData.getErrorType().equals("error_messenger_not_registered")) {
                        new MessageDialogBuilder(unwrap(context))
                                .setTitle(errorData.getErrorParams() != null && errorData.getErrorParams().getTitle() != null ?
                                        errorData.getErrorParams().getTitle() : "УВАГА!")
                                .setSubTitle(subTitle)
                                .setStatus(DialogStatus.ERROR)
                                .setMessage(errorData.getErrorMessage())
                                .setOnConfirmAction(() -> {
                                    sendRegistrationInTelegram(telType);
                                    Toast.makeText(context, "СМС з посиланням відправлений представнику ПТТ, у разі потреби допоможіть йому завершити налаштування", Toast.LENGTH_LONG).show();
                                    return Unit.INSTANCE;
                                })
                                .setOnCancelAction(() -> Unit.INSTANCE)
                                .show();
                    } else

                        new MessageDialogBuilder(unwrap(context))
                                .setTitle(errorData.getErrorParams().getTitle() != null ?
                                        errorData.getErrorParams().getTitle() : "Виникла помилка")
                                .setSubTitle(subTitle)
                                .setStatus(DialogStatus.ERROR)
                                .setMessage(errorData.getErrorMessage() != null ? errorData.getErrorMessage() : "Помилка не визначена")
                                .setOnCancelAction(() -> Unit.INSTANCE
                                )
                                .show();
                }


//            @Override
//            public void onFailure(String error) {
//                try {
//                    if (error.equals("register_messenger")) {
//                        DialogData dialogData = new DialogData(context);
//                        dialogData.setTitle("Внимание! Получатель сообщения (ПТТ) ещё не подключён к нашему боту.");
//                        dialogData.setText("Для того, чтобы он подключился, нажмите кнопку ОК. На телефон ПТТ будет отправлена SMS с ссылкой, " +
//                                "перейдя по которой он автоматически подключится к нашему боту и Вы сможете отправлять сообщения ему через мессенджер.");
//                        dialogData.setOk("Ok", () -> {
//                            sendRegistrationInTelegram(telType);
//                        });
//                        dialogData.setCancel("Нет", dialogData::dismiss);
//                        dialogData.setClose(dialogData::dismiss);
//                        dialogData.show();
//                    } else {
//                        Globals.writeToMLOG("FAIL", "DialogEKL.sendStartEKL/onFailure", "t: " + error);
//                        Log.e("DialogEKL", "sendStartEKL/onFailure: " + error);
//                        Toast.makeText(context, "При отправке кода ЭКЛ возникла ошибка, повторите попытку позже или обратитесь к Вашему руководителю. Ошибка: " + error, Toast.LENGTH_LONG).show();
//
//                        DialogData dialogData = new DialogData(context);
//                        dialogData.setTitle("Виникла помилка: ");
//                        dialogData.setText(error);
//                        dialogData.setClose(dialogData::dismiss);
//                        dialogData.show();
//                    }
//                } catch (Exception e) {
//                    Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/onResponse/onFailure/Exception", "Exception e: " + e);
//                }
//            }
            });
        else
            new MessageDialogBuilder(unwrap(context))
                    .setTitle("Помилка!")
                    .setStatus(DialogStatus.ERROR)
                    .setMessage("Обнаружена проблема с сетью, проверьте интернет соединение и повторите попытку позже.")
                    .setOnConfirmAction(() -> Unit.INSTANCE)
                    .show();
    }


    private void sendRegistrationInTelegram(String telType) {
        try {
            StandartData data = new StandartData();
            data.mod = "sms_verification";
            data.act = "sms_messenger_registration_url";

            data.sotr_id = String.valueOf(user.id);
            data.messenger_type = telType;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Log.e("DialogEKL", "sendStartEKL/dataSend: " + convertedObject);

            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Globals.writeToMLOG("INFO", "sendRegistrationInTelegram/onResponse", "response.body(): " + new Gson().toJson(response.body()));
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Globals.writeToMLOG("INFO", "sendRegistrationInTelegram/onFailure", "Throwable t: " + t);
                }
            });
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "sendRegistrationInTelegram/", "Exception e: " + e);
        }
    }


    private void updatePTTClientList() {
        progress = new ProgressViewModel(1);
        loadingDialog = new LoadingDialogWithPercent(unwrap(context), progress);
        loadingDialog.show();

        progress.onNextEvent("Оновлюю список ТПП", 6_600);

        new EKLRequests().getPTTByAddress(wp.getAddr_id(), new Clicks.clickObjectAndStatus() {
            @Override
            public void onSuccess(Object data) {
                try {
                    EKLRequests.PTTRequest pttRequest = (EKLRequests.PTTRequest) data;

                    if (pttRequest.state) {
                        if (pttRequest.list != null && pttRequest.list.size() > 0) {
                            List<UserSDBJoin> newPttList = new ArrayList<>();
                            for (EKLRequests.PTT item : pttRequest.list) {

                                boolean userExists = allUsersLJoinTovGrps.stream()
                                        .anyMatch(existingUser -> existingUser.fio.equals(item.fio));

                                if (userExists) {
                                    Log.e("userSDB", "Пользователь с fio " + item.fio + " уже существует, пропускаем.");
                                    continue;
                                }

                                UserSDBJoin userSDBJoin = new UserSDBJoin();

                                userSDBJoin.id = Integer.valueOf(item.userId);
                                userSDBJoin.fio = item.fio;
                                userSDBJoin.tel = item.tel != null ? item.tel : "";
                                userSDBJoin.tel2 = item.tel2;
                                userSDBJoin.authorId = (Integer) item.authorId;
                                userSDBJoin.clientId = Integer.valueOf(item.clientId);
                                if (item.otdelId != null) {
                                    try {
                                        if (item.otdelId instanceof Number) {
                                            // Если это Number (Integer, Double и т.д.)
                                            userSDBJoin.otdelId = ((Number) item.otdelId).intValue();
                                        } else if (item.otdelId instanceof String) {
                                            // Если это строка, пытаемся конвертировать в Integer
                                            userSDBJoin.otdelId = Integer.parseInt((String) item.otdelId);
                                        } else {
                                            // Если тип неизвестен, устанавливаем значение по умолчанию
                                            userSDBJoin.otdelId = 0;
                                        }
                                    } catch (NumberFormatException e) {
                                        // Если строка не может быть преобразована в число
                                        userSDBJoin.otdelId = 0;
                                    }
                                } else {
                                    // Если значение null, устанавливаем значение по умолчанию
                                    userSDBJoin.otdelId = 0;
                                }
//                                        userSDBJoin.otdelId = item.otdelId != null ? (Integer) item.otdelId : 0;
                                userSDBJoin.department = (Integer) item.department;
                                userSDBJoin.sendSms = item.sendSms;
                                userSDBJoin.workAddrId = wp.getAddr_id();

                                Log.e("userSDB", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                                Log.e("userSDB", "id: " + userSDBJoin.id);
                                Log.e("userSDB", "tel: " + userSDBJoin.tel);
                                Log.e("userSDB", "otdel_id: " + userSDBJoin.otdelId);
                                Log.e("userSDB", "workAddrId: " + userSDBJoin.workAddrId);
                                newPttList.add(userSDBJoin);
                            }
                            List<UsersSDB> usersSDBList = new ArrayList<>();
                            for (UserSDBJoin userSDBJoin : newPttList) {
                                usersSDBList.add(mapToUsersSDB(userSDBJoin));
                            }

                            allUsersLJoinTovGrps.addAll(newPttList);

                            SQL_DB.usersDao().insertAll(usersSDBList);

                            Log.e("userSDB!!!", "size: " + newPttList.size());

                            adapterUser = new AutoTextUsersViewHolder(
                                    context,
                                    android.R.layout.simple_dropdown_item_1line,
                                    newPttList
                            );
                            adapterUser.setAdditionalInformation(AutoTextUsersViewHolder.AutoTextUserEnum.DEPARTMENT);
//                                    sotr.setAdapter(adapterUser);

                            progress.onCompleted();
                            Toast.makeText(context, "Список ПТТ Оновлено!", Toast.LENGTH_SHORT).show();

                            startUFMD();

                        }
                    }
                } catch (Exception e) {
                    progress.onCanceled();
                    Log.e("EKLRequests", "Exception e: " + e.getMessage());
                    new MessageDialogBuilder(unwrap(context))
                            .setTitle("Виникла помилка при отриманні переліку ПТТ")
                            .setStatus(DialogStatus.ERROR)
                            .setMessage(e.getMessage() != null ? e.getMessage() : "Помилка не визначена")
                            .setOnCancelAction(() -> Unit.INSTANCE
                            )
                            .show();
                }
            }

            @Override
            public void onFailure(String error) {
                Globals.writeToMLOG("INFO", "getPTTByAddress/RES/onFailure", "String error: " + error);
                new MessageDialogBuilder(unwrap(context))
                        .setTitle("Виникла помилка при отриманні переліку ПТТ")
                        .setStatus(DialogStatus.ERROR)
                        .setMessage(error)
                        .setOnCancelAction(() -> Unit.INSTANCE
                        )
                        .show();
//                        DialogData dialogData = new DialogData(context);
//                        dialogData.setTitle("Виникла помилка при отриманні переліку ПТТ");
//                        dialogData.setText(error);
//                        dialogData.setClose(dialogData::dismiss);
//                        dialogData.show();
            }
        });

    }

    /**
     * 06.06.23.
     * Нажатие на кнопку "Отправить код птт".
     */
    private void pressButtonSend() {
        new MessageDialogBuilder(unwrap(context))
                .setTitle("Увага!")
                .setStatus(DialogStatus.ALERT)
                .setMessage("Для відправлення ЄКЛ краще використовувати Вайбер чи Телеграмм. \n\nВідмовитись від відправлення ЄКЛ за допомогою СМС?")
                .setOnCancelAction("Ні", () -> {
                            sendSMSToPTT();
                            return Unit.INSTANCE;
                        }
                )
                .setOnConfirmAction("Так", () -> Unit.INSTANCE)
                .show();
    }


    /**
     * 06.06.23.
     * Функционал который отправляет SMS Сообщение ПТТшнику на телефон.
     * Все проверки и анализ инфы которая возвращается.
     */
    private void sendSMSToPTT() {
        try {
            Toast.makeText(context, "Дождитесь сообщения об окончании работы", Toast.LENGTH_LONG).show();
            if (user != null) {
                Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/", "user: " + user);
                if (enterCode) {
                    Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/", "enterCode: " + enterCode);
                    if (internetStatus == 1) {
                        Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/internetStatus", "internetStatus: " + internetStatus);
                        responseSendPTTEKLCode(new ExchangeInterface.ExchangeResponseInterfaceSingle() {
                            @Override
                            public <T> void onSuccess(T data) {
                                try {
                                    enterCode = false;
                                    EKLRespData resp = (EKLRespData) data;

                                    Gson gson = new Gson();
                                    String json = gson.toJson(resp);
                                    JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                                    Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/onResponse/onSuccess", "convertedObject: " + convertedObject);
                                    Log.e("DialogEKL", "sendStartEKL/onResponse: " + convertedObject);


                                    // Создание в БД нового ЭКЛ-а
                                    EKL_SDB ekl_sdb = new EKL_SDB();
                                    ekl_sdb.id = resp.requestId;
                                    ekl_sdb.userId = wp.getUser_id();    // App User
                                    ekl_sdb.sotrId = user.id;   // PTT
                                    ekl_sdb.clientId = wp.getClient_id();
                                    ekl_sdb.addressId = wp.getAddr_id();
                                    ekl_sdb.dad2 = wp.getCode_dad2();
                                    ekl_sdb.department = user.otdelId != null ? user.otdelId : 0;   // Добавлен отдел, при отправке ЭКЛ
                                    ekl_sdb.state = true;
                                    ekl_sdb.eklHashCode = resp.codeHash;
                                    ekl_sdb.vpi = System.currentTimeMillis() / 1000;

                                    // Запись ЭКЛ-а для текущего окна
                                    ekl = ekl_sdb;

                                    Log.e("DialogEKL", "ekl_sdb: " + ekl_sdb.id);
                                    Log.e("DialogEKL", "ekl_sdb: " + ekl_sdb.dad2);

                                    // Запись ЭКЛ-а в базу данных
                                    SQL_DB.eklDao().insertAll(Collections.singletonList(ekl_sdb));

//                                Toast.makeText(context, "Код отправлен. Ответ: " + convertedObject, Toast.LENGTH_LONG).show();

                                    Toast.makeText(context, "Код Представителю Торговой Точки отправлен", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Возникла ошибка при отправке ЭКЛ. Обратитесь к руководителю", Toast.LENGTH_SHORT).show();
                                    Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/onResponse/onSuccess/Exception", "Exception e: " + e);
                                }
                            }

                            @Override
                            public void onFailure(ErrorData errorData) {
                                Globals.writeToMLOG("FAIL", "DialogEKL.sendStartEKL/onFailure", "t: " + errorData.getErrorMessage());
                                Log.e("DialogEKL", "sendStartEKL/onFailure: " + errorData.getErrorMessage());
//                                Toast.makeText(context, "При отправке кода ЭКЛ возникла ошибка, повторите попытку позже или обратитесь к Вашему руководителю. Ошибка: " + error, Toast.LENGTH_LONG).show();
                                String subTitle = "";
                                if (errorData.getErrorMessage() != null && !errorData.getErrorMessage().isEmpty())
                                    subTitle = "Відповідь від сервера";
                                new MessageDialogBuilder(unwrap(context))
                                        .setTitle(errorData.getErrorParams() != null && errorData.getErrorParams().getTitle() != null ?
                                                errorData.getErrorParams().getTitle() : "При отправке кода ЭКЛ возникла ошибка, повторите попытку позже или обратитесь к Вашему руководителю.")
                                        .setSubTitle(subTitle)
                                        .setStatus(DialogStatus.ERROR)
                                        .setMessage(errorData.getErrorMessage() != null ? errorData.getErrorMessage() : "Помилка не визначена")
                                        .setOnCancelAction(() -> Unit.INSTANCE
                                        )
                                        .show();
                            }

                        });
                    } else {

                        new MessageDialogBuilder(unwrap(context))
                                .setTitle("Помилка!")
                                .setStatus(DialogStatus.ERROR)
                                .setMessage("Обнаружена проблема с сетью, проверьте интернет соединение и повторите попытку позже.")
                                .setOnConfirmAction(() -> Unit.INSTANCE)
                                .show();

                        Toast.makeText(context, "Обнаружена проблема с сетью, проверьте интернет соединение и повторите попытку позже.", Toast.LENGTH_SHORT).show();
                        Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/", "Обнаружена проблема с сетью, проверьте интернет соединение и повторите попытку позже.");
                    }
                } else {
                    Toast.makeText(context, "Вы уже отправили КОД ПТТшнику", Toast.LENGTH_LONG).show();
                    Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/", "Вы уже отправили КОД ПТТшнику");
                }
            } else {
                Toast.makeText(context, "Выберите сотрудника для отправки ЭКЛа", Toast.LENGTH_LONG).show();
                Globals.writeToMLOG("RESP", "DialogEKL.sendStartEKL/", "Выберите сотрудника для отправки ЭКЛа");
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogEKL.sendStartEKL/buttonSend.setOnClickListener", "Exception e: " + e);
        }
    }


    /**
     * 16.07.2021
     * Нажатие на иконочку Добавления
     * <p>
     * Должна открывать Справочник
     */
    private void setAddSotr() {
        addSotr.setOnClickListener(v -> {
            try {
//                Toast.makeText(context, "Добавление нового ПТТ находится в разработке!", Toast.LENGTH_LONG).show();

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(createAddNewPTTLinkVersion2()));
                context.startActivity(browserIntent);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "DialogEKL/setAddSotr", "Exception e: " + e);
            }
        });
    }


    private String editingPTTLinkVersion2() {
        // Формирование основного пути с параметрами

        String link = String.format("/mobile.php?mod=sotr_list**act=add_list**search=%s", EKLDataHolder.Companion.instance().getUsersPTTName());

        // Получаем данные пользователя
        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);
        if (appUser != null) {
            // Формируем hash
            String hash = String.format(
                    "%s%s%s",
                    appUser.getUserId(),
                    appUser.getPassword(),
                    "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3"
            );
            hash = Globals.getSha1Hex(hash);

            // Формируем итоговую ссылку
            String format =
                    String.format(
                            "https://merchik.com.ua/sa.php?&u=%s&s=%s&l=%s",
                            userId,
                            hash,
                            link
                    );

            // Логируем результат
            Globals.writeToMLOG("INFO", "DialogEKL/setAddSotr/createAddNewPTTLink", "format: " + format);
            Log.e("LINK_FORMA", ">> " + format);
            return format;
        } else {
            // Если пользователя нет, возвращаем только путь
            Globals.writeToMLOG("INFO", "DialogEKL/setAddSotr/createAddNewPTTLink", "link: " + link);
            return link;
        }
    }

    private String createAddNewPTTLinkVersion2() {
        // Формирование основного пути с параметрами
        int addrId = wp.getAddr_id(); // Получаем значение
        String link = String.format("/mobile.php?mod=sotr_list**act=add_sotr**filter[addr_id]=%d", addrId);

        // Получаем данные пользователя
        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);
        if (appUser != null) {
            // Формируем hash
            String hash = String.format(
                    "%s%s%s",
                    appUser.getUserId(),
                    appUser.getPassword(),
                    "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3"
            );
            hash = Globals.getSha1Hex(hash);

            // Формируем итоговую ссылку
            String format =
                    String.format(
                            "https://merchik.com.ua/sa.php?&u=%s&s=%s&l=%s",
                            userId,
                            hash,
                            link
                    );

            // Логируем результат
            Globals.writeToMLOG("INFO", "DialogEKL/setAddSotr/createAddNewPTTLink", "format: " + format);
            Log.e("LINK_FORMA", ">> " + format);
            return format;
        } else {
            // Если пользователя нет, возвращаем только путь
            Globals.writeToMLOG("INFO", "DialogEKL/setAddSotr/createAddNewPTTLink", "link: " + link);
            return link;
        }
    }


    // Запрос на сервер на отправку СМС ПТТшнику

    /**
     * 14.07.2021
     * Запрос на отправку СМСки ПТТшнику.
     */
    public void responseSendPTTEKLCode(ExchangeInterface.ExchangeResponseInterfaceSingle exchange) {
        StandartData data = new StandartData();
        data.mod = "sms_verification";
        data.act = "verification_send";

//        data.sotr_id = "19652";
        data.option_id = 84007;
        data.sotr_id = String.valueOf(user.id);
        data.code_dad2 = String.valueOf(wp.getCode_dad2());
        data.tel_type = telType;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("DialogEKL", "sendStartEKL/dataSend: " + convertedObject);

        retrofit2.Call<EKLRespData> call = RetrofitBuilder.getRetrofitInterface().EKL_RESP_DATA_CALL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<EKLRespData>() {
            @Override
            public void onResponse(Call<EKLRespData> call, Response<EKLRespData> response) {
                if (response.body() != null) {
                    if (response.body().state) {
                        Log.e("Result", "response: " + response.toString());
                        exchange.onSuccess(response.body());
                    } else {
                        String errorType = response.body().error_type != null ? response.body().error_type : "unknown_error";
                        String error = response.body().error != null ? response.body().error : "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут.";
                        exchange.onFailure(new ErrorData(errorType, error));
                    }
                } else {
                    exchange.onFailure(new ErrorData("error_send_failed", "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут."));
                }
            }

            @Override
            public void onFailure(Call<EKLRespData> call, Throwable t) {
                exchange.onFailure(new ErrorData("unknown_error", t.toString()));
            }
        });
    }


    /**
     * 30.05.23.
     * Сделано для кнопок "отпарвить на телеграмм", "отправить на вайбер"
     */
    public void responseSendPTTEKLCode(String telType, ExchangeInterface.ExchangeResponseInterfaceSingle exchange) {
        StandartData data = new StandartData();
        data.mod = "sms_verification";
        data.act = "verification_send";

        data.option_id = 84007;
        data.sotr_id = String.valueOf(user.id);
        data.code_dad2 = String.valueOf(wp.getCode_dad2());
        data.tel_type = telType;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("DialogEKL", "sendStartEKL/dataSend: " + convertedObject);

        retrofit2.Call<EKLRespData> call = RetrofitBuilder.getRetrofitInterface().EKL_RESP_DATA_CALL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<EKLRespData>() {
            @Override
            public void onResponse(Call<EKLRespData> call, Response<EKLRespData> response) {
                try {
                    if (response.body() != null) {
                        Globals.writeToMLOG("INFO", "responseSendPTTEKLCode/TELEGRAM", "response.body(): " + new Gson().toJson(response.body()));
                        if (response.body().state) {
                            exchange.onSuccess(response.body());
                        } else {
//                            if (response.body().additional_action != null && response.body().additional_action.equals("register_messenger")) {
//                                exchange.onFailure("register_messenger", "");
//                            }
                            String errorType = response.body().error_type != null ? response.body().error_type : "unknown_error";
                            String error = response.body().error != null ? response.body().error : "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут.";
                            ErrorParams errorParams = response.body().errorParams.toErrorParams();

                            exchange.onFailure(new ErrorData(errorType, error, errorParams));
                        }
                    } else {
                        exchange.onFailure(new ErrorData("error_send_failed", "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут."));
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "responseSendPTTEKLCode/TELEGRAM", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<EKLRespData> call, Throwable t) {
                exchange.onFailure(new ErrorData("unknown_error", t.toString()));
            }
        });
    }


    /**
     * 14.07.2021
     * Отправка кода подтверждения на сервер. Анализ ответа.
     */
    public void responseCheckEKLCode(List<EKL_SDB> eklSdbList, ExchangeInterface.ExchangeResponseInterfaceSingle exchange, Globals.AppWorkMode appMode, boolean sendMode) {

        if (eklSdbList == null || eklSdbList.size() == 0) return;

        StandartData data = new StandartData();
        data.mod = "sms_verification";
        data.act = "verification_check";

        Log.e("DialogEKL", "responseCheckEKLCode_1: " + data);
        if (sendMode) {
            List<DataEKL> list = new ArrayList<>();
            for (EKL_SDB item : eklSdbList) {
                DataEKL ekl = new DataEKL();
                ekl.id = item.id;
                ekl.element_id = item.id;
                ekl.code = item.code;
                if (wp != null)
                    ekl.dad2 = wp.getCode_dad2();
                list.add(ekl);
            }

            data.data = list;

            if (data.data.isEmpty()) {
                Toast.makeText(context, "Данных на выгрузку нет", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Log.e("DialogEKL", "LOOP.POS5");

            Log.e("DialogEKL", "responseCheckEKLCode_2: " + data);
            String code = editText.getText().toString();
            code = Globals.getSha1Hex(Globals.decodeEKL(code));

            Log.e("DialogEKL", "responseCheckEKLCode_2.1: " + code);


            Integer count = SQL_DB.eklDao().getCountHashCode(code);
            if (count == null || count == 0) {
                Toast.makeText(context, "Такой код не обнаружен, проверьте правильность внесения кода.", Toast.LENGTH_LONG).show();
                return;
            } else {
                Integer countWithDad2 = SQL_DB.eklDao().getCountHashCodeAndDad2(code, wp.getCode_dad2());
                if (countWithDad2 == null || countWithDad2 == 0) {
                    Toast.makeText(context, "Такой код уже был введен ранее, но он не относится к данному посещению.", Toast.LENGTH_LONG).show();
                    return;
                }
            }


            CompositeDisposable disposable = new CompositeDisposable();
            String finalCode = code;
            disposable.add(
//                    SQL_DB.eklDao().getByHashCode(code)
                    SQL_DB.eklDao().getByHashCodeAndDad2(code, wp.getCode_dad2())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((EKL_SDB ekl_sdb) -> {
                                Log.e("DialogEKL", "responseCheckEKLCode_3.1: " + data);

                                if (ekl_sdb.id != null) {
                                    ekl_sdb.code = editText.getText().toString();
                                    ekl_sdb.eklCode = finalCode;
                                    ekl_sdb.vpi = System.currentTimeMillis();
                                    SQL_DB.eklDao().insertAll(Collections.singletonList(ekl_sdb));
                                }

                                DataEKL ekl = new DataEKL();
                                ekl.id = ekl_sdb.id;
                                ekl.element_id = ekl_sdb.id;
                                ekl.dad2 = ekl_sdb.dad2 != null ? ekl_sdb.dad2 : 0;
                                ekl.code = editText.getText().toString();

                                data.data = Collections.singletonList(ekl);
                                Log.e("DialogEKL", "responseCheckEKLCode_3.2: " + data);

                                testResp(data, exchange);
                                Log.e("DialogEKL", "responseCheckEKLCode_3.3 END");
                                disposable.dispose();
                            })
            );


            Log.e("DialogEKL", "responseCheckEKLCode_4: " + data);
            return;
        }

        Log.e("DialogEKL", "responseCheckEKLCode_5: " + data);


        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("DialogEKL", "sendEKL/dataSend: " + convertedObject);


        Log.e("DialogEKL", "LOOP.POS6");
        retrofit2.Call<EKLCheckData> call = RetrofitBuilder.getRetrofitInterface().EKL_CHECK_DATA_CALL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<EKLCheckData>() {
            @Override
            public void onResponse(Call<EKLCheckData> call, Response<EKLCheckData> response) {
                if (response.body() != null) {
                    if (response.body().state) {
                        Log.e("DialogEKL", "LOOP.POS7");
                        exchange.onSuccess((EKLCheckData) response.body());
                    } else {

                        String errorType = response.body().error_type != null ? response.body().error_type : "unknown_error";
                        String error = response.body().error != null ? response.body().error : "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут.";

                        exchange.onFailure(new ErrorData(errorType, error));
                    }
                } else {
                    exchange.onFailure(new ErrorData("error_send_failed", "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут."));
                }
            }

            @Override
            public void onFailure(Call<EKLCheckData> call, Throwable t) {
                exchange.onFailure(new ErrorData("unknown_error", t.toString()));
            }
        });
    }

    private void testResp(StandartData data, ExchangeInterface.ExchangeResponseInterfaceSingle exchange) {
        if (data.data == null) {
            Toast.makeText(context, "Данных на выгрузку нет", Toast.LENGTH_LONG).show();
            return;
        }

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("DialogEKL", "sendEKL/dataSend: " + convertedObject);

        retrofit2.Call<EKLCheckData> call = RetrofitBuilder.getRetrofitInterface().EKL_CHECK_DATA_CALL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<EKLCheckData>() {
            @Override
            public void onResponse(Call<EKLCheckData> call, Response<EKLCheckData> response) {
                if (response.body() != null) {
                    if (response.body().state) {
                        exchange.onSuccess((EKLCheckData) response.body());
                    } else {
                        String errorType = response.body().error_type != null ? response.body().error_type : "unknown_error";
                        String error = response.body().error != null ? response.body().error : "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут.";
                        exchange.onFailure(new ErrorData(errorType, error));
                    }
                } else {
                    exchange.onFailure(new ErrorData("error_send_failed", "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут."));
                }
            }

            @Override
            public void onFailure(Call<EKLCheckData> call, Throwable t) {
                exchange.onFailure(new ErrorData("unknown_error", t.toString()));
            }
        });
    }


    /**
     * 14.07.2021
     * Запрос на получение инфы о полученных кодах. (На сколько я понял - для того что б на стороне
     * приложения в будущем писать сигналы )
     */
    private void responseEKLInfo() {
        StandartData data = new StandartData();
        data.mod = "sms_verification";
        data.act = "list";
//        data.dt_change_from = "";
//        data.dt_change_to = "";



        /*mod=sms_verification
        act=list

        фильтры dt_change_from и dt_change_to как и в остальных местах по unixtime


        в ответе будет список заявок на проверку

        ID - код заявки
        dt - дата создания
        dt_verify - дата успешной проверки
        client_id - код клиента
        addr_id - код адреса
        user_id - код автора заявки
        user_id_verify - код сотрудника, которому отправлен код для проверки
        doc_type - код типа документа
        doc_num - номер документа целочисленный
        doc_num_1c - номер документа текстовый
        code_dad2 - код дад2
        department - код отдела
        code_check - хэш проверочного кода
        code_verify - признак проверки кода (1 - код успешно проверен, 0 - код ещё не проверен и ожидается его передача от пользователя на сторону сервера)*/
    }


    /**
     * 14.07.2021
     * Установка телефона
     * <p>
     * Устанавливает в поле "Номер телефона" телефон или телефоны(выпадающий список если больше
     * одного телефона) у пользователя.
     */
    private void setTel() {
        String telNumber1 = EKLDataHolder.Companion.instance().getUsersPTTNumberTel1();
        String telNumber2 = EKLDataHolder.Companion.instance().getUsersPTTNumberTel2();

        if (telNumber1 != null) {
            tel.setVisibility(View.VISIBLE);
            if (!telNumber1.isEmpty() && telNumber2 != null && !telNumber2.isEmpty()) {
                tel.setOnClickListener(arg0 -> {
                    Log.e("DialogEKL", "Click dropdown TEL : " + tel.getText().length());
                    if (tel.getText().length() > 0) {
                        tel.setText("");
                    }
                    tel.showDropDown();
                });

                // Создаю адаптер
                Log.e("DialogEKL", "create adapter");

                tel.setHint("Кликните для выбора телефона");

                String[] telNumber = new String[2];
                telNumber[0] = hideTelephone(telNumber1);
                telNumber[1] = hideTelephone(telNumber2);
//            telNumber[2] = "телеграм";
//            telNumber[3] = "вайбер";

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, telNumber);

                tel.setAdapter(adapter);
                tel.showDropDown();
                tel.setOnItemClickListener((parent, arg1, position, arg3) -> {
                    Object item = parent.getItemAtPosition(position);
                    if (item instanceof String) {
                        tel.setText(hideTelephone((String) item));
                        Log.e("DialogEKL", "setTel position: " + position);
                        if (position == 0) {
                            telType = "tel1";
                        } else if (position == 1) {
                            telType = "tel2";
                        }

                        telephone = (String) item;
                    }
                });


            } else if ((telNumber1 != null && !telNumber1.isEmpty())
                    || (telNumber2 != null && !telNumber2.isEmpty())) {
                // Должен написать НЕ пустое значение
                Log.e("DialogEKL", "set not null data");
                if (!telNumber1.isEmpty()) {
                    tel.setText(hideTelephone(telNumber1));
                    telType = "tel1";
                    telephone = telNumber1;
                } else {
                    tel.setText(hideTelephone(telNumber2));
                    telType = "tel2";
                    telephone = telNumber2;
                }
            } else {
                // Написать что у ПТТ НЕТ телефонов
                Log.e("DialogEKL", "set text when data equals null");
                tel.setHint("У ПТТ нет телефонов");
            }

            callPTT();
        }
    }

    private void callPTT() {
        tel.setOnLongClickListener(v -> {
            if (!telephone.equals("")) {
                Globals.telephoneCall(context, telephone, "Звонок ПТТ");
            }
            return true;
        });
    }


    /**
     * 14.07.2021
     * Скрытие фрагмента телефона. todo (ментор) так быть не должно. как правильно реализовать?
     * <p>
     * "Заменяет" все цифры в номере телефона кроме 4х последних. Сделано для того что б людям не
     * высвечивались полные номера телефонов ПТТшников.
     */
    private String hideTelephone(String tel) {
        String res = "";
        String phoneNumber = tel.trim();

        if (phoneNumber.length() >= 4) {
            String hideTelPart = phoneNumber.substring(0, phoneNumber.length() - 4);
            StringBuilder X = new StringBuilder();
            for (int i = 0; i <= hideTelPart.length(); i++) {
                X.append("X");
            }
            res = phoneNumber.replace(hideTelPart, X);
        }

        Log.e("DialogEKL", "hideTelephone result: " + res);
        return res;
    }

    /*
     */

    /**
     * Получение на выгрузку и выгрузка Кодов пользователя
     *//*
    public void responseCheckEKLList() {
        List<EKL_SDB> list = SQL_DB.eklDao().getEKLToUpload();
        responseCheckEKLCode(list, new ExchangeInterface.ExchangeResponseInterfaceSingle() {
            @Override
            public <T> void onSuccess(T data) {
                updateEKLData((EKLCheckData) data);
            }

            @Override
            public void onFailure(String error) {

            }
        }, Globals.AppWorkMode.OFFLINE, true);
    }*/
    private void updateEKLData(EKLCheckData data) {
        Log.e("DialogEKL", "LOOP.POS3");

        EKLCheckData res = (EKLCheckData) data;

        Gson gson = new Gson();
        String json = gson.toJson(res);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("RESP", "DialogEKL.sendEKL/onResponse", "convertedObject: " + convertedObject);
        Log.e("DialogEKL", "sendEKL/onResponse: " + convertedObject);

        if (data.state) {
            Toast.makeText(context, "Код принят и будет проверен", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "При проверке кода произошла ошибка: " + data.error, Toast.LENGTH_LONG).show();
        }

        for (EKLCheckData.EKLCheckDataList item : res.list) {
            CompositeDisposable disposable = new CompositeDisposable();
            disposable.add(
                    SQL_DB.eklDao().getById(item.id)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((EKL_SDB ekl_sdb) -> {
                                        try {
                                            Globals.writeToMLOG("RESP", "DialogEKL.sendEKL/onResponse", "ekl_sdb: " + ekl_sdb.id);
                                            Log.e("DialogEKL", "LOOP.POS4");
                                            // TODO Разобраться почему не всегда принимаются ЭКЛ
                                            if (item.state) {
                                                Globals.writeToMLOG("RESP", "DialogEKL.sendEKL/onResponse", "item.state: " + item.state);
                                                ekl_sdb.dt = System.currentTimeMillis() / 1000;
                                                ekl_sdb.eklCode = ekl_sdb.eklHashCode;
                                                if (wp != null)
                                                    ekl_sdb.dad2 = wp.getCode_dad2();
                                                ekl_sdb.upload = true;
                                                ekl_sdb.codeVerify = 1;
                                            } else if (item.error.equals("Ця заявка вже успішно перевірена раніше")) {
                                                ekl_sdb.dt = System.currentTimeMillis() / 1000;
                                                ekl_sdb.eklCode = ekl_sdb.eklHashCode;
                                                ekl_sdb.dad2 = wp.getCode_dad2();
//                                                ekl_sdb.upload = true;
                                                ekl_sdb.codeVerify = 1;
                                            } else {
                                                ekl_sdb.dt = System.currentTimeMillis() / 1000;
                                                ekl_sdb.comment = item.error;
                                                ekl_sdb.codeVerify = 1;
                                            }

                                            SQL_DB.eklDao().insertAll(Collections.singletonList(ekl_sdb));
                                            disposable.dispose();
                                        } catch (Exception e) {
                                            Globals.writeToMLOG("ERROR", "DialogEKL.sendEKL/onResponse", "Exception e: " + e);
                                        }
                                    }
                            )
            );
        }
    }

    private SpannableString underLineText(String text, int color) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private UsersSDB mapToUsersSDB(UserSDBJoin userSDBJoin) {
        UsersSDB usersSDB = new UsersSDB();
        usersSDB.id = userSDBJoin.id;
        usersSDB.fio = userSDBJoin.fio;
        usersSDB.tel = userSDBJoin.tel;
        usersSDB.tel2 = userSDBJoin.tel2;
        usersSDB.clientId = userSDBJoin.clientId != null ? String.valueOf(userSDBJoin.clientId) : null;
        usersSDB.department = userSDBJoin.department;
        usersSDB.otdelId = userSDBJoin.otdelId;
        usersSDB.dtUpdate = userSDBJoin.dtUpdate;
        usersSDB.authorId = userSDBJoin.authorId;
        usersSDB.cityId = userSDBJoin.cityId;
        usersSDB.workAddrId = userSDBJoin.workAddrId;
        usersSDB.inn = userSDBJoin.inn;
        usersSDB.sendSms = userSDBJoin.sendSms;
        usersSDB.reportCount = userSDBJoin.reportCount;
        usersSDB.reportDate01 = userSDBJoin.reportDate01;
        usersSDB.reportDate05 = userSDBJoin.reportDate05;
        usersSDB.reportDate20 = userSDBJoin.reportDate20;
        usersSDB.reportDate40 = userSDBJoin.reportDate40;
        // Добавьте преобразование других полей, если необходимо.
        return usersSDB;
    }


    private Activity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        assert context instanceof Activity;
        return (Activity) context;
    }

    /**
     * 14.07.2021
     * POJO данных которые приходят от сервера в ответ на "Нужно отправить СМС ПТТшнику"
     */
    public class EKLRespData {
        @SerializedName("state")
        @Expose
        public Boolean state;
        @SerializedName("request_id")
        @Expose
        public Integer requestId;
        @SerializedName("code_hash")
        @Expose
        public String codeHash;
        @SerializedName("error")
        @Expose
        public String error;

        /*30.05.23 Добавлено для анализа какая именно ошибка.*/
        @SerializedName("additional_action")
        @Expose
        public String additional_action;

        // добавил 30.01.25
        @SerializedName("error_type")
        @Expose
        public String error_type;

        // 04.02.2025 добавляем новый объект для error_params
        @SerializedName("error_params")
        @Expose
        public ErrorParamsWrapper errorParams;

        public static class ErrorParamsWrapper {
            @SerializedName("title")
            @Expose
            public String title;

            @SerializedName("icon")
            @Expose
            public String icon;

            public ErrorParams toErrorParams() {
                return new ErrorParams(title, DialogStatus.Companion.fromString(icon));
            }
        }
    }

    public class EKLCheckData {
        @SerializedName("state")
        @Expose
        public Boolean state;
        @SerializedName("item")
        @Expose
        public List<EKLCheckDataList> list;
        @SerializedName("error")
        @Expose
        public String error;
        // добавил 30.01.25
        @SerializedName("error_type")
        @Expose
        public String error_type;

        public class EKLCheckDataList {
            @SerializedName("state")
            @Expose
            public Boolean state;

            @SerializedName("ID")
            @Expose
            public Integer id;

            @SerializedName("error")
            @Expose
            public String error;
        }
    }


}
