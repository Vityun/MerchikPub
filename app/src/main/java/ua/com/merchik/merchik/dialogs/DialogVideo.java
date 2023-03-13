package ua.com.merchik.merchik.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import ua.com.merchik.merchik.R;

public class DialogVideo extends DialogData {

    private Dialog dialog;

    private TextView textViewTitle;

    private ImageButton imgBtnClose;
    private ImageButton imgBtnLesson;
    private ImageButton imgBtnVideoLesson;


    public DialogVideo(Context context) {
        dialog = new Dialog(context);
        this.context = context;
        //dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_video);

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
    public void setVideoLesson(Context context, boolean visualise, int objectId, DialogClickListener clickListener) {
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
        WebView displayYoutubeVideo = (WebView) dialog.findViewById(R.id.youtube);
        displayYoutubeVideo.setWebChromeClient(new WebChromeClient());

        displayYoutubeVideo.getSettings().setJavaScriptEnabled(true);
        displayYoutubeVideo.getSettings().setAllowFileAccess(true);
        displayYoutubeVideo.getSettings().setAppCacheEnabled(true);
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

    public void setVideos(String url){

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
