package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA;
import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.ERROR_ID;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Date;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.DoubleSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.EditTextAndSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Number;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Text;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import retrofit2.Call;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.Utils.MySimpleExpandableListAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.OborotVedSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.RecentItem;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportHint;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportHintList;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.PromoRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogPhotoTovar;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class RecycleViewDRAdapterTovar extends RecyclerView.Adapter<RecycleViewDRAdapterTovar.ViewHolder> implements Filterable {

    private Context mContext;
    private List<TovarDB> dataList;
    private List<TovarDB> dataFilterable;
    //    private WpDataDB wpDataDB;
    private DRAdapterTovarTPLTypeView tplType;

    private List<Integer> tovIdList;

    private Clicks.clickVoid click;


    private long codeDad2;
    private String clientId;
    private int addressId;

    public enum DRAdapterTovarTPLTypeView {
        GONE, FULL
    }

    public enum OldDateOstatok {
        ELDEST, OLD, NEW
    }


    /*Определяем конструктор*/
    public RecycleViewDRAdapterTovar(Context context, List<TovarDB> list, WpDataDB wp) {
        this.mContext = context;
        this.dataList = list;
        this.dataFilterable = list;

//        this.wpDataDB = wp;
        codeDad2 = wp.getCode_dad2();
        clientId = wp.getClient_id();
        addressId = wp.getAddr_id();


        tplType = DRAdapterTovarTPLTypeView.GONE;
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapterTovar.RecycleViewDRAdapterTovar", "list.size(): " + list.size());
    }

    public RecycleViewDRAdapterTovar(Context context, List<TovarDB> list, TasksAndReclamationsSDB tasksAndReclamationsSDB) {
        this.mContext = context;
        this.dataList = list;
        this.dataFilterable = list;

        codeDad2 = tasksAndReclamationsSDB.codeDad2SrcDoc;
        clientId = tasksAndReclamationsSDB.client;
        addressId = tasksAndReclamationsSDB.addr;

        tplType = DRAdapterTovarTPLTypeView.GONE;
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapterTovar.RecycleViewDRAdapterTovar", "list.size(): " + list.size());
    }

    public void setAkciyaTovList(List<Integer> tovIdList) {
        this.tovIdList = tovIdList;
        Log.e("АКЦИЯ_ТОВАРА", "tovIdList: " + tovIdList);
    }

    @Override
    public RecycleViewDRAdapterTovar.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.dr_tovar_item_tovar, parent, false);
        return new RecycleViewDRAdapterTovar.ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(RecycleViewDRAdapterTovar.ViewHolder viewHolder, int position) {
        viewHolder.bind(dataList.get(position));
    }


    @Override
    public int getItemCount() {
        try {
            Globals.writeToMLOG("INFO", "RecycleViewDRAdapterTovar.getItemCount", "dataList.size(): " + dataList.size());
            return dataList.size();
        } catch (Exception e) {
            Log.e("LOG_FILTER", "FILTER_ERR: " + e);
            Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.getItemCount", "Exception e: " + e);
            return 0;
        }
    }

    public void refreshAdapter(Clicks.clickVoid click) {
        this.click = click;
    }

    public void setTplType(DRAdapterTovarTPLTypeView type) {
        this.tplType = type;
    }

    public boolean switchTPLView() {
        if (tplType.equals(DRAdapterTovarTPLTypeView.GONE)) {
            tplType = DRAdapterTovarTPLTypeView.FULL;
            return true;
        } else {
            tplType = DRAdapterTovarTPLTypeView.GONE;
            return false;
        }
    }


    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {

        Globals globals = new Globals();
        Options options = new Options();

        TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading(); // Может выше поднять?

        // Resourse
        ConstraintLayout constraintLayout;
        ImageView imageView;
        TextView name;
        TextView weight;
        TextView tovGroup;
        TextView tradeMark;
        TextView textViewItemTovarOptLine, article;
        RecyclerView recyclerView;

        TextView balance, facePlan;

        TextView allTov;

        // Dialog Resourse
        Dialog dialog = new Dialog(mContext);
        ImageView imgFullSize;
        TextView dName, dWeight, dTovGroup, dTradeMark;
        Button closeDialog;

        ViewHolder(View v) {
            super(v);
            // Get Resourse id`s

            constraintLayout = (ConstraintLayout) v.findViewById(R.id.dr_tovar_item_tovar_layout);
            imageView = (ImageView) v.findViewById(R.id.imageViewItemTovar);
            imageView.setClickable(true);
            name = (TextView) v.findViewById(R.id.textViewItemTovarTitle);
            weight = (TextView) v.findViewById(R.id.textViewItemTovarSecondLine);
            textViewItemTovarOptLine = (TextView) v.findViewById(R.id.textViewItemTovarOptLine);
            tradeMark = (TextView) v.findViewById(R.id.textViewItemTovarThirdLine);
            balance = v.findViewById(R.id.balance);
            article = v.findViewById(R.id.article);
            facePlan = v.findViewById(R.id.facePlan);
            recyclerView = v.findViewById(R.id.recyclerView2);


            // Dialog
            dialog.setContentView((R.layout.dialog_full_photo));
            imgFullSize = (ImageView) dialog.findViewById(R.id.imageTovFull);
            dName = (TextView) dialog.findViewById(R.id.textView25);
            dWeight = (TextView) dialog.findViewById(R.id.textView24);
            dTovGroup = (TextView) dialog.findViewById(R.id.textView23);
            dTradeMark = (TextView) dialog.findViewById(R.id.textView22);
            closeDialog = (Button) dialog.findViewById(R.id.buttonDialogFullPhoto);
            Log.e("GET_PHOTO_PATH", "IMG1: " + imgFullSize);

        }

        /*
        Получаем значение артикула товара
        mode -- Добавлять перед значением "Арт:" или нет*/
        private String getArticle(TovarDB tovar, int mode) {
            try {
                StringBuilder res = new StringBuilder();
                ArticleSDB articleSDB = SQL_DB.articleDao().getByTovId(Integer.parseInt(tovar.getiD()));
                if (articleSDB != null) {
                    if (mode == 0) {
                        // Пока ничего
                    } else if (mode == 1) {
                        res.append("Арт: ");
                    } else {
                        // Пока ничего
                    }
                    res.append(articleSDB.vendorCode);
                    return res.toString();
                } else {
                    return "";
                }
            } catch (Exception e) {
                return "";
            }
        }

        public void bind(TovarDB list) {

            boolean deletePromoOption = false;

            String balanceData = "?";
            String balanceDate = "?";

            String tovarId = list.getiD();


            imageView.setImageResource(R.mipmap.merchik);

            String weightString = String.format("%s, %s", list.getWeight(), list.getBarcode());
            name.setText(list.getNm());
            name.setTextSize(16);
            weight.setText(weightString);
            weight.setTextSize(16);

            article.setText(getArticle(list, 1));

            try {
                Drawable background = constraintLayout.getBackground();

                Log.e("АКЦИЯ_ТОВАРА", "TEST1: " + tovIdList);
                Log.e("АКЦИЯ_ТОВАРА", "TEST2: " + list.getiD());

                int id = Integer.parseInt(list.getiD());
                Log.e("АКЦИЯ_ТОВАРА", "TEST3: " + tovIdList.contains(id));


                if (tovIdList.contains(id)) {
                    Log.e("АКЦИЯ_ТОВАРА", "YELLOW " + list.getiD());
                    if (background instanceof ShapeDrawable) {
                        ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(mContext, R.color.yellow));
                    } else if (background instanceof GradientDrawable) {
                        ((GradientDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.yellow));
                    } else if (background instanceof ColorDrawable) {
                        ((ColorDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.yellow));
                    }
                    deletePromoOption = false;
                } else {
                    Log.e("АКЦИЯ_ТОВАРА", "WHITE " + list.getiD());
                    if (background instanceof ShapeDrawable) {
                        ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(mContext, R.color.white));
                    } else if (background instanceof GradientDrawable) {
                        ((GradientDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.white));
                    } else if (background instanceof ColorDrawable) {
                        ((ColorDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.white));
                    }
                    deletePromoOption = true;
                }
            } catch (Exception e) {
                Log.e("АКЦИЯ_ТОВАРА", "Exception e: " + e);
            }


