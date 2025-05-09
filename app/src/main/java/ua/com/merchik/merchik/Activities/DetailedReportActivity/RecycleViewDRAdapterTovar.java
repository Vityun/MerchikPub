package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static java.lang.System.out;
import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA;
import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.DT_EXPIRE;
import static ua.com.merchik.merchik.Globals.OptionControlName.ERROR_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.PHOTO;
import static ua.com.merchik.merchik.Globals.OptionControlName.UP;
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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
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

import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.realm.RealmResults;
import retrofit2.Call;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.Utils.CustomRecyclerView;
import ua.com.merchik.merchik.Utils.MySimpleExpandableListAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.OborotVedSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.RecentItem;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportHint;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportHintList;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;
import ua.com.merchik.merchik.database.realm.tables.PromoRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.AdditionalRequirementsAdapter;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogPhotoTovar;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class RecycleViewDRAdapterTovar extends RecyclerView.Adapter<RecycleViewDRAdapterTovar.ViewHolder> implements Filterable {

    private final Context mContext;
    private List<TovarDB> dataList;
    private List<TovarDB> dataFilterable;
    private WpDataDB wpDataDB;
    private DRAdapterTovarTPLTypeView tplType;
    private OpenType openType;

    private List<Integer> tovIdList;
    private List<AdditionalRequirementsDB> adList;

    private Clicks.clickVoid click;
    private Clicks.click clickTovar;


    private long codeDad2;
    private String clientId;
    private int addressId;
    private boolean deletePromoOption = false;

    // Получаем инфу об обязательных опциях
    private List<TovarOptions> tovOptTplList;

    public enum OpenType {
        DEFAULT, DIALOG
    }

    public enum DRAdapterTovarTPLTypeView {
        GONE, FULL
    }

    public enum OldDateOstatok {
        ELDEST, OLD, NEW
    }

    // 08.04.24.
    boolean openNext = true;

    /*Определяем конструктор*/
    public RecycleViewDRAdapterTovar(Context context, List<TovarDB> list, WpDataDB wp, OpenType openType) {
        Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/ENTER");
        this.mContext = context;
        this.dataList = list;
        this.dataFilterable = list;
        this.openType = openType;

        this.wpDataDB = wp;
        codeDad2 = wp.getCode_dad2();
        clientId = wp.getClient_id();
        addressId = wp.getAddr_id();


        tplType = DRAdapterTovarTPLTypeView.GONE;
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapterTovar.RecycleViewDRAdapterTovar", "list.size(): " + list.size());
    }

    public RecycleViewDRAdapterTovar(Context context, List<TovarDB> list, TasksAndReclamationsSDB tasksAndReclamationsSDB, OpenType openType) {
        Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/ENTER2");
        this.mContext = context;
        this.dataList = list;
        this.dataFilterable = list;
        this.openType = openType;

        codeDad2 = tasksAndReclamationsSDB.codeDad2SrcDoc;
        clientId = tasksAndReclamationsSDB.client;
        addressId = tasksAndReclamationsSDB.addr;

        tplType = DRAdapterTovarTPLTypeView.GONE;
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapterTovar.RecycleViewDRAdapterTovar", "list.size(): " + list.size());
    }

    /**
     * 10.04.23.
     * получаем текущие данные с ресайклера.
     */
    public List<TovarDB> getAdapterDataList() {
        return this.dataList;
    }


    public void updateAdapterData(List<TovarDB> data) {
        this.dataList = data;
    }

    public void setAkciyaTovList(List<Integer> tovIdList, List<AdditionalRequirementsDB> adList) {
        Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/setAkciyaTovList");
        this.tovIdList = tovIdList;
        this.adList = adList;
        Log.e("АКЦИЯ_ТОВАРА", "tovIdList: " + tovIdList);
    }

    @Override
    public RecycleViewDRAdapterTovar.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dr_tovar_item_tovar, parent, false);
