package ua.com.merchik.merchik.Activities.TaskAndReclamations;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TARHomeTab extends FragmentPagerAdapter {

    private int totalTabs;
    private FragmentManager fragmentManager;

    public TARHomeTab(FragmentManager fm, int totalTabs) {
        super(fm);
        this.fragmentManager = fm;
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new TARFragmentHome(fragmentManager);
            case 1:
                return new TARFragmentMap();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
