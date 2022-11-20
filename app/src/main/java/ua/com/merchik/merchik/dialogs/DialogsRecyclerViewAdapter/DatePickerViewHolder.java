package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;

public class DatePickerViewHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout layout;
    private TextView text, text2;
    private EditText dateFromEditText, dateToEditText;

    public DatePickerViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.layout);
        text = itemView.findViewById(R.id.text);
        text2 = itemView.findViewById(R.id.text2);
        dateFromEditText = itemView.findViewById(R.id.dateFrom);
        dateToEditText = itemView.findViewById(R.id.dateTo);
    }

    public void bind(ViewHolderTypeList.ChoiceDateLayoutData block) {
        if (block.dataTextTitle != null && !block.dataTextTitle.equals("")) {
            text.setVisibility(View.VISIBLE);
            text.setText(block.dataTextTitle);
        } else {
            text.setVisibility(View.GONE);
        }
        if (block.dataTextTitle2 != null && !block.dataTextTitle2.equals("")) {
            text2.setVisibility(View.VISIBLE);
            text2.setText(block.dataTextTitle2);
        } else {
            text2.setVisibility(View.GONE);
        }

        text.setOnClickListener((v) -> {
            block.click.onSuccess("" + text.getText());
        });
        text2.setOnClickListener((v) -> {
            block.click.onSuccess("" + text2.getText());
        });

        SimpleDateFormat simpleDateFormatDateFrom = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDateFrom = simpleDateFormatDateFrom.format(block.dateFrom);

        SimpleDateFormat simpleDateFormatDateTo = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDateTo = simpleDateFormatDateTo.format(block.dateTo);

        dateFromEditText.setHint(formattedDateFrom);
        dateToEditText.setHint(formattedDateTo);

        if (block.dateFrom != null && block.state) {
            dateFromEditText.setText(formattedDateFrom);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String yyyy = simpleDateFormat.format(block.dateFrom);
            block.resultDateFrom = yyyy;
        }

        if (block.dateTo != null && block.state) {
            dateToEditText.setText(formattedDateTo);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String yyyy = simpleDateFormat.format(block.dateTo);
            block.resultDateTo = yyyy;
        }


        dateFromEditText.setOnTouchListener((view, motionEvent) -> {
            setDatePicker(dateFromEditText, new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    block.resultDateFrom = (String) data;
                    block.click.onSuccess("Вы установили: " + dateFromEditText.getText().toString());
                }
            });
            return false;
        });
        dateToEditText.setOnTouchListener((view, motionEvent) -> {
            setDatePicker(dateToEditText, new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    block.resultDateTo = (String) data;
                    block.click.onSuccess("Вы установили дату: " + dateToEditText.getText().toString());
                }
            });
            return false;
        });
    }


    private void setDatePicker(EditText editText, Clicks.click click) {
        Calendar currentDate = Calendar.getInstance();
        int mYear = currentDate.get(Calendar.YEAR);
        int mMonth = currentDate.get(Calendar.MONTH);
        int mDay = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(itemView.getContext(), (itemView, year, month, dayOfMonth) -> {
            String d = String.valueOf(dayOfMonth);
            if (d.length() < 2) {
                d = "0" + d;
            }

            String m = String.valueOf(month + 1); // +1 потому что каллендарь дохуя умный и месяцы у него 0-11
            if (m.length() < 2) {
                m = "0" + m;
            }

            String date1 = d + "-" + m + "-" + year;
            String date2 = year + "-" + m + "-" + d;

            editText.setText(date1);
            click.click(date2);
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
