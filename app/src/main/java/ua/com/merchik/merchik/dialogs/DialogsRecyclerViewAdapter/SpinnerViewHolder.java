package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;

public class SpinnerViewHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout layout;
    private TextView text;
    private Spinner spinner;
    private boolean initSpinner = false;

    public SpinnerViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.layout);
        text = itemView.findViewById(R.id.text);
        spinner = itemView.findViewById(R.id.spinner);
    }

    public void bind(ViewHolderTypeList.ChoiceSpinnerLayoutData block) {
        if (block.dataTextTitle != null && !block.dataTextTitle.equals("")){
            text.setVisibility(View.VISIBLE);
            text.setText(block.dataTextTitle);
        }else {
            text.setVisibility(View.GONE);
        }

        // Выбор поля по умолчанию:
        if (block.defaultPosition != null){
            spinner.setSelection(block.defaultPosition);
        }

        text.setOnClickListener((v)->{
            block.click.onSuccess("" + text.getText());
        });

        ArrayAdapter spinnerData;
        if (block.dataSpinner != null){
            spinnerData = new ArrayAdapter(itemView.getContext(), android.R.layout.simple_spinner_item, block.dataSpinner);
        }else {
            spinnerData = new ArrayAdapter(itemView.getContext(), android.R.layout.simple_spinner_item, block.dataSpinnerList);
        }

        spinnerData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerData);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!initSpinner) {
                    initSpinner = true;
                }else {
                    String data = adapterView.getItemAtPosition(i).toString();
                    block.resultData = i;
                    block.click.onSuccess(data);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                block.click.onFailure("Ничего не выбрано");
            }
        });


    }
}
