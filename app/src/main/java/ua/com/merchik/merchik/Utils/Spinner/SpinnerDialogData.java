package ua.com.merchik.merchik.Utils.Spinner;

import android.view.View;
import android.widget.AdapterView;

import java.util.HashMap;
import java.util.Map;

import ua.com.merchik.merchik.Globals;

public class SpinnerDialogData implements AdapterView.OnItemSelectedListener {
    private Globals globals = new Globals();
    private String spinnerString = "";
    private Map<Integer, String> mapSpinner = new HashMap<>();

    public void setData(Map<Integer, String> data) {
        mapSpinner = data;
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        try {
            String s = parent.getSelectedItem().toString();
            spinnerString = Globals.getKeyForValueS(s, mapSpinner);
        } catch (Exception e) {
            // TODO Рассматривать ошибку
        }
    }

    public void onNothingSelected(AdapterView parent) {
    }
}
