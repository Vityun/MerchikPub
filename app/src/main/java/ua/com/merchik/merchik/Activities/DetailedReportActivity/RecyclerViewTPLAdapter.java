package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.TovarOptions;

public class RecyclerViewTPLAdapter extends RecyclerView.Adapter<RecyclerViewTPLAdapter.ViewHolder> {

    private List<TovarOptions> dataTpl;
    private ReportPrepareDB dataRp;
    private ClickTPL click;

    public interface ClickTPL {
        void getData(TovarOptions tpl, String data);
    }

    public RecyclerViewTPLAdapter(List<TovarOptions> requiredOptionsTPL, ReportPrepareDB data, ClickTPL click) {
        this.dataTpl = requiredOptionsTPL;
        this.dataRp = data;
        this.click = click;
    }

    @NonNull
    @Override
    public RecyclerViewTPLAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_tpl, parent, false);
        return new RecyclerViewTPLAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewTPLAdapter.ViewHolder holder, int position) {
        holder.bind(dataTpl.get(position));
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
                    click.getData(item, editable.toString());
                    if (Integer.parseInt(editText.getText().toString()) > 0){
                        subtraction.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(subtraction.getContext().getResources().getColor(R.color.active), PorterDuff.Mode.SRC));
                        subtraction.setClickable(true);
                    }else {
                        subtraction.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(subtraction.getContext().getResources().getColor(R.color.inActive), PorterDuff.Mode.SRC));
                        subtraction.setClickable(false);
                    }
                }
            });

            if (item.getOptionControlName().equals(Globals.OptionControlName.FACE)) {
                additions.setVisibility(View.VISIBLE);
                additions.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(subtraction.getContext().getResources().getColor(R.color.active), PorterDuff.Mode.SRC));

                subtraction.setVisibility(View.VISIBLE);

                if (Integer.parseInt(editText.getText().toString()) > 0){
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
                        int data = Integer.parseInt(editText.getText().toString());
                        data++;
                        editText.setText(String.valueOf(data));
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
                        return "-";
                }
            } catch (Exception e) {
                return "-";
            }
        }


    }
}
