package ua.com.merchik.merchik.Activities.FullScreenPhotoActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;

public class PhotoFragments {
    public enum PhotoFragmentsSize {
        BIG, SMALL
    }

    public void setPhotoFragment(FragmentSDB fragment, String text, ImageView image, PhotoFragmentsSize size) {
        // Получаем Bitmap из ImageView
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

        // Создаем новый Bitmap с такими же размерами, как и исходное изображение
        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Создаем объект класса Canvas для нового Bitmap
        Canvas canvas = new Canvas(newBitmap);

        // Рисуем прямоугольник на изображении
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5f);
        paint.setAlpha(64); // устанавливаем прозрачность

        Log.d("Coordinates", "fragment.get(0).x1: " + fragment.x1);
        Log.d("Coordinates", "fragment.get(0).x2: " + fragment.x2);
        Log.d("Coordinates", "fragment.get(0).y1: " + fragment.y1);
        Log.d("Coordinates", "fragment.get(0).y2: " + fragment.y2);

        Log.d("Coordinates", "bitmap.getHeight(): " + bitmap.getHeight());

        Rect rect;
        if (size.equals(PhotoFragmentsSize.BIG)) {
            int bitmapHeight = bitmap.getHeight();
            int newY1 = bitmapHeight - fragment.y1;
            int newY2 = bitmapHeight - fragment.y2;
            rect = new Rect(fragment.x1, newY1, fragment.x2, newY2);
        } else if (size.equals(PhotoFragmentsSize.SMALL)) {
            int bitmapHeight = bitmap.getHeight() * 2;
            int newY1 = bitmapHeight - fragment.y1;
            int newY2 = bitmapHeight - fragment.y2;
            rect = new Rect(fragment.x1 / 2, newY1 / 2, fragment.x2 / 2, newY2 / 2);
        } else {
            int bitmapHeight = bitmap.getHeight();
            int newY1 = bitmapHeight - fragment.y1;
            int newY2 = bitmapHeight - fragment.y2;
            rect = new Rect(fragment.x1, newY1, fragment.x2, newY2);
        }

        canvas.drawRect(rect, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setAlpha(255); // возвращаем непрозрачность

        canvas.drawRect(rect, paint);

        switch (size) {
            case SMALL:
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setTextSize(20f);
                canvas.drawText(text, rect.left + 5, rect.bottom + 25, paint);
                break;

            case BIG:
            default:
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setTextSize(40f);
                canvas.drawText(text, rect.left + 10, rect.bottom + 50, paint);
                break;
        }


        // Устанавливаем новый Bitmap в ImageView
        image.setImageBitmap(newBitmap);
    }

    public void setPhotoFragmentClick(List<FragmentSDB> fragmentSDB, StackPhotoDB stackPhotoDB, ImageView photo){
        photo.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();

                // Конвертируем координаты к реальным размерам изображения
                float[] pts = {x, y};
                Matrix matrix1 = ((ImageView) v).getImageMatrix();
                matrix1.invert(matrix1);
                matrix1.mapPoints(pts);

                for (FragmentSDB item : fragmentSDB){
                    setFragmentClick(item, photo, x, y, v.getContext(), stackPhotoDB);
                }
            }
            return true;
        });
    }

    private void setFragmentClick(FragmentSDB fragmentSDB, ImageView photo, float x, float y, Context context, StackPhotoDB stackPhotoDB){
        Bitmap bitmap = ((BitmapDrawable) photo.getDrawable()).getBitmap();
        int bitmapHeight = bitmap.getHeight();

        int newY1 = bitmapHeight - fragmentSDB.y1;
        int newY2 = bitmapHeight - fragmentSDB.y2;

        // Проверяем, находится ли точка внутри прямоугольника
        RectF rect = new RectF(fragmentSDB.x1, newY1, fragmentSDB.x2, newY2);

        // Получаем матрицу, чтобы перевести координаты прямоугольника в координаты View
        Matrix matrix2 = new Matrix();
        photo.getImageMatrix().invert(matrix2);
        RectF rectInImageView = new RectF();
        matrix2.mapRect(rectInImageView, rect);

        if (rectInImageView.contains(x, y)) {
            // Клик был внутри прямоугольника
            // Здесь можно выполнить нужное действие
            Log.d("Coordinates", "+");
            DialogData dialog = new DialogData(context);
            dialog.setTitle("Фрагмент(" + fragmentSDB.id + ")");
            dialog.setText(fragmentSDB.comment);
            dialog.setClose(dialog::dismiss);
            dialog.show();

            RealmManager.setRowToLog(Collections.singletonList(
                    new LogDB(
                            RealmManager.getLastIdLogDB() + 1,
                            System.currentTimeMillis() / 1000,
                            "Факт натискання на фрагмент. (" + fragmentSDB.id + ")",
                            1258,
                            stackPhotoDB.client_id,
                            stackPhotoDB.addr_id,
                            Long.getLong(stackPhotoDB.photoServerId),
                            null,
                            null,
                            Globals.session,
                            String.valueOf(stackPhotoDB.dt),
                            null,
                            null)));
        } else {
            Log.d("Coordinates", "-");
        }
    }
}
