package ua.com.merchik.merchik.dialogs.DialogAchievement;

import static ua.com.merchik.merchik.Globals.HELPDESK_PHONE_NUMBER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.Spinner.SpinnerAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.dialogs.DialogVideo;

public class DialogAchievement {

    public static Clicks.click clickVoidAchievement;

    private Dialog dialog;
    private Context context;
    private WpDataDB wpDataDB;
    private StackPhotoDB stackPhotoDBTo, stackPhotoDBAfter;

    private ImageButton close, help, videoHelp, call, addSotr;
    private TextView title, client, address, visit, theme, offerFromClient;
    private EditText comment;
    private Button photoTo, photoAfter, save;
    private ImageView photoToIV, photoAfterIV;

    private Spinner spinnerTheme, spinnerClient;
    private String[] themeList = new String[]{
            "Досягнення (покращення розташування товару в ТТ)",     // 595
            "Замовлення на фінансування нового Досягнення",         // 1252
            "Утримання викладки на полиці (досягнутого раніше)"};   // 1251

    private Integer spinnerThemeResult, spinnerClientResult;
    private OptionsDB optionDB;

    public DialogAchievement(Context context, WpDataDB wpDataDB) {
        this.context = context;
        this.wpDataDB = wpDataDB;
        try {
            dialog = new Dialog(context);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setContentView(R.layout.dialog_achievement);
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
            int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.70);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

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

            comment = dialog.findViewById(R.id.comment);

            photoTo = dialog.findViewById(R.id.photo_to);
            photoAfter = dialog.findViewById(R.id.photo_after);
            save = dialog.findViewById(R.id.save);

            photoToIV = dialog.findViewById(R.id.photoTo);
            photoAfterIV = dialog.findViewById(R.id.photoAfter);

            spinnerTheme = dialog.findViewById(R.id.spinner_theme);
            spinnerClient = dialog.findViewById(R.id.spinner_client);

            putTextData();
            buttonSave();

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogAchievement", "Exception e: " + e);
        }
    }

    private void buttonSave() {
        save.setOnClickListener(v -> {
            try {
                AchievementsSDB achievementsSDB = new AchievementsSDB();
                achievementsSDB.serverId = 0;
                achievementsSDB.dt = String.valueOf((System.currentTimeMillis() / 1000));
                achievementsSDB.dt_ut = (System.currentTimeMillis() / 1000);
                achievementsSDB.addrId = wpDataDB.getAddr_id();
                achievementsSDB.dvi = 1;
                achievementsSDB.error = 0;
                achievementsSDB.currentVisit = 0;
                achievementsSDB.score = "0";
                achievementsSDB.clientId = wpDataDB.getClient_id();
                achievementsSDB.codeDad2 = wpDataDB.getCode_dad2();
                if (comment != null && comment.getText() != null) {
                    achievementsSDB.commentDt = String.valueOf((System.currentTimeMillis() / 1000));
                    achievementsSDB.commentUser = String.valueOf(wpDataDB.getUser_id());
                    achievementsSDB.commentTxt = comment.getText().toString();
                }

                achievementsSDB.themeId = 595;
                if (spinnerTheme != null && spinnerThemeResult != null){
                    achievementsSDB.themeId = spinnerThemeResult;
                }

                if (spinnerClientResult != null){
                    achievementsSDB.addRequirementId = spinnerClientResult;
                }else {
                    achievementsSDB.addRequirementId = 0;
                }

                achievementsSDB.img_before_hash = stackPhotoDBTo.photo_hash;
                achievementsSDB.img_after_hash = stackPhotoDBAfter.photo_hash;

                SQL_DB.achievementsDao().insertAll(Collections.singletonList(achievementsSDB));
                Toast.makeText(v.getContext(), "Створено нове досягнення", Toast.LENGTH_LONG).show();
                dialog.dismiss();
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
        client.setText(Html.fromHtml("<b>Кліент: </b> " + wpDataDB.getClient_txt() + ""));
        address.setText(Html.fromHtml("<b>Адреса: </b> " + wpDataDB.getAddr_txt() + ""));
        visit.setText(Html.fromHtml("<b>Відвідування: </b> " + wpDataDB.getCode_dad2() + ""));
        theme.setText(Html.fromHtml("<b>Тема: </b> "));

        createSpinnerTheme();

        createSpinnerClient();

        offerFromClient.setText(Html.fromHtml("<b>Пропозиція від клієнта: </b> " + "" + ""));
    }

    public void buttonPhotoTo() {
        photoTo.setVisibility(View.VISIBLE);
        photoTo.setOnClickListener(v -> {
            try {
                Intent intentPhotoLog = new Intent(v.getContext(), PhotoLogActivity.class);
                intentPhotoLog.putExtra("achievements", true);
                intentPhotoLog.putExtra("choise", true);
                intentPhotoLog.putExtra("dad2", wpDataDB.getCode_dad2());
                intentPhotoLog.putExtra("photoType", 14);
                v.getContext().startActivity(intentPhotoLog);

                photoTo.setVisibility(View.GONE);

                clickVoidAchievement = new Clicks.click() {
                    @Override
                    public <T> void click(T data) {
                        stackPhotoDBTo = (StackPhotoDB) data;
                        photoToIV.setVisibility(View.VISIBLE);
                        photoToIV.setImageURI(Uri.parse(stackPhotoDBTo.photo_num));
                        photoToIV.setOnClickListener(v1 -> {
                            try {
                                DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(v1.getContext());
                                dialogFullPhoto.setPhoto(Uri.parse(stackPhotoDBTo.photo_num));
                                dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                                dialogFullPhoto.show();
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "DialogAchievement/buttonPhotoTo", "Exception e: " + e);
                            }
                        });
                    }
                };

            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "buttonPhotoTo", "Exception e: " + Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void buttonPhotoAfter() {
        photoAfter.setVisibility(View.VISIBLE);
        photoAfter.setOnClickListener(v -> {
            try {
                Intent intentPhotoLog = new Intent(v.getContext(), PhotoLogActivity.class);
                intentPhotoLog.putExtra("achievements", true);
                intentPhotoLog.putExtra("choise", true);
                intentPhotoLog.putExtra("dad2", wpDataDB.getCode_dad2());
                intentPhotoLog.putExtra("photoType", 0);
                v.getContext().startActivity(intentPhotoLog);

                photoAfter.setVisibility(View.GONE);

                clickVoidAchievement = new Clicks.click() {
                    @Override
                    public <T> void click(T data) {
                        stackPhotoDBAfter = (StackPhotoDB) data;
                        photoAfterIV.setVisibility(View.VISIBLE);
                        photoAfterIV.setImageURI(Uri.parse(stackPhotoDBAfter.photo_num));
                        photoAfterIV.setOnClickListener(v1 -> {
                            try {
                                DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(v1.getContext());
                                dialogFullPhoto.setPhoto(Uri.parse(stackPhotoDBTo.photo_num));
                                dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                                dialogFullPhoto.show();
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "DialogAchievement/buttonPhotoAfter", "Exception e: " + e);
                            }
                        });
                    }
                };

            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "buttonPhotoTo", "Exception e: " + Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void createSpinnerTheme(){
//        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, themeList);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SpinnerAdapter adapter1 = new SpinnerAdapter(context, themeList, "Выберите тему");
        spinnerTheme.setAdapter(adapter1);
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String data = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(adapterView.getContext(), "Выбрали: " + data, Toast.LENGTH_SHORT).show();

                if (data.equals(themeList[0])){
                    spinnerThemeResult = 595;
                }else if (data.equals(themeList[1])){
                    spinnerThemeResult = 1252;
                }else if (data.equals(themeList[2])){
                    spinnerThemeResult = 1251;
                }else {
                    spinnerThemeResult = 595;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(adapterView.getContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show();
                spinnerThemeResult = 595;
            }
        });
    }

    public void createSpinnerClient(){
        List<AdditionalRequirementsDB> additionalRequirementsDBList = AdditionalRequirementsRealm.getADByClientAll(wpDataDB.getClient_id());

        if (additionalRequirementsDBList == null) return;

        String[] clientList = new String[additionalRequirementsDBList.size()+1];
        clientList[0] = "це досягнення не відноситься до жодної з пропозицій замовника";
        for (int i=0; i<additionalRequirementsDBList.size(); i++){
            clientList[i+1] = additionalRequirementsDBList.get(i).getNotes();
        }


//        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, clientList);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SpinnerAdapter adapter1 = new SpinnerAdapter(context, clientList, additionalRequirementsDBList.size() > 0 ? "От клиента есть (" + additionalRequirementsDBList.size() + ") предложений" : "Предложений от клиента НЕТ");
        spinnerClient.setAdapter(adapter1);
        spinnerClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String data = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(adapterView.getContext(), "Выбрали: " + data, Toast.LENGTH_SHORT).show();

                // Используем Stream для поиска объекта в списке по значению notes
                Optional<AdditionalRequirementsDB> foundObject = additionalRequirementsDBList.stream()
                        .filter(item -> data.equals(item.getNotes()))
                        .findFirst();

                // Проверяем, найден ли объект
                if (foundObject.isPresent()) {
                    // Объект найден, можно получить его id или выполнить другие действия
                    int objectId = foundObject.get().getId();
                    spinnerClientResult = objectId;
                    Toast.makeText(adapterView.getContext(), "Выбрали: " + data + ", id: " + objectId, Toast.LENGTH_SHORT).show();
                } else if (data.equals("це досягнення не відноситься до жодної з пропозицій замовника")){
                    spinnerClientResult = 0;
                }else {
                    // Объект не найден
                    Toast.makeText(adapterView.getContext(), "Объект не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(adapterView.getContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show();
                spinnerClientResult = 0;
            }
        });
    }

}
