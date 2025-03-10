package ua.com.merchik.merchik.dialogs.DialogAchievement;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;
import static ua.com.merchik.merchik.Globals.HELPDESK_PHONE_NUMBER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Objects;

import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.dialogs.DialogVideo;
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalRequirementsDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.StackPhotoDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.ThemeDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.TradeMarkDBViewModel;

public class DialogCreateAchievement {

    public static Clicks.click clickVoidAchievement;

    public static Clicks.OnUpdateUI onUpdateUI;

    private Dialog dialog;
    private Context context;
//    private WpDataDB wpDataDB;

    private int addressId, userId;
    private long codeDad2;
    private String clientId, clientTxt, addressTxt, userTxt;


    private StackPhotoDB stackPhotoDBTo, stackPhotoDBAfter;

    private ImageButton close, help, videoHelp, call, addSotr;
    private TextView title, client, address, visit, theme, offerFromClient, offerFromClientItem, tovarTxt, tradeMarkItem, themeItem;
    private EditText comment;
    private Button save;
    //    private Button photoTo, photoAfter;
    private ImageView photoToIV, photoAfterIV;

//    private Spinner spinnerTheme;
//    private Spinner spinnerClient, spinnerManufacture;
//    public static String[] themeList = new String[]{
//            "Досягнення (покращення розташування товару в ТТ)",     // 595
//            "Замовлення на фінансування нового Досягнення",         // 1252
//            "Утримання викладки на полиці (досягнутого раніше)"};   // 1251

    //    private Integer spinnerThemeResult, spinnerClientResult, spinnerManufactureResult;
    private OptionsDB optionDB;
//    private TovarDB tovarDB;

