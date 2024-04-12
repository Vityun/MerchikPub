package ua.com.merchik.merchik.dialogs.DialogAchievement;

import static ua.com.merchik.merchik.Globals.HELPDESK_PHONE_NUMBER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.dialogs.DialogVideo;

public class DialogAchievement {

    private Dialog dialog;
    private Context context;
    private ImageButton close, help, videoHelp, call, addSotr;
    private ImageView photoTo, photoAfter;
    private TextView title, text;

    private Button ok;

    private StackPhotoDB stackPhotoBefore, stackPhotoAfter;

    public DialogAchievement(Context context) {
        this.context = context;
        try {
            dialog = new Dialog(context);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setContentView(R.layout.dialog_achievement);
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
            int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.70);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

            ok = dialog.findViewById(R.id.ok);

            close = dialog.findViewById(R.id.imageButtonClose);
            help = dialog.findViewById(R.id.imageButtonLesson);
            videoHelp = dialog.findViewById(R.id.imageButtonVideoLesson);
            call = dialog.findViewById(R.id.imageButtonCall);
            addSotr = dialog.findViewById(R.id.add_sotr);

            title = dialog.findViewById(R.id.title);
            text = dialog.findViewById(R.id.text);

            photoTo = dialog.findViewById(R.id.photoTo);
            photoAfter = dialog.findViewById(R.id.photoAfter);
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

    public void setText(String text) {
        this.text.setVisibility(View.VISIBLE);
        if (text != null && !text.equals("")) {
            this.text.setText(text);
        } else {
            this.text.setVisibility(View.GONE);
        }
    }

    public void setText(SpannableStringBuilder text) {
        this.text.setVisibility(View.VISIBLE);
        if (text != null) {
            this.text.setText(text);
        } else {
            this.text.setVisibility(View.GONE);
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

    // =============================================================================================

    public void setAchievement(AchievementsSDB achievement){
        setTitle("Досягнення (" + achievement.serverId + ")");
        setPhotos(achievement);
        setAchievementData(achievement);
    }

    private void setAchievementData(AchievementsSDB achievement){
        CustomerSDB customerSDB = SQL_DB.customerDao().getById(achievement.clientId);
        UsersSDB usersSDB = SQL_DB.usersDao().getById(achievement.userId);

        SpannableStringBuilder sb = new SpannableStringBuilder();

        sb.append("Дата: ").append(achievement.dt).append("\n");
        sb.append("Кліент: ").append(customerSDB.nm).append("\n");
        sb.append("Адреса: ").append(achievement.adresaNm).append("\n");
        sb.append("Виконавець: ").append(usersSDB.fio).append("\n");
        sb.append("Коментар: ").append(achievement.commentTxt).append("\n");

        setText(sb);
    }

    private void setPhotos(AchievementsSDB achievement){
        try {
            stackPhotoBefore = StackPhotoRealm.getByHash(achievement.img_before_hash);
            if (stackPhotoBefore == null){
                stackPhotoBefore = StackPhotoRealm.getByServerId(String.valueOf(achievement.imgBeforeId));
            }

            stackPhotoAfter = StackPhotoRealm.getByHash(achievement.img_after_hash);
            if (stackPhotoAfter == null){
                stackPhotoAfter = StackPhotoRealm.getByServerId(String.valueOf(achievement.imgAfterId));
            }

            File file = new File(stackPhotoBefore.getPhoto_num());
            Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
            photoTo.setImageBitmap(b);
            photoTo.setOnClickListener(v1 -> {
                try {
                    DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(v1.getContext());
                    dialogFullPhoto.setPhoto(Uri.parse(stackPhotoBefore.photo_num));
                    dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                    dialogFullPhoto.show();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DialogAchievement/buttonPhotoAfter", "Exception e: " + e);
                }
            });

            File file2 = new File(stackPhotoAfter.getPhoto_num());
            Bitmap b2 = decodeSampledBitmapFromResource(file2, 200, 200);
            photoAfter.setImageBitmap(b2);
            photoAfter.setOnClickListener(v1 -> {
                try {
                    DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(v1.getContext());
                    dialogFullPhoto.setPhoto(Uri.parse(stackPhotoAfter.photo_num));
                    dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                    dialogFullPhoto.show();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DialogAchievement/buttonPhotoAfter", "Exception e: " + e);
                }
            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogAchievement/setPhotos", "Exception e: " + e);
            Globals.writeToMLOG("ERROR", "DialogAchievement/setPhotos", "Exception es: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void setOk(CharSequence setButtonText, DialogData.DialogClickListener clickListener) {
        ok.setVisibility(View.VISIBLE);
        if (setButtonText != null) {
            ok.setText(setButtonText);
        }
        ok.setOnClickListener(v -> {
            if (clickListener != null) clickListener.clicked();
            dismiss();
        });
    }

}
