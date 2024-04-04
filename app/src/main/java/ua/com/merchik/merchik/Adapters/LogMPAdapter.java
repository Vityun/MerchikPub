package ua.com.merchik.merchik.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.toolbar_menus;

public class LogMPAdapter extends RecyclerView.Adapter<LogMPAdapter.ViewHolder> {

    private Context context;
    private List<LogMPDB> logMPDBList;

    public LogMPAdapter(Context context, List<LogMPDB> logMPDBList) {
        this.context = context;
        this.logMPDBList = logMPDBList;
    }

    @NonNull
    @Override
    public LogMPAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LogMPAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LogMPAdapter.ViewHolder holder, int position) {
        holder.bind(logMPDBList.get(position));
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

        private TextView textView;
        private ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            layout = itemView.findViewById(R.id.layout);
        }

        public void bind(LogMPDB logMPDB) {
            try {
                textView.setText(coordinateText(logMPDB));
//                textView.setPadding(16, 0, 0, 0);

/*
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,20,0,0);
                textView.setLayoutParams(params);
*/

                layout.setBackgroundResource(R.drawable.border_map);
                layout.setOnClickListener((v)->{
                    // Вывод диалога с МП
                    if (context instanceof toolbar_menus) {
                        ((toolbar_menus) context).dialogMap();
                    }
                });
            }catch (Exception e){
                Globals.writeToMLOG("ERROR", "LogMPAdapter/onBindViewHolder/bind", "Exception e: " + e);
            }
        }

        private StringBuilder coordinateText(LogMPDB logMPDB) {
            StringBuilder res = new StringBuilder();

            if (logMPDB != null){
                res.append(Clock.getHumanTimeSecPattern(logMPDB.CoordTime/1000, "dd.MM HH:mm:ss")).append(" ");  // Время
                res.append(logMPDB.provider == 1 ? "GPS" : "GSM").append(" ");  // Провайдер
                res.append((int) logMPDB.CoordAccuracy).append(" ");    // Точность
                res.append(logMPDB.distance).append("");     // Дистанция от ТТ
            }else {
                res.append("Невизначені координати");
            }

            return res;
        }
    }
}
