package ua.com.merchik.merchik;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Utils.CustomString;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;


public class RecycleViewWPAdapter extends RecyclerView.Adapter<RecycleViewWPAdapter.ViewHolder> implements Filterable {

    Globals globals = new Globals();

    private Context mContext;
    private List<WpDataDB> WP;
    private List<WpDataDB> workPlanList;
    private List<WpDataDB> workPlanList2;

    // Pika чтобы получать список нужных фото, анализируя который можно было нарисовать тот или иной чекбокс на элементе ресиклера
    private List<StackPhotoDB> listPhotos;

    // Pika - создаю элемент, который будет использован для сортировки плана работ
    private WpSortOrder WPSO;

    // ---------------------- сортировка по свежести клиента ------------------------------
    // Pika - создаю класс для определения сортированого порядка получения элементов плана работ
    class WpSortOrder {

        // Pika - мини структура для элемента списка сортировки (возможно, будут и другие)
        class OrderStruct1 {

            private int Pos; // "сортированая" позиция исходного элемента - ее будем возвращать, когда будем брать элементы этого списка последовательно по порядку
            private Date DataZap; // дата запуска
            private Date DataPereZap; // дата перезапуска
            private Date DataMax; // максимальная дата из двух - именно по ней будем сравнивать
            private Date TekDate; // текущая дата работ
            private int AdrKod; // адрес работ

            public OrderStruct1(int posi, Date dzap, Date dperezap, Date drab, int adrCode) {
                this.Pos = posi;
                this.DataZap = dzap;
                this.DataPereZap = dperezap;
                this.TekDate = drab;
                this.AdrKod = adrCode;
                if (dzap.compareTo(dperezap) > 0) {
                    this.DataMax = dzap;
                } else {
                    this.DataMax = dperezap;
                }
            }

            public Date getDataMax() {
                return DataMax;
            }

            public Date getTekDate() {
                return TekDate;
            }

            public int getPos() {
                return Pos;
            }

            public int getAdrKod() {
                return AdrKod;
            }
        }

        // Pika - класс для сравнения элементов, используемый при сортировке (сортирует по "свежести" клиентов в пределах даты)
        public class FreshClientComparator implements Comparator<OrderStruct1> {

