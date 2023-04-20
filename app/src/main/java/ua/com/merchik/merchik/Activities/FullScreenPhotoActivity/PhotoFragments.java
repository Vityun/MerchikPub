package ua.com.merchik.merchik.Activities.FullScreenPhotoActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;

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
}
