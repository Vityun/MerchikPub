package ua.com.merchik.merchik.Activities.TaskAndReclamations;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TARHomeTab extends FragmentStateAdapter {
//public class TARHomeTab extends FragmentPagerAdapter {

    private int totalTabs;
    private FragmentManager fragmentManager;

    public TARHomeTab(FragmentManager fm, Lifecycle lifecycle, int totalTabs) {
        super(fm, lifecycle);
        this.fragmentManager = fm;
        this.totalTabs = totalTabs;
    }

/*    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return TARFragmentHome.newInstance(fragmentManager);
            case 1:
                return new TARFragmentMap();
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
                return TARFragmentHome.newInstance(fragmentManager);
            case 1:
                return new TARFragmentMap();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return totalTabs;
    }
}
