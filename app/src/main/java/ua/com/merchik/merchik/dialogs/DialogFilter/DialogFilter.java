package ua.com.merchik.merchik.dialogs.DialogFilter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.AutoTextStatusAdapter;
import ua.com.merchik.merchik.ViewHolders.AutoTextThemeAdapter;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DFWpResult;

import static ua.com.merchik.merchik.database.realm.tables.CustomerRealm.getAllCustomerDB;

public class DialogFilter<T> {

    private Dialog dialog;
    private Context context;
    private LinearLayout ll; // Общий layout для всего диалога
    private LinearLayout linearLayout; // Layout к которому буду цеплять динамически созданные элементы

    private Globals.SourceAct source;

    // Эти данные потом будут Обьеденяться
    private WpDataDB wpDataDB;

    private TextView title;
    private AutoCompleteTextView autoTextTheme, autoTextStatus;
    private EditText editText, editDate, editDate2;
    private Button apply, cancel;
    private RecyclerView recycler;

    private RadioGroup radGrp, radioGroup;
    private RadioButton radioButton, radioButton2, radioButton3;

    private ImageButton close, help, videoHelp, call;

    private static int tovGrpEl; // для того что б выделять в разделе товаров уже выделенный тип
    private static int wpGrpEl; // для того что б выделять в разделе товаров уже выделенный тип

    // RESULTS
    private List<TovarDB> resTovGrpEl;
    private static String resEditText;
    public static Integer filtered;


    private DFWpResult result = new DFWpResult();   // То что будет возвращаться в прогу


    public DialogFilter(Context context, Globals.SourceAct source) {
        this.context = context;
        this.source = source;
        dialog = new Dialog(context);
//        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_filter);

        linearLayout = dialog.findViewById(R.id.testLayout);
        title = dialog.findViewById(R.id.title);
        editText = dialog.findViewById(R.id.editText);
        apply = dialog.findViewById(R.id.apply);
        cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> dismiss());    // Сразу ставим отмену

        recycler = dialog.findViewById(R.id.recycler);

        editDate = dialog.findViewById(R.id.editTextDate);
        editDate2 = dialog.findViewById(R.id.editTextDate2);

        autoTextTheme = dialog.findViewById(R.id.autoCompleteTextTheme);
        autoTextStatus = dialog.findViewById(R.id.autoCompleteTextViewStatus);

//        radGrp = dialog.findViewById(R.id.radGrp);
//        radioGroup = dialog.findViewById(R.id.radioGroup);
//
//        radioButton = dialog.findViewById(R.id.radioButton);
//        radioButton2 = dialog.findViewById(R.id.radioButton2);
//        radioButton3 = dialog.findViewById(R.id.radioButton3);

        // Кнопки окна
        close = dialog.findViewById(R.id.imageButtonClose);


