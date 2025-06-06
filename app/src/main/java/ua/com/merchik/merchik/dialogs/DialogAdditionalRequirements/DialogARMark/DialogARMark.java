package ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark;

import static ua.com.merchik.merchik.Globals.HELPDESK_PHONE_NUMBER;
import static ua.com.merchik.merchik.Globals.generateUniqueNumber;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogVideo;

public class DialogARMark {

    private Dialog dialog;
    private Context context;

    private ImageButton close, help, videoHelp, call;

    private TextView title, txtId, txtAddr, txtGrp, txtNumber, txtDateStart, txtDateEnd, txtAuthor, txtCustomer, txtMark, txtText;
    private Button ok;
    private RatingBar ratingBar;

    public DialogARMark(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);
        dialog.setContentView(R.layout.dialog_ar_mark);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        title = dialog.findViewById(R.id.title);
        txtId = dialog.findViewById(R.id.txtId);
        txtAddr = dialog.findViewById(R.id.txtAddr);
        txtGrp = dialog.findViewById(R.id.txtGrp);
        txtNumber = dialog.findViewById(R.id.txtNumber);
        txtDateStart = dialog.findViewById(R.id.txtDateStart);
        txtDateEnd = dialog.findViewById(R.id.txtDateEnd);
        txtAuthor = dialog.findViewById(R.id.txtAuthor);
        txtCustomer = dialog.findViewById(R.id.txtCustomer);
        txtMark = dialog.findViewById(R.id.txtMark);
        txtText = dialog.findViewById(R.id.txtText);

        ok = dialog.findViewById(R.id.ok);
        ok.setVisibility(View.GONE);

        ratingBar = dialog.findViewById(R.id.ratingBar);

