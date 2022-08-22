package ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;

public class AdditionalMaterialsAdapter extends RecyclerView.Adapter<AdditionalMaterialsAdapter.ViewHolder> {

    private Context context;
    private List<AdditionalMaterialsSDB> data;

    public AdditionalMaterialsAdapter(Context context, List<AdditionalMaterialsSDB> data) {
        this.context = context;
        this.data = data;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView text, number;
        private ImageView signal;
        private ConstraintLayout layout;

        private CharSequence score;

        ViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.text);
            number = v.findViewById(R.id.number);
            signal = v.findViewById(R.id.signal);
            layout = v.findViewById(R.id.layout);
        }

        public void bind(AdditionalMaterialsSDB item) {
            try {
                text.setText(item.txt);

                signal.setColorFilter(context.getResources().getColor(R.color.shadow));

                if (item.score != null) {
                    score = item.score;
                } else {
                    score = Html.fromHtml("<font color='#EF5350'>0</font>");
                    signal.setColorFilter(context.getResources().getColor(R.color.red_error));
                }

                number.setText(score);


                layout.setOnClickListener(v -> {
                    click(v.getContext(), item);
                });

            } catch (Exception e) {
                Log.e("AdditionalRequirements", "ERR: " + e);
            }
        }


        private void click(Context context, AdditionalMaterialsSDB item) {

            Toast.makeText(context, "Получаю доп. материал. Это может занять некоторое время.", Toast.LENGTH_LONG).show();

            Exchange exchange = new Exchange();
            exchange.getAdditionalMaterialsLinks(item.id, new Click() {
                @Override
                public <T> void onSuccess(T data) {
                    DialogData dialogData = new DialogData(context);
                    dialogData.setTitle("Посмотреть Доп. Материал: " + item.id);
                    dialogData.setText("Вы перейдёте по ссылке: " + data);
                    dialogData.setOk("Перейти", ()->{
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.toString()));
                        context.startActivity(browserIntent);
                    });
                    dialogData.show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(context, "Не получилось получить Доп. Материал: " + error, Toast.LENGTH_LONG).show();
                }
            });

        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_additional_requirements, parent, false);
        return new AdditionalMaterialsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        Log.e("AdditionalRequirements", "elementDB: " + data.size());
        return data.size();
    }


}

