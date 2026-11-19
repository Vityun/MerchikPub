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
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ServerExchange.PhotoUrlUtils;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;

public class DialogFullPhotoR {

    private static final float CAMERA_PULSE_PEAK_SCALE = 1.18f;
    private static final long CAMERA_PULSE_INTERVAL_MS = 5_000L;

    private Dialog dialog;
    private Context context;

    public StackPhotoDB photoDB;

    // ------------------------
    private TouchImageView photo;
    private ImageButton camera;
    private PhotoPreviewLoader<StackPhotoDB> photoLoader;
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
        dialog.setOnDismissListener(ignored -> {
            stopCameraPulse();
            if (photoLoader != null) photoLoader.close();
        });

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
        if (photoLoader != null) photoLoader.close();
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
        renderPhoto(stackPhotoDB, true);
    }

    private void renderPhoto(StackPhotoDB stackPhotoDB, boolean showFragments) {
        try {
            photoDB = stackPhotoDB;
            // TouchImageView 3.1 requires a non-null user listener; false keeps its zoom gestures.
            photo.setOnTouchListener((view, event) -> false);
            // ImageView otherwise keeps the old bitmap when a file is replaced at the same URI.
            photo.setImageDrawable(null);
            photo.setImageURI(Uri.parse(stackPhotoDB.getPhoto_num()));
            if (!showFragments || stackPhotoDB.photoServerId == null || stackPhotoDB.photoServerId.isEmpty()) return;
            List<FragmentSDB> fragmentSDB = SQL_DB.fragmentDao().getAllByPhotoId(Integer.parseInt(stackPhotoDB.photoServerId));
            for (int i = 0; i < fragmentSDB.size(); i++) {
                new PhotoFragments().setPhotoFragment(fragmentSDB.get(i), String.valueOf(i+1),photo, PhotoFragments.PhotoFragmentsSize.BIG);
            }
            if (!fragmentSDB.isEmpty()) new PhotoFragments().setPhotoFragmentClick(fragmentSDB, stackPhotoDB, photo);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DialogFullPhotoR/render",
                    "photo=" + (stackPhotoDB == null ? null : stackPhotoDB.getPhotoServerId()) + ", error=" + e);
            e.printStackTrace();
        }
    }

    public void setPhotoWithDownload(StackPhotoDB preview, String serverId) {
        if (photoLoader != null) photoLoader.close();
        View status = dialog.findViewById(R.id.photoLoadStatus);
        View progress = dialog.findViewById(R.id.photoLoadProgress);
        TextView message = dialog.findViewById(R.id.photoLoadMessage);
        Button retry = dialog.findViewById(R.id.photoLoadRetry);
        photoLoader = new PhotoPreviewLoader<>(new PhotoPreviewLoader.Source<StackPhotoDB>() {
            @Override public boolean isReady(StackPhotoDB value) { return hasOriginal(value); }

            @Override public void download(StackPhotoDB value, PhotoPreviewLoader.Callback<StackPhotoDB> callback) {
                if (value != null && PhotoUrlUtils.isSafeDownloadUrl(value.getPhotoServerURL())) {
                    downloadOriginal(value, callback);
                    return;
                }
                String id = value != null && value.getPhotoServerId() != null
                        && !value.getPhotoServerId().isEmpty() && !"0".equals(value.getPhotoServerId())
                        ? value.getPhotoServerId() : serverId;
                if (id == null || !id.matches("[1-9][0-9]*")) {
                    callback.onFailure("Photo has no server ID or URL");
                    return;
                }
                PhotoTableRequest request = new PhotoTableRequest();
                request.mod = "images_view";
                request.act = "list_image";
                request.nolimit = "1";
                request.id_list = id;
                new PhotoDownload().getPhotoInfoAndSaveItToDB(request, new Clicks.clickObjectAndStatus<StackPhotoDB>() {
                    @Override public void onSuccess(StackPhotoDB loaded) { downloadOriginal(loaded, callback); }
                    @Override public void onFailure(String error) { callback.onFailure(error); }
                });
            }
        }, new PhotoPreviewLoader.Listener<StackPhotoDB>() {
            @Override public void showPhoto(StackPhotoDB value) {
                if (localFile(value) != null) renderPhoto(value, hasOriginal(value));
                setComment(value.getComment());
            }

            @Override public void setLoading(boolean loading) {
                status.setVisibility(loading ? View.VISIBLE : View.GONE);
                progress.setVisibility(loading ? View.VISIBLE : View.GONE);
                retry.setVisibility(View.GONE);
                message.setText(R.string.photo_original_loading);
            }

            @Override public void showError(String error) {
                status.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                retry.setVisibility(View.VISIBLE);
                message.setText(R.string.photo_original_error);
                Globals.writeToMLOG("ERROR", "DialogFullPhotoR/original", "photo=" + serverId + ", " + error);
            }
        });
        retry.setOnClickListener(view -> photoLoader.retry());
        photoLoader.open(preview);
    }

    private void downloadOriginal(StackPhotoDB value, PhotoPreviewLoader.Callback<StackPhotoDB> callback) {
        if (hasOriginal(value)) {
            callback.onSuccess(value);
            return;
        }
        new PhotoDownload().downloadPhoto(true, value, "/TAR", new PhotoDownload.downloadPhotoInterface() {
            @Override public void onSuccess(StackPhotoDB loaded) { callback.onSuccess(loaded); }
            @Override public void onFailure(String error) { callback.onFailure(error); }
        });
    }

    private static boolean hasOriginal(StackPhotoDB value) {
        return localFile(value) != null && ("Full".equalsIgnoreCase(value.getPhoto_size())
                || (!"Small".equalsIgnoreCase(value.getPhoto_size())
                && (value.getPhotoServerId() == null || value.getPhotoServerId().isEmpty()
                || "0".equals(value.getPhotoServerId()))
                && !PhotoUrlUtils.isSafeDownloadUrl(value.getPhotoServerURL())));
    }

    private static File localFile(StackPhotoDB value) {
        if (value == null || value.getPhoto_num() == null || value.getPhoto_num().isEmpty()) return null;
        Uri uri = Uri.parse(value.getPhoto_num());
        String path = uri.getPath();
        if (path == null) return null;
        File file = new File(path);
        return file.isFile() && file.length() > 0 ? file : null;
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

