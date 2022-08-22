package ua.com.merchik.merchik.Activities.WorkPlanActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class WPDataTab extends FragmentPagerAdapter {

    private int totalTabs;

    public WPDataTab(FragmentManager fm, int totalTabs) {
        super(fm);
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new WPDataFragmentHome();
            case 1:
                return new WPDataFragmentMap();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