        setResycler();
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
//        RadioButton3ViewHolder.info = null;
        if (dialog != null) {
            dialog.dismiss();
//            dialog.cancel();
        }
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    public void setRadioButton(DialogData.DialogClickListener clickListener) {
        radioButton.setVisibility(View.VISIBLE);
        radGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == radioButton.getId()) {
                    clickListener.clicked();
                }
            }
        });
    }


    /**
     * 30.03.2021
     * Тут я буду заполнять данными мой диалог. Скорее всего это потом привратится в список значений
     * потому что в будущем это будет сильно разтягиваться в сторону. На данный момент (30.03.21)
     * нужно для функционирования только Строчка с плана работ.
     * <p>
     * Возможно это в перспективе надо закидывать в конструктор.
     * todo спросить у ментора как правильно это надо оформлять
     */
    public void setData(WpDataDB wp) {
        wpDataDB = wp;
    }

    /**
     * 31.03.2021
     * Результат работы Диалога
     */
    public ResultData dialogGetResult() {

        ResultData res = new ResultData();

        switch (source) {
            case PHOTO_LOG:
                break;

            case DETAILED_REPORT:
                Log.e("DialogFilter_L", "resEditText: " + resEditText);
                Log.e("DialogFilter_L", "tovGrpEl: " + tovGrpEl);

                if (resEditText != null && !resEditText.equals("") || tovGrpEl != 0) {
                    res.dataChange = true;
                    res.editText = resEditText;
                    res.tovarDBS = resTovGrpEl;
                    res.massage = "Фильтры установлены";
                    return res;
                }
                break;

            case TASK_AND_RECLAMATION:
                break;

            case WP_DATA:
                break;

            default:
                res.dataChange = false;
                res.massage = "Фильтры не были установлены";
                return res;
        }


        // В любом случае выполнится
        res.dataChange = false;
        res.massage = "Фильтры не были установлены";
        return res;
    }


    /**
     * 31.03.2021
     * Установка начального значения в текстовое поле поиска.
     */
    public void serEditFilter(CharSequence hint, CharSequence text) {
        editText.setHint(hint);
        editText.setText(text);

//        Log.e("DialogFilter_L", "resEditText: " + resEditText);

        if (resEditText != null && !resEditText.equals("")) {
            editText.setText(text);
        } else {
            resEditText = text.toString();
        }
    }


    /**
     * 30.03.2021
     * В зависимости от ИСТОЧНИКА - будет разный набор фильтров.
     * Клика тут быть не должно. Тут буду сохранять значения для того что б в будущем выбрать элемент.
     * <p>
     * todo спросить у ментора как правильно с кликом обойтись
     */
    @SuppressLint("ResourceType")
    public void setFilters() {
        switch (source) {
            case PHOTO_LOG:
                title.setText("Журнал фото");
                radGrp.setVisibility(View.GONE);

                Map<Integer, String> map = new HashMap<>();
                List<CustomerDB> list = getAllCustomerDB();
                for (CustomerDB item : list) {
                    map.put(Integer.parseInt(item.getId()), item.getNm());
                }

                addSpinner("Выберите клиента", map);
                break;

            case DETAILED_REPORT:
                title.setText("Детализированный отчёт");

                radGrp.setVisibility(View.GONE);

                TextView textView = new TextView(context);
                textView.setText("Показать");// Показать\Отобразить
                radioGroup.addView(textView);

                RadioButton all = new RadioButton(context); //1
                all.setText("Все Товары из БД");
                all.setId(1);
                radioGroup.addView(all);

                RadioButton ppa = new RadioButton(context); //2
                ppa.setText("Товары (план по ППА)");
                ppa.setId(2);
                radioGroup.addView(ppa);

                RadioButton notPPA = new RadioButton(context); //3
                notPPA.setText("Товары из отчёта");
                notPPA.setId(3);
                radioGroup.addView(notPPA);


//                Log.e("DialogFilter_L", "all.getAddrId(): " + all.getAddrId());
//                Log.e("DialogFilter_L", "ppa.getAddrId(): " + ppa.getAddrId());
//                Log.e("DialogFilter_L", "notPPA.getAddrId(): " + notPPA.getAddrId());

                if (tovGrpEl != 0) {
                    radioGroup.check(tovGrpEl);
                }


                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

                    switch (checkedId) {
                        case 1:
                            Log.e("DialogFilter_L", "CLICK_1");
                            resTovGrpEl = RealmManager.getTovarListByCustomer(wpDataDB.getClient_id());
                            tovGrpEl = checkedId;
                            break;

                        case 2:
                            Log.e("DialogFilter_L", "CLICK_2");
                            tovGrpEl = checkedId;
                            break;

                        case 3:
                            Log.e("DialogFilter_L", "CLICK_3");
                            resTovGrpEl = RealmManager.getTovarListFromReportPrepareByDad2(wpDataDB.getCode_dad2());
                            tovGrpEl = checkedId;
                            break;

                        default:
//                            Log.e("DialogFilter_L", "checkedId: " + checkedId);
                            Toast.makeText(context, "Это значение обработать невозможно", Toast.LENGTH_LONG).show();
                            break;
                    }
                });

                break;

            case TASK_AND_RECLAMATION:
                title.setText("Задачи и Рекламации");
                break;

            case WP_DATA:
                title.setText("Фильтр (План работ)");

                break;

            default:
                title.setText("Не удалось определить источник");
                break;
        }
    }


    /**
     * 31.03.2021
     * Отработка кнопки "Применить"
     */
    public void pressApply(Globals.JustClick result) {
        apply.setOnClickListener(v -> {
            result.clicked(dialogGetResult());
            dismiss();
        });
    }


    /**
     * 01.04.2021
     * Создание полей для выбора Адреса
     */
    private List<Integer> layoutIds = new ArrayList<>();

    private void addSpinner(String name, Map<Integer, String> dataMap) {
        ConstraintLayout layout = new ConstraintLayout(context);

        layoutIds.add(layout.getId());

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);

        if (layoutIds.size() <= 1) {
            constraintSet.connect(layout.getId(), ConstraintSet.LEFT, linearLayout.getId(), ConstraintSet.LEFT, 8);
            constraintSet.connect(layout.getId(), ConstraintSet.RIGHT, linearLayout.getId(), ConstraintSet.RIGHT, 8);
            Log.e("addSpinner", "<=1");
        } else if (layoutIds.size() > 1) {
            constraintSet.connect(layout.getId(), ConstraintSet.LEFT, layoutIds.get(layoutIds.size() - 1), ConstraintSet.LEFT, 16);
            constraintSet.connect(layout.getId(), ConstraintSet.RIGHT, layoutIds.get(layoutIds.size() - 1), ConstraintSet.RIGHT, 16);
            Log.e("addSpinner", ">1");
        }

        constraintSet.applyTo(layout);


        Spinner spinner = new Spinner(context);
        TextView textView = new TextView(context);

        textView.setText(name);

        String[] result = dataMap.values().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, result);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);


        layout.addView(textView);

