package ua.com.merchik.merchik.dialogs;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;

import ua.com.merchik.merchik.Activities.FullScreenPhotoActivity.PhotoFragments;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public class DialogPhotoTovar {

    private Dialog dialog;
    private Context context;

    private TextView barcode, barcodeError, textInfo;
    private ImageView photo, photoBarcode;

    private ImageButton close, help, videoHelp, call;

    public DialogPhotoTovar(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);

        dialog.setContentView(R.layout.dialog_photo_tovar);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        barcode = dialog.findViewById(R.id.barcode);
        barcodeError = dialog.findViewById(R.id.barcodeError);
        textInfo = dialog.findViewById(R.id.info);
        textInfo.setVisibility(View.GONE);

        photo = dialog.findViewById(R.id.imageTovFull);
        photoBarcode = dialog.findViewById(R.id.photoBarcode);

        // Кнопки окна
        close = dialog.findViewById(R.id.imageButtonClose);
    }

    public void show() {
        Log.e("ФОТО_ТОВАРОВ", "show: должно отобразить");
        Log.e("ФОТО_ТОВАРОВ", "dialog: " + dialog);
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        Log.e("ФОТО_ТОВАРОВ", "setClose: должно отобразить");
        close.setOnClickListener(v -> {
            Log.e("ФОТО_ТОВАРОВ", "setClose: Ckick");
            clickListener.clicked();
            dismiss();
        });
    }
    //----------------------------------------------------------------------------------------------

    public void setPhotoTovar(Uri uri) {
        try {
            Log.e("ФОТО_ТОВАРОВ", "setPhotoTovar: +");
            photo.setImageURI(uri);
        } catch (Exception e) {
            Log.e("ФОТО_ТОВАРОВ", "setPhotoTovar: Ошибка ниже: ");
            e.printStackTrace();
        }
    }

    public void setPhotoTovar(StackPhotoDB stackPhotoDB) {
        try {
            Log.e("ФОТО_ТОВАРОВ", "setPhotoTovar: +");
            barcode.setVisibility(View.GONE);
            photoBarcode.setVisibility(View.GONE);
            photo.setImageURI(Uri.parse(stackPhotoDB.getPhoto_num()));
            List<FragmentSDB> fragmentSDB = SQL_DB.fragmentDao().getAllByPhotoId(Integer.parseInt(stackPhotoDB.photoServerId));
            for (int i = 0; i < fragmentSDB.size(); i++) {
                new PhotoFragments().setPhotoFragment(fragmentSDB.get(i), String.valueOf(i+1),photo, PhotoFragments.PhotoFragmentsSize.BIG);
            }
        } catch (Exception e) {
            Log.e("ФОТО_ТОВАРОВ", "setPhotoTovar: Ошибка ниже: ");
            e.printStackTrace();
        }
    }

    public void setTextInfo(StringBuilder stringBuilder) {
        if (stringBuilder != null) {
            textInfo.setText(stringBuilder);
            textInfo.setVisibility(View.VISIBLE);
        }
    }

    public void setPhotoBarcode(String barcode) {

        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.70);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.10);

        try {
            Log.e("ФОТО_ТОВАРОВ", "setPhotoBarcode: +");

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

            BitMatrix bitMatrix = multiFormatWriter.encode(barcode, BarcodeFormat.EAN_13, width, height);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            photoBarcode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e("ФОТО_ТОВАРОВ", "setPhotoBarcode: Ошибка ниже: ");
            e.printStackTrace();


            android.view.ViewGroup.LayoutParams layoutParams = photoBarcode.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            photoBarcode.setLayoutParams(layoutParams);


            barcodeError.setVisibility(View.VISIBLE);
            Spanned spanned = Html.fromHtml("<font color=red>Штрихкод " + barcode + " распознать не удалось</font>");
            barcodeError.setText(spanned);
        } finally {
//            this.barcode.setText(barcode);
            this.barcode.setVisibility(View.GONE);
        }
    }
}