            @Override
            public int compare(OrderStruct1 o1, OrderStruct1 o2) {
                if (o1.getTekDate().compareTo(o2.getTekDate()) < 0) {
                    return -1;
                } else if (o1.getTekDate().compareTo(o2.getTekDate()) > 0) {
                    return 1;
                } else {
                    if (o1.getAdrKod() < o2.getAdrKod()) {
                        return -1;
                    } else if (o1.getAdrKod() > o2.getAdrKod()) {
                        return 1;
                    } else {
                        if (o1.getDataMax().compareTo(o2.getDataMax()) > 0) {
                            return -1;
                        } else if (o1.getDataMax().compareTo(o2.getDataMax()) < 0) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            }
        }

        // это список который и будет сортироваться
        public List<OrderStruct1> orderList1 = new ArrayList<>();

        // конструктор класса WpSortOrder - заполняет список данными и сортирует
        public WpSortOrder(List<WpDataDB> a1) {
            WpDataDB elem;
            OrderStruct1 os1;
            String s;
            int fillOk, foundId;
            Date wsd, wrsd, tekd;
            int adrKod;

            // список для строк - кодов клиентов
            List<String> ids = new ArrayList<>();
            // список для объектов - самих клиентов
            List<CustomerSDB> customerSDBList = new ArrayList<>();

            // получаю коды клиентов из списка работ плана работ
            int count = 0;
            try {
                for (WpDataDB a : a1) {
                    if (a != null && a.getClient_id() != null) {
                        s = a.getClient_id();
                        if (!ids.contains(s)) ids.add(s);
                        count++;
                    } else {
                        Log.e("WpSortOrder", "count:" + count);
                        Log.e("WpSortOrder", "new Gson().toJson(a):" + new Gson().toJson(a));
                        Globals.writeToMLOG("ERROR", "WpSortOrder", "WpDataDB: " + new Gson().toJson(a));
                    }
                }
            } catch (Exception e) {
                Log.e("WpSortOrder", "count E: " + count);
//                Log.e("WpSortOrder", "count E: " + new Gson().toJson(a1.get(count)));
            }


            // получаю список самих клиентов по этим кодам из базы данных приложения
            if (ids.size() > 0) {
                customerSDBList = SQL_DB.customerDao().getByIds(ids);
            }

            // устанавливаю флаг успешного заполнения сортировочного списка
            // если что пойдет не так, то не нужно будет выполнять сортировку
            fillOk = 1;

            // перебираю элементы плана работ
            for (int i = 0; i < a1.size(); i++) {
                elem = a1.get(i);
                s = elem.getClient_id();

                // устанавливаю начальные значения для даты старта, даты рестарта и текущей даты работ
                tekd = elem.getDt();
                wsd = tekd;
                wrsd = tekd;
                adrKod = elem.getAddr_id();

                // устанавливаю флаг успешного нахождения клиента.
                // если не будет найден клиент по коду клиента, то при этом не нужно будет выполнять сортировку
                foundId = 0;

                // Тут перебираю клиентов и нахожу того, который соответствует текущему коду клиента в плане работ
                // и из него беру workStartDate и workRestartDate
                for (CustomerSDB a : customerSDBList) {
                    if (s.compareTo(a.id) == 0) {
                        foundId = 1;
                        wsd = a.workStartDate;
                        wrsd = a.workRestartDate;
                        break;
                    }
                }

                // если клиент не был найден, то сбрасываю флаг успешности заполнения сортировочного списка, и сортировку не буду делать
                if (foundId == 0) {
                    fillOk = 0;
                }

                // в любом случае добавляю элемент в сортировочный список, чтоб можно было потом получить позицию из него
                // в случае если список не будет отсортирован, то возвращаться будет та же позиция, что и передана
                os1 = new OrderStruct1(i, wsd, wrsd, tekd, adrKod);
                orderList1.add(os1);
            }

            // теперь сортировка в нужном порядке с использованием нужного компаратора
            if (fillOk == 1) {
                Comparator fComparator = new FreshClientComparator();
                Collections.sort(orderList1, fComparator);
            }
        }

        // возвращает позицию исходного элемента списка, для списка переданного на сортировку
        // понятно, что предполагается, что передаваться сюда будут последовательные позиции,
        // а возвращать он будет уже то что нужно
        public int getOrderedPos(int pos) {
            return orderList1.get(pos).getPos();
        }

//        public int getOrderedData(int pos) {
//            return orderList1.get(pos).;
//        }
    }
    // ----------------------------------------------------


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
        TextView theme, themeLabel;
        LinearLayout options = null;
        ImageView wp_image;
        ImageView check;

        TextView numberTTTitle, numberTT;

        TextView mainThemeLabel, mainThemeMessage;

        private TextView groupTitle, groupText; // мережа

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
            themeLabel = view.findViewById(R.id.themelabel);
            options = (LinearLayout) view.findViewById(R.id.option_signal_layout1);//setContentView
            wp_image = (ImageView) view.findViewById(R.id.wp_image1);

            numberTTTitle = view.findViewById(R.id.numberTTTitle);
            numberTT = view.findViewById(R.id.numberTT);

            groupTitle = view.findViewById(R.id.groupTitle);
            groupText = view.findViewById(R.id.groupData);

            check = (ImageView) view.findViewById(R.id.check);

            mainThemeLabel = view.findViewById(R.id.themeMainlabel);
            mainThemeMessage = view.findViewById(R.id.themeMainData);
        }

