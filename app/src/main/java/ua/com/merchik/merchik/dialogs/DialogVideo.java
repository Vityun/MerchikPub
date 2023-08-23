package ua.com.merchik.merchik.dialogs;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Adapters.ButtonAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class DialogVideo extends DialogData {

    private Dialog dialog;

    private TextView textViewTitle;

    private RecyclerView recyclerView;
    private WebView webView;

    private ImageButton imgBtnClose;
    private ImageButton imgBtnLesson;
    private ImageButton imgBtnVideoLesson;


    public DialogVideo(Context context) {
        dialog = new Dialog(context);
        this.context = context;
        //dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_video);

        recyclerView = dialog.findViewById(R.id.recycler_view);
        webView = dialog.findViewById(R.id.youtube);
//        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

        imgBtnClose = dialog.findViewById(R.id.imageButtonClose2);
        imgBtnLesson = dialog.findViewById(R.id.imageButtonLesson2);
        imgBtnVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson2);

        textViewTitle = dialog.findViewById(R.id.titile);

        merchikIco = dialog.findViewById(R.id.merchik_ico2);
    }

    @Override
    public void setClose(DialogClickListener clickListener) {
        imgBtnClose.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    @Override
    public void setLesson(Context context, boolean visualise, int objectId) {
        super.setLesson(context, visualise, objectId);
    }

    @Override
    public void setVideoLesson(Context context, boolean visualise, int objectId, DialogClickListener clickListener, Clicks.clickVoid click) {
        if (visualise) {
            imgBtnVideoLesson.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgBtnVideoLesson.getBackground().setTint(Color.RED);
            } else {
                imgBtnVideoLesson.setBackgroundColor(Color.RED);
            }
            imgBtnVideoLesson.setColorFilter(Color.WHITE);
        }

        imgBtnVideoLesson.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }


    @Override
    public void setMerchikIco(Context context) {
        super.setMerchikIco(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setVideo(String url){
        webView.setVisibility(View.VISIBLE);
        WebView displayYoutubeVideo = (WebView) dialog.findViewById(R.id.youtube);
        displayYoutubeVideo.setWebChromeClient(new WebChromeClient());

        displayYoutubeVideo.getSettings().setJavaScriptEnabled(true);
        displayYoutubeVideo.getSettings().setAllowFileAccess(true);
//        displayYoutubeVideo.getSettings().setAppCacheEnabled(true);
        displayYoutubeVideo.setInitialScale(90);
//        displayYoutubeVideo.getSettings().setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            displayYoutubeVideo.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            displayYoutubeVideo.getSettings().setPluginState(WebSettings.PluginState.ON);
        }
        displayYoutubeVideo.getSettings().setLoadWithOverviewMode(true);

        displayYoutubeVideo.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        displayYoutubeVideo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        displayYoutubeVideo.loadData(url, "text/html", "utf-8");
    }

    public void setVideos(List<SiteHintsDB> data, Clicks.clickVoid click){
        webView.setVisibility(View.GONE);
        imgBtnVideoLesson.setVisibility(View.GONE);
        RecyclerView.Adapter adapter = getAdapter(data, click);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    private RecyclerView.Adapter getAdapter(List<SiteHintsDB> data, Clicks.clickVoid click){
        final ButtonAdapter buttonAdapter = new ButtonAdapter(data, new Clicks.click() {
            @Override
            public <T> void click(T data) {
                adapterClick((SiteHintsDB) data, click);
                recyclerView.getAdapter().notifyDataSetChanged(); // Обновление внешнего вида адаптера
            }
        });
        return buttonAdapter;
    }

    private void adapterClick(SiteHintsDB data, Clicks.clickVoid click){
        try {
            SiteHintsDB vidLesson = (SiteHintsDB) data;

            if (vidLesson != null) {
                long obj = vidLesson.getID();
                RealmManager.setRowToLog(Collections.singletonList(
                        new LogDB(
                                RealmManager.getLastIdLogDB() + 1,
                                System.currentTimeMillis() / 1000,
                                "Факт перегляду відео-урока. " + vidLesson.getTitle(),
                                1261,
                                null,
                                null,
                                obj,
                                null,
                                null,
                                Globals.session,
                                null)));

                try {
                    // Записываю в ЛОГ инфу о том что видео просмотрено.
                    ViewListSDB viewListSDB = new ViewListSDB();
                    viewListSDB.lessonId = vidLesson.getID();
                    viewListSDB.merchikId = Globals.userId;
                    viewListSDB.dt = System.currentTimeMillis() / 1000;
                    SQL_DB.videoViewDao().insertAll(Collections.singletonList(viewListSDB));
                    Globals.writeToMLOG("ERROR", "DialogData/setVideoLesson/videoViewDao().insertAll", "Успешно посмотрел ролик: " + vidLesson.getID());
                    click.click();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DialogData/setVideoLesson/videoViewDao().insertAll", "Exception e: " + e);
                }
            }

            String s = vidLesson.getUrl();
            s = s.replace("http://www.youtube.com/", "http://www.youtube.com/embed/");
            s = s.replace("https://www.youtube.com/", "http://www.youtube.com/embed/");
            s = s.replace("watch?v=", "");


            DialogVideo dialogVideo = new DialogVideo(dialog.getContext());
            dialogVideo.setTitle(vidLesson.getNm());
            dialogVideo.setVideo("<html><body><iframe width=\"700\" height=\"600\" src=\"" + s + "\"></iframe></body></html>");
            dialogVideo.setVideoLesson(context, true, 0, () -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(vidLesson.getUrl()))), null);
            dialogVideo.setClose(dialogVideo::dismiss);
            dialogVideo.show();


        }catch (Exception e){
            Toast.makeText(dialog.getContext(), "Exception e: " + e, Toast.LENGTH_LONG).show();
        }
    }



    public void setTitle(String s){
        textViewTitle.setText(s);
    }


    public void show(){
        dialog.show();
    }

    public void dismiss(){
        if (dialog != null) dialog.dismiss();
    }
}
