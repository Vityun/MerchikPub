package ua.com.merchik.merchik.Utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleExpandableListAdapter;

import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.R;

public class MySimpleExpandableListAdapter extends SimpleExpandableListAdapter {

    public MySimpleExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData, int groupLayout, String[] groupFrom, int[] groupTo, List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom, int[] childTo) {
        super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View result = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        if (isExpanded) {
            result.setBackgroundColor(result.getContext().getResources().getColor(R.color.active));
        } else {
            result.setBackgroundColor(result.getContext().getResources().getColor(R.color.inActive));
        }
        return result;
    }
}
