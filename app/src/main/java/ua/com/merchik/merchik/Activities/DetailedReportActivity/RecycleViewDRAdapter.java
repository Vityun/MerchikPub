package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.OptionsButtons;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class RecycleViewDRAdapter extends RecyclerView.Adapter<RecycleViewDRAdapter.ViewHolder> {

    private List<OptionsDB> butt;
    private List<SiteObjectsSDB> translate;
    private Context mContext;
    private static WpDataDB wpDataDB;


    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        TextView textDescription;
        TextView textTitle;
        TextView textInteger;
        ImageView setCheck;

        ViewHolder(View v) {
            super(v);
            constraintLayout = v.findViewById(R.id.constraintLayout2);
            textDescription = v.findViewById(R.id.textViewDescription);
            textTitle = v.findViewById(R.id.textViewTitle);
            textInteger = v.findViewById(R.id.textViewInteger);
            setCheck = v.findViewById(R.id.imageViewCheck);
            setCheck.setClickable(true);
        }


        //        OptionsDB optionsButtons2 = null;
        public void bind(OptionsDB optionsButtons, SiteObjectsSDB siteObjectsSDB) {
            final int POS = getAdapterPosition();

            Log.e("RViewDRAdapterBind", "optionsButtons: " + optionsButtons);


            String buttText = optionsButtons.getOptionTxt();
            buttText = buttText.replace("&quot;", "\"");
            buttText = buttText.replace("Кнопка ", "");

            if (siteObjectsSDB != null) {
                Log.e("R_TRANSLATES", "siteObjectsSDB.id: " + siteObjectsSDB.id);
                Log.e("R_TRANSLATES", "siteObjectsSDB.commentsTranslation: " + siteObjectsSDB.commentsTranslation);
                buttText = siteObjectsSDB.commentsTranslation;
            }

            Log.e("bindRPA", "=============================================");
            Log.e("bindRPA", "buttText: " + buttText);
            Log.e("bindRPA", "optionsButtons.getOptionId(): " + optionsButtons.getOptionId());
            Log.e("bindRPA", "optionsButtons.getOptionControlId(): " + optionsButtons.getOptionControlId());
            Log.e("bindRPA", "optionsButtons.getIsSignal(): " + optionsButtons.getIsSignal());
            Log.e("bindRPA", "optionsButtons.getBlockPns(): " + optionsButtons.getBlockPns());
            if (optionsButtons.getIsSignal().equals("1") && optionsButtons.getBlockPns().equals("1")) {
                Log.e("bindRPA", "RED");
                textTitle.setText("" + Html.fromHtml("<font color='#FF0000'>" + buttText + "</font>")); // Должно гореть красным
            } else {
                Log.e("bindRPA", "NORM");
                textTitle.setText("" + buttText);
            }


            int optionId = Integer.parseInt(butt.get(getAdapterPosition()).getOptionId());

            if (optionId == 132968) { // Фото витрины)
                textTitle.setTypeface(null, Typeface.BOLD);
            } else {
                textTitle.setTypeface(null, Typeface.NORMAL);
            }

            // 06.08.2020
            // На данный момент опции делаем "нажимными" по id-шникам.
            // TODO заменить на ENUM
            if (optionId == 135809   // Фото витрины ДО начала работ
                    || optionId == 132968   // Фото витрины
                    || optionId == 135158   // Фото Остатков Товаров (ФОТ)
                    || optionId == 132969   // Фото Тележка с Товаром (ФТТ)
                    || optionId == 138518   // Начало работы
                    || optionId == 138520   // Окончание работы
                    || optionId == 138773   // Местоположение
                    || optionId == 137797   // ДеталОтчёт план по товарам
                    || optionId == 138339   // Доп. Требования
                    || optionId == 141360   // Фото товара на складе
                    || optionId == 141910   // Получение заказа в ТТ
                    || optionId == 141888   // Выкуп Товара с ТТ
                    || optionId == 141885   // Фото Документов
                    || optionId == 84007    // ЭКЛ
                    || optionId == 132666   // Стандарт
                    || optionId == 139576   // Версия ПО
                    || optionId == 138767   // Планограмма
            ) {
                constraintLayout.setBackgroundResource(R.drawable.bg_temp);
            } else {
                constraintLayout.setBackgroundResource(R.drawable.button_bg_inactive);
            }

            // Работа с иконками
//            Log.e("bindRPA", "В теории после обновления: " + POS);
//            Log.e("bindRPA", "optionsButtons.getIsSignal(): " + optionsButtons.getIsSignal());


            // color log
            Log.e("ColorLog", "Color.Opt.Id: " + optionsButtons.getID());
            Log.e("ColorLog", "Color.Opt.getOptionTxt: " + optionsButtons.getOptionTxt());
            Log.e("ColorLog", "Color.Opt.getOptionId: " + optionsButtons.getOptionId());
            Log.e("ColorLog", "Color.Opt.getIsSignal: " + optionsButtons.getIsSignal());


            setCheck.setColorFilter(mContext.getResources().getColor(R.color.shadow));
            if (optionsButtons.getIsSignal().equals("1")) {
//                setCheck.setImageResource(R.drawable.red_checkbox);
                setCheck.setImageResource(R.drawable.ic_exclamation_mark_in_a_circle);
                setCheck.setColorFilter(mContext.getResources().getColor(R.color.red_error));
            } else if (optionsButtons.getIsSignal().equals("2")) {
//                setCheck.setImageResource(R.drawable.greeen_checkbox);
                setCheck.setImageResource(R.drawable.ic_check);
                setCheck.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
            } else {
                setCheck.setImageResource(R.drawable.ic_round);
                setCheck.setColorFilter(mContext.getResources().getColor(R.color.shadow));
            }


            try {
                Log.e("RecycleViewDRAdapter", "wpDataDB.getVisit_start_dt(): " + wpDataDB.getVisit_start_dt());
                Log.e("RecycleViewDRAdapter", "wpDataDB.getVisit_end_dt(): " + wpDataDB.getVisit_end_dt());
            } catch (Exception e) {
                e.printStackTrace();
            }


            // =========== СЧЁТЧИК ===========141886
            // todo textInteger.setText(msg); -- могу ли выводить это нормально 1 раз?
            try {
                // Вчтавляем "счётчкик"
                switch (optionId) {
                    // Start Work
                    case (138518):
                        textInteger.setText("" + Clock.getHumanTimeOpt(wpDataDB.getVisit_start_dt() * 1000));
                        break;
                    case (138520):
                        textInteger.setText("" + Clock.getHumanTimeOpt(wpDataDB.getVisit_end_dt() * 1000));
                        break;
                    case (132968):  // Вставляем количество выполненных Фоток Витрин
                        String m = optionsButtons.getAmountMin();
                        if (m.equals("0")) {
                            m = "3";
                        }
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(wpDataDB.getCode_dad2(), 0) + "/" + m);
                        break;
                    case (135809):  // Вставляем количество выполненных Фото витрины ДО начала работ
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(wpDataDB.getCode_dad2(), 14));
                        break;
                    case (135158):  // Вставляем количество выполненных Фото Остатков Товаров (ФОТ)
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(wpDataDB.getCode_dad2(), 4));
                        break;
                    case (132969):  // Вставляем количество выполненных Фото Тележка с Товаром (ФТТ)
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(wpDataDB.getCode_dad2(), 10));
                        break;
                    case (141360):
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(wpDataDB.getCode_dad2(), 31));
                        break;

                    case 137797:    // Остатки
                        String msg = String.format("%s/%s/%s", (int) Options.SKUPlan, (int) Options.SKUFact, (int) Options.OFS);
                        textInteger.setText(msg);
                        break;

                    case 141910:    // "Получение заказа в ТТ"
                        String counter141910 = DetailedReportActivity.rpAmountSum + " шт";
                        textInteger.setText(counter141910);
                        break;

                    case 141888:    // "Выкуп Товара с ТТ"
                        String counter141888 = DetailedReportActivity.rpTotalSumToRedemptionOfGoods + "грн";
                        textInteger.setText(counter141888);
                        break;

                    case 141885:    // Фото Документов
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(wpDataDB.getCode_dad2(), 3));
                        break;

                    case 138339:    // Доп Требования
                        // Устанавливаю в счётчик доп. требований их количество
                        textInteger.setText("" + AdditionalRequirementsRealm.getData3(wpDataDB).size());
                        break;

                    default:
                        textInteger.setText(optionsButtons.getPrice());
                }
            } catch (Exception e) {
                // TODO Вставить обработчик
                e.printStackTrace();
            }


            // Работа с кнопками и нажатиями на них
            final DetailedReportButtons detailedReportButtons = new DetailedReportButtons();

            // Функционал Опций (контроль, NNK..)
            final Options options = new Options();

            constraintLayout.setOnClickListener(view -> {
                Log.e("notifyItemChanged", "CLICK");

                OptionMassageType msgType = new OptionMassageType();
                msgType = options.NNK(mContext, wpDataDB, optionsButtons, msgType, Options.NNKMode.MAKE, () -> {
                    detailedReportButtons.buttonClick(mContext, wpDataDB, butt.get(getAdapterPosition()), 0);
                    setCheck(POS, optionsButtons);
                });

                if (msgType.dialog != null) {
                    msgType.dialog.setDialogIco();
                    msgType.dialog.show();
                }


//                detailedReportButtons.buttonClick(mContext, wpDataDB, butt.get(getAdapterPosition()), 0);
//                setCheck(POS, optionsButtons);
            });

            OptionsDB test = optionsButtons;
            constraintLayout.setOnLongClickListener(view -> {

                optionDetail(test);

                int optId = Integer.parseInt(butt.get(getAdapterPosition()).getOptionId());
                if (optId == 135809
                        || optId == 132968
                        || optId == 135158
                        || optId == 132969
                ) {
                    detailedReportButtons.buttonClick(mContext, wpDataDB, optionsButtons, 1);
                }
                return false;
            });

            setCheck.setOnClickListener(v -> {
                setCheck(POS, optionsButtons);
            });

        }

    }

    /*Нажатие на проверку статуса опции. Нажатие на сигнал*/
    private void setCheck(int POS, OptionsDB optionsButtons) {
        Options options = new Options();
        options.optionControl(mContext, wpDataDB, optionsButtons, null, Options.NNKMode.CHECK_CLICK);

        RealmManager.INSTANCE.executeTransaction(realm -> {
            realm.insertOrUpdate(optionsButtons);
        });

        if (!optionsButtons.getOptionControlId().equals("587")) {
            Toast.makeText(mContext, "Проверка статуса данной опции", Toast.LENGTH_LONG).show();
        }

        updateSignal(POS);
    }

    private void updateSignal(int position) {
        wpDataDB = RealmManager.getWorkPlanRowById(wpDataDB.getId());
        notifyItemChanged(position);
    }


    /*Определяем конструктор*/
    public RecycleViewDRAdapter(Context context, WpDataDB wpDataDB, List<OptionsDB> dataButtons, List<SiteObjectsSDB> list) {
        this.wpDataDB = wpDataDB;
        this.butt = dataButtons;
        this.translate = list;
        this.mContext = context;
    }

    @Override
    public RecycleViewDRAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dr_option_item_button, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(RecycleViewDRAdapter.ViewHolder viewHolder, int position) {
        OptionsDB optionsButtons = butt.get(position);

        SiteObjectsSDB siteObjectsSDB = null;
        if (translate != null && translate.size() > 0) {
            for (SiteObjectsSDB item : translate) {

                Log.e("R_TRANSLATES", "onBindViewHolder.optionsButtons.getOptionId(): " + optionsButtons.getOptionId());
                Log.e("R_TRANSLATES", "onBindViewHolder.item.additionalId: " + item.additionalId);

                if (optionsButtons.getOptionId().equals(String.valueOf(item.additionalId))) {
                    siteObjectsSDB = item;
                    break;
                }
            }
        }

        if (siteObjectsSDB != null) {
            Log.e("R_TRANSLATES", "onBindViewHolder: " + siteObjectsSDB.id);
        } else {
            Log.e("R_TRANSLATES", "onBindViewHolder: " + siteObjectsSDB);
        }


//        OptionsDB opt = RealmManager.getOptionById(optionsButtons.getID());   // Внезапно. Не знаю зачем я это делал, пусть пока меня будет ужасать в будущем
        viewHolder.bind(optionsButtons, siteObjectsSDB);
    }

    @Override
    public int getItemCount() {
        return butt.size();
    }


    /*Активные на данный момент кнопки*/
    private void setEnabledButtons(Button button, OptionsButtons optionsButtons) {
        int optId = optionsButtons.getOptionId();
        if (optId != 135809 && optId != 132968 && optId != 135158 && optId != 132969) {
            button.setEnabled(false);
            button.setVisibility(View.GONE);
        } else {
            button.setEnabled(true);
        }
    }


    private void optionDetail(OptionsDB option) {

        String buttText = option.getOptionTxt();
        buttText = buttText.replace("&quot;", "");
        buttText = buttText.replace("Кнопка ", "");

        DialogData dialog = new DialogData(mContext);
        dialog.setTitle(buttText);
        dialog.setText(option.getOptionControlDescr());
        dialog.setMerchikIco(mContext);
        dialog.show();
    }

}
