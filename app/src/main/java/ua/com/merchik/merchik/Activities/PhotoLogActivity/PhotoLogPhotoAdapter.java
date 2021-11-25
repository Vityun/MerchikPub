package ua.com.merchik.merchik.Activities.PhotoLogActivity;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ortiz.touchview.TouchImageView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public class PhotoLogPhotoAdapter extends RecyclerView.Adapter<PhotoLogPhotoAdapter.TouchPhotoVH> {

    private List<StackPhotoDB> data;

    public PhotoLogPhotoAdapter(List<StackPhotoDB> data, View.OnTouchListener onTouchListener) {
        this.data = data;
    }



    @NonNull
    @Override
    public TouchPhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_photo_log, parent, false);
        return new TouchPhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TouchPhotoVH holder, int position) {
        int c = getItemCount() - position - 1; // Отобразить снизу вверх
        StackPhotoDB photoLogDat = data.get(c);
        holder.bind(photoLogDat);
//        holder.bind(data.get(position));
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
         *
         * На данный момент (02.03.2021) в этом месте может отрисоваться фото по 3м разным сценариям:
         * Первый(основной) - человек делал фото самостоятельно и надо его отобразить с памяти.
         * Второй - Отоб
         * */
        private void setImage(StackPhotoDB photo){
            try{
                if (photo.getPhoto_size() == null){ // Отображение фотографии с памяти телефона
                    Log.e("FULL_PHOTO", "Фото было выполенно на телефоне и загружается с памяти");
//                    Toast.makeText(image.getContext(), "Фото было выполенно на телефоне и загружается с памяти", Toast.LENGTH_LONG).show();

                    image.setImageURI(Uri.parse(photo.getPhoto_num()));
                }else if (photo.getPhoto_size().equals("Full")){    // Если фото большое - просто его отображаем
                    Log.e("FULL_PHOTO", "Фото было загружено с сервера и отображается в БОЛЬШОМ формате");
//                    Toast.makeText(image.getContext(), "Фото было загружено с сервера и отображается в БОЛЬШОМ формате", Toast.LENGTH_LONG).show();

                    image.setImageURI(Uri.parse(photo.getPhoto_num()));
                }else if (photo.getPhoto_size().equals("Small")){
                    Log.e("FULL_PHOTO", "Фото отображается в маленьком формате, но уже начинает грузиться в большом");
//                    Toast.makeText(image.getContext(), "Фото отображается в маленьком формате, но уже начинает грузиться в большом", Toast.LENGTH_LONG).show();

                    image.setImageURI(Uri.parse(photo.getPhoto_num()));
                    downloadFullPhoto(photo);
                }else {
//                    Toast.makeText(image.getContext(), "Не получилось отобразить фото. Обратитесь к Вашему руководителю.", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                // Разбирать ошибку.
            }
        }


        /*Загрузка фото в большом формате*/
        private void downloadFullPhoto(StackPhotoDB photo){
            PhotoDownload photoDownload = new PhotoDownload();
            if (photo.getPhotoServerURL() != null && !photo.getPhotoServerURL().equals("")){
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


    }

}
