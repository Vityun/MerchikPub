package ua.com.merchik.merchik.Activities.WorkPlanActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WPDataTab extends FragmentStateAdapter {

    private int totalTabs;

    public WPDataTab(FragmentManager fm, Lifecycle lifecycle, int totalTabs) {
        super(fm, lifecycle);
        this.totalTabs = totalTabs;
    }

/*    @NonNull
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
    }*/

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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
    public int getItemCount() {
        return totalTabs;
    }
}