//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(layout);
//        constraintSet.connect(textView.getAddrId(), ConstraintSet.LEFT, constraintLayout.getAddrId(), ConstraintSet.RIGHT, 0);

        layout.addView(spinner);


        ll.addView(layout);
        dialog.setContentView(ll);
//        dialog.setContentView(layout);
    }


    /**
     * 17.08.2021
     * Установка данных в EDIT TEXT, если такие есть по умолчанию при открытии диалога
     */
    public void setEditText(String text) {
        editText.setText(text);
    }


    /**
     * 17.08.2021
     * Должно передавать туда откуда вызвано - что нужно и как отфильтровать
     */
    public void apply(Click click) {
        apply.setOnClickListener(v -> {
            String editRes = editText.getText().toString();

            switch (source) {
                case WP_DATA:
                    result.editText = editRes;
                    if (result.status != null){
                        filtered = 1;
                    }else {
                        filtered = 0;
                    }
                    click.onSuccess(result);
                    break;

                default:
                    click.onSuccess(editRes);
                    break;
            }


            dismiss();
        });
    }


    /**
     * 18.08.21.
     * <p>
     * Установка ресайклера с пунктами для выбора
     */
    private void setResycler() {
        DialogFilterAdapter adapter = new DialogFilterAdapter(dialog.getContext(), source, new Click() {
            @Override
            public <T> void onSuccess(T data) {
                switch (source) {
                    case WP_DATA:
                        result = (DFWpResult) data;
                        break;

                    case TASK_AND_RECLAMATION:

                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onFailure(String error) {

            }
        });

        switch (source){
            case WP_DATA:
                List<WpRecycler> list = new ArrayList<>();

                WpRecycler dataresy = new WpRecycler();
                dataresy.datatype = 1;
                list.add(dataresy);

                adapter.setData(list);
                adapter.notifyDataSetChanged();
                break;
        }


        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(dialog.getContext(), LinearLayoutManager.VERTICAL, false));
    }




    private ResultData resultData = new ResultData();
    private Integer themeId;
    private Integer statusId;
    public void setTaRBlock(){
        editDate.setOnClickListener(v -> {
            Calendar mcurrentDate = Calendar.getInstance();
            int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
            int mMonth = mcurrentDate.get(Calendar.MONTH);
            int mYear = mcurrentDate.get(Calendar.YEAR);
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                month = month + 1;
                String date = year + "-" + month + "-" + dayOfMonth;
                editDate.setText(date);
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });
//        editDate.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                resultData.dateFrom = editable.toString();
//            }
//        });

        editDate2.setOnClickListener(v -> {
            Calendar mcurrentDate = Calendar.getInstance();
            int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
            int mMonth = mcurrentDate.get(Calendar.MONTH);
            int mYear = mcurrentDate.get(Calendar.YEAR);
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                month = month + 1;
                String date = year + "-" + month + "-" + dayOfMonth;
                editDate2.setText(date);
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });
//        editDate2.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                resultData.dateTo = editable.toString();
//            }
//        });


        List<ThemeDB> themeList = ThemeRealm.getTARTheme();
        AutoTextThemeAdapter adapterTheme = new AutoTextThemeAdapter(context, android.R.layout.simple_dropdown_item_1line, themeList);
        autoTextTheme.setAdapter(adapterTheme);
        autoTextTheme.setOnItemClickListener((adapterView, view, i, l) -> {
            ThemeDB theme = adapterTheme.getItem(i);
            themeId = Integer.valueOf(theme.getID());
        });


//        String[] str = {"Активные", "Исправленные", "Отменённые", "Истёк срок исполнения", "Выполненные", "Не выполненные"};
//        autoTextStatus.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, str));


        List<TasksAndReclamationsRealm.TaRStatus> statusList = TasksAndReclamationsRealm.getTaRStatus(0);
        AutoTextStatusAdapter adapterStatus = new AutoTextStatusAdapter(context, android.R.layout.simple_dropdown_item_1line, statusList);
        autoTextStatus.setAdapter(adapterStatus);
        autoTextStatus.setOnItemClickListener((adapterView, view, i, l) -> {
            TasksAndReclamationsRealm.TaRStatus status = adapterStatus.getItem(i);
            statusId = status.id;
        });
    }


    public void setApply(Click click){
        apply.setOnClickListener(v->{
            resultData.editText = editText.getText().toString();

            resultData.dateFrom = Clock.dateConvertToLong(editDate.getText().toString())/1000;
            resultData.dateTo = Clock.dateConvertToLong(editDate2.getText().toString())/1000;

            resultData.themeId = themeId;
            resultData.statusId = statusId;

            click.onSuccess(resultData);
            dismiss();
        });
    }


    // уди атседова --------------------------------------------------------------------------------
    public class ResultData {
        public boolean dataChange;
        public String massage;
        public String editText;

        public Long dateFrom;
        public Long dateTo;

        public Integer themeId;
        public Integer statusId;

        public List<TovarDB> tovarDBS;
        public List<T> data;
    }


    public class WpRecycler{
        public Integer datatype;
    }

}
