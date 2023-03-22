package ua.com.merchik.merchik.Activities.PhotoLogActivity;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.dialogs.DialogFullPhoto;

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
    private Clicks.click click;
    private PhotoLogMode photoLogMode;

    public PhotoLogAdapter(Context context, RealmResults<StackPhotoDB> photoLogData, boolean mod, Clicks.click click) {
        this.mContext = context;
        this.photoLogData = RealmManager.INSTANCE.copyFromRealm(photoLogData);
        this.photoLogDataList = RealmManager.INSTANCE.copyFromRealm(photoLogData);
        this.mod = mod;
        this.click = click;
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
//            try {
            POS = getAdapterPosition();

            if (photoLogDat.getError() != null) {
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
                ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.parseColor("#FFBB1F"));
                if (photoLogDat.getGet_on_server() > 0) {
                    ((GradientDrawable) imageView.getBackground()).setStroke(5, Color.GREEN);
                }
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
                if (photoLogDat.getPhoto_typeTxt() != null) {
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
                File file = new File(photoLogDat.getPhoto_num());
                Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
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
                }
            }


            if (photoLogMode.equals(PhotoLogMode.SAMPLE_PHOTO)) {
                openDialog(photoLogMode, photoLogDat);
            }


            // 13/08/2020 Выгрузка фоток из Журнала фото
            imageView.setOnClickListener(v -> {
                openDialog(photoLogMode, photoLogDat);
            });

            //04.01.2021 Долгое нажатие - выгрузка фото
            imageView.setOnLongClickListener(v -> {
                new PhotoLog().sendPhotoOnServer(mContext, photoLogDat);
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


//            } catch (Exception e) {
//                globals.writeToMLOG(Clock.getHumanTime() + "PhotoLogAdapter.bind.Error: " + Arrays.toString(e.getStackTrace()) + "\n");
//            }
        }

        public void openDialog(PhotoLogMode photoLogMode, StackPhotoDB photoLogDat) {
            try {
                Log.e("setPhotos", "2position: " + getAdapterPosition());
                Log.e("setPhotos", "2photoLogData: " + photoLogData.get(getAdapterPosition()).getId());

                DialogFullPhoto dialog = new DialogFullPhoto(mContext);
                Collections.reverse(photoLogData);
                dialog.setPhotos(getAdapterPosition(), photoLogData);

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
                        });
                        break;

                    default:
                        dialog.setClose(dialog::dismiss);
                        break;
                }
//                dialog.setClose(dialog::dismiss);
                dialog.setRating();
                dialog.setDvi();
                dialog.show();
            } catch (Exception e) {
                Toast.makeText(mContext, "Не получилось открыть фото. Ошибка: " + e, Toast.LENGTH_LONG).show();
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

                Toast toast = Toast.makeText(mContext, "Отобрано: " + photoLogData.size() + " фото", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                notifyDataSetChanged();
            }
        };
//        return new Filter() {
//            @SuppressWarnings("unchecked")
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//
//                Log.e("TAG_SEARCH", "SEARCH(constraint): " + constraint);
//                Log.e("TAG_SEARCH", "SEARCH(results): " + results.values);
//
//                photoLogData = (List<StackPhotoDB>) results.values;
//                Log.e("TAG_SEARCH", "SEARCH(7): " + photoLogData);
//
//                notifyDataSetChanged();
//            }
//
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                List<StackPhotoDB> filteredResults = null;
//                Log.e("TAG_SEARCH", "SEARCH(performFiltering): " + constraint);
//
//                if (constraint.length() == 0) {
//                    Log.e("TAG_SEARCH", "SEARCH(constraint.length() == 0): " + constraint);
//                    filteredResults = photoLogDataList;
//                } else {
//                    Log.e("TAG_SEARCH", "SEARCH(constraint.length() == ): " + constraint.length());
//                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
//                    Log.e("TAG_SEARCH", "SEARCH(filteredResults): " + filteredResults);
//                }
//
//                FilterResults results = new FilterResults();
//                results.values = filteredResults;
//
//                Log.e("TAG_SEARCH", "SEARCH(6): " + results);
//
//
//                return results;
//            }
//        };
    }

    private List<StackPhotoDB> getFilteredResults(String constraint) {
        List<StackPhotoDB> results = new ArrayList<>();

        Log.e("TAG_SEARCH", "SEARCH(2): " + constraint);
        Log.e("TAG_SEARCH", "SEARCH(2.workPlanList): " + photoLogDataList.size());
        Log.e("TAG_SEARCH", "SEARCH(2): " + constraint);

        for (StackPhotoDB item : photoLogDataList) {
            Log.e("TAG_SEARCH", "SEARCH(2) USERTXT: " + item.getUserTxt());
            if (item.getUserTxt().toLowerCase().contains(constraint)) {
                Log.e("TAG_SEARCH", "SEARCH(4.0): " + item.getUserTxt().toLowerCase());
                results.add(item);
            } else if (item.getAddressTxt().toLowerCase().contains(constraint)) {
                Log.e("TAG_SEARCH", "SEARCH(4.1): " + item.getAddressTxt().toLowerCase());
                results.add(item);
            } else if (item.getCustomerTxt().toLowerCase().contains(constraint)) {
                Log.e("TAG_SEARCH", "SEARCH(4.2): " + item.getCustomerTxt().toLowerCase());
                results.add(item);
            }
        }

        Log.e("TAG_SEARCH", "SEARCH(5): " + results);

        return results;
    }

    //----------------------------------------------------------------------------------------------

    private PopupWindow popup;
    private TextView tvDetailed, tvError;

    private void addPopup(Context context, View view, StackPhotoDB photoLogDat) {
        setPopUpWindow(context);
        popup.showAsDropDown(view, 200, -100);

        tvDetailed = popup.getContentView().findViewById(R.id.detailed);
        tvError = popup.getContentView().findViewById(R.id.error);

        tvDetailed.setOnClickListener(v -> {
//            globals.alertDialogMsg(photoData(photoLogDat), context);
            popup.dismiss();
        });

        tvError.setOnClickListener(v -> {
            if (photoLogDat.getError() != null) {
                globals.alertDialogMsg(context, getPhotoErrorInfo(photoLogDat));
            } else {
                Toast.makeText(context, "Ошибок нет", Toast.LENGTH_SHORT).show();
            }
            popup.dismiss();
        });
    }


    /**
     * 04.01.2021
     * Установка в POPUP layout-а
     */
    private void setPopUpWindow(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.photo_log_popup, null);
        popup = new PopupWindow(view, 300, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
    }


    /**
     * 04.01.2021
     * Формирование строки для отображения подробной информации о фото.
     * <p>
     * Возвращает строку заранее "костыльно" подготовленную для того что б отобразить подробную инфо
     * о текущей фотографии. В будущем надо заменить такое на String.format()
     */
    public static Spanned photoData(StackPhotoDB data) {

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
        res = Html.fromHtml("<b>ID: </b>" + data.getId() + "<br>"
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
