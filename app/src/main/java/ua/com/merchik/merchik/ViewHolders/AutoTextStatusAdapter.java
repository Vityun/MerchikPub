package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.List;

import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;

public class AutoTextStatusAdapter extends ArrayAdapter<TasksAndReclamationsRealm.TaRStatus> implements Filterable {
    private Context mContext;
    private int mLayoutResourceId;
    private Integer statusId;
    private List<TasksAndReclamationsRealm.TaRStatus> list;
    private List<TasksAndReclamationsRealm.TaRStatus> filterable;

    public AutoTextStatusAdapter(Context context, int resource, List<TasksAndReclamationsRealm.TaRStatus> objects) {
        super(context, resource, objects);
        this.mLayoutResourceId = resource;
        list = objects;
        filterable = objects;
    }

    public int getCount() {
        return list.size();
    }

    public TasksAndReclamationsRealm.TaRStatus getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(mLayoutResourceId, parent, false);
            }
            TasksAndReclamationsRealm.TaRStatus item = getItem(position);
            TextView name = (TextView) convertView.findViewById(android.R.id.text1);
            name.setText(item.nm);
            statusId = Integer.valueOf(item.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return new FilterResults();
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<TasksAndReclamationsRealm.TaRStatus> filteredResults = null;
                if (constraint == null || constraint.length() == 0) {
                    filteredResults = filterable;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter(mContext).getStatusFilterable(item, filteredResults, filterable);
                        }
                    }
                }
                list = filteredResults;
                notifyDataSetChanged();
            }
        };
    }

}
