package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class TaRCommentsAdapter extends RecyclerView.Adapter<TaRCommentsAdapter.ViewHolder> {

    private Context mContext;
    private List<TARCommentsDB> data;
    private CommentPhotoClick commentPhotoClick;

    public interface CommentPhotoClick {
        void commentPhotoClick(int i);
    }

    public TaRCommentsAdapter(Context mContext, List<TARCommentsDB> data, CommentPhotoClick commentPhotoClick) {
        this.mContext = mContext;
        this.data = data;
        this.commentPhotoClick = commentPhotoClick;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView layout;
        private ImageView photo;
        private TextView textLine1, textLine2, textLine3, textLine4, textLine5;

        ViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.universalItem);
            photo = v.findViewById(R.id.photo);
            textLine1 = v.findViewById(R.id.text_line_1);
            textLine2 = v.findViewById(R.id.text_line_2);
            textLine3 = v.findViewById(R.id.text_line_3);
            textLine4 = v.findViewById(R.id.text_line_4);
            textLine5 = v.findViewById(R.id.text_line_5);
        }

        public void bind(TARCommentsDB dataItem) {
            SpannableStringBuilder line1 = new SpannableStringBuilder();
            String time = new SimpleDateFormat("dd-MM-yyyy").format(Clock.timeLongToDAte(Long.parseLong(dataItem.getDt())));
            CharSequence date = Html.fromHtml("<b>Дата: </b>" + time + " ");
            line1.append(date);
            textLine1.setText(line1);

            SpannableStringBuilder line2 = new SpannableStringBuilder();
            String time2 = new SimpleDateFormat("HH:mm:ss").format(Clock.timeLongToDAte(Long.parseLong(dataItem.getDt())));
            CharSequence date2 = Html.fromHtml("<b>Время: </b>" + time2 + " ");
            line2.append(date2);
            textLine2.setText(line2);

            SpannableStringBuilder line3 = new SpannableStringBuilder();
            line3.append(Html.fromHtml("<b>Автор комментария: </b>"));
            if (dataItem.getWho() != null && !dataItem.getWho().equals("")) {
                try {
                    UsersDB usersDB = UsersRealm.getUsersDBById(Integer.parseInt(dataItem.getWho()));
                    CharSequence str = usersDB.getNm();
                    line3.append(str);
                } catch (Exception e) {
                    // TODO data is empty
                    line3.append(dataItem.getWho());
                }

            } else {
                line3.append(dataItem.getWho());
            }
            textLine3.setText(line3);

            SpannableStringBuilder line4 = new SpannableStringBuilder();
            line4.append(Html.fromHtml("<b>Комментарий: </b>"));
            line4.append(Html.fromHtml(dataItem.getComment()));
            textLine4.setText(line4);

            textLine5.setVisibility(View.GONE);


            // Скачивание фотокграфий или их отображение
            // В случае если фото есть в базе данных стэк фото
            try {
                if ((dataItem.photo_hash != null && !dataItem.photo_hash.equals("")) || (dataItem.photo != null && !dataItem.photo.equals("0") && !dataItem.photo.equals(""))) {
                    // Если данные о фотографиях есть - Окей, если нет - даю возможность сделать фото.

                    // Если фото есть - я его должен отобразить, если фото нет - скачать.
                    StackPhotoDB tarCommentPhoto = getTarCommentPhoto(dataItem);
                    if (tarCommentPhoto != null) {
                        // Фото есть на стороне приложения, нужно их отобразить.
                        setTarCommentPhoto(tarCommentPhoto);
                    } else {
                        // Загрузка и отображение фотографий с сайта
                        checkAndDownloadPhoto(dataItem, new Clicks.clickObjectAndStatus<StackPhotoDB>() {
                            @Override
                            public void onSuccess(StackPhotoDB data) {
                                setTarCommentPhoto(data);
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(itemView.getContext(), error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    // Даю возможнось сделать фото (оно потом вігрузиться на сторону Сайта)
                    photo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_47));
                    photo.setOnClickListener((view) -> {
                        commentPhotoClick.commentPhotoClick(data.indexOf(dataItem));
                    });
                }


//                StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(dataItem.getPhoto());
//                if ((dataItem.photo_hash != null && !dataItem.photo_hash.equals("")) || (dataItem.photo != null && !dataItem.photo.equals(""))) {
//                    StackPhotoDB stackPhotoDB = StackPhotoRealm.getByHash(dataItem.photo_hash);
//
//                    if (stackPhotoDB == null && dataItem.photo != null && !dataItem.photo.equals("") && !dataItem.photo.equals("0")) {
//                        stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(dataItem.getPhoto());
//                    }
//
//                    if (stackPhotoDB != null) {
//                        if (stackPhotoDB.getPhoto_num().equals("")) {
//                            if (stackPhotoDB.getPhotoServerId() != null && !stackPhotoDB.getPhotoServerId().equals("")) {
////                                downloadPhoto(stackPhotoDB, TaRCommentsAdapter.this::notifyDataSetChanged);
//                            } else {
//                                new PhotoDownload().downloadPhoto(false, stackPhotoDB, "/TAR", new PhotoDownload.downloadPhotoInterface() {
//                                    @Override
//                                    public void onSuccess(StackPhotoDB data) {
//                                        Globals.writeToMLOG("INFO", "newPhotoDownload().downloadPhoto(", "data: " + data);
////                                photo.setImageURI(Uri.parse(data.getPhoto_num()));
//                                        File file = new File(data.getPhoto_num());
//                                        Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
//                                        if (b != null) {
//                                            photo.setImageBitmap(b);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(String s) {
//                                        Globals.writeToMLOG("INFO", "newPhotoDownload().downloadPhoto(", "s: " + s);
//                                    }
//                                });
//                            }
//                        } else {
////                        photo.setImageURI(Uri.parse(stackPhotoDB.getPhoto_num()));
//                            File file = new File(stackPhotoDB.getPhoto_num());
//                            Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
//                            if (b != null) {
//                                photo.setImageBitmap(b);
//                            }
//                        }
//                    } else {
////                        Toast.makeText(itemView.getContext(), "Не обнаружена фотография.", Toast.LENGTH_LONG).show();
//                        photo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_47));
//                        photo.setOnClickListener((view) -> {
//                            commentPhotoClick.commentPhotoClick(data.indexOf(dataItem));
//                        });
//                    }
//                } else {
////                    Toast.makeText(itemView.getContext(), "Не обнаружена фотография.", Toast.LENGTH_LONG).show();
//                    photo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_47));
//                    photo.setOnClickListener((view) -> {
//                        commentPhotoClick.commentPhotoClick(data.indexOf(dataItem));
//                    });
//                }
            } catch (Exception e) {
                Log.e("test", "НЕ удалось отобразить фото Exception: " + e);
            }


            // Клик по элементу
            layout.setOnClickListener(v -> {
//                try {
//                    StackPhotoDB stackPhotoDB = StackPhotoRealm.getByHash(dataItem.photo_hash);
//                    DialogFullPhoto dialogFullPhoto = new DialogFullPhoto(mContext);
//                    dialogFullPhoto.setPhoto(Uri.parse(stackPhotoDB.getPhoto_num()));
//                    dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
//                    dialogFullPhoto.show();
//                }catch (Exception e){
//                    Log.e("test", "Exception e: " + e);
//                }

                DialogData dialog = new DialogData(mContext);
                dialog.setTitle("");
                dialog.setText(Html.fromHtml("<b>Текст комментария: </b>" + dataItem.getComment()));
                dialog.setClose(dialog::dismiss);
                dialog.show();
            });
        }


        /**
         * 28.02.23.
         * Определяем по какому принципу мы будем загружать фотографию
         */
        private void checkAndDownloadPhoto(TARCommentsDB tarCommentsDB, Clicks.clickObjectAndStatus<StackPhotoDB> clickUpdatePhoto) {
            if (tarCommentsDB.photo_hash != null && !tarCommentsDB.photo_hash.equals("") && !tarCommentsDB.photo_hash.equals("0")) {
                // Загружаю фотку по ХЭШу
                // Пока что не загружаю. В Теории оно у меня уже есть.
                // Пока нужно разобраться с загрузкой фото на мою сторону по ID.
            } else if (tarCommentsDB.photo != null && !tarCommentsDB.photo.equals("") && !tarCommentsDB.photo.equals("0")) {
                // Загружаю фотку по ID
                downloadPhotoInfoById(tarCommentsDB.photo, new Clicks.clickObjectAndStatus<StackPhotoDB>() {
                    @Override
                    public void onSuccess(StackPhotoDB data) {
                        new PhotoDownload().downloadPhoto(false, data, "/TAR", new PhotoDownload.downloadPhotoInterface() {
                            @Override
                            public void onSuccess(StackPhotoDB data) {
                                Globals.writeToMLOG("INFO", "newPhotoDownload().downloadPhoto(", "data: " + data);
                                clickUpdatePhoto.onSuccess(data);
                            }

                            @Override
                            public void onFailure(String s) {
                                Globals.writeToMLOG("INFO", "newPhotoDownload().downloadPhoto(", "s: " + s);
                                clickUpdatePhoto.onFailure(s);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Globals.writeToMLOG("INFO", "downloadPhotoInfoById().onFailure(", "s: " + error);
                        clickUpdatePhoto.onFailure(error);
                    }
                });
            } else {
                // В теории я сюда никогда не должен попасть. А если вдруг как-то попаду - должен
                // отобразить что фото уже есть у Коммента, но отобразить его не могу.
            }
        }


        private void downloadPhotoInfoById(String stackPhotoDBID, Clicks.clickObjectAndStatus<StackPhotoDB> clickUpdatePhoto) {
            PhotoDownload photoDownloader = new PhotoDownload();

            PhotoTableRequest request = new PhotoTableRequest();
            request.mod = "images_view";
            request.act = "list_image";
            request.nolimit = "1";
            request.id_list = stackPhotoDBID;

            photoDownloader.getPhotoInfoAndSaveItToDB(request, clickUpdatePhoto);
        }


        /**
         * 28.02.23.
         * Получает из TARComments инфу о фотке и возвращает саму фотку.
         */
        private StackPhotoDB getTarCommentPhoto(TARCommentsDB tarCommentsDB) {
            StackPhotoDB res;

            if (tarCommentsDB.photo_hash != null && !tarCommentsDB.photo_hash.equals("") && !tarCommentsDB.photo_hash.equals("0")) {
                res = StackPhotoRealm.getByHash(tarCommentsDB.photo_hash);
            } else if (tarCommentsDB.photo != null && !tarCommentsDB.photo.equals("") && !tarCommentsDB.photo.equals("0")) {
                res = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(tarCommentsDB.getPhoto());
            } else {
                return null;
            }

            return res;
        }


        /**
         * 28.02.23
         * Устанавливаем фото.
         */
        private void setTarCommentPhoto(StackPhotoDB stackPhotoDB) {
            File file = new File(stackPhotoDB.getPhoto_num());
            Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
            if (b != null) {
                photo.setImageBitmap(b);
            }
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_universal_adapter, parent, false);
        return new TaRCommentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
