package ua.com.merchik.merchik.Utils.Spinner;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ua.com.merchik.merchik.R;

public class SpinnerAdapter extends BaseAdapter {

    private Context context;
    private String[] themeList;
    private LayoutInflater inflater;
    private boolean isDropDownVisible = false;
    private String hint;

    public SpinnerAdapter(Context context, String[] themeList, String hint) {
        this.context = context;
        this.themeList = themeList;
        this.hint = hint;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return isDropDownVisible ? themeList.length : themeList.length + 1;
    }

    @Override
    public Object getItem(int position) {
        return isDropDownVisible ? themeList[position] : (position == 0 ? "" : themeList[position - 1]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
//            view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            view = inflater.inflate(R.layout.spinner_text, parent, false);
        }

        TextView textView = view.findViewById(android.R.id.text1);

        if (position == 0 && !isDropDownVisible) {
            textView.setText(hint);
            textView.setTextColor(Color.DKGRAY);
        } else {
            textView.setText(themeList[position - 1]);
            textView.setTextColor(Color.BLACK);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        if (position == 0) {
            view.setVisibility(View.GONE);
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }

        return view;
    }

    public void setDropDownVisible(boolean isVisible) {
        isDropDownVisible = isVisible;
    }

}