    public DialogCreateAchievement(Context context/*, WpDataDB wpDataDB*/) {
        this.context = context;
        /*this.wpDataDB = wpDataDB;*/
        try {
            dialog = new Dialog(context);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setContentView(R.layout.dialog_create_achievement);
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
//            int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.90);
//            dialog.getWindow().setLayout(width, height);

//            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
//            dialog.getWindow().setLayout(height, WindowManager.LayoutParams.MATCH_PARENT);

            close = dialog.findViewById(R.id.imageButtonClose);
            help = dialog.findViewById(R.id.imageButtonLesson);
            videoHelp = dialog.findViewById(R.id.imageButtonVideoLesson);
            call = dialog.findViewById(R.id.imageButtonCall);
            addSotr = dialog.findViewById(R.id.add_sotr);

            title = dialog.findViewById(R.id.title);
            client = dialog.findViewById(R.id.client);
            address = dialog.findViewById(R.id.address);
            visit = dialog.findViewById(R.id.visit);
            theme = dialog.findViewById(R.id.theme);
            offerFromClient = dialog.findViewById(R.id.offer_from_the_client);
            offerFromClientItem = dialog.findViewById(R.id.offer_from_the_client_item);
            offerFromClientItem.setOnClickListener(view -> {
                Intent intent = new Intent(context, FeaturesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("viewModel", AdditionalRequirementsDBViewModel.class.getCanonicalName());
                bundle.putString("contextUI", ContextUI.ADD_REQUIREMENTS_FROM_ACHIEVEMENT.toString());
                bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
                bundle.putString("dataJson", new Gson().toJson(clientId));
                bundle.putString("title", "Предложение");
                bundle.putString("subTitle", "Предложение от клиента, которое нужно выполнить для получения дополнительных бонусов");
                intent.putExtras(bundle);
                ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
            });

            comment = dialog.findViewById(R.id.comment);

//            photoTo = dialog.findViewById(R.id.photo_to);
//            photoAfter = dialog.findViewById(R.id.photo_after);
            save = dialog.findViewById(R.id.save);

            photoToIV = dialog.findViewById(R.id.photoTo);
            photoAfterIV = dialog.findViewById(R.id.photoAfter);

//            spinnerTheme = dialog.findViewById(R.id.spinner_theme);
            themeItem = dialog.findViewById(R.id.themeItem);
            themeItem.setOnClickListener(view -> {
                Intent intent = new Intent(context, FeaturesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("viewModel", ThemeDBViewModel.class.getCanonicalName());
                bundle.putString("contextUI", ContextUI.THEME_FROM_ACHIEVEMENT.toString());
                bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
                bundle.putString("dataJson", new Gson().toJson(new String[]{"595", "1252", "1251"}));
                bundle.putString("title", "Вид достижения");
                bundle.putString("subTitle", "Выберите характер достижения, которое Вы выполнили");
                intent.putExtras(bundle);
                FilteringDialogDataHolder.Companion.instance().init();
                ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
            });
//            spinnerClient = dialog.findViewById(R.id.spinner_client);
//            spinnerManufacture = dialog.findViewById(R.id.spinner_trade_mark);
            tradeMarkItem = dialog.findViewById(R.id.tradeMarkItem);
            tradeMarkItem.setOnClickListener(view -> {
                Intent intent = new Intent(context, FeaturesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("viewModel", TradeMarkDBViewModel.class.getCanonicalName());
                bundle.putString("contextUI", ContextUI.TRADE_MARK_FROM_ACHIEVEMENT.toString());
                bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
                bundle.putString("dataJson", new Gson().toJson(codeDad2));
                bundle.putString("title", "Торговые марки");
                bundle.putString("subTitle", "Выберите торговые марки");
                intent.putExtras(bundle);
                ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
            });
            tovarTxt = dialog.findViewById(R.id.tovar_choose);
            tovarTxt.setOnClickListener(view -> {
                Intent intent = new Intent(context, FeaturesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("viewModel", TovarDBViewModel.class.getCanonicalName());
                bundle.putString("contextUI", ContextUI.TOVAR_FROM_ACHIEVEMENT.toString());
                bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
//                bundle.putString("dataJson", "{\"codeDad2\":\""+codeDad2+"\"}");
                try {
                    bundle.putString("dataJson", new Gson().toJson(
                            new JSONObject()
                                    .put("codeDad2", Long.toString(codeDad2))
                                    .put("clientId", clientId))
                    );
                } catch (Exception ignored) {
                }
                bundle.putString("title", "Товари");
                bundle.putString("subTitle", "Выберите товар");
//                bundle.putString('req', "");
//                bundle.putInt("idResImage", R.drawable.ic_caution);
                intent.putExtras(bundle);
                ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
            });

            setTextUI();

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogCreateAchievement", "Exception e: " + e);
        }
    }


    private void buttonSave() {
        save.setOnClickListener(v -> {
            try {
                AchievementsSDB achievementsSDB = new AchievementsSDB();
                achievementsSDB.serverId = 0;
                achievementsSDB.dt = String.valueOf((System.currentTimeMillis() / 1000));
                achievementsSDB.dt_ut = (System.currentTimeMillis() / 1000);
                achievementsSDB.addrId = addressId;
                achievementsSDB.adresaNm = addressTxt;
                achievementsSDB.dvi = 1;
                achievementsSDB.error = 0;
                achievementsSDB.tovar_id = Objects.requireNonNullElse(AchievementDataHolder.Companion.instance().getTovarId(), 0);;
                achievementsSDB.currentVisit = 0;
                achievementsSDB.score = "0";
                achievementsSDB.clientId = clientId;
                achievementsSDB.spiskliNm = clientTxt;
                achievementsSDB.codeDad2 = codeDad2;
                achievementsSDB.sotrFio = userTxt;
                if (comment != null && comment.getText() != null && comment.getText().toString().length() > 10) {
                    achievementsSDB.commentDt = String.valueOf((System.currentTimeMillis() / 1000));
                    achievementsSDB.commentUser = String.valueOf(userId);
                    achievementsSDB.commentTxt = comment.getText().toString();
                } else {
                    Toast.makeText(v.getContext(), "Ви не вказали коментар до досягнення, досягнення створено не буде", Toast.LENGTH_LONG).show();
                    return;
                }

//                achievementsSDB.themeId = 595;
                if (AchievementDataHolder.Companion.instance().getThemeId() != null) {
                    achievementsSDB.themeId = AchievementDataHolder.Companion.instance().getThemeId();
                } else {
                    Toast.makeText(v.getContext(), "Ви не вказали тему досягнення, досягнення створено не буде", Toast.LENGTH_LONG).show();
                    return;
                }

                if (AchievementDataHolder.Companion.instance().getRequirementClientId() != null) {
                    achievementsSDB.addRequirementId = AchievementDataHolder.Companion.instance().getRequirementClientId();
                } else {
                    achievementsSDB.addRequirementId = 0;
                    Toast.makeText(v.getContext(), "Ви не вказали пропозицію клієнта, досягнення створено не буде", Toast.LENGTH_LONG).show();
                    return;
                }

                if (AchievementDataHolder.Companion.instance().getManufactureId() != null) {
                    achievementsSDB.manufacturer = AchievementDataHolder.Companion.instance().getManufactureId();
                }

                if (AchievementDataHolder.Companion.instance().getPhotoHashTo() != null) {
//                    achievementsSDB.img_before_hash = stackPhotoDBTo.photo_hash;
                    achievementsSDB.img_before_hash = AchievementDataHolder.Companion.instance().getPhotoHashTo();
                } else {
                    Toast.makeText(v.getContext(), "Виберіть фото До, досягнення створено не буде", Toast.LENGTH_LONG).show();
                    return;
                }

                if (AchievementDataHolder.Companion.instance().getPhotoHashAfter() != null) {
//                    achievementsSDB.img_after_hash = stackPhotoDBAfter.photo_hash;
                    achievementsSDB.img_after_hash = AchievementDataHolder.Companion.instance().getPhotoHashAfter();
                } else {
                    Toast.makeText(v.getContext(), "Виберіть фото Після, досягнення створено не буде", Toast.LENGTH_LONG).show();
                    return;
                }

                Globals.writeToMLOG("INFO", "" + getClass().getName() + "/buttonSave", "" + new Gson().toJson(achievementsSDB));

                SQL_DB.achievementsDao().insertAll(Collections.singletonList(achievementsSDB));
                Toast.makeText(v.getContext(), "Створено нове досягнення", Toast.LENGTH_LONG).show();
                dismiss();

//                detailedReportOptionsFrag.recycleViewDRAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "DialogAchievement/buttonSave", "Exception e: " + e);
            }
        });
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
        /* 05.02.2025
         TODO это хреновый костыль довести до ума
         */
        Log.e("--------------","+++++++");
//        Intent intent = new Intent(context, FeaturesActivity.class);
//        ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);

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

    public void setOption(OptionsDB optionDB) {
        this.optionDB = optionDB;
    }

    public void putTextData() {
        client.setText(Html.fromHtml("<b>Кліент: </b> " + clientTxt + ""));
        address.setText(Html.fromHtml("<b>Адреса: </b> " + addressTxt + ""));
        visit.setText(Html.fromHtml("<b>Відвідування: </b> " + codeDad2 + ""));
        theme.setText(Html.fromHtml("<b>Тема: </b> "));

//        createSpinnerTheme();

//        createSpinnerClient();

//        createSpinnerManufacture();

        offerFromClient.setText(Html.fromHtml("<b>Пропозиція від клієнта: </b> " + "" + ""));
    }

    /**
     * Это для установки фото ДО. Для создания одного достижения на основании другого.
     */
    public void setPhotoDo(StackPhotoDB stackPhotoDB) {
        stackPhotoDBTo = stackPhotoDB;
        photoToIV.setVisibility(View.VISIBLE);
        photoToIV.setImageURI(Uri.parse(stackPhotoDB.photo_num));
        photoToIV.setOnClickListener(v1 -> {
            try {
                DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(v1.getContext());
                dialogFullPhoto.setPhoto(Uri.parse(stackPhotoDB.photo_num));
                dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                dialogFullPhoto.show();
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "DialogAchievement/buttonPhotoTo", "Exception e: " + e);
            }
        });
    }

    public void buttonPhotoTo() {
//        photoTo.setVisibility(View.GONE);
        photoToIV.setVisibility(View.VISIBLE);
        photoToIV.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeaturesActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
            bundle.putString("contextUI", ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT.toString());
            bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
            bundle.putString("dataJson", new Gson().toJson(codeDad2));
            bundle.putString("title", "Перелік фото звітів");
            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(14).getNm());
            intent.putExtras(bundle);
            ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
        });
//        photoTo.setOnClickListener(v -> {
//            try {
//                Intent intentPhotoLog = new Intent(v.getContext(), PhotoLogActivity.class);
//                intentPhotoLog.putExtra("achievements", true);
//                intentPhotoLog.putExtra("choise", true);
//                intentPhotoLog.putExtra("dad2", codeDad2);
//                intentPhotoLog.putExtra("photoType", 14);
//                v.getContext().startActivity(intentPhotoLog);
//
//                photoTo.setVisibility(View.GONE);
//
//                clickVoidAchievement = new Clicks.click() {
//                    @Override
//                    public <T> void click(T data) {
//                        stackPhotoDBTo = (StackPhotoDB) data;
//                        photoToIV.setVisibility(View.VISIBLE);
//                        photoToIV.setImageURI(Uri.parse(stackPhotoDBTo.photo_num));
//                        photoToIV.setOnClickListener(v1 -> {
//                            try {
//                                DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(v1.getContext());
//                                dialogFullPhoto.setPhoto(Uri.parse(stackPhotoDBTo.photo_num));
//                                dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
//                                dialogFullPhoto.show();
//                            } catch (Exception e) {
//                                Globals.writeToMLOG("ERROR", "DialogAchievement/buttonPhotoTo", "Exception e: " + e);
//                            }
//                        });
//                    }
//                };
//
//            } catch (Exception e) {
//                Globals.writeToMLOG("ERROR", "buttonPhotoTo", "Exception e: " + Arrays.toString(e.getStackTrace()));
//            }
//        });
    }

    public void buttonPhotoAfter() {
//        photoAfter.setVisibility(View.GONE);
        photoAfterIV.setVisibility(View.VISIBLE);
        photoAfterIV.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeaturesActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
            bundle.putString("contextUI", ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT.toString());
            bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
            bundle.putString("dataJson", new Gson().toJson(codeDad2));
            bundle.putString("title", "title");
            bundle.putString("subTitle", "subTitle");
            intent.putExtras(bundle);
            ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
        });
//        photoAfterIV.setOnClickListener(v -> {
//            try {
//                Intent intentPhotoLog = new Intent(v.getContext(), PhotoLogActivity.class);
//                intentPhotoLog.putExtra("achievements", true);
//                intentPhotoLog.putExtra("choise", true);
//                intentPhotoLog.putExtra("dad2", codeDad2);
//                intentPhotoLog.putExtra("photoType", 0);
//                v.getContext().startActivity(intentPhotoLog);
//
////                photoAfter.setVisibility(View.GONE);
//
//                clickVoidAchievement = new Clicks.click() {
//                    @Override
//                    public <T> void click(T data) {
//                        stackPhotoDBAfter = (StackPhotoDB) data;
//                        photoAfterIV.setVisibility(View.VISIBLE);
//                        photoAfterIV.setImageURI(Uri.parse(stackPhotoDBAfter.photo_num));
//                        photoAfterIV.setOnClickListener(v1 -> {
//                            try {
//                                DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(v1.getContext());
//                                dialogFullPhoto.setPhoto(Uri.parse(stackPhotoDBTo.photo_num));
//                                dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
//                                dialogFullPhoto.show();
//                            } catch (Exception e) {
//                                Globals.writeToMLOG("ERROR", "DialogAchievement/buttonPhotoAfter", "Exception e: " + e);
//                            }
//                        });
//                    }
//                };
//
//            } catch (Exception e) {
//                Globals.writeToMLOG("ERROR", "buttonPhotoTo", "Exception e: " + Arrays.toString(e.getStackTrace()));
//            }
//        });
    }

