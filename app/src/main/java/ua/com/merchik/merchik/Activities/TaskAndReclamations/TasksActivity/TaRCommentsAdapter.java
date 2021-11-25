package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;

public class TaRCommentsAdapter extends RecyclerView.Adapter<TaRCommentsAdapter.ViewHolder>  {

    private Context mContext;
    private List<TARCommentsDB> data;

    public TaRCommentsAdapter(Context mContext, List<TARCommentsDB> data) {
        this.mContext = mContext;
        this.data = data;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
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
                }catch (Exception e){
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
            try {
//                StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(dataItem.getPhoto());
                StackPhotoDB stackPhotoDB = StackPhotoRealm.getByHash(dataItem.photo_hash);
                if (stackPhotoDB != null){
                    if (stackPhotoDB.getPhoto_num().equals("")){
                        new PhotoDownload().downloadPhoto(false, stackPhotoDB, "/TAR", new PhotoDownload.downloadPhotoInterface() {
                            @Override
                            public void onSuccess(StackPhotoDB data) {
                                photo.setImageURI(Uri.parse(data.getPhoto_num()));
                            }

                            @Override
                            public void onFailure(String s) {

                            }
                        });
                    }else {
                        photo.setImageURI(Uri.parse(stackPhotoDB.getPhoto_num()));
                    }
                }
            }catch (Exception e){
//                Toast.makeText(mContext, "Не удалось отобразить фото", Toast.LENGTH_SHORT).show();
            }


            // Клик по элементу
            layout.setOnClickListener(v->{
                DialogData dialog = new DialogData(mContext);
                dialog.setTitle("");
                dialog.setText(Html.fromHtml("<b>Текст комментария: </b>" + dataItem.getComment()));
                dialog.setClose(dialog::dismiss);
                dialog.show();
            });
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
