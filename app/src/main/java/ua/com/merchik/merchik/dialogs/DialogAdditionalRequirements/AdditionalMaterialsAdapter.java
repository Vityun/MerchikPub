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

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;

public class AdditionalMaterialsAdapter extends RecyclerView.Adapter<AdditionalMaterialsAdapter.ViewHolder> {

    private Context context;
    private List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data;
    private WpDataDB wpDataDB;

    public AdditionalMaterialsAdapter(Context context, List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data, WpDataDB wp) {
        this.context = context;
        this.data = data;
        this.wpDataDB = wp;
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

        public void bind(AdditionalMaterialsJOINAdditionalMaterialsAddressSDB item) {
            try {
                text.setText(item.txt);

                long dateDocumentLong = Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000));
                long dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000;
                AdditionalRequirementsMarkDB additionalRequirementsMarkDB = AdditionalRequirementsMarkRealm.getMark(dateFrom, item.id, String.valueOf(Globals.userId));

//                Gson gson = new Gson();
//                String json = gson.toJson(additionalRequirementsMarkDB);
//                JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//
//                Log.e("AdRequirementsMark", "convertedObject: " + convertedObject);


                signal.setColorFilter(context.getResources().getColor(R.color.shadow));

                if (additionalRequirementsMarkDB != null) {
                    score = additionalRequirementsMarkDB.getScore();
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


        private void click(Context context, AdditionalMaterialsJOINAdditionalMaterialsAddressSDB item) {

            Toast.makeText(context, "Получаю доп. материал. Это может занять некоторое время.", Toast.LENGTH_LONG).show();

            Exchange exchange = new Exchange();
            exchange.getAdditionalMaterialsLinks(item.id, new Click() {
                @Override
                public <T> void onSuccess(T data) {

                    DialogARMark dialog = new DialogARMark(context);

                    dialog.setClose(dialog::dismiss);
                    dialog.setLesson(context, true, 1234);
                    dialog.setVideoLesson(context, true, 1235, () -> {});

                    dialog.setTitle("Посмотреть Доп. Материал: " + item.id);
                    dialog.setTxtText("Вы перейдёте по ссылке: " + data);

                    dialog.setRatingBarAM(item, Float.parseFloat(String.valueOf(score)), () -> {
                        notifyItemChanged(getAdapterPosition());
                    });

                    dialog.setOk("Перейти", ()->{
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.toString()));
                        context.startActivity(browserIntent);
                    });

                    dialog.show();


//                    DialogData dialogData = new DialogData(context);
//                    dialogData.setTitle("Посмотреть Доп. Материал: " + item.id);
//                    dialogData.setText("Вы перейдёте по ссылке: " + data);
//                    dialogData.setOk("Перейти", ()->{
//                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.toString()));
//                        context.startActivity(browserIntent);
//                    });
//                    dialogData.show();
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

