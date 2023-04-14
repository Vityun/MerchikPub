package ua.com.merchik.merchik.Activities.PhotoLogActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ortiz.touchview.TouchImageView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

/**
 * Журнал фото(Длинная рука) после клика по фотке
 */
public class PhotoLogPhotoAdapter extends RecyclerView.Adapter<PhotoLogPhotoAdapter.TouchPhotoVH> {

    private List<StackPhotoDB> data;

    private OnPhotoClickListener mOnPhotoClickListener;
    private Clicks.clickVoid clickVoid;

    public interface OnPhotoClickListener {
        void onPhotoClicked(StackPhotoDB photoDB);
    }

    public PhotoLogPhotoAdapter(List<StackPhotoDB> data, View.OnTouchListener onTouchListener, OnPhotoClickListener mOnPhotoClickListener, Clicks.clickVoid clickVoid) {
        this.data = data;
        this.mOnPhotoClickListener = mOnPhotoClickListener;
        this.clickVoid = clickVoid;
    }

    @NonNull
    @Override
    public TouchPhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_photo_log, parent, false);
        return new TouchPhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TouchPhotoVH holder, int position) {
/*        int c = getItemCount() - position - 1; // Отобразить снизу вверх
        StackPhotoDB photoLogDat = data.get(c);
        holder.bind(photoLogDat);*/
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class TouchPhotoVH extends RecyclerView.ViewHolder {

        private final TouchImageView image;

        public TouchPhotoVH(@NonNull View itemView) {
            super(itemView);
//            image = (TouchImageView) itemView;
            image = itemView.findViewById(R.id.image);
            image.setOnTouchListener((v, event) -> {
                boolean result = true;
                //can scroll horizontally checks if there's still a part of the image
                //that can be scrolled until you reach the edge
                if (event.getPointerCount() >= 2 || v.canScrollHorizontally(1) || v.canScrollHorizontally(-1)) {
                    //multi-touch event
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                            // Disallow RecyclerView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(true);//????
                            // Disable touch on view
                            result = false;
                            break;
                        case MotionEvent.ACTION_UP:
                            // Allow RecyclerView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            result = true;
                            break;
                    }
                }

                return result;
            });
        }

        public void bind(StackPhotoDB photo) {

            Log.e("FULL_PHOTO", "HERE");

            Log.e("FULL_PHOTO", "photo: " + photo.getId());
            Log.e("FULL_PHOTO", "photo: " + photo.getPhotoServerId());
            Log.e("FULL_PHOTO", "photo: " + photo.getPhoto_num());


            // Отображение фото
            setImage(photo);


//            // Попытка отрисовать отрисовать фото.
//            try {
//                image.setImageURI(Uri.parse(photo.getPhoto_num()));
//            }catch (Exception e){
//                // Есть случаи когда оно пытается отрисовать фото которые ещё не загружены на
//                // телефон - в таком случае ничего не отрисовуем
//            }


//            PhotoDownload photoDownload = new PhotoDownload();
//            if (photo.getPhotoServerURL() != null && !photo.getPhotoServerURL().equals("")){
//                photoDownload.downloadPhoto(true, photo, new PhotoDownload.downloadPhotoInterface() {
//                    @Override
//                    public void onSuccess(StackPhotoDB data) {
//                        Log.e("FULL_PHOTO", "HERE/onSuccess");
//                        image.setImageURI(Uri.parse(photo.getPhoto_num()));
//                    }
//
//                    @Override
//                    public void onFailure(String s) {
//                        Log.e("FULL_PHOTO", "HERE/onFailure");
//                    }
//                });
//            }


        }


        /**
         * 02.03.2021
         * Отрисовка фотографии.
         * <p>
         * На данный момент (02.03.2021) в этом месте может отрисоваться фото по 3м разным сценариям:
         * Первый(основной) - человек делал фото самостоятельно и надо его отобразить с памяти.
         * Второй - Отоб
         */
        private void setImage(StackPhotoDB photo) {
            try {
                if (photo.getPhoto_size() == null) { // Отображение фотографии с памяти телефона
                    Log.e("FULL_PHOTO", "Фото было выполенно на телефоне и загружается с памяти");
//                    Toast.makeText(image.getContext(), "Фото было выполенно на телефоне и загружается с памяти", Toast.LENGTH_LONG).show();

                    image.setImageURI(Uri.parse(photo.getPhoto_num()));
                } else if (photo.getPhoto_size().equals("Full")) {    // Если фото большое - просто его отображаем
                    Log.e("FULL_PHOTO", "Фото было загружено с сервера и отображается в БОЛЬШОМ формате");
//                    Toast.makeText(image.getContext(), "Фото было загружено с сервера и отображается в БОЛЬШОМ формате", Toast.LENGTH_LONG).show();

                    image.setImageURI(Uri.parse(photo.getPhoto_num()));
                } else if (photo.getPhoto_size().equals("Small")) {
                    Log.e("FULL_PHOTO", "Фото отображается в маленьком формате, но уже начинает грузиться в большом");
//                    Toast.makeText(image.getContext(), "Фото отображается в маленьком формате, но уже начинает грузиться в большом", Toast.LENGTH_LONG).show();

                    image.setImageURI(Uri.parse(photo.getPhoto_num()));
                    downloadFullPhoto(photo);
                } else {
//                    Toast.makeText(image.getContext(), "Не получилось отобразить фото. Обратитесь к Вашему руководителю.", Toast.LENGTH_LONG).show();
                }


                if (photo.photoServerId != null && !photo.photoServerId.equals("")) {
                    List<FragmentSDB> fragmentSDB = SQL_DB.fragmentDao().getAllByPhotoId(Integer.parseInt(photo.photoServerId));
                    if (fragmentSDB != null && fragmentSDB.size() > 0) {
                        setFragmentOnImage(fragmentSDB);
//                        image.setOnTouchListener((v, event) -> {
//                            checkFragmentClick(v, event);
//                            return true;
//                        });
                    }
                }


//                setFragmentClick();

                image.setOnLongClickListener(view -> {
                    openFullScreenPhoto(view, photo);
                    return false;
                });

            } catch (Exception e) {
                // Разбирать ошибку.
            }
        }

        /**
         * 13.04.23.
         * Обработка долгого клика по фото.
         * Должно открывать фрагмент и в нём размещать фотографию на весь экран.
         *
         * @param view
         * @param photo
         */
        private void openFullScreenPhoto(View view, StackPhotoDB photo) {
            Toast.makeText(view.getContext(), "Открыл фулл фото", Toast.LENGTH_SHORT).show();
            clickVoid.click();
            mOnPhotoClickListener.onPhotoClicked(photo);
        }


        /*Загрузка фото в большом формате*/
        private void downloadFullPhoto(StackPhotoDB photo) {
            PhotoDownload photoDownload = new PhotoDownload();
            if (photo.getPhotoServerURL() != null && !photo.getPhotoServerURL().equals("")) {
                photoDownload.downloadPhoto(true, photo, new PhotoDownload.downloadPhotoInterface() {
                    @Override
                    public void onSuccess(StackPhotoDB data) {
                        Log.e("FULL_PHOTO", "HERE/onSuccess");
                        Log.e("FULL_PHOTO", "Фото было в маленьком формате, но загрузилось в большом");
//                        Toast.makeText(image.getContext(), "Фото было в маленьком формате, но загрузилось в большом", Toast.LENGTH_LONG).show();

                        image.setImageURI(Uri.parse(photo.getPhoto_num()));
                    }

                    @Override
                    public void onFailure(String s) {
                        Log.e("FULL_PHOTO", "HERE/onFailure");
                    }
                });
            }
        }

        /**
         * 12.04.23.
         * Отрисовка фрагмента на фото
         * НУЖНО ПЕРЕДАВАТЬ КООРДИНАТЫ
         */
        private Rect rect;

        private void setFragmentOnImage(List<FragmentSDB> fragment) {
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

            rect = new Rect(fragment.get(0).x1, fragment.get(0).y1, fragment.get(0).x2, fragment.get(0).y2);
            canvas.drawRect(rect, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setAlpha(255); // возвращаем непрозрачность

            canvas.drawRect(rect, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(40f);

            canvas.drawText("1", rect.left + 10, rect.top + 50, paint);


            // Устанавливаем новый Bitmap в ImageView
            image.setImageBitmap(newBitmap);
        }


        /**
         * 12.04.23.
         * Обработка клика по фрагменту
         * НУЖНО ПЕРЕДАВАТЬ КООРДИНАТЫ
         */
        private void setFragmentClick() {

            image.setOnTouchListener((v, event) -> {
                float x = event.getX();
                float y = event.getY();
                Log.d("Coordinates", "x: " + x + " y: " + y);

                // Проверяем, попадает ли клик в прямоугольную область
                if (x >= 100 && x <= 300 && y >= 200 && y <= 400) {
                    // Клик попадает в прямоугольную область
                    // Добавьте здесь код для обработки клика
//                        Toast.makeText(image.getContext(), "Клик по ФРАГМЕНТУ!", Toast.LENGTH_SHORT).show();
                } else {
//                        Toast.makeText(image.getContext(), "Клик мимо фрагмента!", Toast.LENGTH_SHORT).show();
                }

                if (rect.contains((int) x, (int) y)) {
                    // Точка находится внутри прямоугольника
                } else {
                    // Точка находится за пределами прямоугольника
                }


                return true;
            });

/*            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Получаем координаты клика
                    float x = v.getX();
                    float y = v.getY();


                }
            });*/
        }
    }

}
