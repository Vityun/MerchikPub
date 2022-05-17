package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.List;

import ua.com.merchik.merchik.data.RetrofitResponse.EDRPOUResponse;

public class testAutoTextAdapter extends ArrayAdapter<EDRPOUResponse> {
    private Filter filter = new testAutoTextAdapter.KNoFilter();
    public List<EDRPOUResponse> items;

    public testAutoTextAdapter(@NonNull Context context, int resource, @NonNull List<EDRPOUResponse> objects) {
        super(context, resource, objects);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class KNoFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence arg0) {
            FilterResults result = new FilterResults();
            result.values = items;
            result.count = items.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence arg0, FilterResults arg1) {
            notifyDataSetChanged();
        }
    }
}
