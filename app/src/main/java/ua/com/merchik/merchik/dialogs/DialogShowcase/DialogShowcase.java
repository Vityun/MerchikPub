package ua.com.merchik.merchik.dialogs.DialogShowcase;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogVideo;

public class DialogShowcase extends DialogData {

    private Context context;
    private Dialog dialog;
    public Clicks.click click;

    public ImageButton close, help, videoHelp, call, merchikIco;
    private TextView title;
    private Button cancel;
    private RecyclerView recyclerView;
    private EditText searchView;

    public WpDataDB wpDataDB;

    public DialogShowcase(Context context) {
        this.context = context;
        try {
            initializeDialog();
//            populateDialogData();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogShowcase", "Exception e: " + e);
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

            Log.e("setImgBtnCall", "pressed?");

            String telephone = "+380674484493";

            Globals.telephoneCall(context, telephone);

        }));

        Log.e("setImgBtnCall", "and here");
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    private void initializeDialog() {
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_showcase);

        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.70);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        close = dialog.findViewById(R.id.imageButtonClose);
        help = dialog.findViewById(R.id.imageButtonLesson);
        videoHelp = dialog.findViewById(R.id.imageButtonVideoLesson);
        call = dialog.findViewById(R.id.imageButtonCall);

        title = dialog.findViewById(R.id.title);
        cancel = dialog.findViewById(R.id.button);
        recyclerView = dialog.findViewById(R.id.recyclerView);
        searchView = dialog.findViewById(R.id.filterEditText);

        merchikIco = dialog.findViewById(R.id.merchik_ico);
        merchikIco.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_caution));
    }

    public void populateDialogData(Clicks.click click) {
        this.click = click;
        setRecyclerView();
    }

    public void result(Clicks.click click){
        this.click = click;
    }

    private void setRecyclerView() {
        try {
            List<ShowcaseSDB> showcaseDataList = SQL_DB.showcaseDao().getByDoc(wpDataDB.getClient_id(), wpDataDB.getAddr_id());
//            showcaseDataList.add(newTestShowcase(1));
//            showcaseDataList.add(newTestShowcase(2));
//            showcaseDataList.add(newTestShowcase(3));
//            showcaseDataList.add(newTestShowcase(4));
//            showcaseDataList.add(newTestShowcase(5));
//            showcaseDataList.add(newTestShowcase(6));
//            showcaseDataList.add(newTestShowcase(7));
//            showcaseDataList.add(newTestShowcase(8));
//            showcaseDataList.add(newTestShowcase(9));
//            showcaseDataList.add(newTestShowcase(10));

            ShowcaseAdapter adapter = new ShowcaseAdapter(showcaseDataList, click);
            setFilter(adapter);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        } catch (Exception e) {
            Log.e("text", "Exception e: " + e);
        }
    }

    private ShowcaseSDB newTestShowcase(int id) {
        ShowcaseSDB res = new ShowcaseSDB();
        res.id = id;
        res.tovarGrp = id;
        res.photoPlanogramId = id;
        return res;
    }

    private void setFilter(ShowcaseAdapter adapter){
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    adapter.getFilter().filter(s);
                    recyclerView.scheduleLayoutAnimation();
                }else {
                    adapter.getFilter().filter("");
                    recyclerView.scheduleLayoutAnimation();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
//
//        adapter.notifyDataSetChanged();
    }


}
