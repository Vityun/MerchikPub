package ua.com.merchik.merchik.Activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.toolbar_menus;


public class MenuMainActivity extends toolbar_menus {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();


        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
                test();
            });

            findViewById(R.id.fab).setOnLongClickListener(v -> {
                Toast.makeText(this, "Отладочная информация!\nДолгий клик по подсказке.", Toast.LENGTH_SHORT).show();
//                test2(v.getContext());
                return true;
            });


            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "ОшибкаMain: " + e);
        }
    }

    private void test() {
//        new FragmentsExchange().downloadFragmentsTable(new ExchangeInterface.ExchangeResponseInterface() {
//            @Override
//            public <T> void onSuccess(List<T> data) {
//
//            }
//
//            @Override
//            public void onFailure(String error) {
//
//            }
//        });

/*        // Получаем Bitmap из ImageView
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

// Создаем новый Bitmap с такими же размерами, как и исходное изображение
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

// Создаем объект класса Canvas для нового Bitmap
        Canvas canvas = new Canvas(newBitmap);

// Рисуем прямоугольник на изображении
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);

        Rect rect = new Rect(left, top, right, bottom);
        canvas.drawRect(rect, paint);

// Устанавливаем новый Bitmap в ImageView
        imageView.setImageBitmap(newBitmap);*/
    }

    // =================================== --- onCreate --- ========================================

    private void setActivityContent() {
        setContentView(R.layout.toolbar_new);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_menu_main));
    }



}
