package ua.com.merchik.merchik.dialogs.DialogAchievement;

import static ua.com.merchik.merchik.Globals.HELPDESK_PHONE_NUMBER;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogVideo;

public class DialogAchievement {

    private Dialog dialog;
    private Context context;
    private WpDataDB wpDataDB;

    private ImageButton close, help, videoHelp, call, addSotr;
    private TextView title, client, address, visit, theme, offerFromClient;
    private EditText comment;
    private Button photoTo, photoAfter, save;

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

            photoTo = dialog.findViewById(R.id.photo_to);
            photoAfter = dialog.findViewById(R.id.photo_after);
            save = dialog.findViewById(R.id.save);

            putTextData();

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogAchievement", "Exception e: " + e);
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


    public void putTextData() {
        client.setText(Html.fromHtml("<b>Кліент: </b> " + wpDataDB.getClient_txt() + ""));
        address.setText(Html.fromHtml("<b>Адреса: </b> " + wpDataDB.getAddr_txt() + ""));
        visit.setText(Html.fromHtml("<b>Відвідування: </b> " + "" + ""));
        theme.setText(Html.fromHtml("<b>Тема: </b> " + wpDataDB.getTheme_id() + ""));
        offerFromClient.setText(Html.fromHtml("<b>Пропозиція від клієнта: </b> " + "" + ""));
    }

}