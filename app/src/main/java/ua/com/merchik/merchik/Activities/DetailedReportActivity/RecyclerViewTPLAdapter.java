package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.ERROR_ID;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.MySimpleExpandableListAdapter;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ErrorRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class RecyclerViewTPLAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TovarOptions> dataTpl;
    private ReportPrepareDB dataRp;
    private ClickTPL click;

    public interface ClickTPL {
        void getData(TovarOptions tpl, String data, String data2);
    }

    public RecyclerViewTPLAdapter(List<TovarOptions> requiredOptionsTPL, ReportPrepareDB data, ClickTPL click) {
        this.dataTpl = requiredOptionsTPL;
        this.dataRp = data;
        this.click = click;
    }

    @Override
    public int getItemViewType(int position) {
        switch (dataTpl.get(position).getOptionControlName()) {
            case PHOTO:
            case ERROR_ID:
                return 3;
            case DT_EXPIRE:
                return 2;
            case AKCIYA:
            case AKCIYA_ID:
                return 1;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_tpl, parent, false);
//        return new RecyclerViewTPLAdapter.ViewHolder(v);

        switch (viewType) {
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_tpl, parent, false));
            case 1:
                return new ViewHolderPromo(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_tpl_promotion, parent, false));
            case 2:
                return new ViewHolderUniversal(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_tpl_universal, parent, false));
            case 3:
                return new ViewHolderButton(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_tpl_button, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        holder.bind(dataTpl.get(position));
        switch (holder.getItemViewType()) {
            case 0:
                ViewHolder viewHolder0 = (ViewHolder) holder;
                viewHolder0.bind(dataTpl.get(position));
                break;

            case 1:
                ViewHolderPromo viewHolder1 = (ViewHolderPromo) holder;
                viewHolder1.bind(dataTpl.get(position));
                break;

            case 2:
                ViewHolderUniversal viewHolderUniversal = (ViewHolderUniversal) holder;
                viewHolderUniversal.bind(dataTpl.get(position));
                break;

            case 3:
                ViewHolderButton viewHolderButton = (ViewHolderButton) holder;

                switch (dataTpl.get(position).getOptionControlName()) {
                    case PHOTO:
                        viewHolderButton.bind();
                        break;

                    case ERROR_ID:
                        viewHolderButton.bind(dataTpl.get(position));
                        break;
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        try {
            return dataTpl.size();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RecyclerViewTPLAdapter.getItemCount", "Exception e: " + e);
            return 0;
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private EditText editText;
        private Button subtraction, additions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tpl);
            editText = itemView.findViewById(R.id.editText);
            subtraction = itemView.findViewById(R.id.subtraction);
            additions = itemView.findViewById(R.id.additions);
        }

        public void bind(TovarOptions item) {
            textView.setText(item.getOptionLong() + ": ");
            editText.setText(getDataFromReportPrepare(item.getOptionControlName(), dataRp));

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    click.getData(item, editable.toString(), null);
                    if (item.getOptionControlName().equals(Globals.OptionControlName.FACE) ||
                            item.getOptionControlName().equals(Globals.OptionControlName.UP) && !editText.getText().toString().equals("")) {

                        int text;
                        try {
                            text = Integer.parseInt(editText.getText().toString());
                        }catch (Exception e){
                            text = 0;
                        }

                        if (text > 0) {
                            subtraction.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(subtraction.getContext().getResources().getColor(R.color.active), PorterDuff.Mode.SRC));
                            subtraction.setClickable(true);
                        } else {
                            subtraction.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(subtraction.getContext().getResources().getColor(R.color.inActive), PorterDuff.Mode.SRC));
                            subtraction.setClickable(false);
                        }
                    } else {

                    }
                }
            });

            if (item.getOptionControlName().equals(Globals.OptionControlName.FACE) ||
                    item.getOptionControlName().equals(Globals.OptionControlName.UP)) {
                additions.setVisibility(View.VISIBLE);
                additions.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(subtraction.getContext().getResources().getColor(R.color.active), PorterDuff.Mode.SRC));

                subtraction.setVisibility(View.VISIBLE);

                if (editText.getText().toString().equals("") || Integer.parseInt(editText.getText().toString()) > 0) {
                    subtraction.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(subtraction.getContext().getResources().getColor(R.color.active), PorterDuff.Mode.SRC));
                    subtraction.setClickable(true);
                }

                subtraction.setOnClickListener(view -> {
                    try {
                        int data = Integer.parseInt(editText.getText().toString());
                        data--;
                        editText.setText(String.valueOf(data));
                    } catch (Exception e) {
                        Toast.makeText(itemView.getContext(), "Ошибка. Передайте её руководителю: " + e, Toast.LENGTH_LONG).show();
                    }
                });
                additions.setOnClickListener(view -> {
                    try {
                        if (!editText.getText().toString().equals("")) {
                            int data = Integer.parseInt(editText.getText().toString());
                            data++;
                            editText.setText(String.valueOf(data));
                        } else {
                            editText.setText("1");
                        }
                    } catch (Exception e) {
                        Toast.makeText(itemView.getContext(), "Ошибка. Передайте её руководителю: " + e, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                subtraction.setVisibility(View.GONE);
                additions.setVisibility(View.GONE);
            }

        }

        private String getDataFromReportPrepare(Globals.OptionControlName tplName, ReportPrepareDB reportPrepare) {
            try {
                switch (tplName) {
                    case FACE:
                        return reportPrepare.getFace();

                    case PRICE:
                        return reportPrepare.getPrice();

                    case AMOUNT:
                        return String.valueOf(reportPrepare.getAmount());

                    case UP:
                        return reportPrepare.getUp();

                    case NOTES:
                        return reportPrepare.getNotes();

                    case AKCIYA:
                        return reportPrepare.getAkciya();

//                case ERROR_ID:
//                    return reportPrepare.getErrorId();

//                case AKCIYA_ID:
//                    return reportPrepare.getAkciyaId();

                    case DT_EXPIRE:
                        return reportPrepare.getDtExpire();

                    case EXPIRE_LEFT:
                        return reportPrepare.getExpireLeft();

                    case OBOROTVED_NUM:
                        return reportPrepare.getOborotvedNum();

                    default:
                        return "0";
                }
            } catch (Exception e) {
                return "0";
            }
        }
    }

    class ViewHolderPromo extends RecyclerView.ViewHolder {
        private TextView textView1, textView2;
        private MultiStateToggleButton button;
        private Spinner spinner;

        public String data1;
        public String data2;

        public ViewHolderPromo(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.tplText1);
            textView2 = itemView.findViewById(R.id.tplText2);
            button = itemView.findViewById(R.id.mstb_multi_id);
            spinner = itemView.findViewById(R.id.spinner);
        }

        public void bind(TovarOptions item) {
            setText();
            setSwitch(item);
            setSpinner(item);
        }

        private void setText() {
            textView1.setText("Тип Акції: ");
            textView2.setText("Наявність Акції: ");
        }

        private void setSwitch(TovarOptions item) {
            button.setColorRes(R.color.active, R.color.inActive);

            if (dataRp != null) {
                int akciya;
                try {
                    akciya = Integer.parseInt(dataRp.akciya);
                } catch (Exception e) {
                    akciya = 0; // Потому что акция - пустая строка
                }

                switch (akciya) {
                    case 0:
                        button.setStates(new boolean[]{false, true, false});
                        break;

                    case 1:
                        button.setStates(new boolean[]{false, false, true});
                        break;

                    case 2:
                        button.setStates(new boolean[]{true, false, false});
                        break;
                }
            }

            button.setOnValueChangedListener(position -> {
                switch (position) {
                    case 0:
                        data2 = "2";
                        break;

                    case 1:
                        data2 = "0";
                        break;

                    case 2:
                        data2 = "1";
                        break;
                }


                click.getData(item, dataRp.akciyaId, data2);

            });


        }

        private void setSpinner(TovarOptions item) {
            // AKCIYA_ID
            // Выбор какая именно у нас Акция
            Map<Integer, String> mapSpinner2 = getSpinnerDataMap();
            String[] res = mapSpinner2.values().toArray(new String[0]);

            ArrayAdapter<String> adapter = new ArrayAdapter(spinner.getContext(), android.R.layout.simple_spinner_item, res) {
                @Override
                public boolean isEnabled(int position) {
                    if (position == 0) {
                        return false;
                    } else {
                        return true;
                    }
                }

                @Override
                public View getDropDownView(int position, View convertView,
                                            ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if (position == 0) {
                        // Set the hint text color gray
                        tv.setTextColor(Color.GRAY);
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinner.setAdapter(adapter);

            data1 = mapSpinner2.get(Integer.parseInt(dataRp.akciyaId));
            int spinnerPosition = adapter.getPosition(data1);
            spinner.setSelection(spinnerPosition);

//            SpinnerDialogData spinnerDialogData = new SpinnerDialogData();
//            spinnerDialogData.setData(mapSpinner2);
//            spinner.setOnItemSelectedListener(spinnerDialogData);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        String s = adapterView.getSelectedItem().toString();
                        data1 = Globals.getKeyForValueS(s, mapSpinner2);

                        click.getData(item, data1, dataRp.akciya);

                    } catch (Exception e) {
                        // TODO Рассматривать ошибку
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }

        private Map<Integer, String> getSpinnerDataMap() {
            Map<Integer, String> map = new HashMap<>();
            RealmResults<PromoDB> promoDbList = RealmManager.getAllPromoDb();
            for (int i = 0; i < promoDbList.size(); i++) {
                if (promoDbList.get(i).getNm() != null && !promoDbList.get(i).getNm().equals("")) {
                    map.put(Integer.valueOf(promoDbList.get(i).getID()), promoDbList.get(i).getNm());
                }
            }
            map.put(0, "Оберіть тип акції");
            return map;
        }
    }

    class ViewHolderUniversal extends RecyclerView.ViewHolder {
        private ConstraintLayout layout, layoutData;
        private TextView textTPL;
        private Spinner spinner;
        private ImageButton imageButton;
        private EditText editText, editTextDate;
        private ExpandableListView expListView;

        public ViewHolderUniversal(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            layoutData = itemView.findViewById(R.id.layoutData);
            textTPL = itemView.findViewById(R.id.tpl);
            spinner = itemView.findViewById(R.id.spinner);
            imageButton = itemView.findViewById(R.id.imageButton);
            editText = itemView.findViewById(R.id.editText);
            editTextDate = itemView.findViewById(R.id.editTextDate);
            expListView = itemView.findViewById(R.id.expListView);
        }

        public void bind(TovarOptions item) {
            textTPL.setText(item.getOptionLong() + ": ");
            switch (item.getOptionControlName()) {
                case DT_EXPIRE:
                    showImageButtonWithCalendar(item);
                    break;

                case ERROR_ID:
                    showErrorList(item);
//                    showSpinnerWithErrorList(item);
                    break;
            }
        }

        private void showImageButtonWithCalendar(TovarOptions item) {
            imageButton.setVisibility(View.VISIBLE);
            editTextDate.setVisibility(View.VISIBLE);

            editTextDate.setText(dataRp.dtExpire);
            imageButton.setOnClickListener(view -> {
                Calendar mcurrentDate = Calendar.getInstance();
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mYear = mcurrentDate.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(imageButton.getContext(), (v, year, month, dayOfMonth) -> {
                    month = month + 1;
                    String date = year + "-" + month + "-" + dayOfMonth;
                    editTextDate.setText(date);
                    click.getData(item, date, null);    // Сохранение данных
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            });
        }

        private void showSpinnerWithErrorList(TovarOptions item) {
            spinner.setVisibility(View.VISIBLE);

            Map<Integer, String> mapSpinner2 = getSpinnerDataMap();
            String[] res = mapSpinner2.values().toArray(new String[0]);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(spinner.getContext(), android.R.layout.simple_spinner_item, res);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinner.setAdapter(adapter);

            spinner.setSelection(adapter.getPosition(mapSpinner2.get(dataRp.akciyaId)));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        String s = adapterView.getSelectedItem().toString();
                        String res = Globals.getKeyForValueS(s, mapSpinner2);

                        click.getData(item, res, dataRp.errorComment);

                    } catch (Exception e) {
                        // TODO Рассматривать ошибку
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        private Map<Integer, String> getSpinnerDataMap() {
            Map<Integer, String> map = new HashMap<>();
            RealmResults<ErrorDB> errorDbList = RealmManager.getAllErrorDb();
            for (int i = 0; i < errorDbList.size(); i++) {
                if (errorDbList.get(i).getNm() != null && !errorDbList.get(i).getNm().equals("")) {
                    map.put(Integer.valueOf(errorDbList.get(i).getID()), errorDbList.get(i).getNm());
                }
            }
            return map;
        }

        /**
         * 30.03.23.
         * Отображение Ошибок. (Как в модальном окне)
         */
        private void showErrorList(TovarOptions item) {
            expListView.setVisibility(View.VISIBLE);
            expListView.setAdapter(createExpandableAdapter(expListView.getContext()));
            expListView.setOnChildClickListener(getErrorExpandableListView(item));
        }

        private MySimpleExpandableListAdapter createExpandableAdapter(Context context) {
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

            return adapter;
        }

        private ExpandableListView.OnChildClickListener getErrorExpandableListView(TovarOptions item) {
            return (expandableListView, view, groupPos, childPos, l) -> {
                Map<String, String> map;
                map = (Map<String, String>) expandableListView.getExpandableListAdapter().getChild(groupPos, childPos);

                String str = map.get("itemName");
                String res = str.replace("* ", "");

                Toast.makeText(expListView.getContext(), "Выбрали ошибку: " + res, Toast.LENGTH_SHORT).show();

                String result = ErrorRealm.getErrorDbByNm(res).getID();
                click.getData(item, result, dataRp.errorComment);
                return false;
            };
        }
    }

    class ViewHolderButton extends RecyclerView.ViewHolder {

        private Button button;
        private TextView textView;

        public ViewHolderButton(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
            textView = itemView.findViewById(R.id.title);
        }

        public void bind(TovarOptions item) {
            textView.setText("Ошибка Товара");
            button.setOnClickListener(v -> {
                TovarDB tov = TovarRealm.getById(dataRp.tovarId);
                DialogData dialog = new DialogData(itemView.getContext());
                dialog.setTitle("");
                dialog.setText("");
                dialog.setClose(dialog::dismiss);
                dialog.setImage(true, getPhotoFromDB(tov));
                dialog.setAdditionalText(setPhotoInfo(TPL, tov, "", ""));
                String groupPos = null;
                if (TPL.getOptionId().contains(135591)){
                    groupPos = "22";
                }
                dialog.setExpandableListView(createExpandableAdapter(dialog.context, groupPos), () -> {
                    if (dialog.getOperationResult() != null) {
                        operetionSaveRPToDB(TPL, dataRp, dialog.getOperationResult(), dialog.getOperationResult2(), null, dialog.context);
                    }
                });
                dialog.show();
            });
        }

        public void bind() {
            button.setOnClickListener(v -> {
                new TovarRequisites(TovarRealm.getById(dataRp.tovarId), dataRp).createDialog(itemView.getContext(), WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(dataRp.codeDad2)), null).show();
            });
        }


        TovarOptions TPL = new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592, 157242);

        private File getPhotoFromDB(TovarDB tovar) {
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

        private PhotoDescriptionText setPhotoInfo(TovarOptions tpl, TovarDB tovar, String finalBalanceData1, String finalBalanceDate1) {
            PhotoDescriptionText res = new PhotoDescriptionText();

            try {
                String weightString = String.format("%s, %s", tovar.getWeight(), tovar.getBarcode()); // составление строк веса и штрихкода для того что б выводить в одно поле

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

                res.row4TextValue = RealmManager.getNmById(tovar.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "";

                res.row5Text = "Ост.:";
                res.row5TextValue = finalBalanceData1 + " шт на " + finalBalanceDate1;
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.setPhotoInfo", "Exception e: " + e);
            }
            return res;
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

            return adapter;
        }

        private void operetionSaveRPToDB(TovarOptions tpl, ReportPrepareDB rp, String data, String data2, TovarDB tovarDB, Context context) {
            if (data == null || data.equals("")) {
                Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tpl.getOptionControlName() == AKCIYA_ID) {
                INSTANCE.executeTransaction(realm -> {
                    rp.setAkciyaId(data);
                    rp.setAkciya(data2);
                    rp.setUploadStatus(1);
                    rp.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(rp);
                });
            }
        }
    }
}