        public void bind(WpDataDB wpDataDB) {
            check.setColorFilter(mContext.getResources().getColor(R.color.shadow));
            if (wpDataDB.getStatus() == 1) {
                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                check.setColorFilter(mContext.getResources().getColor(R.color.colorInetGreen));
            } else {
                // Pika Если есть паеорамное фото по этому ДАД2 то рисуем желтый кружок
                listPhotos = RealmManager.stackPhotoByDad2AndType(wpDataDB.getCode_dad2(), 0);
                if (listPhotos != null && listPhotos.size() > 0) {
                    check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                    check.setColorFilter(mContext.getResources().getColor(R.color.colorInetYellow));
                } else {
                    if (Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000)) < System.currentTimeMillis()) {    //+TODO CHANGE DATE
                        check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                        check.setColorFilter(mContext.getResources().getColor(R.color.colorInetRed));
                    } else {
                        check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_round));
                        check.setColorFilter(mContext.getResources().getColor(R.color.shadow));
                    }
                }
            }


            long otchetId;
            int action = wpDataDB.getAction();
            if (action == 1 || action == 94) {
                otchetId = wpDataDB.getDoc_num_otchet_id();
            } else {
                otchetId = wpDataDB.getDoc_num_1c_id();
            }

//            // План/Снижение/Расчёт Факт
//            String t_price = String.format("%s/+%s/%s", 0.00, 0, wpDataDB.getCash_ispolnitel());

            // План/Снижение/Расчёт Факт
            String t_price = String.format("%s/-%s/%s", (int) wpDataDB.getCash_ispolnitel(), (int) wpDataDB.cash_penalty, (int) wpDataDB.cash_fact);

            SpannableString string = new SpannableString(t_price);
            string.setSpan(new UnderlineSpan(), 0, string.length(), 0);

            AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
            String numberTTS = addressSDB != null && addressSDB.nomerTT != null && addressSDB.nomerTT != 0 ? "" + addressSDB.nomerTT + "" : "";

            if (!numberTTS.equals("")) {
                numberTTTitle.setVisibility(View.VISIBLE);
                numberTT.setVisibility(View.VISIBLE);
                numberTT.setText(numberTTS);
            } else {
                numberTTTitle.setVisibility(View.GONE);
                numberTT.setVisibility(View.GONE);
            }

            addr.setText(wpDataDB.getAddr_txt());
            cust.setText(wpDataDB.getClient_txt());
            merc.setText(wpDataDB.getUser_txt());
//            date.setText(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime()/1000) + " " + Clock.getHumanTimeOpt(wpDataDB.getDt_start() * 1000));
            date.setText(Clock.getHumanTimeSecPattern(wpDataDB.getDt().getTime() / 1000, "dd-MM-yy") + " " + Clock.getHumanTimeOpt(wpDataDB.getDt_start() * 1000));
            price.setText(string);
            price.setMovementMethod(LinkMovementMethod.getInstance());


