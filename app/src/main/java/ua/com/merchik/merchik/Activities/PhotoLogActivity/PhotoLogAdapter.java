package ua.com.merchik.merchik.Activities.PhotoLogActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.dialogs.DialogFullPhoto;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;

/**
 * Начальный Список Журнала фото.
 */
public class PhotoLogAdapter extends RecyclerView.Adapter<PhotoLogAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<StackPhotoDB> photoLogData;
    private List<StackPhotoDB> photoLogDataList;
    private Globals globals = new Globals();
    private int POS;
    private boolean mod;
    private boolean openDefaultStarDialog = true;
    private Clicks.click click;
    private PhotoLogMode photoLogMode;

    // Pika
    // добавил, чтоб получать данные по фото образцов
    private List<SamplePhotoSDB> samplePhotoSDBList;


    private PhotoLogPhotoAdapter.OnPhotoClickListener mOnPhotoClickListener;

    // Pika
    // Добавил в конструктор 2 параметра photoTp,grpId, они нужны для получения данных по образцам фото
    public PhotoLogAdapter(Context context, RealmResults<StackPhotoDB> photoLogData, int photoTp, int grpId, boolean mod, Clicks.click click, PhotoLogPhotoAdapter.OnPhotoClickListener mOnPhotoClickListener) {
        this.mContext = context;
        this.photoLogData = RealmManager.INSTANCE.copyFromRealm(photoLogData);
        this.photoLogDataList = RealmManager.INSTANCE.copyFromRealm(photoLogData);
        this.mod = mod;
        this.click = click;
        this.mOnPhotoClickListener = mOnPhotoClickListener;
        // Pika
        // добавил, чтоб получать данные по фото образцов (а конкретно - нужен комментарий именно к образцу фото, а не к самому фото)
        this.samplePhotoSDBList = SQL_DB.samplePhotoDao().getPhotoLogActiveAndTp(1, photoTp, grpId);
    }

    public void updateData(RealmResults<StackPhotoDB> photoLogData) {
        this.photoLogData = RealmManager.INSTANCE.copyFromRealm(photoLogData);
        this.photoLogDataList = RealmManager.INSTANCE.copyFromRealm(photoLogData);
    }

    /**
     * Установка текущего режима для работы/отображения фотографий в журнале фото
     */
    public void setPhotoLogMode(PhotoLogMode mode) {
        this.photoLogMode = mode;
    }


    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;

        private TextView date;
        private TextView addr;
        private TextView cust;
        private TextView user;
        private TextView typePhoto;

        private TextView tv9;
        private TextView tv10;
        private TextView tv11;
        private TextView tv12;
        private TextView tv13;

        private ImageView imageView, check;

        ViewHolder(View v) {
            super(v);

            layout = v.findViewById(R.id.layout_wp);

            date = (TextView) v.findViewById(R.id.textViewPhotoLogDATE);
            addr = (TextView) v.findViewById(R.id.textViewPhotoLogADDR);
            cust = (TextView) v.findViewById(R.id.textViewPhotoLogCUST);
            user = (TextView) v.findViewById(R.id.textViewPhotoLogUSER);
            typePhoto = (TextView) v.findViewById(R.id.textViewPhotoLogPHOTOTYPE);

//            tv9 = (TextView) v.findViewById(R.id.textView9);
            tv10 = (TextView) v.findViewById(R.id.textView10);
            tv11 = (TextView) v.findViewById(R.id.textView11);
            tv12 = (TextView) v.findViewById(R.id.textView12);
            tv13 = (TextView) v.findViewById(R.id.textView13);

            imageView = (ImageView) v.findViewById(R.id.wp_image1);
            check = v.findViewById(R.id.check);
        }

        @SuppressLint("SimpleDateFormat")
        public void bind(StackPhotoDB photoLogDat) {
            try {
                POS = getAdapterPosition();

                if (photoLogDat.getError() != null && photoLogDat.get_on_server == 0) {
                    layout.setBackgroundColor(mContext.getResources().getColor(R.color.errorLightColor));
                    ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.RED);
                } else {
                    // ВЕРНУТЬ К ИЗНАЧАЛЬНОМУ СОСТОЯНИЮ
                    ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.LTGRAY);
                }

                if (photoLogDat.getUpload_to_server() > 0) {
                    layout.setBackgroundColor(Color.WHITE);
                }

                if (photoLogDat.getUpload_to_server() > 0) {
                    ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.parseColor("#FFBB1F"));   //желтый
                } else {
                    ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.LTGRAY);
                }

                if (photoLogDat.getGet_on_server() > 0) {
                    ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.GREEN);
                } else {
                    ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.LTGRAY);
                }

                Log.e("PhotoLogAdapter", "Id: " + photoLogDat.getId() + "  CodeDad2: " + photoLogDat.getCode_dad2());

                String sd = String.valueOf(photoLogDat.getTime_event());
                String sa = String.valueOf(photoLogDat.getAddr_id());
                String sc = String.valueOf(photoLogDat.getClient_id());

                String s9 = String.valueOf(photoLogDat.getId());
                String s10 = String.format("(%s) %s", s9, photoLogDat.getPhoto_num());

                String userId = String.valueOf(photoLogDat.getUser_id());


                // Нормальное заполнение КЛИЕНТОВ
                StringBuilder customer = new StringBuilder();
                try {
                    customer.append("(").append(sc).append(") ");
                    if (photoLogDat.getCustomerTxt() != null) {
                        customer.append(photoLogDat.getCustomerTxt());
                    } else {
                        customer.append(CustomerRealm.getCustomerById(sc).getNm());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "PhotoLogAdapter.bind.Нормальное заполнение КЛИЕНТОВ", "Exception e: " + e);
                    customer.append("Не удалось определить");
                }


                // Нормальное заполенние АДРЕСОВ
                StringBuilder address = new StringBuilder();
                try {
                    address.append("(").append(sa).append(") ");
                    if (photoLogDat.getAddressTxt() != null) {
                        address.append(photoLogDat.getAddressTxt());
                    } else {
                        address.append(AddressRealm.getAddressById(photoLogDat.getAddr_id()).getNm());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "PhotoLogAdapter.bind.Нормальное заполенние АДРЕСОВ", "Exception e: " + e);
                    address.append("Не удалось определить");
                }

                // Нормальное заполенние ПОЛЬЗОВАТЕЛЕЙ
                StringBuilder merch = new StringBuilder();
                try {
                    merch.append("(").append(userId).append(") ");
                    if (photoLogDat.getUserTxt() != null) {
                        merch.append(photoLogDat.getUserTxt());
                    } else {
                        merch.append(UsersRealm.getUsersDBById(Integer.parseInt(userId)).getNm());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "PhotoLogAdapter.bind.Нормальное заполенние ПОЛЬЗОВАТЕЛЕЙ", "Exception e: " + e);
                    merch.append("Не удалось определить");
                }


                // Нормальное заполенние ТИПОВ ФОТО
                StringBuilder phototype = new StringBuilder();
                try {
                    phototype.append("(").append(photoLogDat.getPhoto_type()).append(") ");
                    if (photoLogDat.getPhoto_typeTxt() != null && !photoLogDat.getPhoto_typeTxt().equals("null")) {
                        phototype.append(photoLogDat.getPhoto_typeTxt());
                    } else {
                        phototype.append(ImagesTypeListRealm.getByID(photoLogDat.getPhoto_type()).getNm());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "PhotoLogAdapter.bind.Нормальное заполенние ПОЛЬЗОВАТЕЛЕЙ", "Exception e: " + e);
                    phototype.append("Не удалось определить");
                }


                if (sd.equals("null")) {
                    if (photoLogDat.getDt() != null) {
                        sd = Clock.getHumanTime3(photoLogDat.getDt());
                    } else {
                        sd = "Не могу определить";
                    }
                }
                if (sd == null) {
                    sd = "Не могу определить";
                }
                date.setText(sd);
                addr.setText(address);
                cust.setText(customer);
                user.setText(merch);
