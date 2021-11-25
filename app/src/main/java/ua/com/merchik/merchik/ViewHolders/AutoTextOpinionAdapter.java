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
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;

public class AutoTextOpinionAdapter  extends ArrayAdapter<OpinionSDB> implements Filterable {

    private Context mContext;
    private int mLayoutResourceId;
    private List<OpinionSDB> list;
    private List<OpinionSDB> filterable;

    public AutoTextOpinionAdapter(Context context, int resource, List<OpinionSDB> objects) {
        super(context, resource, objects);
        this.mLayoutResourceId = resource;
        list = objects;
        filterable = objects;
    }

    public int getCount() {
        return list.size();
    }

    public OpinionSDB getItem(int position) {
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
            OpinionSDB item = getItem(position);
            TextView name = (TextView) convertView.findViewById(android.R.id.text1);
            name.setText(item.nm);
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
                List<OpinionSDB> filteredResults = null;
                if (constraint == null || constraint.length() == 0) {
                    filteredResults = filterable;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter(mContext).getOpinionsFilterable(item, filteredResults, filterable);
                        }
                    }
                }
                list = filteredResults;
                notifyDataSetChanged();
            }
        };
    }
}