//        mContext = parent.getContext();
        return new RecycleViewDRAdapterTovar.ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bind(dataList.get(position));
    }


    @Override
    public int getItemCount() {
        try {
            return dataList.size();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.getItemCount", "Exception e: " + e);
            return 0;
        }
    }

    public void refreshAdapter(Clicks.clickVoid click) {
        this.click = click;
    }

    public void elementClick(Clicks.click click) {
        this.clickTovar = click;
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

//        TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading(); // Может выше поднять?

        // Resourse
        ConstraintLayout constraintLayout;
        ImageView imageView;
        TextView name;
        TextView weight;
        TextView tovGroup;
        TextView tradeMark;
        TextView textViewItemTovarOptLine, article;
        //        RecyclerView recyclerView;
        CustomRecyclerView recyclerView;

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
            Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/ViewHolder");

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
            recyclerView.setMode(1);

            if (openType.equals(OpenType.DIALOG)) {
                textViewItemTovarOptLine.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }


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

        //        @RequiresApi(api = Build.VERSION_CODES.N)
        public void bind(TovarDB list) {
            try {
                Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/bind/list: " + list);

                String balanceData = "?";
                String balanceDate = "?";

                String tovarId = list.getiD();


                imageView.setImageResource(R.mipmap.merchik);

                String weightString = String.format("%s, %s", list.getWeight(), list.getBarcode());
                name.setText(list.getNm() + " (" + list.getiD() + ")");
                name.setTextSize(16);
                weight.setText(weightString);
                weight.setTextSize(16);

                weight.setOnLongClickListener(v -> {
                    // Получение объекта ClipboardManager из системы
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

                    // Создание объекта ClipData для копирования текста в буфер обмена
                    ClipData clip = ClipData.newPlainText("Штрихкод", list.getBarcode());

                    // Копирование ClipData в буфер обмена
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(mContext, "Скопировано в буфер обмена: " + list.getBarcode(), Toast.LENGTH_LONG).show();
                    return false;
                });

                article.setText(getArticle(list, 1));

                ReportPrepareDB reportPrepareTovar = RealmManager.getTovarReportPrepare(String.valueOf(codeDad2), list.getiD());
                ReportPrepareDB reportPrepareTovar2 = null;
                List<OptionsDB> optionsList2 = RealmManager.getTovarOptionInReportPrepare(String.valueOf(codeDad2), list.getiD());


//                List<OptionsDB> test = RealmManager.INSTANCE.copyFromRealm(optionsList2);

/*                StringBuilder sb = new StringBuilder();
                for (OptionsDB item : test){
                    Gson gson = new Gson();
                    StringWriter stringWriter = new StringWriter();
                    JsonWriter jsonWriter = new JsonWriter(stringWriter);
                    jsonWriter.setIndent(" "); // Установка отступа, чтобы сделать вывод более читабельным

                    gson.toJson(item, item.getClass(), jsonWriter);

                    String debugOpt = stringWriter.toString();

//                    String debugOpt = new Gson().toJson(item);
                    Log.e("optionsList2", debugOpt);
                }
                Log.e("optionsList2", sb.toString());*/


                try {
                    Drawable background = constraintLayout.getBackground();

                    Log.e("АКЦИЯ_ТОВАРА", "TEST1: " + tovIdList);
                    Log.e("АКЦИЯ_ТОВАРА", "TEST2: " + list.getiD());

                    int id = Integer.parseInt(list.getiD());
                    Log.e("АКЦИЯ_ТОВАРА", "TEST3: " + tovIdList.contains(id));


                    if (tovIdList.contains(id)) {
                        Log.e("АКЦИЯ_ТОВАРА", "YELLOW " + list.getiD());

                        AdditionalRequirementsDB ad = showTovarAdditionalRequirement(list);

                        if (ad != null && ad.color != null && !ad.color.equals("")) {
                            int color = Color.parseColor("#" + ad.color);
                            Drawable coloredBackground = new ColorDrawable(color);
                            constraintLayout.setBackground(coloredBackground);
                        } else {
                            if (background instanceof ShapeDrawable) {
                                ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(mContext, R.color.yellow));
                            } else if (background instanceof GradientDrawable) {
                                ((GradientDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.yellow));
                            } else if (background instanceof ColorDrawable) {
                                ((ColorDrawable) background).setColor(ContextCompat.getColor(mContext, R.color.yellow));
                            }
                        }


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Optional<AdditionalRequirementsDB> result;
                            result = adList.stream()
                                    .filter(obj -> obj.getOptionId().equals("80977"))
                                    .findFirst();
                            if (result.isPresent()/* && !Options.optionConstraintTPL(optionsList2)*/) {
                                deletePromoOption = false;

                            } else {

                                deletePromoOption = true;
                            }
                        } else {
                            deletePromoOption = false;
                        }

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
                            java.util.Date df = new Date(ostatokDate * 1000);
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

                            CharSequence finalBalance = Html.fromHtml("<b>Кон. Ост. </b>(" + data.get(data.size() - 1).dat + "): " + data.get(data.size() - 1).kolOst + "<br>");
                            oborotVed.append(finalBalance);

                        } catch (Exception e) {
                            Log.e("OBOROT_VED", "Exception e: " + e);
                            Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_4.клик по балансу", "Exception e: " + e);
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
                        dialog.show();
                    });

                    showFacePlan(rp);

                } catch (Exception err) {
                    Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_6", "Exception e: " + err);
                }

                //================================================


