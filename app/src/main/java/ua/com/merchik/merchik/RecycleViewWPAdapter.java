package ua.com.merchik.merchik;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class RecycleViewWPAdapter extends RecyclerView.Adapter<RecycleViewWPAdapter.ViewHolder> implements Filterable {

    Globals globals = new Globals();

    private Context mContext;
    private List<WpDataDB> WP;
    private List<WpDataDB> workPlanList;
    private List<WpDataDB> workPlanList2;

    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {

        WorkPlan workPlan = new WorkPlan();
        private View mView;

        ConstraintLayout layoutWp;

        TextView addr;
        TextView cust;
        TextView merc;
        TextView date;
        TextView price;
        TextView theme;
        LinearLayout options = null;
        ImageView wp_image;
        ImageView check;

        ViewHolder(View view) {
            super(view);
            this.mView = view;

            layoutWp = view.findViewById(R.id.layout_wp);

            addr = (TextView) view.findViewById(R.id.addr1);
            cust = (TextView) view.findViewById(R.id.cust1);
            merc = (TextView) view.findViewById(R.id.merc1);
            date = (TextView) view.findViewById(R.id.date1);
            price = (TextView) view.findViewById(R.id.wp_adapter_price);
            theme = view.findViewById(R.id.theme);
            options = (LinearLayout) view.findViewById(R.id.option_signal_layout1);//setContentView
            wp_image = (ImageView) view.findViewById(R.id.wp_image1);

            check = (ImageView) view.findViewById(R.id.check);
        }

        public void bind(WpDataDB wpDataDB) {
            check.setColorFilter(mContext.getResources().getColor(R.color.shadow));
            if (wpDataDB.getStatus() == 1) {
                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                check.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
            } else {
                if (Clock.dateConvertToLong(wpDataDB.getDt()) < System.currentTimeMillis()) {
                    check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                    check.setColorFilter(mContext.getResources().getColor(R.color.red_error));
                } else {
                    check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_round));
                    check.setColorFilter(mContext.getResources().getColor(R.color.shadow));
                }
            }


            long otchetId;
            int action = wpDataDB.getAction();
            if (action == 1 || action == 94) {
                otchetId = wpDataDB.getDoc_num_otchet_id();
            } else {
                otchetId = wpDataDB.getDoc_num_1c_id();
            }

            // Расчёт Факт/Снижение/План
            String t_price = String.format("%s/+%s/%s", 0.00, 0, wpDataDB.getCash_ispolnitel());

            SpannableString string = new SpannableString(t_price);
            string.setSpan(new UnderlineSpan(), 0, string.length(), 0);

            addr.setText(wpDataDB.getAddr_txt());
            cust.setText(wpDataDB.getClient_txt());
            merc.setText(wpDataDB.getUser_txt());
            date.setText(wpDataDB.getDt() + " " + Clock.getHumanTimeOpt(wpDataDB.getDt_start() * 1000));
            price.setText(string);
            price.setMovementMethod(LinkMovementMethod.getInstance());

            try {
                theme.setText(ThemeRealm.getByID(String.valueOf(wpDataDB.getTheme_id())).getNm());
                if (wpDataDB.getTheme_id() != 998){
                    theme.setTextColor(mContext.getResources().getColor(R.color.red_error));
                }else {
                    theme.setTextColor(mContext.getResources().getColor(android.R.color.tab_indicator_text));
                }
            }catch (Exception e){
                // Тема не успела загрузиться
                theme.setText("Тема не обнаружена");
            }


            options.removeAllViews();
            options.addView(workPlan.getOptionLinearLayout(mContext, otchetId));
            wp_image.setImageResource(R.mipmap.merchik);
//            wp_image.setBackgroundColor(Color.BLACK);

            // Слушатель для нажатия на элемент (кпс)
            mView.setOnClickListener(arg0 -> {
                setDialog(wpDataDB, otchetId);
            });

            price.setOnClickListener(v -> {
                setPriceInfo(v.getContext(), wpDataDB);
            });

        }

        private void setPriceInfo(Context context, WpDataDB wp) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("План: ").append(wp.getCash_ispolnitel()).append("\n");
            stringBuilder.append("Снижение: ").append("0").append("\n");
            stringBuilder.append("Факт: ").append(wp.getCash_ispolnitel()).append("\n");

            DialogData dialog = new DialogData(context);
            dialog.setTitle("Расчёт");
            dialog.setText(stringBuilder);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }


        private void setDialog(WpDataDB wp, long otchetId) {
            String msg = String.format("Дата: %s\nАдрес: %s\nКлиент: %s\nИсполнитель: %s\n", wp.getDt(), wp.getAddr_txt(), wp.getClient_txt(), wp.getUser_txt());

            DialogData errorMsg = new DialogData(mContext);
            errorMsg.setTitle("");
            errorMsg.setText(mContext.getString(R.string.re_questioning_wpdata_err_msg));
            errorMsg.setClose(errorMsg::dismiss);

            DialogData dialog = new DialogData(mContext);
            dialog.setTitle("Открыть посещение?");
            dialog.setText(msg);
            dialog.setOk(null, () -> {
                if (wp.getTheme_id() == 1182){
                    DialogData dialogQuestionOne = new DialogData(mContext);
                    dialogQuestionOne.setTitle("");
                    dialogQuestionOne.setText(mContext.getString(R.string.re_questioning_wpdata_first_msg));
                    dialogQuestionOne.setOk("Да", errorMsg::show);
                    dialogQuestionOne.setCancel("Нет", ()->{
                        DialogData dialogQuestionOTwo = new DialogData(mContext);
                        dialogQuestionOne.dismiss();
                        dialogQuestionOTwo.setTitle("");
                        dialogQuestionOTwo.setText(mContext.getString(R.string.re_questioning_wpdata_second_msg));
                        dialogQuestionOTwo.setOk("Да", errorMsg::show);
                        dialogQuestionOTwo.setCancel("Нет", ()->{openReportPrepare(wp, otchetId);});
                        dialogQuestionOTwo.show();
                    });
                    dialogQuestionOne.show();
                }else {
                    openReportPrepare(wp, otchetId);
                }
            });
            dialog.show();
        }

        private void openReportPrepare(WpDataDB wp, long otchetId) {
            try {
                Data D = new Data(
                        wp.getId(),
                        wp.getAddr_txt(),
                        wp.getClient_txt(),
                        wp.getUser_txt(),
                        wp.getDt(),
                        otchetId,
                        "",
                        R.mipmap.merchik);

                WPDataObj wpDataObj = workPlan.getKPS(wp.getId());

                Intent intent = new Intent(mContext, DetailedReportActivity.class);
                intent.putExtra("dataFromWP", D);
                intent.putExtra("rowWP", (Serializable) wp);
                intent.putExtra("dataFromWPObj", wpDataObj);
                mContext.startActivity(intent);
            } catch (Exception e) {
                globals.alertDialogMsg(mContext, "Возникла ошибка. Сообщите о ней своему администратору. Ошибка2: " + e);
            }
        }
    }



    /*Определяем конструктор*/
    public RecycleViewWPAdapter(Context context, RealmResults<WpDataDB> wp) {
        this.mContext = context;
        this.WP = RealmManager.INSTANCE.copyFromRealm(wp);
        this.workPlanList = RealmManager.INSTANCE.copyFromRealm(wp);
        this.workPlanList2 = RealmManager.INSTANCE.copyFromRealm(wp);
    }

    public void updateData(List<WpDataDB> wp) {
        this.WP = RealmManager.INSTANCE.copyFromRealm(wp);
        this.workPlanList = RealmManager.INSTANCE.copyFromRealm(wp);
        this.workPlanList2 = RealmManager.INSTANCE.copyFromRealm(wp);
//        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecycleViewWPAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_workplan_kps, parent, false);
        return new RecycleViewWPAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecycleViewWPAdapter.ViewHolder viewHolder, int position) {
        try {
            WpDataDB wpDataDB = WP.get(position);
            viewHolder.bind(wpDataDB);
        } catch (Exception e) {
            globals.alertDialogMsg(mContext, "Возникла ошибка. Сообщите о ней своему администратору. Ошибка: " + e);
        }

    }

    @Override
    public int getItemCount() {
        try {
            return WP.size();
        } catch (Exception e) {
            return 0;
        }
    }


    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<WpDataDB> filteredResults = null;

                if (constraint.length() == 0) {
                    filteredResults = workPlanList;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter(mContext).getFilteredResultsWP(item, filteredResults, workPlanList);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                WP = (List<WpDataDB>) results.values;

//                Toast toast = Toast.makeText(mContext, "Отобрано: " + WP.size() + " КПС", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();

                notifyDataSetChanged();
            }
        };
    }


