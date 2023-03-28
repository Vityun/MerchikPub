package ua.com.merchik.merchik.Activities.DetailedReportActivity;

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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;

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
            case DT_EXPIRE:
            case ERROR_ID:
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
                    if (!editText.getText().toString().equals("")) {
                        if (Integer.parseInt(editText.getText().toString()) > 0) {
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
            // Тут будем описывать что делает свич

            // "2", "Акция отсутствует"
            // "1", "Есть акция"
            // AKCIYA

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
            Map<String, String> mapSpinner2 = getSpinnerDataMap();
            String[] res = mapSpinner2.values().toArray(new String[0]);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(spinner.getContext(), android.R.layout.simple_spinner_item, res);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinner.setAdapter(adapter);

            data1 = mapSpinner2.get(dataRp.akciyaId);
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

        private Map<String, String> getSpinnerDataMap() {
            Map<String, String> map = new HashMap<>();
            RealmResults<PromoDB> promoDbList = RealmManager.getAllPromoDb();
            for (int i = 0; i < promoDbList.size(); i++) {
                if (promoDbList.get(i).getNm() != null && !promoDbList.get(i).getNm().equals("")) {
                    map.put(promoDbList.get(i).getID(), promoDbList.get(i).getNm());
                }
            }
            return map;
        }
    }

    class ViewHolderUniversal extends RecyclerView.ViewHolder {
        private ConstraintLayout layout, layoutData;
        private TextView textTPL;
        private Spinner spinner;
        private ImageButton imageButton;
        private EditText editText;

        public ViewHolderUniversal(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            layoutData = itemView.findViewById(R.id.layoutData);
            textTPL = itemView.findViewById(R.id.tpl);
            spinner = itemView.findViewById(R.id.spinner);
            imageButton = itemView.findViewById(R.id.imageButton);
            editText = itemView.findViewById(R.id.editText);
        }

        public void bind(TovarOptions item) {
            textTPL.setText(item.getOptionLong() + ": ");
            switch (item.getOptionControlName()) {
                case DT_EXPIRE:
                    showImageButtonWithCalendar();
                    break;

                case ERROR_ID:
                    showSpinnerWithErrorList();
                    break;
            }
        }

        private void showImageButtonWithCalendar() {
            imageButton.setVisibility(View.VISIBLE);
        }

        private void showSpinnerWithErrorList() {
            spinner.setVisibility(View.VISIBLE);
        }

    }
}