//                WorkPlan workPlan = new WorkPlan();

                Log.e("OPTIONS_TPL", "TOV_ID: " + list.getiD());

                dName.setText(list.getNm());
                dWeight.setText(weightString);
                closeDialog.setOnClickListener(v -> dialog.cancel());

                if (reportPrepareTovar != null) {
                    reportPrepareTovar2 = reportPrepareTovar;
                }

                // TODO Обрати внимание что тут по опциям
                String s = options.getOptionString(optionsList2, reportPrepareTovar2, deletePromoOption);

                try {
                    if (openType.equals(OpenType.DEFAULT)) {
                        ReportPrepareDB finalReportPrepareTovar1 = reportPrepareTovar2;

                        List<TovarOptions> requiredOptionsTPL = options.getRequiredOptionsTPL(optionsList2, deletePromoOption);
                        // Тут должно быть условие. Я его пока не добавляю. (если фейс = 0 и есть ОК 159707)
                        requiredOptionsTPL.add(new TovarOptions().createTovarOptionPhoto());
                        AdditionalRequirementsDB ar = showTovarAdditionalRequirement(list);
                        if (ar != null) {
                            requiredOptionsTPL.add(0, new TovarOptions().createLinkText());
                        }

                        RecyclerViewTPLAdapter recyclerViewTPLAdapter = new RecyclerViewTPLAdapter(
                                requiredOptionsTPL,
                                finalReportPrepareTovar1,
                                (tpl, data, data2) -> {
                                    OptionsDB option = OptionsRealm.getOptionControl(String.valueOf(codeDad2), "165276");
                                    if (option != null && operationType(tpl).equals(Date)) {
                                        long tovExpirationDate = list.expirePeriod * 86400;         // термін придатності товару. (дни перевожу в секунды)
                                        long dtCurrentWPData = wpDataDB.getDt().getTime() / 1000;   // дата посещения в секундах
                                        long dtUserSetToTovar = 0;                                  // То что указал в Дате окончания срока годности мерчик
                                        long resDays = 0;                                           // Дата текущего посещения + срок годности товара

                                        if (data != null && !data.equals("")) {
                                            dtUserSetToTovar = Clock.dateConvertToLong(data) / 1000;
                                        }

                                        resDays = dtCurrentWPData + tovExpirationDate;
                                        int exPer = list.expirePeriod;
                                        if (exPer != 0 && dtUserSetToTovar > resDays) {
                                            DialogData dialogBadData = new DialogData(mContext);
                                            dialogBadData.setTitle("Зауваження до Дати");
                                            dialogBadData.setText("Ви внесли некоректну дату закінчення терміну придатності! Відмовитись від її збереження?");
                                            dialogBadData.setOk("Так", () -> {
                                                dialogBadData.dismiss();
                                                Toast.makeText(mContext, "Дата не збережена!", Toast.LENGTH_LONG).show();
//                                                DialogData dialogBadData2 = new DialogData(mContext);
//                                                dialogBadData2.setTitle("Зауваження до Дати");
//                                                dialogBadData2.setText("Впевнені що не хочете зберігати некоректні данні?");
//                                                dialogBadData2.setOk("Так", () -> {
//                                                    dialogBadData2.dismiss();
//                                                });
//                                                dialogBadData2.setCancel("Ні", () -> {
//                                                    dialogBadData2.dismiss();
//                                                    operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list);
//                                                });
//                                                dialogBadData2.setClose(dialogBadData2::dismiss);
//                                                dialogBadData2.show();
                                            });
                                            dialogBadData.setCancel("Ні", () -> {
                                                dialogBadData.dismiss();
                                                operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list);
                                            });
                                            dialogBadData.setClose(dialogBadData::dismiss);
                                            dialogBadData.show();
                                        } else {
                                            operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list);
                                        }
                                    } else {
                                        operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list);
                                    }
                                }
                        );
                        recyclerViewTPLAdapter.setAddReq(ar, () -> {
                            showTovarAdditionalRequirementDialog(mContext, list, ar);
                        });

                        recyclerView.setAdapter(recyclerViewTPLAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                    }else {
                        ReportPrepareDB finalReportPrepareTovar1 = createNewRPRow(list);
                        List<TovarOptions> requiredOptionsTPL = options.getRequiredOptionsTPL(optionsList2, deletePromoOption);
                        RecyclerViewTPLAdapter recyclerViewTPLAdapter = new RecyclerViewTPLAdapter(
                                requiredOptionsTPL,
                                finalReportPrepareTovar1,
                                (tpl, data, data2) -> {
                                    OptionsDB option = OptionsRealm.getOptionControl(String.valueOf(codeDad2), "165276");
                                    if (option != null && operationType(tpl).equals(Date)) {
                                        long tovExpirationDate = list.expirePeriod * 86400;         // термін придатності товару. (дни перевожу в секунды)
                                        long dtCurrentWPData = wpDataDB.getDt().getTime() / 1000;   // дата посещения в секундах
                                        long dtUserSetToTovar = 0;                                  // То что указал в Дате окончания срока годности мерчик
                                        long resDays = 0;                                           // Дата текущего посещения + срок годности товара

                                        if (data != null && !data.equals("")) {
                                            dtUserSetToTovar = Clock.dateConvertToLong(data) / 1000;
                                        }

                                        resDays = dtCurrentWPData + tovExpirationDate;
                                        int exPer = list.expirePeriod;
                                        if (exPer != 0 && dtUserSetToTovar > resDays) {
                                            DialogData dialogBadData = new DialogData(mContext);
                                            dialogBadData.setTitle("Зауваження до Дати");
                                            dialogBadData.setText("Ви внесли некоректну дату закінчення терміну придатності! Відмовитись від її збереження?");
                                            dialogBadData.setOk("Так", () -> {
                                                dialogBadData.dismiss();
                                                Toast.makeText(mContext, "Дата не збережена!", Toast.LENGTH_LONG).show();
                                            });
                                            dialogBadData.setCancel("Ні", () -> {
                                                dialogBadData.dismiss();
                                                operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list);
                                            });
                                            dialogBadData.setClose(dialogBadData::dismiss);
                                            dialogBadData.show();
                                        } else {
                                            operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list);
                                        }
                                    } else {
                                        operetionSaveRPToDB(tpl, finalReportPrepareTovar1, data, data2, list);
                                    }
                                }
                        );

                        recyclerView.setAdapter(recyclerViewTPLAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERR", "RecyclerViewTPLAdapter", "Exception e: " + e);
                }

                if (tplType.equals(DRAdapterTovarTPLTypeView.FULL)) {
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                }

                Log.e("!!!!!!!!!!!!!!!!!","TEXT: " + s);
                Log.e("!!!!!!!!!!!!!!!!!","TEXT: " + Html.fromHtml("<u>" + s + "</u>"));

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
                        if (openType.equals(OpenType.DEFAULT)) {
                            try {
                                // Отображаем инфу по особенному Товару.
                                String tovId = list.getiD(); // Идентификатор Товара
                                Optional<AdditionalRequirementsDB> result = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    result = adList.stream()
                                            .filter(obj -> obj.getTovarId().equals(tovId) && obj.getOptionId().isEmpty())
                                            .findFirst();

                                    if (result.isPresent()) {
                                        AdditionalRequirementsDB foundObject = result.get();
                                        // Выполняйте операции с найденным объектом
                                        DialogData dialogMsg = new DialogData(mContext);
                                        dialogMsg.setTitle("Додаткова вимога до товару.");
                                        dialogMsg.setText(foundObject.getNm());
                                        dialogMsg.setClose(dialogMsg::dismiss);
                                        dialogMsg.show();
                                    } else {
                                        // Обработка случая, когда объект не найден
                                    }
                                }


                                //------------------------------------------------------------------

                                // На всякий случай зачищаю модальные окна.
                                dialogList = new ArrayList<>();

                                // Получаем инфу об обязательных опциях
                                tovOptTplList = options.getRequiredOptionsTPL(optionsList2, finalDeletePromoOption);

                                Log.e("DRAdapterTovar", "Кол-во. обязательных опций: " + tovOptTplList.size());

                                if (tovOptTplList.size() > 0) {
                                    // В Цикле открываем Н количество инфы
                                    for (int i = tovOptTplList.size() - 1; i >= 0; i--) {
                                        if (tovOptTplList.get(i).getOptionControlName() != AKCIYA) {
                                            if (tovOptTplList.get(i).getOptionControlName().equals(AKCIYA_ID) && finalDeletePromoOption) {
                                                // втыкаю
                                                Log.e("dialogShowRule", "1");
                                                showDialog(list, tovOptTplList.get(i), finalReportPrepareTovar, tovarId, String.valueOf(codeDad2), clientId, finalBalanceData1, finalBalanceDate1, true, true);
                                            } else {
                                                Log.e("dialogShowRule", "2");
                                                showDialog(list, tovOptTplList.get(i), finalReportPrepareTovar, tovarId, String.valueOf(codeDad2), clientId, finalBalanceData1, finalBalanceDate1, true, true);
                                            }
                                        }
                                    }


                                    Collections.reverse(dialogList);

                                    boolean optionExists = false;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        String opt = "159707";
                                        optionExists = optionsList2.stream().anyMatch(
                                                optionsDB -> optionsDB.getOptionId().equals(opt) ||
                                                        optionsDB.getOptionControlId().equals(opt));

                                        if (optionExists) {
                                            Optional<OptionsDB> matchingOption = optionsList2.stream()
                                                    .filter(optionsDB ->
                                                            optionsDB.getOptionId().equals(opt) ||
                                                                    optionsDB.getOptionControlId().equals(opt))
                                                    .findFirst();

                                            if (matchingOption.isPresent()) {
                                                OptionsDB optionsDB = matchingOption.get();
                                                // Делайте что-то с объектом OptionsDB
                                                out.println(optionsDB);
                                                dialogList.add(new TovarRequisites(list, finalReportPrepareTovar).createDialog(mContext, wpDataDB, optionsDB, ()->{}));

                                            } else {
                                                // Обработка случая, когда объект OptionsDB не найден
                                                out.println("Объект OptionsDB не найден");
                                            }
                                        }
                                    }


                                    dialogList.get(0).show();
                                } else {
                                    DialogData dialog = new DialogData(mContext);
                                    dialog.setTitle("Внимание!");
                                    dialog.setText("Для данного товара не определены реквизиты обязательные для заполнения. Для принудительного вызова списка реквизитов выполните длинный клик по товару. ");
                                    dialog.setClose(dialog::dismiss);
                                    dialog.show();
                                }

                                AdditionalRequirementsDB ar = showTovarAdditionalRequirement(list);
                                if (ar != null) {
                                    showTovarAdditionalRequirementDialog(mContext, list, ar);
                                }


                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_7", "Exception e: " + e);
                                Globals.alertDialogMsg(mContext, DialogStatus.ERROR,
                                        "Увага",
                                        "Не удалось открыть Опцию. Если ошибка повторяется - обратитесь к своему руководителю.\n\nОшибка: " + e);
                            }
                        } else {
                            clickTovar.click(list);
                        }
                    });

                    constraintLayout.setOnLongClickListener(v -> {
                        if (openType.equals(OpenType.DEFAULT)) {
                            try {
                                // Получаем инфу о всех опциях
                                List<TovarOptions> tovOptTplList = options.getAllOptionsTPL();
                                // В Цикле открываем Н количество инфы
                                for (int i = tovOptTplList.size() - 1; i >= 0; i--) {
                                    if (tovOptTplList.get(i).getOptionControlName() != AKCIYA) {
                                        Log.e("dialogShowRule", "3");
                                        Log.e("dialogShowRule", "tovOptTplList: " + new Gson().toJson(tovOptTplList));
                                        showDialog(list, tovOptTplList.get(i), finalReportPrepareTovar, tovarId, String.valueOf(codeDad2), clientId, finalBalanceData1, finalBalanceDate1, false, true);
                                    }
                                }
                                Collections.reverse(dialogList);
                                dialogList.get(0).show();
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_7.1", "Exception e: " + e);
                            }
                        }
                        return true;
                    });
                } else {// Если такого товара НЕТ

                    boolean finalDeletePromoOption1 = deletePromoOption;
                    constraintLayout.setOnClickListener(v -> {
                        if (openType.equals(OpenType.DEFAULT)) {
                            ReportPrepareDB rp = createNewRPRow(list);
                            Log.e("DRAdapterTovar", "ClickListenerТовара нет");
                            try {
                                // Получаем инфу об обязательных опциях
                                List<TovarOptions> tovOptTplList = options.getRequiredOptionsTPL(optionsList2, finalDeletePromoOption1);

                                // В Цикле открываем Н количество инфы
                                Collections.reverse(tovOptTplList); // Реверснул что б отображалось более менее адекватно пользователю (в естественном порядке)
                                for (TovarOptions tpl : tovOptTplList) {
                                    if (tpl.getOptionControlName() != AKCIYA) {
                                        Log.e("dialogShowRule", "4");
                                        // Для новых Товаров делаю так что б реквизиты работали в клик - тру
//                                        showDialog(list, tpl, rp, tovarId, String.valueOf(codeDad2), clientId, "", "", false);
                                        showDialog(list, tpl, rp, tovarId, String.valueOf(codeDad2), clientId, "", "", true, true);
                                    }
                                }
                                Collections.reverse(dialogList);
                                dialogList.get(0).show();
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.bind_8", "Exception e: " + e);
                                globals.alertDialogMsg(mContext, "Не удалось открыть Опцию. Если ошибка повторяется - обратитесь к своему руководителю.\n\nОшибка: " + e);
                            }
                        } else {
                            clickTovar.click(list);
                        }
                    });
                }


                boolean b = setTovPhoto(list);
                if (b) {
                    imageView.setOnClickListener(v -> {
                        Log.e("ФОТО_ТОВАРОВ", "Click");
                        Exchange exchange = new Exchange();
                        displayFullSizeTovarPhotoDialog(list);
                        exchange.getTovarImg(Collections.singletonList(list), "full", new Globals.OperationResult() {
                            @Override
                            public void onSuccess() {
                                Log.e("ФОТО_ТОВАРОВ", "onSuccess");
//                                displayFullSizeTovarPhotoDialog(list);
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("ФОТО_ТОВАРОВ", "onFailure");
//                                displayFullSizeTovarPhotoDialog(list);
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
                        }, new Clicks.clickStatusMsgMode() {
                            @Override
                            public void onSuccess(String data, Clicks.MassageMode mode) {

                            }

                            @Override
                            public void onFailure(String error) {

                            }
                        }, view.getContext());

                        return false;
                    });
                }
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar/bind", "Exception e: " + e);
            }
        }

        private AdditionalRequirementsDB showTovarAdditionalRequirement(TovarDB tovar) {
            final AdditionalRequirementsDB[] res = {null};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Optional<AdditionalRequirementsDB> result;
                result = adList.stream()
                        .filter(obj -> obj.getTovarId().equals(tovar.getiD()))
                        .findFirst();
                result.ifPresent(currentAR -> {
                    currentAR = result.get();
                    // если опция контроля не указана
                    if (currentAR.getOptionId() != null && currentAR.getOptionId().equals("0")) {
                        res[0] = currentAR;
                    } else {
                        out.println();
                        res[0] = null;
                    }
                });
            }
            return res[0];
        }

        public void showTovarAdditionalRequirementDialog(Context context, TovarDB tovar, AdditionalRequirementsDB additionalRequirementsDB) {
            TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(tovar.getManufacturerId());
            new AdditionalRequirementsAdapter().click(context, additionalRequirementsDB, tovar, tradeMarkDB);
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
                            }else {
                                imageView.setImageResource(R.mipmap.merchik);
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

                            StringBuilder sb = new StringBuilder();
                            sb.append("Штрихкод: ").append(tovar.getBarcode()).append("\n");
                            sb.append("Артикул: ").append(getArticle(tovar, 0));

                            dialogPhotoTovar.setPhotoBarcode(tovar.getBarcode());
                            dialogPhotoTovar.setTextInfo(sb);

                            dialogPhotoTovar.setClose(dialogPhotoTovar::dismiss);
                            dialogPhotoTovar.show();

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
         *
         * @param pos true - простой add
         *            false - добавляем на 0 позицию
         */
//        @RequiresApi(api = Build.VERSION_CODES.N)
        private void showDialog(TovarDB list, TovarOptions tpl, ReportPrepareDB reportPrepareDB, String tovarId, String cd2, String clientId, String finalBalanceData1, String finalBalanceDate1, boolean clickType, boolean pos) {
            try {
                final int adapterPosition = getAdapterPosition();

                Log.e("showDialog", "TovarOptions tpl: " + tpl);

                DialogData dialog = new DialogData(mContext);
                dialog.setTitle("");
                dialog.setText("");
                dialog.setClose(() -> {
                    closeDialogRule(dialog, dialog::dismiss);    // Особенное правило закрытия для модального окна с Акцией
                });
                dialog.setLesson(mContext, true, 802);
                dialog.setVideoLesson(mContext, true, 803, null, null);
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

                        Map<Integer, String> map = new HashMap<>();
                        map.put(2, "Акция отсутствует");
                        map.put(1, "Есть акция");

                        String akciya = map.get(Integer.parseInt(reportPrepareDB.getAkciya()));

                        dialog.setOperationTextData2(akciya);
                        break;

                }

                if (tpl.getOptionControlName() != null && tpl.getOptionControlName().equals(ERROR_ID)) {    // Работа с ошибками
                    String groupPos = null;
                    boolean containsOptionId = false;
                    boolean containsOptionId2 = false;
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            containsOptionId = tovOptTplList.stream().anyMatch(tovarOptions -> tovarOptions.getOptionId().contains(135591));
                            containsOptionId2 = tovOptTplList.stream().anyMatch(tovarOptions -> tovarOptions.getOptionId().contains(157241));
                        }
                        if (containsOptionId) {
                            groupPos = "22";
                        }

                        if (containsOptionId2) {
                            groupPos = "13";
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "dialog tovars error in stream", "Exception e: " + e);
                    }

                    dialog.setExpandableListView(createExpandableAdapter(dialog.context, groupPos), () -> {
                        if (dialog.getOperationResult() != null) {
                            operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
                            refreshElement(cd2, list.getiD());
                            dialogShowRule(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
                        }

                        notifyItemChanged(adapterPosition);
                    });
                } else {
                    dialog.setOperation(operationType(tpl), getCurrentData(tpl, cd2, tovarId), setMapData(tpl.getOptionControlName()), () -> {
                        // Сделал удаление даты тут потому что 08.04.24. в setOperation пришлось это убрать.
                        // Мне надо проверять корректность внесенной даты и отталкиваясь от этого выводить модальное окно или нет.
                        OptionsDB option = OptionsRealm.getOptionControl(String.valueOf(codeDad2), "165276");
                        if (option != null && operationType(tpl).equals(Date)) {
                            openNext = false;

                            long tovExpirationDate = list.expirePeriod * 86400;         // термін придатності товару. (дни перевожу в секунды)
                            long dtCurrentWPData = wpDataDB.getDt().getTime() / 1000;   // дата посещения в секундах
                            long dtUserSetToTovar = 0;                                  // То что указал в Дате окончания срока годности мерчик
                            long resDays = 0;                                           // Дата текущего посещения + срок годности товара

                            if (dialog.getOperationResult() != null && !dialog.getOperationResult().equals("")) {
                                dtUserSetToTovar = Clock.dateConvertToLong(dialog.getOperationResult()) / 1000;
                            }

                            resDays = dtCurrentWPData + tovExpirationDate;
                            int exPer = list.expirePeriod;
                            if (exPer != 0 && dtUserSetToTovar > resDays) {
                                DialogData dialogBadData = new DialogData(dialog.context);
                                dialogBadData.setTitle("Зауваження до Дати");
                                dialogBadData.setText("Ви внесли некоректну дату закінчення терміну придатності! Відмовитись від її збереження?");
                                dialogBadData.setOk("Так", () -> {
                                    dialogBadData.dismiss();
                                    Toast.makeText(mContext, "Дата не збережена!", Toast.LENGTH_LONG).show();
                                });
                                dialogBadData.setCancel("Ні", () -> {
                                    dialogBadData.dismiss();
                                    pushOkButtonRequisites(tpl, reportPrepareDB, dialog, cd2, list, tovarId, clientId, finalBalanceData1, finalBalanceDate1, clickType);
                                });
                                dialogBadData.setClose(dialogBadData::dismiss);
                                dialogBadData.show();
                            } else {
                                openNext = true;
                                dialog.dismiss();
                            }
                        } else if (operationType(tpl).equals(Date)) {
                            dialog.dismiss();
                        }

                        if (openNext && dialog.getOperationResult() != null && !dialog.getOperationResult().equals("")) {

                            pushOkButtonRequisites(tpl, reportPrepareDB, dialog, cd2, list, tovarId, clientId, finalBalanceData1, finalBalanceDate1, clickType);

                            // 08.04.24. Перенес это в отдельную функцию pushOkButtonRequisites
//                            operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
//                            Toast.makeText(mContext, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
//                            refreshElement(cd2, list.getiD());
//                            dialogShowRule(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
                        } else {
                            Toast.makeText(dialog.context, "Внесите корректно данные", Toast.LENGTH_LONG).show();
                        }

                        notifyItemChanged(adapterPosition);
                    });
                }

                dialog.setCancel("Пропустить", () -> closeDialogRule(dialog, () -> {
                    dialog.dismiss();
                    dialogShowRule(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
                }));

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
                                                dialogShowRule(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
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

//                if (pos){
                dialogList.add(dialog);
//                }else {
//                    dialogList.add(0, dialog);
//                }


            } catch (Exception e) {
                Log.d("test", "test" + e);
            }
        }

        /**
         * 08.04.2024
         * Сохранение данных и открытие нового модального окошка с реквизитами.
         * (то что должно происходить при нажатии на Ок в модальном окне при внесении реквизитов)
         */
        private void pushOkButtonRequisites(TovarOptions tpl, ReportPrepareDB reportPrepareDB, DialogData dialog, String cd2, TovarDB list, String tovarId, String clientId, String finalBalanceData1, String finalBalanceDate1, boolean clickType) {
            operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
            Toast.makeText(mContext, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
            refreshElement(cd2, list.getiD());
            dialogShowRule(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
        }

        /**
         * 29.03.23.
         * Специальное правило по которому отображаю последовательно модальные окошки из
         * списка dialogList.
         */
        private void dialogShowRule(TovarDB list, TovarOptions tpl, ReportPrepareDB reportPrepareDB, String tovarId, String cd2, String clientId, String finalBalanceData1, String finalBalanceDate1, boolean clickType) {
            Log.e("dialogShowRule", "clickType: " + clickType);
            ReportPrepareDB report = dialogList.get(0).reportPrepareDB;
            dialogList.remove(0);

            boolean option165276 = false;
            OptionsDB option = OptionsRealm.getOptionControl(String.valueOf(codeDad2), "165276");
            if (option != null) option165276 = true;

            if (dialogList.size() > 0) {
                dialogList.get(0).reportPrepareDB = report;
                int face = 0;
                if (dialogList.get(0).reportPrepareDB.face != null && !dialogList.get(0).reportPrepareDB.face.equals(""))
                    face = Integer.parseInt(dialogList.get(0).reportPrepareDB.face);

                if (clickType &&
                        dialogList.get(0).tovarOptions.getOptionControlName().equals(ERROR_ID) &&
                        (dialogList.get(0).tovarOptions.getOptionId().contains(157242) ||
                                dialogList.get(0).tovarOptions.getOptionId().contains(157241) ||
                                dialogList.get(0).tovarOptions.getOptionId().contains(157243)) &&
                        face > 0) {
                    // НЕ отображаю модальное окно и удаляю его. Уникальное правило потому что потому.
                    dialogList.remove(0);
                    if (dialogList.size() > 0) {
                        dialogList.get(0).show();
                    }
                } else if (clickType &&
                        (dialogList.get(0).tovarOptions.getOptionControlName().equals(UP) ||
                                dialogList.get(0).tovarOptions.getOptionControlName().equals(DT_EXPIRE)) &&
                        face == 0) {
                    dialogList.remove(0);
                    if (dialogList.size() > 0) {
                        dialogList.get(0).show();
                    }
                } /*else if (clickType &&
                        dialogList.get(0).tovarOptions.getOptionControlName().equals(PHOTO) &&
//                        dialogList.get(0).tovarOptions.getOptionId().contains(159707) &&
                        face != 0
                ) {
                    dialogList.remove(0);
                    if (dialogList.size() > 0) {
                        dialogList.get(0).show();
                    }
                }*/ else if (clickType && (
                        dialogList.get(0).tovarOptions.getOptionControlName().equals(AKCIYA_ID) ||
                                dialogList.get(0).tovarOptions.getOptionControlName().equals(AKCIYA)
                )
                ) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        Optional<AdditionalRequirementsDB> result;
                        result = adList.stream()
                                .filter(obj -> obj.getOptionId().equals("80977"))
                                .findFirst();

//                        AdditionalRequirementsDB foundObject = result.get();

                        if (result.isPresent()) {
                            // Делайте что-то с найденным объектом
                            dialogList.get(0).show();
                        } else {
                            // Обработка случая, когда объект не найден
//                            dialogList.get(0).show();
                            dialogList.remove(0);
                            if (dialogList.size() > 0) {
                                dialogList.get(0).show();
                            }
                        }
                    }


                } else if (clickType
                        && option165276
                        && face > 0
                        && !tpl.getOptionShort().equals("Ш")
                ) {
                    OptionsDB optionsDB = OptionsRealm.getOptionControl(String.valueOf(codeDad2), "165276");
                    if ((dialogList.get(0).reportPrepareDB.dtExpire != null
                            && !dialogList.get(0).reportPrepareDB.dtExpire.equals("")
                            && !dialogList.get(0).reportPrepareDB.dtExpire.equals("0000-00-00"))
                            && optionsDB != null
                    ) {
                        if (optionsDB.getAmountMax() != null && !optionsDB.getAmountMax().equals("")) {
                            int max = Integer.parseInt(optionsDB.getAmountMax());
                            int colMax = max == 0 ? 30 : max;

                            long dat = wpDataDB.getDt().getTime() / 1000;
                            long colMaxLong = colMax * 86400L;
                            long optionControlDate = dat + colMaxLong;
                            long reportDate = 0;
                            if (dialogList.get(0).reportPrepareDB.dtExpire != null && !dialogList.get(0).reportPrepareDB.dtExpire.equals("") && !dialogList.get(0).reportPrepareDB.dtExpire.equals("0000-00-00")) {
                                reportDate = Clock.dateConvertToLong(dialogList.get(0).reportPrepareDB.dtExpire) / 1000;
                            }

                            // Если ДАТА плохая:
                            if (reportDate <= optionControlDate) {
                                // Мы смотрим на ВОЗВРАТ и ЕСЛИ он 0 - Выводим ОШИБКУ
                                if (dialogList.get(0).reportPrepareDB.expireLeft != null
                                        && (dialogList.get(0).reportPrepareDB.expireLeft.equals("0") || dialogList.get(0).reportPrepareDB.expireLeft.equals(""))) {

                                    boolean existError = false;
                                    try {
                                        for (DialogData item : dialogList) {
                                            if (item.tovarOptions.getOptionShort().equals("Ш")) {
                                                existError = true;
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e("dialogShowRule", "Exception e: " + e);
                                    }

                                    if (!existError && tpl.getOptionShort().equals("В")) {
                                        TovarOptions to = new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592, 157242);
                                        showDialog(list, to, reportPrepareDB, tovarId, String.valueOf(codeDad2), clientId, finalBalanceData1, finalBalanceDate1, true, false);
                                        if (dialogList.size() > 0 /*&& dialogList.get(0).tovarOptions.getOptionShort().equals("P")*/) {
                                            Collections.swap(dialogList, 0, 1);
                                            dialogList.get(0).show();
                                        }
                                    } else {
                                        if (dialogList.size() > 0) {
                                            dialogList.get(0).show();
                                        }
                                    }

                                    // Отображаем то что у нас дальше
                                } else {
                                    if (dialogList.size() > 0) {
                                        dialogList.get(0).show();
                                    }
                                }
                                // Если ДАТА Хорошая
                            } else {

                                if (tpl.getOptionShort().equals("Д")) {  // Если текущее окно - ДАТА
                                    // Удаляем Возврат
                                    try {
                                        for (DialogData item : dialogList) {
                                            if (item.tovarOptions.getOptionShort().equals("В")) {
                                                dialogList.remove(item);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e("dialogShowRule", "Exception e: " + e);
                                    }
                                }


                                // Тут мы не должны указывать ВОЗВРАТ
                                dialogList.get(0).show();
                            }
                        }
                    } else {
                        dialogList.get(0).show();
                    }
                } else if (clickType &&
                        dialogList.get(0).tovarOptions.getOptionControlName().equals(PHOTO) &&
//                        dialogList.get(0).tovarOptions.getOptionId().contains(159707) &&
                        face != 0
                ) {
                    dialogList.remove(0);
                    if (dialogList.size() > 0) {
                        dialogList.get(0).show();
                    }
                } else {
                    dialogList.get(0).show();
                }
            }
        }

        /**
         * 30.03.23.
         * Уникальное событие для Акций.
         * Если модальное окно для внесения Акции и внесён один из реквизитов - запрещаю что-то
         * делать.
         */
        private void closeDialogRule(DialogData dialog, Clicks.clickVoid click) {
            if (dialog.tovarOptions.getOptionControlName().equals(AKCIYA) || dialog.tovarOptions.getOptionControlName().equals(AKCIYA_ID)) {
                if ((dialog.getOperationResult() == null && dialog.getOperationResult2() != null) ||
                        (dialog.getOperationResult() != null && dialog.getOperationResult2() == null) ||
                        ((dialog.getOperationResult() != null && (dialog.getOperationResult().equals("") || dialog.getOperationResult().equals("0"))) &&
                                (dialog.getOperationResult2() != null && (!dialog.getOperationResult2().equals("") && !dialog.getOperationResult2().equals("0")))
                        ) ||
                        ((dialog.getOperationResult2() != null && (dialog.getOperationResult2().equals("") || dialog.getOperationResult2().equals("0"))) &&
                                (dialog.getOperationResult() != null && (!dialog.getOperationResult().equals("") && !dialog.getOperationResult().equals("0")))
                        )
                ) {
                    Toast.makeText(dialog.context, "Внесіть, будь-ласка, обидва реквізити!", Toast.LENGTH_LONG).show();
                } else {
                    click.click();
                }
            } else {
                click.click();
            }
        }


        private MySimpleExpandableListAdapter createExpandableAdapter(Context context, String groupPos) {

            Map<String, String> map;
            ArrayList<Map<String, String>> groupDataList = new ArrayList<>();

            // список атрибутов групп для чтения
            String[] groupFrom = new String[]{"groupName"};
            // список ID view-элементов, в которые будет помещены атрибуты групп
            int groupTo[] = new int[]{android.R.id.text1};

            // список атрибутов элементов для чтения
            String childFrom[] = new String[]{"itemName"};
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
                map.put("groupId", group.getID());

                groupDataList.add(map);

                RealmResults<ErrorDB> errorItemsDB = errorDbList.where().equalTo("parentId", group.getID()).findAll();
                if (errorItemsDB != null && errorItemsDB.size() > 0) {
                    сhildDataItemList = new ArrayList<>();
                    for (ErrorDB item : errorItemsDB) {
                        map = new HashMap<>();
                        map.put("itemName", "* " + item.getNm());
                        сhildDataItemList.add(map);
                    }
                    сhildDataList.add(сhildDataItemList);
                } else {
                    сhildDataItemList = new ArrayList<>();
                    map = new HashMap<>();
                    map.put("itemName", "* " + group.getNm());
                    сhildDataItemList.add(map);
                    сhildDataList.add(сhildDataItemList);
                }
            }

            MySimpleExpandableListAdapter adapter = new MySimpleExpandableListAdapter(
                    context, groupDataList,
                    android.R.layout.simple_expandable_list_item_1, groupFrom,
                    groupTo, сhildDataList, android.R.layout.simple_list_item_1,
                    childFrom, childTo);


            // Проверка наличия группы с идентификатором 22
            int groupPosition = -1;
            for (int i = 0; i < groupDataList.size(); i++) {
                Map<String, String> groupData = groupDataList.get(i);
                String groupId = groupData.get("groupId"); // Здесь нужно использовать правильный ключ для идентификатора группы
                if (groupId != null && groupId.equals(groupPos)) {
                    groupPosition = i;
                    break;
                }
            }
            adapter.group = groupPosition;

            return adapter;
        }

        private Map<Integer, String> setMapData(Globals.OptionControlName optionControlName) {
            Map<Integer, String> map = new HashMap<>();
            switch (optionControlName) {
                case ERROR_ID:
                    RealmResults<ErrorDB> errorDbList = RealmManager.getAllErrorDb();
                    for (int i = 0; i < errorDbList.size(); i++) {
                        if (errorDbList.get(i).getNm() != null && !errorDbList.get(i).getNm().equals("")) {
                            map.put(Integer.valueOf(errorDbList.get(i).getID()), errorDbList.get(i).getNm());
                        }
                    }
                    return map;

                case AKCIYA_ID:
                    RealmResults<PromoDB> promoDbList = RealmManager.getAllPromoDb();
                    for (int i = 0; i < promoDbList.size(); i++) {
                        if (promoDbList.get(i).getNm() != null && !promoDbList.get(i).getNm().equals("")) {
                            map.put(Integer.valueOf(promoDbList.get(i).getID()), promoDbList.get(i).getNm());
                        }
                    }

                    map.put(0, "Оберіть тип акції");

                    return map;

                case AKCIYA:
                    map.put(2, "Акция отсутствует");
                    map.put(1, "Есть акция");

                    map.put(0, "Оберіть наявність акції");

                    return map;

                default:
                    return null;
            }
        }


        /**
         * !!! 05.05.23. Перенёс эту штуку для простого вызова в TovarRequisites
         * <p>
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
            ReportPrepareDB reportPrepareTovar2 = reportPrepareTovar;

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

            if (data == null || data.isEmpty()) {
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
                    long curent = System.currentTimeMillis() / 1000;
                    long millis = System.currentTimeMillis();
                    long seconds = millis / 1000;
                    Log.d("TIME_CHECK", "Millis: " + millis + ", Seconds: " + seconds);
                    Log.e("SAVE_TO_REPORT_OPT", "TIME: " + curent);

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
                        if (data2 != null && !data2.isEmpty()) {
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


    /*Устаревшее, такое же есть в TovarRequisites*/
    public File getPhotoFromDB(TovarDB tovar) {

        int id = Integer.parseInt(tovar.getiD());

        StackPhotoDB stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(id, tovar.photoId, 18, false);
        if (stackPhotoDB != null) {
            if (stackPhotoDB.getObject_id() == id) {
                if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                    File file = new File(stackPhotoDB.getPhoto_num());
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
                if (constraint.length() != 0) {
                    dataList = (List<TovarDB>) results.values;

//                    Toast toast = Toast.makeText(mContext, "Отобрано: " + dataList.size() + " товаров", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
                } else {
                    dataList = dataFilterable;
                }
                notifyDataSetChanged();
            }
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