        close = dialog.findViewById(R.id.imageButtonClose);
        help = dialog.findViewById(R.id.imageButtonLesson);
        videoHelp = dialog.findViewById(R.id.imageButtonVideoLesson);
        call = dialog.findViewById(R.id.imageButtonCall);
    }


    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
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
    //----------------------------------------------------------------------------------------------

    public void setTitle(CharSequence msg) {
        title.setText(msg);
    }

    public void setTxtText(CharSequence msg) {
        txtText.setText(msg);
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

    public void setData(CharSequence id, CharSequence addr, CharSequence grp, CharSequence number, CharSequence dateStart, CharSequence dateEnd, CharSequence author, CharSequence customer, CharSequence mark, CharSequence text) {
        txtId.setText(id);
        txtAddr.setText(addr);
        if (grp.equals(""))
            txtGrp.setVisibility(View.GONE);
        else
            txtGrp.setText(grp);
        txtNumber.setText(number);
        txtDateStart.setText(dateStart);
        txtDateEnd.setText(dateEnd);
        txtAuthor.setText(author);
        txtCustomer.setText(customer);
        txtMark.setText(mark);
        txtText.setText(text);
    }

    public void setRatingBarAR(AdditionalRequirementsDB db, Float data, DialogData.DialogClickListener clickListener) {
        if (data != null) {
            ratingBar.setRating(data);
        }

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            int rate = (int) rating;
            ratingBar.setRating(rate);

            if (rate < 6) {
                DialogData dialogData = new DialogData(context);
                dialogData.setTitle("Коментар");
                dialogData.setText("Внесіть коментар до низбкої оцінки");
                dialogData.setOperation(DialogData.Operations.TEXT, "", null, () -> {
                });
                dialogData.setOk("Ok", () -> {
                    if (dialogData.getOperationResult() != null && dialogData.getOperationResult().length() > 10) {
                        saveNewARMark(db, rate, dialogData.getOperationResult());
                        Toast.makeText(context, "Оценка: " + rate + " установлена.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Внесіть коментар більший за 10 символів.", Toast.LENGTH_LONG).show();
                    }
                });
                dialogData.setClose(this::dismiss);
                dialogData.show();
            } else {

                saveNewARMark(db, rate, "");

                Toast.makeText(context, "Оценка: " + rate + " установлена.", Toast.LENGTH_LONG).show();
            }

            clickListener.clicked();
        });
    }

    public void setRatingBarPlanogrammVizitShowcase(PlanogrammVizitShowcaseSDB db, WpDataDB wpData, Float data, String comment, DialogData.DialogClickListener clickListener) {
        if (data != null) {
            ratingBar.setRating(data);
        }

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            int rate = (int) rating;
            ratingBar.setRating(rate);

            if (rate < 6) {
                DialogData dialogData = new DialogData(context);
                dialogData.setTitle("Коментар");
                dialogData.setText("Внесіть коментар до низбкої оцінки");
                dialogData.setOperation(DialogData.Operations.TEXT, "", null, () -> {
                });
                dialogData.setOk("Ok", () -> {
                    if (dialogData.getOperationResult() != null && dialogData.getOperationResult().length() > 10) {
                        saveNewVotes(db, wpData, rate, dialogData.getOperationResult());
                        Toast.makeText(context, "Оцінка: " + rate + " встановлена.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Внесіть коментар більший за 10 символів. Оцінка не буде збережена", Toast.LENGTH_LONG).show();
                    }
                });
                dialogData.setClose(this::dismiss);
                dialogData.show();
            } else {

                saveNewVotes(db, wpData, rate, comment);

                Toast.makeText(context, "Оцінка: " + rate + " встановлена.", Toast.LENGTH_LONG).show();
            }

            clickListener.clicked();
        });
    }

    private void saveNewVotes(PlanogrammVizitShowcaseSDB db, WpDataDB wpDataDB, int rate, String comment) {

        if (wpDataDB.getClient_end_dt() > 0){
            DialogData dialogData = new DialogData(context);
            dialogData.setTitle("Оцінка не буде змінена");
            dialogData.setText("Роботи з поточного відвідування вже завершено, змінити оцінку не можна");
            dialogData.show();
        } else {
            VoteSDB vote = new VoteSDB();
            vote.serverId = generateUniqueNumber();
            vote.dtUpload = 0L;
//        vote.codeDad2 = db.code_dad2;
            vote.codeDad2 = wpDataDB.getCode_dad2();
            vote.isp = db.isp;
            vote.themeId = 1314;
            vote.kli = db.client_id;
            vote.addrId = db.addr_id;
            vote.dt = System.currentTimeMillis() / 1000;
            vote.merchik = db.author_id;
            vote.voterId = wpDataDB.getUser_id();
            vote.photoId = db.planogram_photo_id != null ? db.planogram_photo_id.longValue() : 0;
            vote.voteClass = 5;
            vote.score = rate;
            vote.comments = comment;

            SQL_DB.votesDao().insertAll(Collections.singletonList(vote));
        }

//        AdditionalRequirementsMarkRealm.setNewMark(markDB);
    }

    private void saveNewARMark(AdditionalRequirementsDB db, int rate, String comment) {
        AdditionalRequirementsMarkDB markDB = new AdditionalRequirementsMarkDB();
        markDB.setId(String.valueOf(System.currentTimeMillis()));
        markDB.setItemId(db.getId());
        markDB.setDt(System.currentTimeMillis() / 1000);
        markDB.setUserId(String.valueOf(Globals.userId));
        markDB.setScore(String.valueOf(rate));
        markDB.setTp("1");  // Для Доп. Требований
        markDB.setUploadStatus("0");
        markDB.comment = comment;

        AdditionalRequirementsMarkRealm.setNewMark(markDB);
    }


    public void setRatingBarAR(Float data, Clicks.click click) {
        if (data != null) {
            ratingBar.setRating(data);
        }
    }


    // Для Доп. Материалов

    public void setRatingBarAM(AdditionalMaterialsJOINAdditionalMaterialsAddressSDB db, Float data, DialogData.DialogClickListener clickListener) {
        if (data != null) {
            ratingBar.setRating(data);
        }

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            int rate = (int) rating;
            ratingBar.setRating(rate);
            Toast.makeText(context, "Оценка: " + rate + " установлена.", Toast.LENGTH_LONG).show();


            AdditionalRequirementsMarkDB markDB = new AdditionalRequirementsMarkDB();
            markDB.setId(String.valueOf(System.currentTimeMillis()));
            markDB.setItemId(db.id);
            markDB.setDt(System.currentTimeMillis() / 1000);
            markDB.setUserId(String.valueOf(Globals.userId));
            markDB.setScore(String.valueOf(rate));
            markDB.setTp("0");  // Доп. Материалы
            markDB.setUploadStatus("0");

            AdditionalRequirementsMarkRealm.setNewMark(markDB);

            clickListener.clicked();
        });
    }


    public void setRatingBarAM(Float data, Clicks.click click) {
        if (data != null) {
            ratingBar.setRating(data);
        }
    }

}
