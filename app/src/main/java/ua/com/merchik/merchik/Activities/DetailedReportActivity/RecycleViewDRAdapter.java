package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
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

import java.util.Calendar;
import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Options.Controls.OptionControlReclamationAnswer;
import ua.com.merchik.merchik.Options.Controls.OptionControlTaskAnswer;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.OptionsButtons;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class RecycleViewDRAdapter<T> extends RecyclerView.Adapter<RecycleViewDRAdapter.ViewHolder> {

    private List<OptionsDB> butt;
    private List<SiteObjectsSDB> translate;
    private Context mContext;
    //    private static WpDataDB wpDataDB;
    private T dataDB;

    long dad2, startDt, endDt;


//    public void blinkItem() {
//        Timer timer = new Timer();
//        timer.schedule(new BlinkTask(), 1000, 1000);
//    }

//    private class BlinkTask extends TimerTask {
//        private boolean blink = true;
//
//        public BlinkTask() {
//        }
//
//        public void run() {
//            if (blink){
//
//                constraintLayout.setBackgroundResource(R.color.greenCol);
//                blink = false;
//            }else {
//                constraintLayout.setBackgroundResource(R.drawable.bg_temp);
//                blink = true;
//            }
//        }
//    }


    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        TextView textDescription;
        TextView textTitle;
        TextView textInteger, textInteger2;
        ImageView setCheck;

        ViewHolder(View v) {
            super(v);
            constraintLayout = v.findViewById(R.id.constraintLayout2);
            textDescription = v.findViewById(R.id.textViewDescription);
            textTitle = v.findViewById(R.id.textViewTitle);
            textInteger = v.findViewById(R.id.textViewInteger);
            textInteger2 = v.findViewById(R.id.textViewInteger2);
            setCheck = v.findViewById(R.id.imageViewCheck);
            setCheck.setClickable(true);
        }

        public void bind(OptionsDB optionsButtons, SiteObjectsSDB siteObjectsSDB) {
            final int POS = getAdapterPosition();
            boolean describedOption = true;

            Log.e("RViewDRAdapterBind", "optionsButtons: " + optionsButtons);

            textInteger.setVisibility(View.VISIBLE);

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

            // Подсвечивает Кнопки Опций с блоком ПНС() КРАСНЫМ цветом
            if (optionsButtons.getIsSignal().equals("1") && optionsButtons.getBlockPns().equals("1")) {
                Log.e("bindRPA", "RED");
                textTitle.setText("" + Html.fromHtml("<font color='#FF0000'>" + buttText + "</font>")); // Должно гореть красным
            } else {
                Log.e("bindRPA", "NORM");
                textTitle.setText("" + buttText);
            }

            int optionId = Integer.parseInt(butt.get(getAdapterPosition()).getOptionId());

            // Выделяет жирным ОСОБЕННЫЕ Кнопки Опций
            if (optionId == 132968) { // Фото витрины)
                textTitle.setTypeface(null, Typeface.BOLD);
            } else {
                textTitle.setTypeface(null, Typeface.NORMAL);
            }

            // 06.08.2020
            // На данный момент опции делаем "нажимными" по id-шникам.
            // ОПИСАННЫЕ Кнопки Опций
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
                    || optionId == 135742   // "Дет.Отчет" (по Клиенто-Адресу)
                    || optionId == 132621   // Оценка
                    || optionId == 84003    // Мнение о сотруднике
                    || optionId == 138340   // Доп. Материалы
                    || optionId == 135327   // Задача
                    || optionId == 135328   // Рекламация
                    || optionId == 156882   // Акции
                    || optionId == 151139   // Фото планограммы
                    || optionId == 132623   // Комментарий
                    || optionId == 133382   // Потенциальный клиент
                    || optionId == 157275   // 1.
                    || optionId == 157276   // 2. Две опции контроля тут на всяк случай. Тестим.
                    || optionId == 157274   // 3. ..три
            ) {
                constraintLayout.setBackgroundResource(R.drawable.bg_temp);
                textInteger2.setVisibility(View.VISIBLE);
                if (optionsButtons.getIsSignal().equals("1") && !optionsButtons.getBlockPns().equals("1")) {
                    textInteger2.setText(counter2Text());
                } else {
                    if (optionId == 133382) {
                        textInteger2.setVisibility(View.VISIBLE);
                        textInteger2.setText("+1100 грн.");
                    }else {
                        textInteger2.setVisibility(View.GONE);
                    }
                }
            } else {
                describedOption = false;
                textInteger2.setVisibility(View.GONE);
                constraintLayout.setBackgroundResource(R.drawable.button_bg_inactive);
            }


            // color log
            Log.e("ColorLog", "Color.Opt.Id: " + optionsButtons.getID());
            Log.e("ColorLog", "Color.Opt.getOptionTxt: " + optionsButtons.getOptionTxt());
            Log.e("ColorLog", "Color.Opt.getOptionId: " + optionsButtons.getOptionId());
            Log.e("ColorLog", "Color.Opt.getIsSignal: " + optionsButtons.getIsSignal());


            // Определения цвета которым будет гореть СИГНАЛ
            setCheck.setColorFilter(mContext.getResources().getColor(R.color.shadow));
            if (describedOption) {
                setCheck.setVisibility(View.VISIBLE);
                if (optionsButtons.getIsSignal().equals("1")) {
//                setCheck.setImageResource(R.drawable.red_checkbox);
                    setCheck.setImageResource(R.drawable.ic_exclamation_mark_in_a_circle);
                    setCheck.setColorFilter(mContext.getResources().getColor(R.color.red_error));
                } else if (optionsButtons.getIsSignal().equals("2")) {
//                setCheck.setImageResource(R.drawable.greeen_checkbox);
                    setCheck.setImageResource(R.drawable.ic_check);
                    setCheck.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
                } else {
                    if (optionsButtons.getOptionControlId().equals("0")) {
                        setCheck.setVisibility(View.INVISIBLE);
                    } else {
                        setCheck.setImageResource(R.drawable.ic_round);
                        setCheck.setColorFilter(mContext.getResources().getColor(R.color.shadow));
                    }
                }
            } else {
                setCheck.setVisibility(View.INVISIBLE);
                setCheck.setImageResource(R.drawable.ic_round);
                setCheck.setColorFilter(mContext.getResources().getColor(R.color.colorUnselectedTab));
            }


            // =========== СЧЁТЧИК ===========
            // У Каждой кнопки есть какое-то значение, тут я его считаю и вставляю
            // todo textInteger.setText(msg); -- могу ли выводить это нормально 1 раз?
            try {
                // Вчтавляем "счётчкик"
                switch (optionId) {
                    // Start Work
                    case (138518):
                        long startTime;
                        if (dataDB instanceof WpDataDB) {
                            startTime = ((WpDataDB) dataDB).getVisit_start_dt();
                        } else {
                            startTime = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
                        }
                        textInteger.setText("" + Clock.getHumanTimeOpt(startTime * 1000));
                        break;
                    case (138520):
                        long endTime;
                        if (dataDB instanceof WpDataDB) {
                            endTime = ((WpDataDB) dataDB).getVisit_end_dt();
                        } else {
                            endTime = ((TasksAndReclamationsSDB) dataDB).dt_end_fact;
                        }
                        textInteger.setText("" + Clock.getHumanTimeOpt(endTime * 1000));
                        break;
                    case (132968):  // Вставляем количество выполненных Фоток Витрин
                        String m = optionsButtons.getAmountMin();
                        if (m.equals("0")) {
                            m = "3";
                        }
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 0) + "/" + m);

                        final CharSequence text = textInteger.getText();
                        final SpannableString spannableString = new SpannableString(text);
                        spannableString.setSpan(new URLSpan(""), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textInteger.setText(spannableString, TextView.BufferType.SPANNABLE);

                        textInteger.setOnClickListener(view -> {
                            Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
                            intent.putExtra("report_prepare", true);
                            intent.putExtra("dad2", dad2);
                            view.getContext().startActivity(intent);
                        });
                        break;
                    case (135809):  // Вставляем количество выполненных Фото витрины ДО начала работ
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 14));
                        break;
                    case (135158):  // Вставляем количество выполненных Фото Остатков Товаров (ФОТ)
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 4));
                        break;
                    case (132969):  // Вставляем количество выполненных Фото Тележка с Товаром (ФТТ)
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 10));
                        break;
                    case (141360):
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 31));
                        break;

                    case 137797:    // Остатки
                        String msg = String.format("%s/%s/%s", (int) DetailedReportActivity.SKUPlan, (int) DetailedReportActivity.SKUFact, (int) DetailedReportActivity.OFS);
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
                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 3));
                        break;

                    case 138339:    // Доп Требования
                        // Устанавливаю в счётчик доп. требований их количество
                        textInteger.setText("" + AdditionalRequirementsRealm.getData3(dataDB).size());
                        break;

                    case 135328:    // Рекламация
                        OptionMassageType type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlReclamationAnswer<?> optionControlReclamationAnswer = new OptionControlReclamationAnswer<>(itemView.getContext(), dataDB, optionsButtons, type, Options.NNKMode.NULL);

                        textInteger.setText("" + optionControlReclamationAnswer.problemReclamationCount());
                        break;

                    case 135327:    // Задачи
                        type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlTaskAnswer<?> optionControlTask = new OptionControlTaskAnswer<>(itemView.getContext(), dataDB, optionsButtons, type, Options.NNKMode.NULL);

                        textInteger.setText("" + optionControlTask.problemTaskCount());
                        break;

                    default:
                        textInteger.setVisibility(View.GONE);
