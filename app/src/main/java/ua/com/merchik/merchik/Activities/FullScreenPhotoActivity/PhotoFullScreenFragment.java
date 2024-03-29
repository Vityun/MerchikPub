package ua.com.merchik.merchik.Activities.FullScreenPhotoActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ortiz.touchview.TouchImageView;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;

public class PhotoFullScreenFragment extends Fragment {

    private StackPhotoDB photoDB;

    private Rect rect;

    private ImageView back;
    private TextView title, text;
    private TouchImageView image;
//    private MyTouchImageView image;

    private FloatingActionButton fab;

    public PhotoFullScreenFragment(StackPhotoDB photoDB) {
        this.photoDB = photoDB;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_full_photo, container, false);

        back = v.findViewById(R.id.back);
        title = v.findViewById(R.id.title);
        text = v.findViewById(R.id.sub_title);

//        image = new MyTouchImageView(getActivity());
        image = v.findViewById(R.id.image);

        fab = v.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> setBack());

        setData();

        return v;
    }

    private void setData() {
//        setBack();
        setPhoto();
    }

    private void setPhoto() {
        image.setImageURI(Uri.parse(photoDB.getPhoto_num()));

        if (photoDB.photoServerId != null && !photoDB.photoServerId.equals("")) {
            List<FragmentSDB> fragmentSDB = SQL_DB.fragmentDao().getAllByPhotoId(Integer.parseInt(photoDB.photoServerId));
            if (fragmentSDB != null && fragmentSDB.size() > 0) {
                setPhotoFragment(fragmentSDB);
                /*image.setOnTouchListener((v, event) -> {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        float x = event.getX();
                        float y = event.getY();

                        // Конвертируем координаты к реальным размерам изображения
                        float[] pts = {x, y};
                        Matrix matrix = ((ImageView) v).getImageMatrix();
                        matrix.invert(matrix);
                        matrix.mapPoints(pts);

                        float newY1 = bitmapHeight - fragmentSDB.get(0).y1;
                        float newY2 = bitmapHeight - fragmentSDB.get(0).y2;

                        Log.d("Coordinates", "newY1: " + newY1);
                        Log.d("Coordinates", "newY2: " + newY2);

//                        RectF rect = new RectF(fragmentSDB.get(0).x1, newY1, fragmentSDB.get(0).x2, newY2);
                        RectF rect = new RectF(fragmentSDB.get(0).x1, newY1, fragmentSDB.get(0).x2, newY2);

                        // Проверяем, находится ли точка внутри прямоугольника
//                        RectF rect = new RectF(fragmentSDB.get(0).x1, fragmentSDB.get(0).y1, fragmentSDB.get(0).x2, fragmentSDB.get(0).y2);
                        if (rect.contains(pts[0], pts[1])) {
                            // Клик был внутри прямоугольника
                            // Здесь можно выполнить нужное действие
                            Log.d("Coordinates", "+");
//                            Toast.makeText(v.getContext(), fragmentSDB.get(0).comment, Toast.LENGTH_SHORT).show();

                            DialogData dialog = new DialogData(v.getContext());
                            dialog.setTitle("Фрагмент(" + fragmentSDB.get(0).id + ")");
                            dialog.setText(fragmentSDB.get(0).comment);
                            dialog.setClose(dialog::dismiss);
                            dialog.show();

                            RealmManager.setRowToLog(Collections.singletonList(
                                    new LogDB(
                                            RealmManager.getLastIdLogDB() + 1,
                                            System.currentTimeMillis() / 1000,
                                            "Факт натискання на фрагмент. (" + fragmentSDB.get(0).id + ")",
                                            1258,
                                            photoDB.client_id,
                                            photoDB.addr_id,
                                            Long.getLong(photoDB.photoServerId),
                                            null,
                                            System.currentTimeMillis() / 1000,
                                            Globals.session,
                                            String.valueOf(photoDB.dt))));
                        } else {
                            Log.d("Coordinates", "-");
                        }
                    }
                    return false;

//                    checkFragmentClick(v, event);
//                    return true;
                });*/

                image.setOnTouchListener((v, event) -> {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        float x = event.getX();
                        float y = event.getY();

                        // Конвертируем координаты к реальным размерам изображения
                        float[] pts = {x, y};
                        Matrix matrix1 = ((ImageView) v).getImageMatrix();
                        matrix1.invert(matrix1);
                        matrix1.mapPoints(pts);

                        int newY1 = bitmapHeight - fragmentSDB.get(0).y1;
                        int newY2 = bitmapHeight - fragmentSDB.get(0).y2;

                        // Проверяем, находится ли точка внутри прямоугольника
//                        RectF rect = new RectF(fragmentSDB.get(0).x1, fragmentSDB.get(0).y1, fragmentSDB.get(0).x2, fragmentSDB.get(0).y2);
                        RectF rect = new RectF(fragmentSDB.get(0).x1, newY1, fragmentSDB.get(0).x2, newY2);

                        // Получаем матрицу, чтобы перевести координаты прямоугольника в координаты View
                        Matrix matrix2 = new Matrix();
                        image.getImageMatrix().invert(matrix2);
                        RectF rectInImageView = new RectF();
                        matrix2.mapRect(rectInImageView, rect);

                        if (rectInImageView.contains(x, y)) {
                            // Клик был внутри прямоугольника
                            // Здесь можно выполнить нужное действие
                            Log.d("Coordinates", "+");
                            DialogData dialog = new DialogData(v.getContext());
                            dialog.setTitle("Фрагмент(" + fragmentSDB.get(0).id + ")");
                            dialog.setText(fragmentSDB.get(0).comment);
                            dialog.setClose(dialog::dismiss);
                            dialog.show();

                            RealmManager.setRowToLog(Collections.singletonList(
                                    new LogDB(
                                            RealmManager.getLastIdLogDB() + 1,
                                            System.currentTimeMillis() / 1000,
                                            "Факт натискання на фрагмент. (" + fragmentSDB.get(0).id + ")",
                                            1258,
                                            photoDB.client_id,
                                            photoDB.addr_id,
                                            Long.getLong(photoDB.photoServerId),
                                            null,
                                            System.currentTimeMillis() / 1000,
                                            Globals.session,
                                            String.valueOf(photoDB.dt))));
                        } else {
                            Log.d("Coordinates", "-");
                        }
                    }
                    return true;
                });
            }
        }
    }

    private void setBack() {
        getFragmentManager().beginTransaction().remove(PhotoFullScreenFragment.this).commit();
    }

    int bitmapHeight;
    private void setPhotoFragment(List<FragmentSDB> fragment) {
        // Получаем Bitmap из ImageView
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

        // Создаем новый Bitmap с такими же размерами, как и исходное изображение
        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Создаем объект класса Canvas для нового Bitmap
        Canvas canvas = new Canvas(newBitmap);

        // Рисуем прямоугольник на изображении
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5f);
        paint.setAlpha(128); // устанавливаем прозрачность

        Log.d("Coordinates", "fragment.get(0).x1: " + fragment.get(0).x1);
        Log.d("Coordinates", "fragment.get(0).x2: " + fragment.get(0).x2);
        Log.d("Coordinates", "fragment.get(0).y1: " + fragment.get(0).y1);
        Log.d("Coordinates", "fragment.get(0).y2: " + fragment.get(0).y2);

        Log.d("Coordinates", "bitmap.getHeight(): " + bitmap.getHeight());

        bitmapHeight = bitmap.getHeight();
        int newY1 = bitmapHeight - fragment.get(0).y1;
        int newY2 = bitmapHeight - fragment.get(0).y2;

        Log.d("Coordinates", "newY1: " + newY1);
        Log.d("Coordinates", "newY2: " + newY2);

        rect = new Rect(fragment.get(0).x1, newY1, fragment.get(0).x2, newY2);
//        rect = new Rect(100, 200, 300, 400);
        canvas.drawRect(rect, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setAlpha(255); // возвращаем непрозрачность

        canvas.drawRect(rect, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(40f);

        canvas.drawText("1", rect.left + 10, rect.bottom + 50, paint);

        // Устанавливаем новый Bitmap в ImageView
        image.setImageBitmap(newBitmap);
    }

    private void checkFragmentClick(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.d("Coordinates", "x: " + x + " y: " + y);

        Log.d("Coordinates", "l:" + rect.left + "/r:" + rect.right + "/b:" + rect.bottom + "/t:" + rect.top);
        if (rect.contains((int) x, (int) y)) {
            // Точка находится внутри прямоугольника
            Log.d("Coordinates", "+");
        } else {
            // Точка находится за пределами прямоугольника
            Log.d("Coordinates", "-");
        }
    }
}
