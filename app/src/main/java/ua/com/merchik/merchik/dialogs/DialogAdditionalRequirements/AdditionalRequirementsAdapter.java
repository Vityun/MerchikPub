package ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark;
import ua.com.merchik.merchik.dialogs.DialogData;

public class AdditionalRequirementsAdapter extends RecyclerView.Adapter<AdditionalRequirementsAdapter.ViewHolder> {

    private Context context;
    private List<AdditionalRequirementsDB> data;
    private WpDataDB wpDataDB;

    public AdditionalRequirementsAdapter(){

    }

    public AdditionalRequirementsAdapter(Context context, List<AdditionalRequirementsDB> data, WpDataDB wp) {
        this.context = context;
        this.data = data;
        this.wpDataDB = wp;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView text, number, tsumm;
        private ImageView signal;
        private ConstraintLayout layout;

        private CharSequence score;

        ViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.text);
            number = v.findViewById(R.id.number);
            signal = v.findViewById(R.id.signal);
            layout = v.findViewById(R.id.layout);
            tsumm = v.findViewById(R.id.summ);
        }

        public void bind(AdditionalRequirementsDB elementDB) {
            try {
                Log.e("AdditionalRequirements", "elementDB: " + elementDB.getId());

                if (elementDB.color != null && !elementDB.color.equals("")) {
                    int color = Color.parseColor("#" + elementDB.color);
                    Drawable coloredBackground = new ColorDrawable(color);
                    layout.setBackground(coloredBackground);
                }

                TradeMarkDB tradeMarkDB = null;
                TovarDB tovarDB = TovarRealm.getById(elementDB.getTovarId());

                StringBuilder additionalText = new StringBuilder();
                additionalText.append(elementDB.getNotes());

                if (tovarDB != null){
                    tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(tovarDB.getManufacturerId());
                    if (tradeMarkDB != null){
                        additionalText.append("\n");
                        additionalText.append(createTovarText(tovarDB, tradeMarkDB));
                    }
                }

                text.setText(additionalText);

                long dateDocumentLong = Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000));
                long dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000;
                AdditionalRequirementsMarkDB additionalRequirementsMarkDB = AdditionalRequirementsMarkRealm.getMark(dateFrom, elementDB.getId(), String.valueOf(Globals.userId));

                signal.setColorFilter(context.getResources().getColor(R.color.shadow));

                if (additionalRequirementsMarkDB != null) {
                    score = additionalRequirementsMarkDB.getScore();
                } else {
                    score = Html.fromHtml("<font color='#EF5350'>0</font>");
                    signal.setColorFilter(context.getResources().getColor(R.color.red_error));
                }

                number.setText(score);

                // Pika - показать сумму за доптребование
                String sums = elementDB.getSumm();
                if (sums.compareTo("0") != 0) {
                    tsumm.setText(sums);
                    tsumm.setTextColor(context.getResources().getColor(R.color.red_error));
                } else { tsumm.setText(""); }
                // ----

                TradeMarkDB finalTradeMarkDB = tradeMarkDB;
                layout.setOnClickListener(v -> {
                    click(v.getContext(), elementDB, tovarDB, finalTradeMarkDB);
                });

            } catch (Exception e) {
                Log.e("AdditionalRequirements", "ERR: " + e);
            }
        }


        public void click(Context context, AdditionalRequirementsDB data, TovarDB tovarDB, TradeMarkDB tradeMarkDB) {
            DialogARMark dialog = new DialogARMark(context);

            dialog.setTitle("Доп. Требование (" + data.getId() + "/" + data.getSiteId() + ")");

            StringBuilder bId = new StringBuilder();
            String id = "<b>Идентификатор: </b>";
            bId.append(id);
            try {
                String id2 = String.format("%s", data.getId());
                bId.append(id2);
            } catch (Exception e) {
            }
            CharSequence endbId = Html.fromHtml(bId.toString());


            StringBuilder bAddr = new StringBuilder();
            String addr = "<b>Адрес: </b>";
            bAddr.append(addr);
            try {
                String addr2 = String.format("%s", AddressRealm.getAddressById(data.getId()).getNm());
                bAddr.append(addr2);
            } catch (Exception e) {
                bAddr.append("Для всех адресов");
            }
            CharSequence endbAddr = Html.fromHtml(bAddr.toString());


            StringBuilder bGrp = new StringBuilder();
            String grp = "<b>Сеть: </b>";
            bGrp.append(grp);
            try {
                Log.e("AdditionalRequirements", "Сеть: " + data.getGrpId());
                if (data.getGrpId().equals("0")) {
                    bGrp.append("Для всех сетей");
                } else {
                    String grp2 = String.format("%s", data.getGrpId());
                    bGrp.append(grp2);
                }
            } catch (Exception e) {
                bGrp.append("Для всех сетей");
            }
            CharSequence endbGrp = Html.fromHtml(bGrp.toString());


            StringBuilder bNum = new StringBuilder();
            String number = "<b>Номер: </b>";
            bNum.append(number);
            try {
                String number2 = String.format("%s", data.getSiteId());
                bNum.append(number2);
            } catch (Exception e) {
            }
            CharSequence endbNum = Html.fromHtml(bNum.toString());


            StringBuilder bStart = new StringBuilder();
            String dStart = "<b>Дата начала: </b>";
            bStart.append(dStart);
            try {
//                Log.e("AdditionalRequirements", "Дата начала: " + data.getDtStart());
//                if (data.getDtStart().equals("0000-00-00")) {
//                    bStart.append("Не определена");
//                } else {
                    String dStart2 = String.format("%s", data.dtStart);
                    bStart.append(dStart2);
//                }
            } catch (Exception e) {
                bStart.append("Не определена");
            }
            CharSequence endbStart = Html.fromHtml(bStart.toString());


            StringBuilder bEnd = new StringBuilder();
            String dEnd = "<b>Дата окончания: </b>";
            bEnd.append(dEnd);
            try {
//                Log.e("AdditionalRequirements", "Дата окончания: " + data.getDtEnd());
//                if (data.getDtEnd().equals("0000-00-00")) {
//                    bEnd.append("Не определена");
//                } else {
                    String dEnd2 = String.format("%s", data.dtEnd);
                    bEnd.append(dEnd2);
//                }
            } catch (Exception e) {
                bEnd.append("Не определена");
            }
            CharSequence endbEnd = Html.fromHtml(bEnd.toString());

            StringBuilder bAuthor = new StringBuilder();
            String author = "<b>Автор: </b>";
            bAuthor.append(author);
            try {
                Log.e("AdditionalRequirements", "Автор: " + data.getAuthorId());
                String author2 = String.format("%s", UsersRealm.getUsersDBById(Integer.parseInt(data.getAuthorId())).getNm());
                bAuthor.append(author2);
            } catch (Exception e) {
                bAuthor.append("Автор не определён");
            }
            CharSequence endbAuthor = Html.fromHtml(bAuthor.toString());


            StringBuilder bCust = new StringBuilder();
            String customer = "<b>Клиент: </b>";
            bCust.append(customer);
            try {
                String customer2 = String.format("%s", CustomerRealm.getCustomerById(data.getClientId()).getNm());
                bCust.append(customer2);
            } catch (Exception e) {
            }
            CharSequence endbCust = Html.fromHtml(bCust.toString());


            StringBuilder bMark = new StringBuilder();
            String mark = "<b>Оценка: </b>";
            bMark.append(mark);
            try {
                String mark2 = String.format("%s", "");
                bMark.append(mark2);
            } catch (Exception e) {
            }
            CharSequence endbMark = Html.fromHtml(bMark.toString());


            StringBuilder bText = new StringBuilder();
            String text = "<b>Текст: </b>";
            bText.append(text);
            try {
                String text2 = String.format("%s", data.getNotes());
                bText.append(text2);


            } catch (Exception e) {
            }

            /*Добавляю Товары*/
            try {
                bText.append("\n").append("<b>Товар: </b>");
                bText.append(createTovarText(tovarDB, tradeMarkDB));
            } catch (Exception e) {
                e.printStackTrace();
            }

            CharSequence endbText = Html.fromHtml(bText.toString());


            dialog.setData(endbId, endbAddr, endbGrp, endbNum, endbStart, endbEnd, endbAuthor, endbCust, endbMark, endbText);


            dialog.setRatingBarAR(data, Float.parseFloat(String.valueOf(score)), () -> {
                notifyItemChanged(getAdapterPosition());
            });

            dialog.setClose(dialog::dismiss);
            dialog.setLesson(context, true, 1234);
            dialog.setVideoLesson(context, true, 1235, () -> {
            });

            dialog.show();
        }

        private StringBuilder createTovarText(TovarDB tovarDB, TradeMarkDB tradeMarkDB){
            return new StringBuilder().append(tovarDB.getNm())
                    .append(", ").append(tovarDB.getWeight())
                    .append(", ").append(tradeMarkDB.getNm())
                    .append(", ").append(tovarDB.getBarcode());
        }


        /**
         * 14.04.2021
         * Клик по Доп.Тредованию
         */
        private void layoutClick(Context context, AdditionalRequirementsDB data) {
            DialogData dialog = new DialogData(context);

            dialog.setTitle("Доп. Требование(" + data.getId() + ")");


            StringBuilder builder = new StringBuilder();

            try {
                String id = String.format("<b>Идентификатор: </b>%s<br>", data.getId());
                builder.append(id);
            } catch (Exception e) {
            }

            try {
                String addTxt = "<b>Адрес: </b>";
                String addres = String.format("%s%s<br>", addTxt, AddressRealm.getAddressById(data.getId()).getNm());
                builder.append(addres);
            } catch (Exception e) {
            }

            try {
                String test = String.format("<b>Сеть: </b>%s<br>", data.getGrpId());
                builder.append(test);
            } catch (Exception e) {
            }

            try {
                String number = String.format("<b>Номер: </b>%s<br>", data.getSiteId());
                builder.append(number);
            } catch (Exception e) {
            }

            try {
                String dtStart = String.format("<b>Дата начала: </b>%s<br>", data.dtStart);
                builder.append(dtStart);
            } catch (Exception e) {
            }

            try {
                String dtEnd = String.format("<b>Дата окончания: </b>%s<br>", data.dtEnd);
                builder.append(dtEnd);
            } catch (Exception e) {
            }

            try {
                String author = String.format("<b>Автор: </b>%s<br>", UsersRealm.getUsersDBById(Integer.parseInt(data.getAuthorId())).getNm());
                builder.append(author);
            } catch (Exception e) {
            }

            try {
                String customer = String.format("<b>Клиент: </b>%s<br>", CustomerRealm.getCustomerById(data.getClientId()).getNm());
                builder.append(customer);
            } catch (Exception e) {
            }

            try {
                String mark = String.format("<b>Оценка: </b>%s<br>", "---");
                builder.append(mark);
            } catch (Exception e) {
            }

            try {
                String txt = String.format("<b>Текст: </b>%s<br>", data.getNotes());
                builder.append(txt);
            } catch (Exception e) {
            }

            CharSequence msg = Html.fromHtml(builder.toString());

            dialog.setText(msg);

            dialog.setClose(dialog::dismiss);
            dialog.show();
        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_additional_requirements, parent, false);
        return new AdditionalRequirementsAdapter.ViewHolder(v);
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



    // ШОК, УЖАС, ТЬМА !!!

    public void click(Context context, AdditionalRequirementsDB data, TovarDB tovarDB, TradeMarkDB tradeMarkDB) {
        DialogARMark dialog = new DialogARMark(context);

        dialog.setTitle("Доп. Требование (" + data.getId() + "/" + data.getSiteId() + ")");

        StringBuilder bId = new StringBuilder();
        String id = "<b>Идентификатор: </b>";
        bId.append(id);
        try {
            String id2 = String.format("%s", data.getId());
            bId.append(id2);
        } catch (Exception e) {
        }
        CharSequence endbId = Html.fromHtml(bId.toString());


        StringBuilder bAddr = new StringBuilder();
        String addr = "<b>Адрес: </b>";
        bAddr.append(addr);
        try {
            String addr2 = String.format("%s", AddressRealm.getAddressById(data.getId()).getNm());
            bAddr.append(addr2);
        } catch (Exception e) {
            bAddr.append("Для всех адресов");
        }
        CharSequence endbAddr = Html.fromHtml(bAddr.toString());


        StringBuilder bGrp = new StringBuilder();
        String grp = "<b>Сеть: </b>";
        bGrp.append(grp);
        try {
            Log.e("AdditionalRequirements", "Сеть: " + data.getGrpId());
            if (data.getGrpId().equals("0")) {
                bGrp.append("Для всех сетей");
            } else {
                String grp2 = String.format("%s", data.getGrpId());
                bGrp.append(grp2);
            }
        } catch (Exception e) {
            bGrp.append("Для всех сетей");
        }
        CharSequence endbGrp = Html.fromHtml(bGrp.toString());


        StringBuilder bNum = new StringBuilder();
        String number = "<b>Номер: </b>";
        bNum.append(number);
        try {
            String number2 = String.format("%s", data.getSiteId());
            bNum.append(number2);
        } catch (Exception e) {
        }
        CharSequence endbNum = Html.fromHtml(bNum.toString());


        StringBuilder bStart = new StringBuilder();
        String dStart = "<b>Дата начала: </b>";
        bStart.append(dStart);
        try {
//            Log.e("AdditionalRequirements", "Дата начала: " + data.getDtStart());
//            if (data.getDtStart().equals("0000-00-00")) {
//                bStart.append("Не определена");
//            } else {
                String dStart2 = String.format("%s", data.dtStart);
                bStart.append(dStart2);
//            }
        } catch (Exception e) {
            bStart.append("Не определена");
        }
        CharSequence endbStart = Html.fromHtml(bStart.toString());


        StringBuilder bEnd = new StringBuilder();
        String dEnd = "<b>Дата окончания: </b>";
        bEnd.append(dEnd);
        try {
//            Log.e("AdditionalRequirements", "Дата окончания: " + data.getDtEnd());
//            if (data.getDtEnd().equals("0000-00-00")) {
//                bEnd.append("Не определена");
//            } else {
                String dEnd2 = String.format("%s", data.dtEnd);
                bEnd.append(dEnd2);
//            }
        } catch (Exception e) {
            bEnd.append("Не определена");
        }
        CharSequence endbEnd = Html.fromHtml(bEnd.toString());

        StringBuilder bAuthor = new StringBuilder();
        String author = "<b>Автор: </b>";
        bAuthor.append(author);
        try {
            Log.e("AdditionalRequirements", "Автор: " + data.getAuthorId());
            String author2 = String.format("%s", UsersRealm.getUsersDBById(Integer.parseInt(data.getAuthorId())).getNm());
            bAuthor.append(author2);
        } catch (Exception e) {
            bAuthor.append("Автор не определён");
        }
        CharSequence endbAuthor = Html.fromHtml(bAuthor.toString());


        StringBuilder bCust = new StringBuilder();
        String customer = "<b>Клиент: </b>";
        bCust.append(customer);
        try {
            String customer2 = String.format("%s", CustomerRealm.getCustomerById(data.getClientId()).getNm());
            bCust.append(customer2);
        } catch (Exception e) {
        }
        CharSequence endbCust = Html.fromHtml(bCust.toString());


        StringBuilder bMark = new StringBuilder();
        String mark = "<b>Оценка: </b>";
        bMark.append(mark);
        try {
            String mark2 = String.format("%s", "");
            bMark.append(mark2);
        } catch (Exception e) {
        }
        CharSequence endbMark = Html.fromHtml(bMark.toString());


        StringBuilder bText = new StringBuilder();
        String text = "<b>Текст: </b>";
        bText.append(text);
        try {
            String text2 = String.format("%s", data.getNotes());
            bText.append(text2);


        } catch (Exception e) {
        }

        /*Добавляю Товары*/
        try {
            bText.append("\n").append("<b>Товар: </b>");
            bText.append(createTovarText(tovarDB, tradeMarkDB));
        } catch (Exception e) {
            e.printStackTrace();
        }

        CharSequence endbText = Html.fromHtml(bText.toString());


        dialog.setData(endbId, endbAddr, endbGrp, endbNum, endbStart, endbEnd, endbAuthor, endbCust, endbMark, endbText);

        dialog.setClose(dialog::dismiss);
        dialog.setLesson(context, true, 1234);
        dialog.setVideoLesson(context, true, 1235, () -> {
        });

        dialog.show();
    }

    private StringBuilder createTovarText(TovarDB tovarDB, TradeMarkDB tradeMarkDB){
        return new StringBuilder().append(tovarDB.getNm())
                .append(", ").append(tovarDB.getWeight())
                .append(", ").append(tradeMarkDB.getNm())
                .append(", ").append(tovarDB.getBarcode());
    }


}
