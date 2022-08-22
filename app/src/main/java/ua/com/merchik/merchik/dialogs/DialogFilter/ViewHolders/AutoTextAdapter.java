package ua.com.merchik.merchik.dialogs.DialogFilter.ViewHolders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;

public class AutoTextAdapter <T> extends ArrayAdapter<T> implements Filterable {

    private int mLayoutResourceId;
    private List<T> data;
    private List<T> filterableData;

    public AutoTextAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.data = objects;
        this.filterableData = objects;
        this.mLayoutResourceId = resource;
    }

    public int getCount() {
        return data.size();
    }

    public T getItem(int position) {
        return data.get(position);
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
            TextView title = (TextView) convertView.findViewById(android.R.id.text1);


            if (data != null && data.size()>0){
                if (data.get(0) instanceof AddressSDB){
                    AddressSDB address = (AddressSDB) data.get(position);
                    title.setText(address.nm);
                }else if (data.get(0) instanceof UsersSDB){
                    UsersSDB users = (UsersSDB) data.get(position);
                    title.setText(users.fio);
                }else if (data.get(0) instanceof CustomerSDB){
                    CustomerSDB customer = (CustomerSDB) data.get(position);
                    title.setText(customer.nm);
                }else if (data.get(0) instanceof ThemeDB){
                    ThemeDB theme = (ThemeDB) data.get(position);
                    title.setText(theme.getNm());
                }
            }else {
                // data empty
            }



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
                List<T> filteredResults = null;
                if (constraint == null || constraint.length() == 0) {
                    filteredResults = filterableData;
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter().getFilterableDataSDB(item, filteredResults, filterableData);
                        }
                    }
                }
                data = filteredResults;
                notifyDataSetChanged();
            }
        };
    }
}
