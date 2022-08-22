package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class DetailedReportTab extends FragmentPagerAdapter {

    private AppCompatActivity myContext;
    private ArrayList<Data> list;
    private WpDataDB wpDataDB;
    int totalTabs;

    public static DetailedReportOptionsFrag detailedReportOptionsFrag;

    public DetailedReportTab(AppCompatActivity context, FragmentManager fm, int totalTabs, ArrayList<Data> dataArrayList, WpDataDB wpDataDBList) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        myContext = context;
        this.totalTabs = totalTabs;
        this.list = dataArrayList;
        this.wpDataDB = wpDataDBList;
    }

    public static void refreshAdapter(){
        detailedReportOptionsFrag.recycleViewDRAdapter.notifyDataSetChanged();
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DetailedReportHomeFrag(myContext, list, wpDataDB);
            case 1:

                detailedReportOptionsFrag = new DetailedReportOptionsFrag(myContext, list, wpDataDB);
                return detailedReportOptionsFrag;
            case 2:
                return new DetailedReportTovarsFrag(myContext, list, wpDataDB);
            case 3:
                return new DetailedReportTARFrag(myContext, wpDataDB);
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }
}