//    @Override
//    public Filter getFilter() {
//        return new Filter() {
//            @SuppressWarnings("unchecked")
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                WP = (List<WpDataDB>) results.values;
//                notifyDataSetChanged();
//            }
//
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                List<WpDataDB> filteredResults = null;
////                if (constraint.length() == 0) {
////                    filteredResults = workPlanList;
////                } else {
////                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
////                }
//
//
//                if (constraint.length() == 0) {
//                    Log.e("getFilteredResults", "Я должен быть тут");
//                    filteredResults = workPlanList;
//                } else {
//                    String[] splited = constraint.toString().split("\\s+");
//                    int count = 0;
//                    for (String item : splited) {
//                        Log.e("getFilteredResults", "item(" + count + "): " + item);
//                        if (item != null && !item.equals("")) {
//                            Log.e("getFilteredResults", "Отбор в фильтре будет происходит по строке: '" + item + "' ");
//                            filteredResults = getFilteredResults(item, filteredResults);
//                        }
//                        count++;
//                    }
//                }
//
//
//                FilterResults results = new FilterResults();
//                results.values = filteredResults;
//                return results;
//            }
//        };
//    }
//
//
//    private List<WpDataDB> getFilteredResults(String constraint, List<WpDataDB> list) {
//        List<WpDataDB> results = new ArrayList<>();
//
//        if (list == null) {
//            list = workPlanList;
//        }
//
//
//        for (WpDataDB item : list) {
//
//            // Дата
//            if (item.getDt() != null && !item.getDt().equals("") && item.getDt().toLowerCase().contains(constraint)) {
//                results.add(item);
//            }
//
//            // Адрес
//            else if (item.getAddr_txt() != null && !item.getAddr_txt().equals("") && item.getAddr_txt().toLowerCase().contains(constraint)) {
//                results.add(item);
//            }
//
//            // Клиент
//            else if (item.getClient_txt() != null && !item.getClient_txt().equals("") && item.getClient_txt().toLowerCase().contains(constraint)) {
//                results.add(item);
//            }
//
//            // Пользователь
//            else if (item.getUser_txt() != null && !item.getUser_txt().equals("") && item.getUser_txt().toLowerCase().contains(constraint)) {
//                results.add(item);
//            }
//        }
//
//
////        workPlanList = results;
//        Log.e("getFilteredResults", "return results: " + results.size());
//
//        Toast toast = Toast.makeText(mContext, "Отобрано: " + results.size() + " КПС", Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//
//        return results;
//    }


}