//                        textInteger.setText(optionsButtons.getPrice());
                }
            } catch (Exception e) {
                // TODO Вставить обработчик
                e.printStackTrace();
            }


            // Работа с кнопками и нажатиями на них
            final DetailedReportButtons detailedReportButtons = new DetailedReportButtons();

            // Функционал Опций (контроль, NNK..)
            final Options options = new Options();


            // Кликаем по кнопке Опции
            boolean finalDescribedOption = describedOption;
            constraintLayout.setOnClickListener(view -> {
                Log.e("notifyItemChanged", "CLICK");

                // Если эта кнопка НЕ активная - значит она находится в разработке
                if (finalDescribedOption) {

                    // Обработка нажатия на кнопку
                    OptionMassageType msgType = new OptionMassageType();
                    msgType.type = OptionMassageType.Type.DIALOG;
                    msgType = options.NNK(mContext, dataDB, optionsButtons, msgType, Options.NNKMode.MAKE, () -> {
                        if (dataDB instanceof WpDataDB) {
                            detailedReportButtons.buttonClick(mContext, (WpDataDB) dataDB, butt.get(getAdapterPosition()), 0);
                            setCheck(POS, optionsButtons, Options.NNKMode.NULL);
                        }
                        notifyDataSetChanged();
                    });

                    // todo Определить нафиг это сделано
                    // Это нужно для старого отображения ошибки контроля опции
                    if (msgType != null && msgType.dialog != null) {
                        msgType.dialog.setDialogIco();
                        msgType.dialog.show();
                    }

                    // todo Определить нафиг это сделано
                    // Это нужно для старого отображения ошибки контроля опции
                    if (msgType != null && msgType.msg != null && !msgType.msg.equals("")) {
                        Toast.makeText(mContext, msgType.msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(view.getContext(), "Данная Опция находится в РАЗРАБОТКЕ", Toast.LENGTH_SHORT).show();
                }

            });


            // ДОЛГИЙ клик по Кнопке Опции
            OptionsDB test = optionsButtons;
            constraintLayout.setOnLongClickListener(view -> {
                if (optionId == 132968) {
                    DialogData dialog = new DialogData(itemView.getContext());
                    dialog.setTitle("Внесите пароль!");
                    dialog.setText("Для продолжения внесите пароль: ");
                    dialog.setClose(dialog::dismiss);
                    dialog.setOperation(DialogData.Operations.TEXT, "", null, () -> {
                    });
                    dialog.setOk("Ok", () -> {
                        Toast.makeText(dialog.context, "Внесли: " + dialog.getOperationResult(), Toast.LENGTH_SHORT).show();

                        int res = Integer.parseInt(dialog.getOperationResult());

                        Calendar calendar = Calendar.getInstance();
                        int day = calendar.get(Calendar.DAY_OF_WEEK);
                        int dat2 = calendar.get(Calendar.DAY_OF_MONTH);

                        int pass = day + dat2;

                        if (res == pass) {
                            int optId = Integer.parseInt(butt.get(getAdapterPosition()).getOptionId());
                            longClickButton(test, optId, detailedReportButtons, optionsButtons);
                        } else {
                            Toast.makeText(dialog.context, "Внесите корректный пароль", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                } else {
                    int optId = Integer.parseInt(butt.get(getAdapterPosition()).getOptionId());
                    longClickButton(test, optId, detailedReportButtons, optionsButtons);
                }
                return false;
            });


            // Нажатие на СИГНАЛ Кнопки Опции.
            setCheck.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Проверка статуса данной опции", Toast.LENGTH_SHORT).show();
                setCheck(POS, optionsButtons, Options.NNKMode.CHECK_CLICK);
            });
        }
    }

    private CharSequence counter2Text() {
        CharSequence res = "";
        if (dataDB instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) dataDB;
            res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * 0.08);
            res = Html.fromHtml("<font color=red>" + res + "грн" + "</font>");
        }
        return res;
    }

    private void longClickButton(OptionsDB test, int optId, DetailedReportButtons detailedReportButtons, OptionsDB optionsButtons) {
        optionDetailPhotos(test);

        if (optId == 132968) {
            if (dataDB instanceof WpDataDB) {
                WpDataDB wpDataDB = (WpDataDB) dataDB;
                detailedReportButtons.buttonClick(mContext, wpDataDB, optionsButtons, 1);
            }
        }
    }

    /*Нажатие на проверку статуса опции. Нажатие на сигнал*/
    private void setCheck(int POS, OptionsDB optionsButtons, Options.NNKMode mode) {
        Options options = new Options();
        options.optionControl(mContext, dataDB, optionsButtons, null, mode);

        RealmManager.INSTANCE.executeTransaction(realm -> {
            realm.insertOrUpdate(optionsButtons);
        });
        updateSignal(POS);
    }

    private void updateSignal(int position) {
        if (dataDB instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) dataDB;
            WpDataDB wp = RealmManager.INSTANCE.copyFromRealm(RealmManager.getWorkPlanRowById(wpDataDB.getId()));
            dataDB = (T) wp;
            notifyItemChanged(position);
        }
    }


    /*Определяем конструктор*/
    public RecycleViewDRAdapter(Context context, T dataDB, List<OptionsDB> dataButtons, List<SiteObjectsSDB> list) {
        this.dataDB = dataDB;
        this.butt = dataButtons;
        this.translate = list;
        this.mContext = context;

        if (dataDB instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) dataDB;
            dad2 = wp.getCode_dad2();
            startDt = wp.getVisit_start_dt();
            endDt = wp.getVisit_end_dt();
        } else {
            TasksAndReclamationsSDB tar = (TasksAndReclamationsSDB) dataDB;
            dad2 = tar.codeDad2;
            startDt = tar.dt_start_fact;
            endDt = tar.dt_end_fact;
        }
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


        viewHolder.bind(optionsButtons, siteObjectsSDB);
    }

    @Override
    public int getItemCount() {
        return butt.size();
    }

    public int getItemPosition(OptionsDB item) {
        return butt.indexOf(item);
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


    private void optionDetailPhotos(OptionsDB option) {
        String additionalText = "\n\n";

        String buttText = option.getOptionTxt();
        buttText = buttText.replace("&quot;", "");
        buttText = buttText.replace("Кнопка ", "");

        /*
         * 132968 - 0  - фото витрины
         * 135809 - 14 - Фото витрины До начала работ
         * 135158 - 4  - Фото остатков товаров
         * 132969 - 10 - Фото тележка с товаром
         * 141360 - 31 - Фото товара на складе
         * 141885 - 3  - Фото Документов
         * */
        int photoType = 0;
        boolean showPhotoLink = false;
        switch (option.getOptionId()) {
            case "132968":  // - 0  - фото витрины
                photoType = 0;
                showPhotoLink = true;
                break;
            case "135809":  // - 14 - Фото витрины До начала работ
                photoType = 14;
                showPhotoLink = true;
                break;
            case "135158":  // - 4  - Фото остатков товаров
                photoType = 4;
                showPhotoLink = true;
                break;
            case "132969":  // - 10 - Фото тележка с товаром
                photoType = 10;
                showPhotoLink = true;
                break;
            case "141360":  // - 31 - Фото товара на складе
                photoType = 31;
                showPhotoLink = true;
                break;
            case "141885":  // - 3  - Фото Документов
                photoType = 3;
                showPhotoLink = true;
                break;
        }

/*        switch (option.getOptionId()) {
            case "133382":  // Потенциальный клиент
                additionalText += additionalText133382();
                break;
        }*/

        SpannableStringBuilder ss = new SpannableStringBuilder();
        ss.append(option.getOptionDescr());
        ss.append(additionalText);
        if (showPhotoLink) {
            ss.append("\n\n");
            ss.append(createLinkedString(mContext, "Показать образец фото", photoType));
        }

        DialogData dialog = new DialogData(mContext);
        dialog.setTitle(buttText);
        dialog.setText(ss, () -> {
        });
        dialog.setMerchikIco(mContext);
        dialog.show();
    }

    /*Дополнительная подсказка: Потенциальный клиент*/
    private String additionalText133382() {
        int test = 11000;   // TODO будут мне норм значение передавать "ЗП мерчика"
        String res;

        double requestSend = test * 0.0005;
        double requestRegistered = test * 0.005;
        double presentation = test * 0.01;
        double clientStart = test * 0.1;


        res = String.format("- За регистрацию потенциального клиента (ПК) начисляются следующие премии:\n" +
                "1. При подаче заявки: %s грн.\n" +
                "2. Если и менеджер (после проверки реквизитов) регистрирует этого ПК в нашей базе данных: %s грн.\n" +
                "3. Если для этого ПК проводится презентация: %s грн.\n" +
                "4. Основная премия, при \"запуске\" клиента в работу: %s грн.", requestSend, requestRegistered, presentation, clientStart);

        return res;
    }

    private SpannableString createLinkedString(Context context, String msg, int photoType) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    Toast.makeText(context, "Показать образец фото", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, PhotoLogActivity.class);
                    intent.putExtra("SamplePhoto", true);
                    intent.putExtra("photoTp", photoType);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Показать образец фото error: " + e, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

}
