package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.Detailed;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class PremiumTableDataAdapter extends RecyclerView.Adapter<PremiumTableDataAdapter.PremiumTableHeaderViewHolder> {

    private List<Detailed> data;
    private PremiumTableHeaderAdapter.PremiumListener listener;

    public PremiumTableDataAdapter(List<Detailed> data) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PremiumTableDataAdapter.PremiumTableHeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PremiumTableDataAdapter.PremiumTableHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.premium_table_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PremiumTableDataAdapter.PremiumTableHeaderViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PremiumTableHeaderViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private TextView name;
        private TextView column1, column2, column3, column4;

        public PremiumTableHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.premium_table_item);
            name = itemView.findViewById(R.id.name);
            name.setPaintFlags(name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            column1 = itemView.findViewById(R.id.col1);
            column2 = itemView.findViewById(R.id.col2);
            column3 = itemView.findViewById(R.id.col3);
            column4 = itemView.findViewById(R.id.col4);
        }

        public void bind(Detailed detailed) {

            if ((int) detailed.prihod == 0) {
                column2.setVisibility(View.INVISIBLE);
            } else {
                column2.setVisibility(View.VISIBLE);
            }

            if ((int) detailed.rashod == 0) {
                column3.setVisibility(View.INVISIBLE);
            } else {
                column3.setVisibility(View.VISIBLE);
            }


            CharSequence prihodChar = (int) detailed.prihod < 0 ? Html.fromHtml("<font color=red>" + (int) detailed.prihod + "</font>") : "" + (int) detailed.prihod;
            CharSequence rashodChar = (int) detailed.rashod < 0 ? Html.fromHtml("<font color=red>" + (int) detailed.rashod + "</font>") : "" + (int) detailed.rashod;

            name.setText(detailed.docNom + "(" + detailed.docDat + ")");
            name.setTextColor(-10987432);
            column1.setText("");
            column1.setVisibility(View.GONE);
            column2.setText(prihodChar);
            column3.setText(rashodChar);
            column4.setText("");

            name.setOnClickListener(view -> {
//                Toast.makeText(view.getContext(), "Ви натиснули на " + detailed.docNom + ", він має код дад2: " + detailed.codeDad2, Toast.LENGTH_LONG).show();
                openDoc(detailed.codeDad2);
            });
        }

        private void openDoc(long codeDad2) {
            if (codeDad2 != 0){
                WpDataDB wpDataDB = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowByDad2Id(codeDad2));

                if (wpDataDB != null){
                    long otchetId;
                    int action = wpDataDB.getAction();
                    if (action == 1 || action == 94) {
                        otchetId = wpDataDB.getDoc_num_otchet_id();
                    } else {
                        otchetId = wpDataDB.getDoc_num_1c_id();
                    }

                    String addrTxt;
                    if (wpDataDB.getAddr_txt() != null && !wpDataDB.getAddr_txt().equals("")){
                        addrTxt = wpDataDB.getAddr_txt();
                    }else {
                        AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                        if (addressSDB != null){
                            addrTxt = addressSDB.nm;
                            wpDataDB.setAddr_location_xd(String.valueOf(addressSDB.locationXd));
                            wpDataDB.setAddr_location_yd(String.valueOf(addressSDB.locationYd));
                        }else {
                            addrTxt = "Адресс не определён";
                        }
                        wpDataDB.setAddr_txt(addrTxt);
                        WpDataRealm.setWpData(Collections.singletonList(wpDataDB));
                    }

                    String msg = String.format("Дата: %s\nАдрес: %s\nКлиент: %s\nИсполнитель: %s\n", Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime()/1000), addrTxt, wpDataDB.getClient_txt(), wpDataDB.getUser_txt());

                    DialogData errorMsg = new DialogData(itemView.getContext());
                    errorMsg.setTitle("");
                    errorMsg.setText(itemView.getContext().getString(R.string.re_questioning_wpdata_err_msg));
                    errorMsg.setClose(errorMsg::dismiss);

                    DialogData dialog = new DialogData(itemView.getContext());
                    dialog.setTitle("Открыть посещение?");
                    dialog.setText(msg);
                    dialog.setOk(null, () -> {
                        if (wpDataDB.getTheme_id() == 1182){
                            DialogData dialogQuestionOne = new DialogData(itemView.getContext());
                            dialogQuestionOne.setTitle("");
                            dialogQuestionOne.setText(itemView.getContext().getString(R.string.re_questioning_wpdata_first_msg));
                            dialogQuestionOne.setOk("Да", errorMsg::show);
                            dialogQuestionOne.setCancel("Нет", ()->{
                                DialogData dialogQuestionOTwo = new DialogData(itemView.getContext());
                                dialogQuestionOne.dismiss();
                                dialogQuestionOTwo.setTitle("");
                                dialogQuestionOTwo.setText(itemView.getContext().getString(R.string.re_questioning_wpdata_second_msg));
                                dialogQuestionOTwo.setOk("Да", errorMsg::show);
                                dialogQuestionOTwo.setCancel("Нет", ()->{openReportPrepare(wpDataDB, otchetId);});
                                dialogQuestionOTwo.show();
                            });
                            dialogQuestionOne.show();
                        }else {
                            openReportPrepare(wpDataDB, otchetId);
                        }
                    });
                    dialog.show();

                }else {
                    Toast.makeText(itemView.getContext(), "Звіт не знайдено.", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(itemView.getContext(), "Звіт не знайдено.", Toast.LENGTH_SHORT).show();
            }
        }

        private void openReportPrepare(WpDataDB wp, long otchetId) {
            try {
                WorkPlan workPlan = new WorkPlan();
                Data D = new Data(
                        wp.getId(),
                        wp.getAddr_txt(),
                        wp.getClient_txt(),
                        wp.getUser_txt(),
                        wp.getDt(),  //+TODO CHANGE DATE
                        otchetId,
                        "",
                        R.mipmap.merchik);

                WPDataObj wpDataObj = workPlan.getKPS(wp.getId());

                Intent intent = new Intent(itemView.getContext(), DetailedReportActivity.class);
                intent.putExtra("dataFromWP", D);
                intent.putExtra("rowWP", (Serializable) wp);
                intent.putExtra("dataFromWPObj", wpDataObj);
                itemView.getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(itemView.getContext(), "Помилка: " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*Обработчик кликов по заголовку ПУНКТУ премии*/
    public interface PremiumListener {
        void onClick(View view, Detailed item);
    }
}