//                typePhoto.setText("(" + photoLogDat.getPhoto_type() + ") " + photoLogDat.getPhoto_typeTxt());
                typePhoto.setText(phototype);


                try {
                    imageView.setImageURI(Uri.parse(photoLogDat.getPhoto_num()));
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "Exception e: " + e);
                    try {
                        File file = new File(photoLogDat.getPhoto_num());
                        Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
                        imageView.setImageBitmap(b);
                    } catch (Exception e1) {
                        Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "Exception e1: " + e1);
                        try {
                            File file = new File(photoLogDat.getPhoto_num());
                            Uri uriFileProvider = FileProvider.getUriForFile(mContext,  "ua.com.merchik.merchik.provider", file);
                            imageView.setImageURI(uriFileProvider);
                        } catch (Exception e2) {
                            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "Exception e2: " + e2);
                        }
                    }
                }






/*            try {
//                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "photoLogDat: " + photoLogDat.getId());
//                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "photoLogDat.getPhoto_num(): " + photoLogDat.getPhoto_num());
                File file = new File(photoLogDat.getPhoto_num());
//                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "file: " + file.length());
                Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
//                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "b: " + b);
                if (b != null) {
                    imageView.setImageBitmap(b);
                } else {
                    imageView.setImageURI(Uri.parse(photoLogDat.getPhoto_num()));
                }
            } catch (Exception e) {
                try {
                    imageView.setImageURI(Uri.parse(photoLogDat.getPhoto_num()));
                } catch (Exception exception) {
                    // TODO cant visualise photo exception
                    Log.e("test", "test");
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "Exception exception: " + exception);
                }
                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/PHOTO_LOG", "Exception e: " + e);
            }*/


                if (photoLogMode.equals(PhotoLogMode.SAMPLE_PHOTO) && openDefaultStarDialog) {
                    if (getBindingAdapterPosition() == 0) {
                        Toast.makeText(mContext, "2222222222222222222222222222", Toast.LENGTH_SHORT).show();
                        openDialog(photoLogMode, photoLogDat, mOnPhotoClickListener, 0);
                        openDefaultStarDialog = false;
                    }
                }

                // 13/08/2020 Выгрузка фоток из Журнала фото
                imageView.setOnClickListener(v -> {
//                    Toast.makeText(mContext, "11111111111111111111111111111111111", Toast.LENGTH_SHORT).show();
                    openDialog(photoLogMode, photoLogDat, mOnPhotoClickListener, getBindingAdapterPosition());
                });

                //04.01.2021 Долгое нажатие - выгрузка фото
                imageView.setOnLongClickListener(v -> {
                    new PhotoLog().sendPhotoOnServer(mContext, photoLogDat, new ExchangeInterface.UploadPhotoReports() {
                        @Override
                        public void onSuccess(StackPhotoDB photoDB, String s) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("photoDB: ").append("{").append(photoDB.getId()).append("|").append(photoDB.getPhotoServerId()).append("}").append("s: ").append(s);

                            Globals.writeToMLOG("INFO", "долгий клик по фото/onSuccess", "" + stringBuilder);
                        }

                        @Override
                        public void onFailure(StackPhotoDB photoDB, String error) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("photoDB: ").append("{").append(photoDB.getId()).append("|").append(photoDB.getPhotoServerId()).append("}").append("error: ").append(error);

                            Globals.writeToMLOG("INFO", "долгий клик по фото/onFailure", "" + stringBuilder);
                        }
                    });
                    Toast.makeText(mContext, "Начинаю выгрузку фото.", Toast.LENGTH_SHORT).show();
                    return true;
                });

                // Работа Журнала фото в зависимости от переданного "photoLogMode"
                if (photoLogMode != null) {
                    switch (photoLogMode) {
                        case PLANOGRAM:
                            if (photoLogDat.getApprove() != null && photoLogDat.getApprove() == 1) {
                                check.setVisibility(View.VISIBLE);

                                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                                check.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
                            } else if (photoLogDat.getApprove() != null && photoLogDat.getApprove() == 0) {
                                check.setVisibility(View.VISIBLE);

                                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_question_circle_regular)); //"?"
                                check.setColorFilter(mContext.getResources().getColor(R.color.red_error));
                            } else {
                                check.setVisibility(View.INVISIBLE);
                            }
                            break;

                        case ACHIEVEMENTS:

                            break;


                        default:
                            check.setVisibility(View.GONE);
                            break;
                    }
                }


                try {
                    if (mod) {
                        layout.setOnClickListener(l -> {
                            if (click != null) {
                                click.click(photoLogDat);
                            }
                        });
                    } else {
                        layout.setOnClickListener(l -> {
                            alertOnlyMassage(mContext, photoData(photoLogDat));
                        });
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "ERROR: " + e, Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                globals.writeToMLOG(Clock.getHumanTime() + "PhotoLogAdapter.bind.ErrorException e: " + e + "\n");
            }
        }

        public void openDialog(PhotoLogMode photoLogMode, StackPhotoDB photoLogDat, PhotoLogPhotoAdapter.OnPhotoClickListener mOnPhotoClickListener, int position) {

            if (photoLogMode.equals(PhotoLogMode.SAMPLE_PHOTO)) {

                // Pika
                // Сделал отображение всех фото образцов одно над другим,
                // а когда позакрываются диалоговые окна фото, будет видео журнал в котором снова можно будет их открыть кликая на фото в журнале
                // при первом открытии покажутся все фото в окнах, потом при выборе определенного фото будет открываться только оно
                if (samplePhotoSDBList != null) {
                    for (SamplePhotoSDB a : samplePhotoSDBList) {
                        StackPhotoDB photo = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(String.valueOf(a.photoId));
                        if (openDefaultStarDialog == true || photoLogDat.photoServerId.equalsIgnoreCase(String.valueOf(a.photoId))) {
                            DialogFullPhotoR dialog = new DialogFullPhotoR(mContext);
                            dialog.setPhoto(photo);
                            dialog.commentOn = true;
                            String commentPhoto = a.about;
                            if (commentPhoto != null && commentPhoto != "") {
                                dialog.setComment(commentPhoto);
                            } else dialog.setComment(photo.getComment());
                            dialog.scaleType(ImageView.ScaleType.FIT_CENTER);
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }
                    }
                }
            } else {

                try {
                    Log.e("setPhotos", "2position: " + getBindingAdapterPosition());
                    Log.e("setPhotos", "2photoLogData: " + photoLogData.get(position).getId());
//коректно
                    DialogFullPhoto dialog = new DialogFullPhoto(mContext);
                    Collections.reverse(photoLogData);

                    dialog.setPhotos(position, photoLogData, mOnPhotoClickListener, dialog::dismiss);

                    dialog.setTextInfo(photoData(photoLogDat));
                    dialog.getComment(photoLogDat.getComment(), () -> {
                        Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "photoLogDat: " + new Gson().toJson(photoLogDat));
                        Globals.writeToMLOG("INFO", "SAVE_PHOTO_COMMENT", "photoLogDat.getComment(): " + photoLogDat.getComment());
                        RealmManager.INSTANCE.executeTransaction(realm -> {
                            photoLogDat.setComment(dialog.commentResult);
                            photoLogDat.setCommentUpload(true);
                        });
                        RealmManager.stackPhotoSavePhoto(photoLogDat);
                        Toast.makeText(mContext, "Комментарий сохранён", Toast.LENGTH_LONG).show();
                    });


                    try {
                        dialog.setTask(photoLogDat.getUser_id(), photoLogDat.getAddr_id(), photoLogDat.getClient_id(), photoLogDat.getCode_dad2(), photoLogDat);
                    } catch (Exception e) {

                    }

                    switch (photoLogMode) {
                        case SAMPLE_PHOTO:
                            dialog.setClose(() -> {
                                click.click(null);
                                dialog.dismiss();
                            });
                            break;

                        default:
                            dialog.setClose(dialog::dismiss);
                            break;
                    }
                    dialog.setRating();
                    dialog.setDvi();
                    dialog.show();
                } catch (Exception e) {
                    Toast.makeText(mContext, "Не получилось открыть фото. Ошибка: " + e, Toast.LENGTH_LONG).show();
                }

            }

        }

    }


    @NonNull
    @Override
    public PhotoLogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.obj_recyclerview_photo_log, viewGroup, false);
        return new PhotoLogAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoLogAdapter.ViewHolder viewHolder, int i) {
        int c = getItemCount() - i - 1; // Отобразить снизу вверх
        StackPhotoDB photoLogDat = photoLogData.get(c);
//        StackPhotoDB photoLogDat = photoLogData.get(i); // Используем прямой индекс
        viewHolder.bind(photoLogDat);
    }

    @Override
    public int getItemCount() {
        try {
            return photoLogData.size();
        } catch (Exception e) {
            return 0;
        }
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<StackPhotoDB> filteredResults = null;

                if (constraint.length() == 0) {
                    filteredResults = photoLogDataList;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter(mContext).getFilteredResultsSP(item, filteredResults, photoLogDataList);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;


                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                photoLogData = (List<StackPhotoDB>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 04.01.2021
     * Формирование строки для отображения подробной информации о фото.
     * <p>
     * Возвращает строку заранее "костыльно" подготовленную для того что б отобразить подробную инфо
     * о текущей фотографии. В будущем надо заменить такое на String.format()
     */
    public static Spanned photoData(StackPhotoDB data) {

        try {
            Globals.writeToMLOG("INFO", "PhotoLogAdapter.photoData", "StackPhotoDB data: " + new Gson().toJson(data));
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "PhotoLogAdapter.photoData", "Exception e: " + e);
        }

        String create = "Не обнаружено", upload = "Не обнаружено", server = "Не обнаружено";

        String timeMls = String.valueOf(data.getErrorTime());
        long l = Long.parseLong(timeMls);
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("HH:mm:ss").format(l);
        if (data.getErrorTime() == 0) {
            time = "-";
            timeMls = "-";
        }

        long createLong = data.getCreate_time();
        long uploadLong = data.getUpload_to_server();
        long serverLong = data.getGet_on_server();

        if (createLong > 0) {
            create = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(createLong);
        }

        if (uploadLong > 0) {
            upload = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(uploadLong);
        }

        if (serverLong > 0) {
            server = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(serverLong);
        }

        Spanned res;
        res = Html.fromHtml("<b>ID: </b>" + data.getId() + " / " + data.photoServerId + "<br>"
                + "<b>Дата: </b>" + data.getTime_event() + "<br>"
                + "<b>Пользователь: </b>" + data.getUserTxt() + "<br>"
                + "<b>Клиент: </b>" + data.getCustomerTxt() + "<br>"
                + "<b>Адрес: </b>" + data.getAddressTxt() + "<br>"
                + "<b>Тип фото: </b>(" + data.getPhoto_type() + ") " + data.getPhoto_typeTxt() + "<br>"
                + "<b>dad2: </b>" + data.getCode_dad2() + "<br>"
                + "<b>hash: </b>" + data.getPhoto_hash() + "<br><br>"
                + "<b>Время создания: </b>" + create + "<br>"
                + "<b>Время отправки: </b><font color='#FFBB1F'>" + upload + "</font><br>"
                + "<b>Время получения: </b><font color='#00FF00'>" + server + "</font><br><br>"
                + "<b>Время ошибки: </b>" + time + "(" + timeMls + ")<br>"
                + "<b>Текст ошибки: </b>" + data.getErrorTxt() + "<br>");

//        return String.format("id: %s\ndate: %s\nuser: %s\nclient: %s\naddress: %s\nphotoType: %s\ndad2: %s\nhash: %s\n\nВремя создания: %s\nВремя отправки: %s\nВремя получения: %s\n\nВремя ошибки: %s(%s)\nТекст ошибки: %s",
//                data.getAddrId(), data.getTime_event(), data.getUserTxt(), data.getCustomerTxt(), data.getAddressTxt(), data.getPhoto_type(), data.getCode_dad2(), data.getPhoto_hash(), create, upload, server, time, timeMls, data.getErrorTxt());
        return res;
    }


    private void setBorderColor(StackPhotoDB data) {

    }


    /**
     * 05.01.2021
     */
    private String getPhotoErrorInfo(StackPhotoDB data) {
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
        return String.format("Время ошибки: %s(%s)\nТекст ошибки: %s", time, data.getErrorTime(), data.getErrorTxt());
    }

    private void alertOnlyMassage(Context context, Spanned msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton("Ок", (dialog, which) -> {
        });
        builder.create().show();
    }


    // ---------------------------------------------------------------------------------------------
}