//    public void createSpinnerTheme() {
////        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, themeList);
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        SpinnerAdapter adapter1 = new SpinnerAdapter(context, themeList, "Выберите тему");
//        spinnerTheme.setAdapter(adapter1);
//        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String data = adapterView.getItemAtPosition(i).toString();
//                Toast.makeText(adapterView.getContext(), "Выбрали: " + data, Toast.LENGTH_SHORT).show();
//
//                if (data.equals(themeList[0])) {
//                    spinnerThemeResult = 595;
//                } else if (data.equals(themeList[1])) {
//                    spinnerThemeResult = 1252;
//                } else if (data.equals(themeList[2])) {
//                    spinnerThemeResult = 1251;
//                }/*else {
//                    spinnerThemeResult = 595;
//                }*/
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                Toast.makeText(adapterView.getContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show();
////                spinnerThemeResult = 595;
//            }
//        });
//    }

//    public void createSpinnerClient() {
//        List<AdditionalRequirementsDB> additionalRequirementsDBList = AdditionalRequirementsRealm.getADByClientAll(clientId, "1253");
//
//        if (additionalRequirementsDBList == null) return;
//
//        String[] clientList = new String[additionalRequirementsDBList.size() + 1];
//        clientList[0] = "це досягнення не відноситься до жодної з пропозицій замовника";
//        for (int i = 0; i < additionalRequirementsDBList.size(); i++) {
//            clientList[i + 1] = additionalRequirementsDBList.get(i).getNotes();
//        }
//
//
////        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, clientList);
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        SpinnerAdapter adapter1 = new SpinnerAdapter(context, clientList, additionalRequirementsDBList.size() > 0 ? "От клиента есть (" + additionalRequirementsDBList.size() + ") предложений" : "Предложений от клиента НЕТ");
//        spinnerClient.setAdapter(adapter1);
//        spinnerClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String data = adapterView.getItemAtPosition(i).toString();
//                Toast.makeText(adapterView.getContext(), "Выбрали: " + data, Toast.LENGTH_SHORT).show();
//
//                // Используем Stream для поиска объекта в списке по значению notes
//                Optional<AdditionalRequirementsDB> foundObject = additionalRequirementsDBList.stream()
//                        .filter(item -> data.equals(item.getNotes()))
//                        .findFirst();
//
//                // Проверяем, найден ли объект
//                if (foundObject.isPresent()) {
//                    // Объект найден, можно получить его id или выполнить другие действия
//                    int objectId = foundObject.get().getId();
//                    spinnerClientResult = objectId;
//                    Toast.makeText(adapterView.getContext(), "Выбрали: " + data + ", id: " + objectId, Toast.LENGTH_SHORT).show();
//                } else if (data.equals("це досягнення не відноситься до жодної з пропозицій замовника")) {
//                    spinnerClientResult = 1;
//                } else {
//                    // Объект не найден
//                    Toast.makeText(adapterView.getContext(), "Объект не найден", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                Toast.makeText(adapterView.getContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show();
//                spinnerClientResult = 0;
//            }
//        });
//    }

//    public void createSpinnerManufacture() {
//        List<TovarDB> tovarDBList = RealmManager.INSTANCE.copyFromRealm(RealmManager.getTovarListFromReportPrepareByDad2(codeDad2));
//
//        String[] ids = new String[tovarDBList.size()];
//        int j = 0;
//        for (TovarDB item : tovarDBList) {
//            ids[j++] = item.getManufacturerId();
//        }
//
//        List<TradeMarkDB> tradeMarkDBList = TradeMarkRealm.getTradeMarkByIds(ids);
//
//        String[] list = new String[tradeMarkDBList.size()];
//        for (int i = 0; i < tradeMarkDBList.size(); i++) {
//            list[i] = tradeMarkDBList.get(i).getNm();
//        }
//
//        SpinnerAdapter adapter1 = new SpinnerAdapter(context, list, "Оберіть торгівельну марку товару");
//        spinnerManufacture.setAdapter(adapter1);
//        spinnerManufacture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String data = adapterView.getItemAtPosition(i).toString();
//                Toast.makeText(adapterView.getContext(), "Выбрали: " + data, Toast.LENGTH_SHORT).show();
//
//                // Используем Stream для поиска объекта в списке по значению notes
//                Optional<TradeMarkDB> foundObject = tradeMarkDBList.stream()
//                        .filter(item -> data.equals(item.getNm()))
//                        .findFirst();
//
//                // Проверяем, найден ли объект
//                if (foundObject.isPresent()) {
//                    // Объект найден, можно получить его id или выполнить другие действия
//                    int objectId = Integer.parseInt(foundObject.get().getID());
//                    spinnerManufactureResult = objectId;
//                    Toast.makeText(adapterView.getContext(), "Выбрали: " + data + ", id: " + objectId, Toast.LENGTH_SHORT).show();
//                }/* else if (data.equals("це досягнення не відноситься до жодної з пропозицій замовника")) {
//                    spinnerManufactureResult = 1;
//                }*/ else {
//                    // Объект не найден
//                    Toast.makeText(adapterView.getContext(), "Объект не найден", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                Toast.makeText(adapterView.getContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show();
////                spinnerManufactureResult = 0;
//            }
//        });
//    }

    public void setData(WpDataDB wpDataDB) {
        AchievementDataHolder.Companion.instance().init();

        userId = wpDataDB.getUser_id();
        userTxt = wpDataDB.getUser_txt();
        addressId = wpDataDB.getAddr_id();
        addressTxt = wpDataDB.getAction_txt();
        clientId = wpDataDB.getClient_id();
        clientTxt = wpDataDB.getClient_txt();
        codeDad2 = wpDataDB.getCode_dad2();

        putTextData();
        buttonSave();

        onUpdateUI = () -> {

            setTextUI();
            if (AchievementDataHolder.Companion.instance().getPhotoToURI() != null) {
                photoToIV.setImageURI(
                        Uri.parse(AchievementDataHolder.Companion.instance().getPhotoToURI())
                );
            }
            if (AchievementDataHolder.Companion.instance().getPhotoAfterURI() != null) {
                photoAfterIV.setImageURI(
                        Uri.parse(AchievementDataHolder.Companion.instance().getPhotoAfterURI())
                );
            }
        };
    }

    public void setData(AchievementsSDB data) {
        CustomerSDB customerSDB = SQL_DB.customerDao().getById(data.clientId);
        UsersSDB usersSDB = SQL_DB.usersDao().getById(data.userId);

        userId = data.userId;
        userTxt = usersSDB.fio;
        addressId = data.addrId;
        addressTxt = data.adresaNm;
        clientId = data.clientId;
        clientTxt = customerSDB.nm;
        codeDad2 = data.codeDad2;

        putTextData();
        buttonSave();

        onUpdateUI = () -> {

            setTextUI();

            if (AchievementDataHolder.Companion.instance().getPhotoToURI() != null) {
                photoToIV.setImageURI(
                        Uri.parse(AchievementDataHolder.Companion.instance().getPhotoToURI())
                );
            }
            if (AchievementDataHolder.Companion.instance().getPhotoAfterURI() != null) {
                photoAfterIV.setImageURI(
                        Uri.parse(AchievementDataHolder.Companion.instance().getPhotoAfterURI())
                );
            }
        };
    }

    private void setTextUI() {

        offerFromClientItem.setText(underLineText(AchievementDataHolder.Companion.instance().getRequirementClientName() == null ?
                "Натисніть для обрання Пропозиції" : AchievementDataHolder.Companion.instance().getRequirementClientName()));

        tovarTxt.setText(underLineText(
                AchievementDataHolder.Companion.instance().getTovarName() == null ?
                        "Натисніть для обрання Товару" : AchievementDataHolder.Companion.instance().getTovarName()));
        tradeMarkItem.setText(underLineText(
                AchievementDataHolder.Companion.instance().getManufactureName() == null ?
                        "Натисніть для обрання Марки товару" : AchievementDataHolder.Companion.instance().getManufactureName())
        );
        themeItem.setText(underLineText(
                AchievementDataHolder.Companion.instance().getThemeName() == null ?
                        "Натисніть для обрання Теми" : AchievementDataHolder.Companion.instance().getThemeName())
        );
    }

    private SpannableString underLineText(String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(android.R.color.holo_blue_dark)), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private Activity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        assert context instanceof Activity;
        return (Activity) context;
    }

}
