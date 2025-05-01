package ua.com.merchik.merchik;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.dialogs.DialogData;

public class ListViewAdapter extends BaseAdapter {

    // Declare Variables
    private Context mContext;
    private LayoutInflater inflater;
    private List<Data> workplan = null;
    private ArrayList<Data> arraylist;

    public ListViewAdapter(Context context, List<Data> wp) {
        mContext = context;
        this.workplan = wp;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<Data>();
        this.arraylist.addAll(wp);
    }


    public class ViewHolder {
        TextView addr;
        TextView cust;
        TextView merc;
        TextView date;
        LinearLayout options = null;
        ImageView wp_image;
    }

    @Override
    public int getCount() {
        return workplan.size();
    }

    @Override
    public Data getItem(int position) {
        return workplan.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
//            view = inflater.inflate(R.layout.wp_data_customkps, null, false);

            holder.addr = (TextView) view.findViewById(R.id.addr1);
            holder.cust = (TextView) view.findViewById(R.id.cust1);
            holder.merc = (TextView) view.findViewById(R.id.merc1);
            holder.date = (TextView) view.findViewById(R.id.date1);
            holder.options = (LinearLayout) view.findViewById(R.id.option_signal_layout1);//setContentView
            holder.wp_image = (ImageView) view.findViewById(R.id.wp_image1);
            view.setTag(holder);


            if(workplan.get(position).getOptionsSignals().getParent() != null) {
                ((ViewGroup)workplan.get(position).getOptionsSignals().getParent()).removeView(workplan.get(position).getOptionsSignals());
            }
            holder.options.addView(workplan.get(position)
                    .getOptionsSignals());
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.addr.setText(workplan.get(position).getAddr());
        holder.cust.setText(workplan.get(position).getCust());
        holder.merc.setText(workplan.get(position).getMerc());
        holder.date.setText(Clock.getHumanTimeYYYYMMDD(workplan.get(position).getDate().getTime()/1000));

        holder.wp_image.setImageResource(workplan.get(position)
                .getImages());

        // Слушатель для нажатия на элемент (кпс)
        view.setOnClickListener(arg0 -> {
            String msg = mContext.getString(R.string.open_visit);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setCancelable(false);
            builder.setMessage(msg);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    try {
                        Data D = new Data(
                                workplan.get(position).getId(),
                                workplan.get(position).getAddr(),
                                workplan.get(position).getCust(),
                                workplan.get(position).getMerc(),
                                workplan.get(position).getDate(),
                                workplan.get(position).getOtchetId(),
                                null,
                                "",
                                workplan.get(position).getImages());

                        Intent intent = new Intent(mContext, DetailedReportActivity.class);
                        intent.putExtra("dataFromWP", D);
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                   }

                }
            });
            builder.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.create().show();
        });

        return view;
    }


    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        workplan.clear();
        if (charText.length() == 0) {
            workplan.addAll(arraylist);
        } else {
            System.out.println("TEST.FILTER.SEARCH.ARR: " + arraylist);
            for (Data wp : arraylist) {
                if (wp.getAddr().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    workplan.add(wp);
                }

                if (wp.getCust().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    workplan.add(wp);
                }

                if (wp.getMerc().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    workplan.add(wp);
                }

                if (Clock.getHumanTimeYYYYMMDD(wp.getDate().getTime()/1000).toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    workplan.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}