package ua.com.merchik.merchik.dialogs;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.daimajia.androidanimations.library.YoYo;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.util.List;

import ua.com.merchik.merchik.Activities.FullScreenPhotoActivity.PhotoFragments;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public class DialogFullPhotoR {

    private static final float CAMERA_PULSE_PEAK_SCALE = 1.18f;
    private static final long CAMERA_PULSE_INTERVAL_MS = 5_000L;

    private Dialog dialog;
    private Context context;

    public StackPhotoDB photoDB;

    // ------------------------
    private TouchImageView photo;
    private ImageButton camera;
    private boolean cameraPulseEnabled;
    private YoYo.YoYoString cameraPulse;
    private final Runnable cameraPulseTask = new Runnable() {
        @Override
        public void run() {
            if (!cameraPulseEnabled || !isShowing() || !camera.isAttachedToWindow()
                    || camera.getVisibility() != View.VISIBLE) {
                stopCameraPulse();
                return;
            }
            if (camera.getWindowVisibility() == View.VISIBLE) {
                if (cameraPulse != null) cameraPulse.stop();
                cameraPulse = YoYo.with(new BaseViewAnimator() {
                            @Override
                            protected void prepare(View target) {
                                getAnimatorAgent().playTogether(
                                        ObjectAnimator.ofFloat(target, View.SCALE_X,
                                                1f, CAMERA_PULSE_PEAK_SCALE, 1f),
                                        ObjectAnimator.ofFloat(target, View.SCALE_Y,
                                                1f, CAMERA_PULSE_PEAK_SCALE, 1f));
                            }
                        })
                        .duration(500)
                        .repeat(1)
                        .playOn(camera);
            }
            camera.postDelayed(this, CAMERA_PULSE_INTERVAL_MS);
        }
    };

    // Pika
    private Button comment1;
    private String textComment1 = "";
    private String commentTitle = "Комментарий";
    private Clicks.clickObject<DialogData> commentDialogSetup;

    // Pika комментарий сразу поверх фото
    public boolean commentOn = false;

    private int score;
    // ------------------------
    private ImageButton close, help, videoHelp, call;

    public DialogFullPhotoR(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.97);
//        int height = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);

        dialog.setContentView(R.layout.dialog_photo_fullscreen);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.MATCH_PARENT);

        close = dialog.findViewById(R.id.imageButtonClose);
        help = dialog.findViewById(R.id.imageButtonLesson);

        photo = dialog.findViewById(R.id.photo);
//        photo.setAdjustViewBounds(true);
        camera = dialog.findViewById(R.id.camera);
        camera.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) { }

            @Override
            public void onViewDetachedFromWindow(View view) {
                stopCameraPulse();
            }
        });
        dialog.setOnDismissListener(ignored -> stopCameraPulse());

        // Pika
        comment1 = dialog.findViewById(R.id.buttonComment);
        comment1.setOnClickListener(v -> {
            try {
                showComment(false);
            } catch (Exception e) {
                Log.e("DialogFullPhotoR", "Cannot show photo information", e);
            }
        });

    }

    public void show() {
        if (dialog != null)
            dialog.show();
        startCameraPulse();
        // Pika
        if (commentOn) {
            showComment(true);
        }
    }

    private void showComment(boolean includeScore) {
        DialogData dialogData = new DialogData(context);
        dialogData.setTitle(commentTitle);
        dialogData.setText(textComment1);
        if (includeScore) dialogData.setScore(score);
        if (commentDialogSetup != null) commentDialogSetup.click(dialogData);
        dialogData.setClose(dialogData::dismiss);
        dialogData.show();
    }

    public void setCommentDialogSetup(Clicks.clickObject<DialogData> setup) {
        commentDialogSetup = setup;
    }

    public void dismiss() {
        stopCameraPulse();
        if (dialog != null) dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void setTitle(String text) {
        TextView title = dialog.findViewById(R.id.photoTitle);
        title.setText(text);
        title.setVisibility(text == null || text.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void setCommentTitle(String text) {
        commentTitle = text;
        comment1.setText(text);
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    // ---------------------------------------------------------------------------------------------

    public void hideCamera() {
        stopCameraPulse();
        camera.setVisibility(View.GONE);
    }

    public void setCameraPulseEnabled(boolean enabled) {
        cameraPulseEnabled = enabled;
        startCameraPulse();
    }

    private void startCameraPulse() {
        stopCameraPulse();
        if (cameraPulseEnabled && isShowing() && camera.getVisibility() == View.VISIBLE) {
            camera.post(cameraPulseTask);
        }
    }

    private void stopCameraPulse() {
        camera.removeCallbacks(cameraPulseTask);
        if (cameraPulse != null) {
            cameraPulse.stop();
            cameraPulse = null;
            camera.setScaleX(1f);
            camera.setScaleY(1f);
        }
    }

    public void setCamera(Clicks.clickVoid clickVoid){
        camera.setOnClickListener((view) -> {
            clickVoid.click();
        });
    }

    public void setGallery(Clicks.clickVoid clickVoid) {
        setCameraPulseEnabled(false);
        camera.setImageResource(R.drawable.ic_menu_gallery);
        camera.setContentDescription("Вибрати з галереї");
        camera.setOnClickListener(view -> clickVoid.click());
    }

    // Pika Виктор добавил этот метод чтоб при создании диалога можно было установить масштабирование
    public void scaleType(ImageView.ScaleType type){
        photo.setScaleType(type);
    }

    public void setPhoto(Uri data) {
        File file = new File(data.toString());
        Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
        if (b != null) {
            photo.setImageBitmap(b);
        }
    }

    public void setPhoto(StackPhotoDB stackPhotoDB) {
        try {
            photo.setImageURI(Uri.parse(stackPhotoDB.getPhoto_num()));
            List<FragmentSDB> fragmentSDB = SQL_DB.fragmentDao().getAllByPhotoId(Integer.parseInt(stackPhotoDB.photoServerId));
            for (int i = 0; i < fragmentSDB.size(); i++) {
                new PhotoFragments().setPhotoFragment(fragmentSDB.get(i), String.valueOf(i+1),photo, PhotoFragments.PhotoFragmentsSize.BIG);
            }
            new PhotoFragments().setPhotoFragmentClick(fragmentSDB, stackPhotoDB, photo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pika
    public void setComment(String txt) {
        if (txt == null)
            textComment1="Комментарий отсутствует";
        else if (txt.isEmpty())
            textComment1="Комментарий отсутствует";
        else
            textComment1=txt;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

