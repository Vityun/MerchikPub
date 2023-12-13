package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;


/**
 * 17.03.2021
 * Адаптер предусмотрин для использования в Задачах и Рекламациях для отображения списка
 * задач/рекламаций
 */
public class UniversalAdapter extends RecyclerView.Adapter<UniversalAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<TasksAndReclamationsSDB> data;
    private List<TasksAndReclamationsSDB> dataFilterable;

    Globals.TARInterface onClickListener;

    PhotoDownload photoDownloader = new PhotoDownload();

    public UniversalAdapter(Context context, List<TasksAndReclamationsSDB> data, boolean instantOpen, Globals.TARInterface onClickListener) {
        this.mContext = context;
        Log.e("UniversalAdapter", "data: " + data.size());
        this.data = data;
        this.dataFilterable = data;
        this.onClickListener = onClickListener;

        try {
            if (data.size() == 1 && instantOpen) {
                onClickListener.onSuccess(data.get(0));
            }
        } catch (Exception e) {
            Log.e("UniversalAdapter", "Exception e: " + e);
        }
    }

    public void updateData(List<TasksAndReclamationsSDB> data) {
        this.data = data;
    }


    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout, layoutWp;
        private ImageView photo, status;
        private TextView textLine1, textLine2, textLine3, textLine4, textLine5;

        // Определяем элементы представления
        ViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.universalItem);
            layoutWp = v.findViewById(R.id.layout_wp);
            photo = v.findViewById(R.id.photo);
            status = v.findViewById(R.id.status);
            textLine1 = v.findViewById(R.id.text_line_1);
            textLine2 = v.findViewById(R.id.text_line_2);
            textLine3 = v.findViewById(R.id.text_line_3);
            textLine4 = v.findViewById(R.id.text_line_4);
            textLine5 = v.findViewById(R.id.text_line_5);
        }


        public void bind(TasksAndReclamationsSDB dataItem) {

            try {
                int TARType = dataItem.tp;
                int state = dataItem.state;

                try {
                    Drawable background = layoutWp.getBackground();
                    Drawable photoBG = photo.getBackground();
                    if (state == 0) {
                        if (background instanceof ShapeDrawable) {
                            ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(mContext, R.color.yellow));
                        } else if (background instanceof GradientDrawable) {
                            ((GradientDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.yellow));
                        } else if (background instanceof ColorDrawable) {
                            ((ColorDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.yellow));
                        }
                    } else {
                        if (background instanceof ShapeDrawable) {
                            ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(mContext, R.color.white));
                        } else if (background instanceof GradientDrawable) {
                            ((GradientDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.white));
                        } else if (background instanceof ColorDrawable) {
                            ((ColorDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.white));
                        }
                    }
                    if (photoBG instanceof ShapeDrawable) {
                        ((ShapeDrawable) photoBG).getPaint().setColor(ContextCompat.getColor(mContext, R.color.white));
                    } else if (photoBG instanceof GradientDrawable) {
                        ((GradientDrawable) photoBG).setColor(ContextCompat.getColor(mContext, R.color.white));
                    } else if (photoBG instanceof ColorDrawable) {
                        ((ColorDrawable) photoBG).setColor(ContextCompat.getColor(mContext, R.color.white));
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "UniversalAdapter.bind.Drawable", "Exception e: " + e);
                }

                if (TARType == 0) {     // Рекламации
                    // Pika Для рекламаций (TARType == 0) (правда и других объектов передаваемых при отображении этим универсальным адаптером)
                    // для статуса > 0 (а для рекламаций это исправленные, отмененные и т.д., но не активная - устанавливаю отображение)
                    // кружочка на фото с умолчательного(установленого в ХМЛ-красный знак вопроса) на зеленый кружок типа ОК
                    if (state > 0) {
                        status.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                        status.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
                    }
                } else if (TARType == 1) {  // Задачи
                    if (state == 0) {
                        status.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                        status.setColorFilter(mContext.getResources().getColor(R.color.red_error));
                    }
                    if (state == 1) {
                        status.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                        status.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
                    }
                    if (state == 2) {
                        status.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                        status.setColorFilter(mContext.getResources().getColor(R.color.shadow));
                    }
                    if (state == 3) {
                        status.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_times_circle_regular));
                        status.setColorFilter(mContext.getResources().getColor(R.color.shadow));
                    }
                }


                SpannableStringBuilder line1 = new SpannableStringBuilder();

                CharSequence date = new SimpleDateFormat("dd-MM-yyyy").format(Clock.timeLongToDAte(dataItem.dt)) + " ";
                CharSequence elementId = Html.fromHtml("<b>ID: </b>" + dataItem.id + " ");
                CharSequence icId = Html.fromHtml("<b>1cID: </b>" + dataItem.id1c + " ");
                CharSequence status = TasksAndReclamationsRealm.getStatusTxt(TARType, state);

                line1.append(date);
                line1.append(elementId);
                line1.append(icId);
                line1.append(Html.fromHtml("<b>Состояние: </b>"));
                line1.append(status);
                textLine1.setText(line1);


                SpannableStringBuilder line2 = new SpannableStringBuilder();
                line2.append(Html.fromHtml("<b>Адрес: </b>"));
                if (dataItem.addr != null && !dataItem.addr.equals("")) {
                    try {
                        Log.e("BUG_TAR", "S dataItem.getAddr(): " + dataItem.addr);
                        AddressDB addressDB = AddressRealm.getAddressById(dataItem.addr);
                        CharSequence addr = addressDB.getNm();
                        line2.append(addr);
                        Log.e("BUG_TAR", "E addr: " + addr);
                    } catch (Exception e) {
                        // TODO dataItem is empty
                        line2.append("" + dataItem.addr);
                        Log.e("BUG_TAR", "Exception e: " + dataItem.addr);
                    }
                } else {
                    Log.e("BUG_TAR", "dataItem.getAddr(): " + dataItem.addr);
                    line2.append("" + dataItem.addr);
                }
                Log.e("BUG_TAR", "line2: " + line2);
                textLine2.setText(line2);


                SpannableStringBuilder line3 = new SpannableStringBuilder();
                line3.append(Html.fromHtml("<b>Клиент: </b>"));

                if (dataItem.client != null && !dataItem.client.equals("")) {
                    try {
                        CustomerDB customerDB = CustomerRealm.getCustomerById(String.valueOf(dataItem.client));
                        CharSequence str = customerDB.getNm();
                        line3.append(str);
                    } catch (Exception e) {
                        // TODO dataItem is empty
                        line3.append("" + dataItem.client);
                    }

                } else {
                    line3.append("" + dataItem.client);
                }

                textLine3.setText(line3);


                SpannableStringBuilder line4 = new SpannableStringBuilder();
                line4.append(Html.fromHtml("<b>Ответственнный: </b>"));

                if (dataItem.vinovnik != null && !dataItem.vinovnik.equals("")) {
                    try {
                        UsersDB usersDB = UsersRealm.getUsersDBById(dataItem.vinovnik);
                        CharSequence str = usersDB.getNm();
                        line4.append(str);
                    } catch (Exception e) {
                        // TODO dataItem is empty
                        line4.append("" + dataItem.vinovnik);
                    }

                } else {
                    line4.append("" + dataItem.vinovnik);
                }

                textLine4.setText(line4);

                SpannableStringBuilder line5 = new SpannableStringBuilder();
                try {

                    if (dataItem.tp == 1) {
                        line5.append(Html.fromHtml("<font color='red'>" + dataItem.sumPenalty + "</font>"));
                        line5.append("/");
                        if (dataItem.state == 0) {
                            line5.append(Html.fromHtml("<font color='red'>" + dataItem.sumPremiya + "</font>"));
                        } else {
                            line5.append(Html.fromHtml("<font color='red'>" + 0 + "</font>"));
                        }
                    } else {

                        if (dataItem.state == 0) {
                            line5.append(Html.fromHtml("<font color='red'>" + dataItem.sumPenalty + "</font>"));
                            line5.append("/");
                            line5.append(Html.fromHtml("<font color='red'>-" + dataItem.sumPenalty + "</font>"));
                        } else if (dataItem.state == 1) {
                            line5.append(Html.fromHtml("<font color='red'>" + dataItem.sumPenalty + "</font>"));
                            line5.append("/");
                            line5.append(Html.fromHtml("<font color='red'>" + 0 + "</font>"));
                        }
//                        if (dataItem.state == 1) {
//                            line5.append(Html.fromHtml("<font color='red'>" + dataItem.sumPenalty + "</font>"));
//                        } else {
//                            line5.append(Html.fromHtml("<font color='red'>" + 0 + "</font>"));
//                        }
//                        line5.append("/");
//                        line5.append(Html.fromHtml("<font color='red'>" + dataItem.sumPremiya + "</font>"));
                    }


                    line5.append(" ");

                } catch (Exception e) {

                }

                line5.append(dataItem.comment);
                textLine5.setText(line5);

                layout.setOnClickListener(v -> {
                    onClickListener.onSuccess(dataItem);
                });

                try {
                    StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(String.valueOf(dataItem.photo));

                    Log.e("UniversalAdapter", "stackPhotoDB_1: " + stackPhotoDB);

                    if (stackPhotoDB == null) {
                        stackPhotoDB = StackPhotoRealm.getById(dataItem.photo);
                    }

                    Log.e("UniversalAdapter", "stackPhotoDB_2: " + stackPhotoDB);
                    if (stackPhotoDB != null) {
                        if (stackPhotoDB.getPhoto_num() == null || stackPhotoDB.getPhoto_num().equals("")) {
                            photoDownloader.downloadPhoto(false, stackPhotoDB, "/TAR", new PhotoDownload.downloadPhotoInterface() {
                                @Override
                                public void onSuccess(StackPhotoDB data) {
                                    Log.e("UniversalAdapter", "Загрузка фото. Успех. dataItem: " + data);
                                    photo.setImageURI(Uri.parse(data.getPhoto_num()));
                                }

                                @Override
                                public void onFailure(String s) {
                                    Log.e("UniversalAdapter", "Загрузка фото. Провал.: " + s);
                                }
                            });
                        } else {
                            Log.e("UniversalAdapter", "Фотка уже есть?");
                            photo.setImageURI(Uri.parse(stackPhotoDB.getPhoto_num()));
                        }
                    } else {
                        photo.setImageResource(R.mipmap.merchik);
                    }
                } catch (Exception e) {
                    Log.e("UniversalAdapter", "Exception e: " + e);
                    photo.setImageResource(R.mipmap.merchik);
                }


            } catch (Exception e) {

            }
        }
    }


    @NonNull
    @Override
    public UniversalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_universal_adapter, viewGroup, false);
        return new UniversalAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UniversalAdapter.ViewHolder viewHolder, int i) {
        viewHolder.bind(data.get(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<TasksAndReclamationsSDB> filteredResults = null;

                if (constraint.length() == 0) {
                    filteredResults = dataFilterable;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter(mContext).getFilteredResultsTAR(item, filteredResults, data);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = (List<TasksAndReclamationsSDB>) results.values;
                notifyDataSetChanged();
            }
        };
    }


}