//            try {
//                theme.setText(ThemeRealm.getByID(String.valueOf(wpDataDB.getTheme_id())).getNm());
//                if (wpDataDB.getTheme_id() != 998) {
//                    theme.setTextColor(mContext.getResources().getColor(R.color.red_error));
//                } else {
//                    theme.setTextColor(mContext.getResources().getColor(android.R.color.tab_indicator_text));
//                }
//            } catch (Exception e) {
//                // Тема не успела загрузиться
//                theme.setText("Тема не обнаружена");
//            }

            try {
                // Получаем данные темы
                int themeCode = wpDataDB.getTheme_id();
                String glOptionCode = wpDataDB.getMain_option_id(); // Предположим, что есть метод для получения кода опции
                String optionName = RealmManager.getOptionNameByOptionId(glOptionCode);

                // Настройка темы
                theme.setText(ThemeRealm.getThemeById(String.valueOf(themeCode)).getNm());
                mainThemeMessage.setText(optionName);

                // Настройка видимости и цветов
                if (themeCode == 998) {
                    // Базовый мерчандайзинг
                    theme.setVisibility(View.VISIBLE);
                    themeLabel.setVisibility(View.VISIBLE);
                    theme.setTextColor(ContextCompat.getColor(mContext, android.R.color.tab_indicator_text));

                    mainThemeLabel.setVisibility(View.GONE);
                    mainThemeMessage.setVisibility(View.GONE);

                } else if (themeCode == 1132) {
                    // Мерчандайзинг СУППР
                    theme.setVisibility(View.GONE);
                    themeLabel.setVisibility(View.GONE);

                    // Настройка главной опции
                    mainThemeLabel.setVisibility(View.VISIBLE);
                    mainThemeMessage.setVisibility(View.VISIBLE);
                    if (Objects.equals(glOptionCode, "574")) {
                        // Выкладка товара
                        mainThemeMessage.setTextColor(ContextCompat.getColor(mContext, android.R.color.tab_indicator_text));
                    } else {
                        // Все остальные опции
                        mainThemeMessage.setTextColor(ContextCompat.getColor(mContext, R.color.red_error));
                    }

                } else {
                    // Общий случай
                    theme.setVisibility(View.VISIBLE);
                    themeLabel.setVisibility(View.VISIBLE);
                    theme.setTextColor(ContextCompat.getColor(mContext, R.color.red_error));

                    mainThemeLabel.setVisibility(View.VISIBLE);
                    mainThemeMessage.setVisibility(View.VISIBLE);
                    mainThemeMessage.setTextColor(ContextCompat.getColor(mContext, R.color.red_error));
                }

            } catch (Exception e) {
                // Обработка ошибок
                theme.setText("Тема не обнаружена");
                theme.setVisibility(View.VISIBLE);
                themeLabel.setVisibility(View.VISIBLE);
                mainThemeLabel.setVisibility(View.GONE);
                mainThemeMessage.setVisibility(View.GONE);
                Log.e("TAG", "Error loading theme", e);
            }

            try {
//                wpDataDB.gr
                AddressSDB addr = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));
                groupText.setText(tradeMarkDB.getNm());
            } catch (Exception e) {
                groupText.setText("Мережа не знайдена");
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
            stringBuilder.append("План: ").append(wp.getCash_ispolnitel()).append(" грн.").append("\n");
            stringBuilder.append("Снижение: ").append(wp.cash_penalty).append(" грн.").append("\n");
            stringBuilder.append("Факт: ")
                    .append(wp.cash_fact)
                    .append(" грн.")
                    .append(" Зайняло часу: ")
                    .append(CustomString.getTimeDifference(wp.getVisit_end_dt(), wp.getVisit_start_dt()))
                    .append("\n");

            DialogData dialog = new DialogData(context);
            dialog.setTitle("Расчёт");
            dialog.setText(stringBuilder);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }


        private void setDialog(WpDataDB wpDataDB, long otchetId) {

            String addrTxt;
            if (wpDataDB.getAddr_txt() != null && !wpDataDB.getAddr_txt().equals("")) {
                addrTxt = wpDataDB.getAddr_txt();
            } else {
                AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                if (addressSDB != null) {
                    addrTxt = addressSDB.nm;
                    wpDataDB.setAddr_location_xd(String.valueOf(addressSDB.locationXd));
                    wpDataDB.setAddr_location_yd(String.valueOf(addressSDB.locationYd));
                } else {
                    addrTxt = "Адреса не визначена";
                }
                wpDataDB.setAddr_txt(addrTxt);
                WpDataRealm.setWpData(Collections.singletonList(wpDataDB));
            }

            String msg = String.format("Дата: %s\nАдреса: %s\nКлієнт: %s\nВиконавець: %s\n", Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000), addrTxt, wpDataDB.getClient_txt(), wpDataDB.getUser_txt());

            DialogData errorMsg = new DialogData(mContext);
            errorMsg.setTitle("");
            errorMsg.setText(mContext.getString(R.string.re_questioning_wpdata_err_msg));
            errorMsg.setClose(errorMsg::dismiss);

            DialogData dialog = new DialogData(mContext);
            dialog.setTitle(mContext.getResources().getString(R.string.open_visit) + "?");
            dialog.setText(msg);
            dialog.setOk(null, () -> {
                Globals.writeToMLOG("INFO", "RecycleViewWPAdapter/openReportPrepare/CLICK_KPS", "wpDataDB: " + wpDataDB);
                if (wpDataDB.getTheme_id() == 1182) {
                    DialogData dialogQuestionOne = new DialogData(mContext);
                    dialogQuestionOne.setTitle("");
                    dialogQuestionOne.setText(mContext.getString(R.string.re_questioning_wpdata_first_msg));
                    dialogQuestionOne.setOk("Так", errorMsg::show);
                    dialogQuestionOne.setCancel("Hi", () -> {
                        DialogData dialogQuestionOTwo = new DialogData(mContext);
                        dialogQuestionOne.dismiss();
                        dialogQuestionOTwo.setTitle("");
                        dialogQuestionOTwo.setText(mContext.getString(R.string.re_questioning_wpdata_second_msg));
                        dialogQuestionOTwo.setOk("Так", errorMsg::show);
                        dialogQuestionOTwo.setCancel("Нi", () -> {
                            openReportPrepare(wpDataDB, otchetId);
                        });
                        dialogQuestionOTwo.show();
                    });
                    dialogQuestionOne.show();
                } else {
//                    // Pika test
//                    Sandbox();
                    openReportPrepare(wpDataDB, otchetId);
                }
            });
            dialog.show();
        }




        private void openReportPrepare(WpDataDB wp, long otchetId) {
            try {
//                Data D = new Data(
//                        wp.getId(),
//                        wp.getAddr_txt(),
//                        wp.getClient_txt(),
//                        wp.getUser_txt(),
//                        wp.getDt(),  //+TODO CHANGE DATE
//                        otchetId,
//                        null,
//                        "",
//                        R.mipmap.merchik);
//
//                WPDataObj wpDataObj = workPlan.getKPS(wp.getId());

                Intent intent = new Intent(mContext, DetailedReportActivity.class);

                intent.putExtra("WpDataDB_ID", wp.getId());

//                intent.putExtra("dataFromWP", D);
//                intent.putExtra("rowWP", wp);
//                intent.putExtra("dataFromWPObj", wpDataObj);
//
//                WpDataDB rowWP = intent.getParcelableExtra("rowWP");
//                Data dataFromWP = intent.getParcelableExtra("dataFromWP");


                mContext.startActivity(intent);
            } catch (Exception e) {
//                globals.alertDialogMsg(mContext, "Возникла ошибка. Сообщите о ней своему администратору. Ошибка2: " + e);

                Globals.writeToMLOG("ERROR", "RecycleViewWPAdapter/openReportPrepare", "Exception e: " + e);
                DialogData dialogData = new DialogData(mContext);
                dialogData.setTitle("Виникла помилка");
                dialogData.setDialogIco();
                dialogData.setText("План робіт оновився і треба в нього перезайти. Якщо це повідомлення повторюється передайте його Вашому керівнику або в слуюбу підтримки merchik.");
                dialogData.setOk("Перезайти", () -> {
                    Intent intent = new Intent(mContext, WPDataActivity.class);
                    mContext.startActivity(intent);
                });
                dialogData.show();
            }
        }
    }

    /*Определяем конструктор*/
    public RecycleViewWPAdapter(Context context, RealmResults<WpDataDB> wp) {
        this.mContext = context;
        this.WP = RealmManager.INSTANCE.copyFromRealm(wp);
        this.workPlanList = RealmManager.INSTANCE.copyFromRealm(wp);
        this.workPlanList2 = RealmManager.INSTANCE.copyFromRealm(wp);
        // Pika
        // создаю класс для определения порядка сортировки
//        WPSO = new WpSortOrder(WP);
    }

    public void updateData(List<WpDataDB> wp) {
        this.WP.clear();
        this.workPlanList.clear();
        this.workPlanList2.clear();

        Globals.writeToMLOG("INFO", "RecycleViewWPAdapter/updateData", "wp: " + wp);
        this.WP = wp;
        this.workPlanList = wp;
        this.workPlanList2 = wp;

        // Pika
        // создаю класс для определения порядка сортировки
//        WPSO = new WpSortOrder(WP);
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
            // Pika - было:
            WpDataDB wpDataDB = WP.get(position);
            // Pika - стало: (сортировка по свежести клиента)
//            WpDataDB wpDataDB = WP.get(WPSO.getOrderedPos(position));

            viewHolder.bind(wpDataDB);
        } catch (Exception e) {
            e.printStackTrace();
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
                if (constraint.length() != 0) {
                    WP = (List<WpDataDB>) results.values;
//                    WPSO = new WpSortOrder(WP); // 29.02.2024 Victor учитываю изменение в фильтре для алгоритма @Pika
                }
                notifyDataSetChanged();
            }
        };
    }

}
