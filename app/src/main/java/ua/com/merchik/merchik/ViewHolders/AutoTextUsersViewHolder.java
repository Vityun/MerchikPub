package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.List;

import ua.com.merchik.merchik.Filter.MyFilter;
import ua.com.merchik.merchik.data.Database.Room.UsersSDBDat.UserSDBJoin;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;

public class AutoTextUsersViewHolder<T> extends ArrayAdapter<T> implements Filterable {
    private Context mContext;
    private int mLayoutResourceId;
    private List<T> list;
    private List<T> filterable;
    private AutoTextUserEnum userEnum;

    private UpdateListener updateListener;

    public enum AutoTextUserEnum {
        DEFAULT,
        DEPARTMENT
    }

    public interface UpdateListener {
        void updatePTT();
    }

    public AutoTextUsersViewHolder(
        Context context,
        int layoutResId,
        UpdateListener updateListener,
        List<T> objects
    ) {
        super(context, layoutResId, objects);
        this.mLayoutResourceId = layoutResId;
        this.updateListener = updateListener;
        list = objects;
        filterable = objects;
        userEnum = AutoTextUserEnum.DEFAULT;
    }

    public int getCount() {
        return list.size();
    }

    public T getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int position;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(mLayoutResourceId, parent, false);
            }

            this.position = position;


            TextView name = (TextView) convertView.findViewById(android.R.id.text1);

            String text = "";

            switch (userEnum) {
                case DEPARTMENT:
                    UserSDBJoin departmentItem = (UserSDBJoin) getItem(position);
                    if (departmentItem != null) {

                        try {
                            if (departmentItem.nm == null) {
                                departmentItem.nm = "Отдел не определён";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (departmentItem.id == -1111) {
                            convertView.setOnClickListener(v -> {
                                if (updateListener != null) {
                                    updateListener.updatePTT();
                                }
                            });
                            text = departmentItem.fio;
                        } else {
                            text = departmentItem.fio + " (" + departmentItem.nm + ") ";
                        }
                    }
                    break;

                default:
                    UsersSDB item = (UsersSDB) getItem(position);
                    if (item != null) {
                        text = item.fio;
                    }
                    break;
            }

            name.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public void setAdditionalInformation(AutoTextUserEnum userEnum) {
        this.userEnum = userEnum;
    }

    //=============

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
                    filteredResults = filterable;
                    Log.e("DialogEKL", "Пусто: " + filteredResults.size());
                } else {
                    String[] splited = constraint.toString().split("\\s+");
                    for (String item : splited) {
                        if (item != null && !item.equals("")) {
                            filteredResults = new MyFilter(mContext).getUserSdbFilterable(userEnum, item, filteredResults, filterable);
                        }
                    }
                    Log.e("DialogEKL", "Что-то ищу: " + filteredResults.size());
                }
                list = filteredResults;

                Log.e("DialogEKL", "Должен отобразить: " + list.size());
                notifyDataSetChanged();
            }
        };
    }
}
