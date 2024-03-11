package ua.com.merchik.merchik.dialogs;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.util.List;

import ua.com.merchik.merchik.Activities.FullScreenPhotoActivity.PhotoFragments;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public class DialogFullPhotoR {

    private Dialog dialog;
    private Context context;

    public StackPhotoDB photoDB;

    // ------------------------
    private TouchImageView photo;
    private ImageButton camera;

    // Pika
    private Button comment1;
    private String textComment1 = "";

    // Pika комментарий сразу поверх фото
    public boolean commentOn = false;


    // ------------------------
    private ImageButton close, help, videoHelp, call;

    public DialogFullPhotoR(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);
//        int height = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);

        dialog.setContentView(R.layout.dialog_photo_fullscreen);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.MATCH_PARENT);

        close = dialog.findViewById(R.id.imageButtonClose);
        help = dialog.findViewById(R.id.imageButtonLesson);

        photo = dialog.findViewById(R.id.photo);
//        photo.setAdjustViewBounds(true);
        camera = dialog.findViewById(R.id.camera);

        // Pika
        comment1 = dialog.findViewById(R.id.buttonComment);
        comment1.setOnClickListener(v -> {

        try {
//            Toast.makeText(context, textComment1, Toast.LENGTH_LONG).show();
            DialogData dialogData = new DialogData(context);
            dialogData.setTitle("Комментарий");
            dialogData.setText(textComment1);
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
        }catch (Exception e){
//            DialogData dialogData = new DialogData(context);
//            dialogData.setTitle("Произошла ошибка");
//            dialogData.setText(textComment1);
//            dialogData.setClose(dialogData::dismiss);
//            dialogData.show();
        }

        });

    }

    public void show() {
        if (dialog != null)
            dialog.show();
        // Pika
        if (commentOn) {
            DialogData dialogData = new DialogData(context);
            dialogData.setTitle("Комментарий");
            dialogData.setText(textComment1);
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
        }
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    // ---------------------------------------------------------------------------------------------

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
        else if (txt == "")
            textComment1="Комментарий отсутствует";
        else
            textComment1=txt;
    }

}