//            if ()
//            RealmManager.getNmById(list.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "";

            try {
                Log.e("ПРОИЗВОДИТЕЛЬ", "ШТО ТУТ?:" + RealmManager.getNmById(list.getManufacturerId()) != null ? RealmManager.getNmById(list.getManufacturerId()).getNm() : "");
                tradeMark.setText(RealmManager.getNmById(list.getManufacturerId()) != null ? RealmManager.getNmById(list.getManufacturerId()).getNm() : "");
            } catch (Exception e) {
                // todo обработать исключение
                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_1", "Exception e: " + e);
            }

            //================================================
            try {
                // Когда сюда вернусь - обратить внимание что в ЗИР нет код ИЗА
//                PPADB ppadbList = getPPAIZA(wpDataDB.getCode_iza(), wpDataDB.getClient_id(), String.valueOf(wpDataDB.getAddr_id()), list.getiD());

//                String ostatok = ppadbList.getOstatok();
//                Long ostatokDate = Long.parseLong(ppadbList.getDtUpdate());


                // Получение RP
                ReportPrepareDB rp = ReportPrepareRealm.getReportPrepareByTov(String.valueOf(codeDad2), list.getiD());
                String ostatok = rp.getOborotvedNum();
                Long ostatokDate = Long.parseLong(rp.oborotved_num_date);


                // 30 дней в миллисекундах == 2592000000
                // Дата старше 30 дней или нет


                // --------------------------

                // Остаток
                try {
                    if (ostatok != null) {
                        balanceData = ostatok;
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_2", "Exception e: " + e);
                }

                // Дата
                try {
                    if (ostatokDate != null && ostatokDate != 0) {
                        Log.e("ПОЛУЧАЮ_ОСТАТКИ", "l: " + ostatokDate);
                        java.util.Date df = new java.util.Date(ostatokDate * 1000);
                        balanceDate = new SimpleDateFormat("dd-MM").format(df);
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_3", "Exception e: " + e);
                }

                OldDateOstatok isOld = isOldOstatokDate(ostatokDate);

                String balanceTxt = String.format("Ост: %s / %s", balanceData, balanceDate);

                CharSequence text = Html.fromHtml("<u>" + balanceTxt + "</u>");

                // Разукрашивание строки в Зелёный цвет если на не старая
                if (isOld.equals(OldDateOstatok.NEW)) {
                    text = Html.fromHtml("<u><font color='#00FF00'>" + balanceTxt + "</font></u>");
                }

                if (isOld.equals(OldDateOstatok.OLD)) {
                    text = Html.fromHtml("<u><font color='#e6e6e6'>" + balanceTxt + "</font></u>");
                }

                if (ostatok != null && !ostatok.equals("0") && (isOld.equals(OldDateOstatok.NEW) || isOld.equals(OldDateOstatok.OLD))) {
                    balance.setText(text);
                } else if (ostatok == null) {

                } else {
                    balance.setText(text);
                }


                // Возможность кликать по тексту. Вызывает описание того что это.
                String finalBalanceData = balanceData;
                String finalBalanceDate = balanceDate;
                balance.setOnClickListener(v -> {

                    SpannableStringBuilder oborotVed = new SpannableStringBuilder();

                    try {
                        List<OborotVedSDB> data = SQL_DB.oborotVedDao().getOborotData(Clock.today, Clock.today_7, Integer.parseInt(list.getiD()), addressId);

                        Log.e("OBOROT_VED", "data: " + data);


//                        for (OborotVedSDB test : data) {
//                            Gson gson = new Gson();
//                            String json = gson.toJson(test);
//                            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//                            Log.e("OBOROT_VED", "test(" + test.tovId + "): " + convertedObject);
//                        }


//                        CharSequence startBalance = Html.fromHtml("<b>Нач. Ост.("+data.get(0).dat+"): </b>" + data.get(0).kolOst + "<br>");
//                        oborotVed.append(startBalance);

                        CharSequence col1 = Html.fromHtml("<b>Приход:</b>");
                        CharSequence col2 = Html.fromHtml("<b>Расход:</b><br>");

                        oborotVed.append("_______________Приход");
                        oborotVed.append("__|___Расход\n");

                        int kolPostSum = 0;
                        int kolProdSum = 0;
                        for (OborotVedSDB item : data) {

                            CharSequence coming = Html.fromHtml("(" + item.dat + ")___" + item.kolPost + "________|");
                            CharSequence consumption = Html.fromHtml("___" + item.kolProd + "<br>");

                            kolPostSum += item.kolPost;
                            kolProdSum += item.kolProd;

                            oborotVed.append(coming);
                            oborotVed.append(consumption);
                        }

                        CharSequence kolPostSumCHAR = Html.fromHtml("<b>ИТОГ: _________</b>" + kolPostSum + "________|___");
                        CharSequence kolProdSumCHAR = Html.fromHtml("" + kolProdSum + "<br>");

                        oborotVed.append(kolPostSumCHAR);
                        oborotVed.append(kolProdSumCHAR);

//                        CharSequence endBalance = Html.fromHtml("<b>Кон. Ост.: </b>" + data.get(data.size()-1).kolOst + "<br><br>");
//                        oborotVed.append(endBalance);

                        CharSequence finalBalance = Html.fromHtml("<b>Кон. Ост. </b>(" + data.get(data.size() - 1).dat + "): " + data.get(data.size() - 1).kolOst + "<br>");
                        oborotVed.append(finalBalance);

                    } catch (Exception e) {
                        Log.e("OBOROT_VED", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_4.клик по балансу", "Exception e: " + e);
                    }


                    try {
                        Log.e("TAG_REALM_LOG", "ЗАПИСЬ 4");
//                        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Нажатие на Остатки", 1169, null, null, null, null, null, Globals.session, null)));
                    } catch (Exception e) {
                        Log.e("TAG_REALM_LOG", "Ошибка(4): " + e);
                        Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_5", "Exception e: " + e);
                    }

                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

                    CharSequence addres = Html.fromHtml("<b>Адрес: </b>" + SQL_DB.addressDao().getById(addressId).nm + "<br>");
                    CharSequence client = Html.fromHtml("<b>Клиент: </b>" + SQL_DB.customerDao().getById(clientId).nm + "<br>");

                    CharSequence tovarCode = Html.fromHtml("<b>Код товара: </b>" + list.getiD() + "<br>");
                    CharSequence tovar = Html.fromHtml("<b>Товар: </b>" + list.getNm() + "<br>");
                    CharSequence barcode = Html.fromHtml("<b>Штрихкод: </b>" + list.getBarcode() + "<br>");

                    CharSequence articul;
                    String articulStr = getArticle(list, 0);
                    if (articulStr != null && !articulStr.equals("")) {
                        articul = Html.fromHtml("<b>Артикул: </b>" + articulStr + "<br>");
                    } else {
                        articul = Html.fromHtml("<b>Артикул: </b>(нет данных) <br>");
                    }

                    stringBuilder.append(addres);   // Адрес
                    stringBuilder.append(client);   // Клиент

                    stringBuilder.append(tovarCode);    // ID Товара
                    stringBuilder.append(tovar);    // Товар
                    stringBuilder.append(barcode);    // Штрихкод товара
                    stringBuilder.append(articul);    // Артикул товара

                    if (isOld.equals(OldDateOstatok.OLD) || isOld.equals(OldDateOstatok.ELDEST)) {
                        CharSequence date = Html.fromHtml("<strong>Дата остатков: </strong><font color='#e6e6e6'>" + finalBalanceDate + "</font><br>");
                        stringBuilder.append(date);     // Дата остатков

                        CharSequence ostatokChar = Html.fromHtml("<b>Остаток: </b><font color='#e6e6e6'>Устарел</font>" + "<br><br>");
                        stringBuilder.append(ostatokChar);       // Остаток
                        stringBuilder.append(Html.fromHtml("Данные об остатке <font color='#e6e6e6'>устарели</font>"));
                    } else {
                        CharSequence date = Html.fromHtml("<strong>Дата остатков: </strong><font color='#00A800'>" + finalBalanceDate + "</font><br>");
                        stringBuilder.append(date);     // Дата остатков

                        CharSequence ostatokChar = Html.fromHtml("<b>Остаток: </b><font color='#00A800'>" + finalBalanceData + "</font> <b>шт</b>" + "<br><br>");
                        stringBuilder.append(ostatokChar);       // Остаток
                        stringBuilder.append(Html.fromHtml("Данные об остатке <b><font color='#00A800'>актуальны</font></b>"));
                    }

                    // Додаємо пробільчики, для того щоб не поряд була вся інфа
                    stringBuilder.append("\n\n\n");

                    // Додавання "таблички"
                    stringBuilder.append(oborotVed);

                    DialogData dialog = new DialogData(mContext);
                    dialog.setTitle("Остатки товара в ТТ");
                    dialog.setText(stringBuilder);
//                    dialog.setText(stringBuilder + "\n\n\n" + oborotVed);
                    dialog.show();
                });

                showFacePlan(rp);

            } catch (Exception err) {
                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_6", "Exception e: " + err);
            }

            //================================================


            WorkPlan workPlan = new WorkPlan();

            Log.e("OPTIONS_TPL", "TOV_ID: " + list.getiD());

            dName.setText(list.getNm());
            dWeight.setText(weightString);
            closeDialog.setOnClickListener(v -> dialog.cancel());

            ReportPrepareDB reportPrepareTovar = RealmManager.getTovarReportPrepare(String.valueOf(codeDad2), list.getiD());
            ReportPrepareDB reportPrepareTovar2 = null;
            List<OptionsDB> optionsList2 = RealmManager.getTovarOptionInReportPrepare(String.valueOf(codeDad2), list.getiD());
            if (reportPrepareTovar != null) {
                reportPrepareTovar2 = INSTANCE.copyFromRealm(reportPrepareTovar);
            }


            String s = options.getOptionString(optionsList2, reportPrepareTovar2, deletePromoOption);

            Log.e("onBindViewHolder", "s: " + s);


            ReportPrepareDB finalReportPrepareTovar1 = reportPrepareTovar2;
            RecyclerViewTPLAdapter recyclerViewTPLAdapter = new RecyclerViewTPLAdapter(
                    options.getRequiredOptionsTPL(optionsList2),
                    finalReportPrepareTovar1,
                    (tpl, data, data2) -> operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list)
            );
            recyclerView.setAdapter(recyclerViewTPLAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

            if (tplType.equals(DRAdapterTovarTPLTypeView.FULL)) {
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
            }


            textViewItemTovarOptLine.setText(Html.fromHtml("<u>" + s + "</u>"));
            textViewItemTovarOptLine.setOnClickListener(v -> {
                click.click();
            });


            if (reportPrepareTovar != null) {
                ReportPrepareDB finalReportPrepareTovar = reportPrepareTovar2; // TODO Тест, надо будет убрать

                String finalBalanceData1 = balanceData;
                String finalBalanceDate1 = balanceDate;
                boolean finalDeletePromoOption = deletePromoOption;
                constraintLayout.setOnClickListener(v -> {
                    Log.e("DRAdapterTovar", "ClickListener");
                    try {
                        // Получаем инфу об обязательных опциях
                        List<TovarOptions> tovOptTplList = options.getRequiredOptionsTPL(optionsList2);

                        Log.e("DRAdapterTovar", "Кол-во. обязательных опций: " + tovOptTplList.size());

                        if (tovOptTplList.size() > 0) {
                            // В Цикле открываем Н количество инфы
                            for (int i = tovOptTplList.size() - 1; i >= 0; i--) {
                                if (tovOptTplList.get(i).getOptionControlName() != Globals.OptionControlName.AKCIYA) {
                                    if (tovOptTplList.get(i).getOptionControlName().equals(AKCIYA_ID) && finalDeletePromoOption) {
                                        // втыкаю
//                                        showDialog(list, tovOptTplList.get(i), finalReportPrepareTovar, tovarId, String.valueOf(codeDad2), clientId, finalBalanceData1, finalBalanceDate1);
                                    } else {
                                        showDialog(list, tovOptTplList.get(i), finalReportPrepareTovar, tovarId, String.valueOf(codeDad2), clientId, finalBalanceData1, finalBalanceDate1, true);
                                    }
                                }
                            }
                            Collections.reverse(dialogList);
                            dialogList.get(0).show();
                        } else {
                            DialogData dialog = new DialogData(mContext);
                            dialog.setTitle("Внимание!");
                            dialog.setText("Для данного товара не определены реквизиты обязательные для заполнения. Для принудительного вызова списка реквизитов выполните длинный клик по товару. ");
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }


                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_7", "Exception e: " + e);
                        globals.alertDialogMsg(mContext, "Не удалось открыть Опцию. Если ошибка повторяется - обратитесь к своему руководителю.\n\nОшибка: " + e);
                    }
                });

                constraintLayout.setOnLongClickListener(v -> {
                    try {
                        // Получаем инфу о всех опциях
                        List<TovarOptions> tovOptTplList = options.getAllOptionsTPL();
                        // В Цикле открываем Н количество инфы
                        for (int i = tovOptTplList.size() - 1; i >= 0; i--) {
                            if (tovOptTplList.get(i).getOptionControlName() != Globals.OptionControlName.AKCIYA) {
                                showDialog(list, tovOptTplList.get(i), finalReportPrepareTovar, tovarId, String.valueOf(codeDad2), clientId, finalBalanceData1, finalBalanceDate1, false);
                            }
                        }
                        Collections.reverse(dialogList);
                        dialogList.get(0).show();
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_7.1", "Exception e: " + e);
                    }


                    return true;
                });
            } else {// Если такого товара НЕТ
                constraintLayout.setOnClickListener(v -> {

                    ReportPrepareDB rp = createNewRPRow(list);

                    Log.e("DRAdapterTovar", "ClickListenerТовара нет");

                    try {
                        // Получаем инфу об обязательных опциях
                        List<TovarOptions> tovOptTplList = options.getRequiredOptionsTPL(optionsList2);

                        // В Цикле открываем Н количество инфы
                        Collections.reverse(tovOptTplList); // Реверснул что б отображалось более менее адекватно пользователю (в естественном порядке)
                        for (TovarOptions tpl : tovOptTplList) {
                            if (tpl.getOptionControlName() != Globals.OptionControlName.AKCIYA) {
                                showDialog(list, tpl, rp, tovarId, String.valueOf(codeDad2), clientId, "", "", false);
                            }
                        }
                        Collections.reverse(dialogList);
                        dialogList.get(0).show();
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_8", "Exception e: " + e);
                        globals.alertDialogMsg(mContext, "Не удалось открыть Опцию. Если ошибка повторяется - обратитесь к своему руководителю.\n\nОшибка: " + e);
                    }

                });
            }


            boolean b = setTovPhoto(list);
            if (b) {
                imageView.setOnClickListener(v -> {
                    Log.e("ФОТО_ТОВАРОВ", "Click");
                    Exchange exchange = new Exchange();
                    exchange.getTovarImg(Collections.singletonList(list), "full", new Globals.OperationResult() {
                        @Override
                        public void onSuccess() {
                            Log.e("ФОТО_ТОВАРОВ", "onSuccess");
                            displayFullSizeTovarPhotoDialog(list);
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("ФОТО_ТОВАРОВ", "onFailure");
                            displayFullSizeTovarPhotoDialog(list);
                        }
                    });

                });
            } else {
                imageView.setOnClickListener(v -> {
                    Toast.makeText(mContext, "Фото товара не обнаружено", Toast.LENGTH_LONG).show();
                });

                imageView.setOnLongClickListener(view -> {
                    PhotoDownload.getPhotoURLFromServer(Collections.singletonList(list), new Clicks.clickStatusMsg() {
                        @Override
                        public void onSuccess(String data) {
                            Log.d("t", "t:" + data);
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.d("t", "te:" + error);
                        }
                    });

                    return false;
                });
            }
        }


        /*
         * 15.09.2022.
         * Отображение плана по фейсам.
         *
         * Если Плана по фейсам нет - поле вообще не нужно отображать.
         * */
        private void showFacePlan(ReportPrepareDB reportPrepareDB) {
            if (reportPrepareDB != null && reportPrepareDB.facesPlan != null && reportPrepareDB.facesPlan > 0) {
                facePlan.setVisibility(View.VISIBLE);
                facePlan.setText("План: " + reportPrepareDB.facesPlan + " фейс.");
                facePlan.setOnClickListener(view -> {
                    Toast.makeText(mContext, "План по фейсам равен: " + reportPrepareDB.facesPlan, Toast.LENGTH_SHORT).show();
                });
            } else {
                facePlan.setVisibility(View.GONE);
            }
        }


        /**
         * 09.03.2021
         * Проверка "устарелости" данных
         * <p>
         * Возвращает true - есть дата остатков старше 30 дней
         * <p>
         * / 30 дней в миллисекундах == 2592000000
         * / Дата старше 30 дней или нет
         *
         * @param date -- дата которую проверяем
         */
        // TODO работа с датой. проверяю устарелость.
        private OldDateOstatok isOldOstatokDate(Long date) {
            // Если данных нет - считаю их устаревшими
            // 1667772000000
            // 1676298698480
            if (date == null) {
                return OldDateOstatok.ELDEST;
            }

            long dateMill = date * 1000;

            // Если дата остатков больше -30 дней -- значит они уже устарели
            long ostatok30day = System.currentTimeMillis() - 2592000000L;
            if (dateMill <= ostatok30day) {
                return OldDateOstatok.ELDEST;
            }

            // Если дата остатков больше -14 дней -- значит они ещё актуальны, иначе - нет.
            long ostatok14day = System.currentTimeMillis() - 1209600000L;
            if (dateMill <= ostatok14day) {
                return OldDateOstatok.OLD;
            }

            return OldDateOstatok.NEW;
        }


        private boolean setTovPhoto(TovarDB tovar) {

            int tovId = Integer.parseInt(tovar.getiD());

            StackPhotoDB stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(tovId, tovar.photoId, 18, false);

            if (stackPhotoDB != null) {
                if (stackPhotoDB.getObject_id() == tovId) {
                    Log.e("R_TOVAR", "ФОТО ЕСТЬ(" + stackPhotoDB.getObject_id() + "/" + tovId + ")");

                    if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                        File file = new File(stackPhotoDB.getPhoto_num());
                        Log.e("R_TOVAR", "PATH: " + file.getPath());

                        if (file.length() > 0) {
                            Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
                            if (b != null) {
                                imageView.setImageBitmap(b);
                            }
                            return true;
                        }

                    }

                }
            }
            return false;
        }


        /**
         * 09.03.2021
         * Новое отображение фото в полном размере
         */
        private void displayFullSizeTovarPhotoDialog(TovarDB tovar) {
            int tovId = Integer.parseInt(tovar.getiD());
            StackPhotoDB stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(tovId, tovar.photoId, 18, true);
            if (stackPhotoDB != null) {
                if (stackPhotoDB.getObject_id() == tovId) {
                    if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                        Log.e("ФОТО_ТОВАРОВ", "displayFullSizeTovarPhotoDialog: " + Uri.parse(stackPhotoDB.getPhoto_num()));

                        DialogPhotoTovar dialogPhotoTovar = new DialogPhotoTovar(mContext);

                        dialogPhotoTovar.setPhotoTovar(Uri.parse(stackPhotoDB.getPhoto_num()));

                        StringBuilder sb = new StringBuilder();
                        sb.append("Штрихкод: ").append(tovar.getBarcode()).append("\n");
                        sb.append("Артикул: ").append(getArticle(tovar, 0));


                        dialogPhotoTovar.setPhotoBarcode(tovar.getBarcode());
//                        dialogPhotoTovar.setPhotoBarcode(sb);

                        dialogPhotoTovar.setTextInfo(sb);

                        dialogPhotoTovar.setClose(dialogPhotoTovar::dismiss);
                        dialogPhotoTovar.show();

                        Log.e("ФОТО_ТОВАРОВ", "Вроде отобразил диалог");
                    }
                }
            } else {
                stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(tovId, tovar.photoId, 18, false);
                if (stackPhotoDB != null) {
                    if (stackPhotoDB.getObject_id() == tovId) {
                        if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                            Log.e("ФОТО_ТОВАРОВ", "displayFullSizeTovarPhotoDialog: " + Uri.parse(stackPhotoDB.getPhoto_num()));

                            DialogPhotoTovar dialogPhotoTovar = new DialogPhotoTovar(mContext);

                            dialogPhotoTovar.setPhotoTovar(Uri.parse(stackPhotoDB.getPhoto_num()));
                            dialogPhotoTovar.setPhotoBarcode(tovar.getBarcode());

                            dialogPhotoTovar.setClose(dialogPhotoTovar::dismiss);
                            dialogPhotoTovar.show();

                            Log.e("ФОТО_ТОВАРОВ", "Вроде отобразил диалог");
                        }
                    }
                }
            }
        }


        /**
         * 11.01.2021
         * Отображаю диалог для внесения данных в Опции Контроля (Цена, фейс, количество...)
         */
        private List<DialogData> dialogList = new ArrayList<>();

        /**
         * 29.03.23
         * boolean clickType - добавлено для того что б различать долгий/короткий клик
         * true - короткий
         * false - длинный
         */
        private void showDialog(TovarDB list, TovarOptions tpl, ReportPrepareDB reportPrepareDB, String tovarId, String cd2, String clientId, String finalBalanceData1, String finalBalanceDate1, boolean clickType) {
            try {
                final int adapterPosition = getAdapterPosition();

                DialogData dialog = new DialogData(mContext);
                dialog.setTitle("");
                dialog.setText("");
                dialog.setClose(dialog::dismiss);
                dialog.setLesson(mContext, true, 802);
                dialog.setVideoLesson(mContext, true, 803, null);
                dialog.setImage(true, getPhotoFromDB(list));
                dialog.setAdditionalText(setPhotoInfo(reportPrepareDB, tpl, list, finalBalanceData1, finalBalanceDate1));

                // Сделано для того что б можно было контролировать какая опция сейчас открыта
                dialog.tovarOptions = tpl;
                dialog.reportPrepareDB = reportPrepareDB;

                // Устанавливаем дату для операций (в данной реализации только для DoubleSpinner & EditTextAndSpinner)
                switch (tpl.getOptionControlName()) {
//                case ERROR_ID:
//                    dialog.setOperationSpinnerData(setMapData(tpl.getOptionControlName()));
//
//                    dialog.setOperationTextData(reportPrepareDB.getErrorId());
//                    dialog.setOperationTextData2(reportPrepareDB.getErrorComment());
//                    break;

                    case AKCIYA_ID:
                        dialog.setOperationSpinnerData(setMapData(tpl.getOptionControlName()));
                        dialog.setOperationSpinner2Data(setMapData(Globals.OptionControlName.AKCIYA));

                        PromoDB promoDB = PromoRealm.getPromoDBById(reportPrepareDB.getAkciyaId());
                        dialog.setOperationTextData(promoDB != null ? promoDB.getNm() : reportPrepareDB.getAkciyaId());

                        Map<String, String> map = new HashMap<>();
                        map.put("2", "Акция отсутствует");
                        map.put("1", "Есть акция");

                        String akciya = map.get(reportPrepareDB.getAkciya());

                        dialog.setOperationTextData2(akciya);
                        break;

                }

                if (tpl.getOptionControlName() != null && tpl.getOptionControlName().equals(ERROR_ID)) {    // Работа с ошибками
                    dialog.setExpandableListView(createExpandableAdapter(dialog.context), () -> {
                        if (dialog.getOperationResult() != null) {
                            operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
                            refreshElement(cd2, list.getiD());
                        }

                        dialogShowRule();

                        notifyItemChanged(adapterPosition);
                    });
                } else {
                    dialog.setOperation(operationType(tpl), getCurrentData(tpl, cd2, tovarId), setMapData(tpl.getOptionControlName()), () -> {
                        if (dialog.getOperationResult() != null) {
                            operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
                            Toast.makeText(mContext, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
                            refreshElement(cd2, list.getiD());
                        }

                        dialogShowRule();

                        notifyItemChanged(adapterPosition);
                    });
                }

                dialog.setCancel("Пропустить", () -> {
                    dialog.dismiss();
                    dialogShowRule();
                });

                if (!tpl.getOptionControlName().equals(AKCIYA_ID) && !tpl.getOptionControlName().equals(AKCIYA)) {
                    String mod = "report_prepare";
                    String act = "get_param_stats";
                    retrofit2.Call<ReportHint> call = RetrofitBuilder.getRetrofitInterface().GET_REPORT_HINT(mod, act, tovarId, cd2, clientId);
                    call.enqueue(new retrofit2.Callback<ReportHint>() {
                        @Override
                        public void onResponse(retrofit2.Call<ReportHint> call, retrofit2.Response<ReportHint> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ReportHint reportHint = response.body();
                                if (reportHint.getState()) {
                                    for (ReportHintList item : reportHint.getList()) {
                                        if (tpl.getOrderField().equals(item.getField())) {
                                            dialog.setAdditionalOperation(setAdapter(tpl, reportHint.getList(), value -> {
                                                operetionSaveRPToDB(tpl, reportPrepareDB, value, dialog.getOperationResult2(), null);
                                                Toast.makeText(mContext, "Внесено: " + value, Toast.LENGTH_LONG).show();
                                                refreshElement(cd2, list.getiD());
                                                notifyItemChanged(adapterPosition);
                                                dialog.dismiss();
                                            }), setLayout());
                                        }
                                    }
//                            dialog.show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ReportHint> call, Throwable t) {
//                    dialog.show();
                        }
                    });
                }

                dialogList.add(dialog);

            } catch (Exception e) {
                Log.d("test", "test" + e);
            }
        }

        /**
         * 29.03.23.
         * Специальное правило по которому отображаю последовательно модальные окошки из
         * списка dialogList.
         */
        private void dialogShowRule() {
            dialogList.remove(0);
            if (dialogList.size() > 0) {
                int face = 0;
                if (dialogList.get(0).reportPrepareDB.face != null && !dialogList.get(0).reportPrepareDB.face.equals(""))
                    face = Integer.parseInt(dialogList.get(0).reportPrepareDB.face);
                if (dialogList.get(0).tovarOptions.getOptionControlName().equals(ERROR_ID) && (dialogList.get(0).tovarOptions.getOptionId().contains(157242) || dialogList.get(0).tovarOptions.getOptionId().contains(157241) || dialogList.get(0).tovarOptions.getOptionId().contains(157243)) && face > 0) {
                    // НЕ отображаю модальное окно и удаляю его. Уникальное правило потому что потому.
                    dialogList.remove(0);
                } else {
                    dialogList.get(0).show();
                }
            }
        }

        private MySimpleExpandableListAdapter createExpandableAdapter(Context context) {

            Map<String, String> map;
            ArrayList<Map<String, String>> groupDataList = new ArrayList<>();

            // список атрибутов групп для чтения
            String[] groupFrom = new String[]{"groupName"};
            // список ID view-элементов, в которые будет помещены атрибуты групп
            int groupTo[] = new int[]{android.R.id.text1};

            // список атрибутов элементов для чтения
            String childFrom[] = new String[]{"monthName"};
            // список ID view-элементов, в которые будет помещены атрибуты
            // элементов
            int childTo[] = new int[]{android.R.id.text1};

            // создаем общую коллекцию для коллекций элементов
            ArrayList<ArrayList<Map<String, String>>> сhildDataList = new ArrayList<>();
            // создаем коллекцию элементов для первой группы
            ArrayList<Map<String, String>> сhildDataItemList = new ArrayList<>();

            // Получение данных с БД
            RealmResults<ErrorDB> errorDbList = RealmManager.getAllErrorDb();
            RealmResults<ErrorDB> errorGroupsDB = errorDbList.where().equalTo("parentId", "0").findAll();

            for (ErrorDB group : errorGroupsDB) {
                map = new HashMap<>();
                map.put("groupName", group.getNm());
                groupDataList.add(map);

                RealmResults<ErrorDB> errorItemsDB = errorDbList.where().equalTo("parentId", group.getID()).findAll();
                if (errorItemsDB != null && errorItemsDB.size() > 0) {
                    сhildDataItemList = new ArrayList<>();
                    for (ErrorDB item : errorItemsDB) {
                        map = new HashMap<>();
                        map.put("monthName", "* " + item.getNm());
                        сhildDataItemList.add(map);
                    }
                    сhildDataList.add(сhildDataItemList);
                } else {
                    сhildDataItemList = new ArrayList<>();
                    map = new HashMap<>();
                    map.put("monthName", "* " + group.getNm());
                    сhildDataItemList.add(map);
                    сhildDataList.add(сhildDataItemList);
                }
            }

            MySimpleExpandableListAdapter adapter = new MySimpleExpandableListAdapter(
                    context, groupDataList,
                    android.R.layout.simple_expandable_list_item_1, groupFrom,
                    groupTo, сhildDataList, android.R.layout.simple_list_item_1,
                    childFrom, childTo);

            return adapter;
        }

        private Map<String, String> setMapData(Globals.OptionControlName optionControlName) {
            Map<String, String> map = new HashMap<>();
            switch (optionControlName) {
                case ERROR_ID:
                    RealmResults<ErrorDB> errorDbList = RealmManager.getAllErrorDb();
                    for (int i = 0; i < errorDbList.size(); i++) {
                        if (errorDbList.get(i).getNm() != null && !errorDbList.get(i).getNm().equals("")) {
                            map.put(errorDbList.get(i).getID(), errorDbList.get(i).getNm());
                        }
                    }
                    return map;

                case AKCIYA_ID:
                    RealmResults<PromoDB> promoDbList = RealmManager.getAllPromoDb();
                    for (int i = 0; i < promoDbList.size(); i++) {
                        if (promoDbList.get(i).getNm() != null && !promoDbList.get(i).getNm().equals("")) {
                            map.put(promoDbList.get(i).getID(), promoDbList.get(i).getNm());
                        }
                    }
                    return map;

                case AKCIYA:
                    map.put("2", "Акция отсутствует");
                    map.put("1", "Есть акция");

                    return map;

                default:
                    return null;
            }
        }


        /**
         * 11.01.2021
         * Заполнение обекта с данными о фото
         *
         * @param tpl               -- TODO заполнить описание
         * @param tovar             -- TODO заполнить описание
         * @param finalBalanceData1
         * @param finalBalanceDate1
         */
        private PhotoDescriptionText setPhotoInfo(ReportPrepareDB reportPrepareDB, TovarOptions tpl, TovarDB tovar, String finalBalanceData1, String finalBalanceDate1) {
            PhotoDescriptionText res = new PhotoDescriptionText();

            try {
                String weightString = String.format("%s, %s", tovar.getWeight(), tovar.getBarcode()); // составление строк веса и штрихкода для того что б выводить в одно поле
                Log.e("КОСТЫЛИ", "tpl.getOptionLong(): " + tpl.getOptionLong());

                String title = tpl.getOptionLong();

                if (DetailedReportActivity.rpThemeId == 1178) {
                    if (tpl.getOptionId().contains(578) || tpl.getOptionId().contains(1465)) {
                        title = "Кол-во выкуп. товара";
                    }

                    if (tpl.getOptionId().contains(579)) {
                        title = "Цена выкуп. товара";
                    }
                }

                if (DetailedReportActivity.rpThemeId == 33) {
                    if (tpl.getOptionId().contains(587)) {
                        title = "Кол-во заказанного товара";
                    }
                }

                res.row1Text = title;
                res.row1TextValue = "";
                res.row2TextValue = tovar.getNm();
                res.row3TextValue = weightString;
                Log.e("ПРОИЗВОДИТЕЛЬ", "2ШТО ТУТ?:" + RealmManager.getNmById(tovar.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "");

                res.row4TextValue = RealmManager.getNmById(tovar.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "";

                res.row5Text = "Ост.:";
                res.row5TextValue = finalBalanceData1 + " шт на " + finalBalanceDate1;

                if (reportPrepareDB.facesPlan != null && reportPrepareDB.facesPlan > 0) {
                    res.row6Text = "План фейс.:";
                    res.row6TextValue = "" + reportPrepareDB.facesPlan;
                }
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.setPhotoInfo", "Exception e: " + e);
            }
            return res;
        }


        /**
         * 18.01.2021
         * Обновление опций (фейс, уена, кол-во ..)
         *
         * @param cd2
         * @param id
         */
        private void refreshElement(String cd2, String id) {
            boolean deletePromoOption;
            int tovId = Integer.parseInt(id);
            ReportPrepareDB reportPrepareTovar = RealmManager.getTovarReportPrepare(cd2, id);
            ReportPrepareDB reportPrepareTovar2 = INSTANCE.copyFromRealm(reportPrepareTovar);

            deletePromoOption = tovIdList.contains(tovId);

            List<OptionsDB> optionsList2 = RealmManager.getTovarOptionInReportPrepare(cd2, id);
            Options options = new Options();
            String s = options.getOptionString(optionsList2, reportPrepareTovar2, deletePromoOption);

            Log.e("refreshElement", "s: " + s);

            textViewItemTovarOptLine.setText(Html.fromHtml(s));
        }


        /**
         * 11.01.2021
         * Установкаа типа операции
         */
        private Operations operationType(TovarOptions tpl) {
            switch (tpl.getOrderField()) {
                case ("price"):
                case ("face"):
                case ("expire_left"):
                case ("amount"):
                case ("oborotved_num"):
                case ("up"):
                    return Number;

                case ("dt_expire"):
                    return Date;


                case ("akciya_id"):
//                case ("akciya"):
                    return DoubleSpinner;

                case ("error_id"):
                    return EditTextAndSpinner;

                case ("notes"):
                    return Text;

                default:
                    return Text;
            }
        }


        /**
         * 15.01.2021
         * <p>
         * Функционал в зависимости от операции
         */
        private void operetionSaveRPToDB(TovarOptions tpl, ReportPrepareDB rp, String data, String data2, TovarDB tovarDB) {
            if (rp == null) {
                rp = createNewRPRow(tovarDB);
            }

            if (data == null || data.equals("")) {
                Toast.makeText(mContext, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
                return;
            }

            ReportPrepareDB table = rp;
            switch (tpl.getOptionControlName()) {
                case PRICE:
                    Log.e("SAVE_TO_REPORT_OPT", "PRICE: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setPrice(data);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case FACE:
                    Log.e("SAVE_TO_REPORT_OPT", "FACE: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setFace(data);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case EXPIRE_LEFT:
                    Log.e("SAVE_TO_REPORT_OPT", "EXPIRE_LEFT: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setExpireLeft(data);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case AMOUNT:
                    Log.e("SAVE_TO_REPORT_OPT", "AMOUNT: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setAmount(Integer.parseInt(data));
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case OBOROTVED_NUM:
                    Log.e("SAVE_TO_REPORT_OPT", "OBOROTVED_NUM: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setOborotvedNum(data);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case UP:
                    Log.e("SAVE_TO_REPORT_OPT", "UP: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setUp(data);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case DT_EXPIRE:
                    Log.e("SAVE_TO_REPORT_OPT", "DT_EXPIRE: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setDtExpire(data);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case ERROR_ID:
                    Log.e("SAVE_TO_REPORT_OPT", "ERROR_ID: " + data);
                    Log.e("SAVE_TO_REPORT_OPT", "ERROR_COMMENT: " + data2);
                    INSTANCE.executeTransaction(realm -> {
                        table.setErrorId(data);
                        table.setErrorComment(data2);
                        table.setNotes(data2);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

                case AKCIYA_ID:
                    Log.e("SAVE_TO_REPORT_OPT", "AKCIYA_ID: " + data);
                    Log.e("SAVE_TO_REPORT_OPT", "AKCIYA_ID_А: " + data2);
                    INSTANCE.executeTransaction(realm -> {
                        table.setAkciyaId(data);
                        if (data2 != null && !data2.equals("")) {
                            table.setAkciya(data2);
                        }
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

//                case AKCIYA:
//                    Log.e("SAVE_TO_REPORT_OPT", "AKCIYA: " + data);
//                    INSTANCE.executeTransaction(realm -> {
//                        table.setAkciya(data2);     // 25.03.23. Поменял с Дата на Дата2 потому что в выпадающем списке ТПЛов боюсь что запутаются данные с AKCIYA_ID. Сделалл так что б было одинаково и там и там
//                        table.setUploadStatus(1);
//                        table.setDtChange(System.currentTimeMillis() / 1000);
//                        RealmManager.setReportPrepareRow(table);
//                    });
//                    break;

                case NOTES:
                    Log.e("SAVE_TO_REPORT_OPT", "NOTES: " + data);
                    INSTANCE.executeTransaction(realm -> {
                        table.setNotes(data);
                        table.setUploadStatus(1);
                        table.setDtChange(System.currentTimeMillis() / 1000);
                        RealmManager.setReportPrepareRow(table);
                    });
                    break;

            }
        }


        /**
         * 15.01.2021
         * <p>
         * Функционал в зависимости от операции
         */
        private String getCurrentData(TovarOptions tpl, String cd, String id) {
            ReportPrepareDB table = RealmManager.getTovarReportPrepare(cd, id);
            switch (tpl.getOptionControlName()) {
                case PRICE:
                    return table.getPrice();

                case FACE:
                    return table.getFace();

                case EXPIRE_LEFT:
                    return table.getExpireLeft();

                case AMOUNT:
                    return String.valueOf(table.getAmount());

                case OBOROTVED_NUM:
                    return table.getOborotvedNum();

                case UP:
                    return table.getUp();

                case DT_EXPIRE:
                    return table.getDtExpire();

                case ERROR_ID:
                    return table.getErrorId();

                case AKCIYA_ID:
                    return table.getAkciyaId();

                case AKCIYA:
                    return table.getAkciya();

                case NOTES:
                    return table.getNotes();

            }

            return null;
        }


        private RecyclerView.LayoutManager setLayout() {
            return new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false);
        }

        private RecyclerView.Adapter setAdapter(TovarOptions tpl, List<ReportHintList> reportHintLists, RecyclerViewOptionControlHint.HintListener listener) {
            List<RecentItem> recentItems = null;
            for (ReportHintList o : reportHintLists) {   // Пробегаемся по всему ответу в поиске подходящего поля
                if (o.getField().equals(tpl.getOrderField())) {   // Если нужное поле найдено - получаем и отображаем данные подсказок
                    recentItems = o.getRecentItems();
                }
            }

            List<RecentItem> finalRecentItems = recentItems;
            if (finalRecentItems.size() == 0) {
                return null;
            }
            return new RecyclerViewOptionControlHint(finalRecentItems, listener);
        }

    }

    private ReportPrepareDB createNewRPRow(TovarDB list) {
        ReportPrepareDB rp = new ReportPrepareDB();

        long id = RealmManager.reportPrepareGetLastId();
        id = id + 1;

        rp.setID(id);
        rp.setDt(String.valueOf(System.currentTimeMillis()));
        rp.setDtReport(String.valueOf(System.currentTimeMillis()));
        rp.setKli(clientId);
        rp.setTovarId(list.getiD());
        rp.setAddrId(String.valueOf(addressId));
        rp.setPrice("");
        rp.setFace("");
        rp.setAmount(0);
        rp.setDtExpire("");
        rp.setExpireLeft("");
        rp.setNotes("");
        rp.setUp("");
        rp.setAkciya("");
        rp.setAkciyaId("");
        rp.setOborotvedNum("");
        rp.setErrorId("");
        rp.setErrorComment("");
        rp.setCodeDad2(String.valueOf(codeDad2));

        // TODO сохранение в БД новой строки что б потом работать с ней в getCurrentData()
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(rp);
        INSTANCE.commitTransaction();
        return rp;
    }


    private File getPhotoFromDB(TovarDB tovar) {

        int id = Integer.parseInt(tovar.getiD());

        StackPhotoDB stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(id, tovar.photoId, 18, false);
        if (stackPhotoDB != null) {
            if (stackPhotoDB.getObject_id() == id) {
                Log.e("R_TOVAR", "ФОТО ЕСТЬ(" + stackPhotoDB.getObject_id() + "/" + id + ")");
                if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                    File file = new File(stackPhotoDB.getPhoto_num());
                    Log.e("R_TOVAR", "PATH: " + file.getPath());
                    return file;
                }
            }
        }
        return null;
    }


    // ------------ FILTER ------------
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<TovarDB> filteredResults = null;

                if (constraint.length() == 0) {
                    filteredResults = dataFilterable;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter(mContext).getFilteredResultsTOV(item, filteredResults, dataFilterable);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dataList = (List<TovarDB>) results.values;

                Toast toast = Toast.makeText(mContext, "Отобрано: " + dataList.size() + " товаров", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                notifyDataSetChanged();
            }

//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                List<TovarDB> filteredResults = null;
//                if (constraint.length() == 0) {
//                    filteredResults = dataList;
//                } else {
//                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
//                }
//
//                FilterResults results = new FilterResults();
//                results.values = filteredResults;
//                return results;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                dataList = (List<TovarDB>) results.values;
//                notifyDataSetChanged();
//            }

        };


    }

    private List<TovarDB> getFilteredResults(String constraint) {
        List<TovarDB> results = new ArrayList<>();
        for (TovarDB item : dataFilterable) {
            if (item.getNm().toLowerCase().contains(constraint)) {
                results.add(item);
            } else if (item.getBarcode().toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }
        return results;
    }

}
