package ua.com.merchik.merchik.Adapters;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.dialogs.DialogMap;

public class RecyclerAndPhotoAdapter extends RecyclerView.Adapter<RecyclerAndPhotoAdapter.ViewHolder> {

    private Context context;
    private List<LogMPDB> logMPDBList;

    private int address;

    public RecyclerAndPhotoAdapter(Context context, List<LogMPDB> logMPDBList, int address) {
        this.context = context;
        this.logMPDBList = logMPDBList;
        this.address = address;
    }

    @NonNull
    @Override
    public RecyclerAndPhotoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerAndPhotoAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_adapter_and_photo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAndPhotoAdapter.ViewHolder holder, int position) {
        holder.bindLogMPDB(logMPDBList.get(position));
    }

    @Override
    public int getItemCount() {
        try {
            return logMPDBList.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout layout;
        private RecyclerView recyclerView;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recycler_view);
            imageView = itemView.findViewById(R.id.image);
            layout = itemView.findViewById(R.id.layout);
        }

        public void bindLogMPDB(LogMPDB logMPDB) {
            try {
                recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                recyclerView.setAdapter(createStringAdapter(recyclerView.getContext(), getSpannableStringBuilderListFromLogMPDB(logMPDB)));

//                imageView.setImageResource(R.drawable.icons8_google_maps_old);
                imageView.setImageResource(R.drawable.gps);

                recyclerView.setOnClickListener(v -> {
                    openMap(logMPDB);
                });
                layout.setOnClickListener(v -> {
                    openMap(logMPDB);
                });
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "InfoTextAndPhotoAdapter/onBindViewHolder/bind", "Exception e: " + e);
            }
        }

        private RecyclerView.Adapter createStringAdapter(Context context, List<SpannableStringBuilder> dataList) {
            StringAdapter adapter = new StringAdapter(context, dataList);
            return adapter;
        }

        private List<SpannableStringBuilder> getSpannableStringBuilderListFromLogMPDB(LogMPDB logMPDB) {
            List<SpannableStringBuilder> res = new ArrayList<>();

            // Время
            SpannableStringBuilder time = new SpannableStringBuilder();
            time.append("Час:");
            time.setSpan(new StyleSpan(Typeface.BOLD), 0, time.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            time.append(" ").append(Clock.getHumanTimeSecPattern(logMPDB.CoordTime / 1000, "dd.MM HH:mm:ss"));
            res.add(time);

            // Провайдер
            SpannableStringBuilder provider = new SpannableStringBuilder();
            provider.append("Провайдер:");
            provider.setSpan(new StyleSpan(Typeface.BOLD), 0, provider.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            provider.append(" ").append(logMPDB.provider == 1 ? "GPS" : "GSM");
            res.add(provider);

            // Точность
            SpannableStringBuilder accuracy = new SpannableStringBuilder();
            accuracy.append("Точність:");
            accuracy.setSpan(new StyleSpan(Typeface.BOLD), 0, accuracy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            accuracy.append(" ").append(String.valueOf(Math.round(logMPDB.CoordAccuracy)));
            res.add(accuracy);

            // Дистанция от ТТ
            SpannableStringBuilder distance = new SpannableStringBuilder();
            distance.append("Дистанція від ТТ:");
            distance.setSpan(new StyleSpan(Typeface.BOLD), 0, distance.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            distance.append(" ").append(logMPDB.distance > 1000 ? logMPDB.distance / 1000 + "км." : logMPDB.distance + "м.");
            res.add(distance);

            return res;

//            List<TextView> res1 = new ArrayList<TextView>();
//            res1.add(new TextView(context));
//            res1.get(0).setText(time);
//            res1.get(0).setPadding(0,0,0,0);
//            res1.add(new TextView(context));
//            res1.get(1).setText(provider);
//            res1.get(1).setPadding(0,0,0,0);
//            res1.add(new TextView(context));
//            res1.get(2).setText(accuracy);
//            res1.get(2).setPadding(0,0,0,0);
//            res1.add(new TextView(context));
//            res1.get(3).setText(distance);
//            res1.get(3).setPadding(0,0,0,0);
//
//            return res1;

        }

        private void openMap(LogMPDB logMPDB) {
            try {
                AddressSDB addressSDB = SQL_DB.addressDao().getById(address);

                DialogMap dialogMap = new DialogMap((AppCompatActivity) context, "", addressSDB.locationXd, addressSDB.locationYd, "Місцеположення ТТ", logMPDB.CoordX, logMPDB.CoordY, "Ваше місцеположення");
//                dialogMap.updateMap2(addressSDB.locationXd, addressSDB.locationYd, "Місцеположення ТТ", logMPDB.CoordX, logMPDB.CoordY, "Ваше місцеположення");
                dialogMap.setData("", "Місцеположення");
                dialogMap.show();
            } catch (Exception e) {
                Log.e("RecyclerAndPhotoAdapter", "Exception e: " + e);
            }
        }
    }
}
