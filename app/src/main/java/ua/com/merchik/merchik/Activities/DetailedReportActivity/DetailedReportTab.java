package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class DetailedReportTab extends FragmentStateAdapter {

    private AppCompatActivity myContext;
    private WpDataDB wpDataDB;
    int totalTabs;

    public static DetailedReportOptionsFrag detailedReportOptionsFrag;
    private CommentViewModel viewModel;

    public DetailedReportTab(AppCompatActivity context, FragmentManager fm, Lifecycle lifecycle, int totalTabs, WpDataDB wpDataDBList, CommentViewModel commentViewModel) {
        super(fm, lifecycle);
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "create");
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "context: " + context);
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "totalTabs: " + totalTabs);
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "fm: " + fm);
        myContext = context;
        this.totalTabs = totalTabs;
        this.wpDataDB = wpDataDBList;
        this.viewModel = commentViewModel;
    }

/*    public DetailedReportTab(AppCompatActivity context, FragmentManager fm, int totalTabs, ArrayList<Data> dataArrayList, WpDataDB wpDataDBList) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "create");
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "context: " + context);
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "totalTabs: " + totalTabs);
        Globals.writeToMLOG("INFO", "DetailedReportTab/1", "fm: " + fm);
        myContext = context;
        this.totalTabs = totalTabs;
        this.list = dataArrayList;
        this.wpDataDB = wpDataDBList;
    }*/

    public static void refreshAdapter() {
        try {
            Globals.writeToMLOG("ERROR", "DetailedReportTab/refreshAdapter", "HERE");
            if (detailedReportOptionsFrag != null)
                if (detailedReportOptionsFrag.recycleViewDRAdapter != null)
                    detailedReportOptionsFrag.recycleViewDRAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTab/refreshAdapter", "Exception e: " + e);
        }
    }

/*    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        try {
            Globals.writeToMLOG("INFO", "DetailedReportTab/getItem", "position: " + position);
            switch (position) {
                case 0:
//                    return new DetailedReportHomeFrag(myContext, list, wpDataDB);
                    return DetailedReportHomeFrag.newInstance(myContext, list, wpDataDB);
                case 1:
//                    detailedReportOptionsFrag = new DetailedReportOptionsFrag(myContext, list, wpDataDB);
                    detailedReportOptionsFrag = DetailedReportOptionsFrag.newInstance(myContext, list, wpDataDB);
                    return detailedReportOptionsFrag;
                case 2:
//                    return new DetailedReportTovarsFrag(myContext, list, wpDataDB);
                    return DetailedReportTovarsFrag.newInstance(myContext, list, wpDataDB);
                case 3:
//                    return new DetailedReportTARFrag(myContext, wpDataDB);
                    return DetailedReportTARFrag.newInstance(myContext, list, wpDataDB);
                default:
                    Globals.writeToMLOG("ERROR", "DetailedReportTab/getItem", "default/ Fragment == NULL");
                    return null;
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTab/getItem", "catch/ Fragment == NULL Exception e: " + e);
            return null;
        }
    }

    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }*/

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            Globals.writeToMLOG("INFO", "DetailedReportTab/getItem", "position: " + position);
            DetailedReportViewModel detailedReportViewModel = new ViewModelProvider((DetailedReportActivity) myContext).get(DetailedReportViewModel.class);


            return switch (position) {
                case 0 -> DetailedReportHomeFrag.newInstance(wpDataDB, viewModel);
                case 1 -> {
                    detailedReportOptionsFrag = DetailedReportOptionsFrag.newInstance(detailedReportViewModel);
                    yield detailedReportOptionsFrag;
                }
                case 2 -> DetailedReportTovarsFrag.newInstance(detailedReportViewModel);
                case 3 -> DetailedReportTARFrag.newInstance(myContext, wpDataDB);
                default -> {
                    Globals.writeToMLOG("ERROR", "DetailedReportTab/getItem", "default/ Fragment == NULL");
                    yield null;
                }
            };
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTab/getItem", "catch/ Fragment == NULL Exception e: " + e);
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return totalTabs;
    }
}
