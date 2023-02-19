package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovarsFrag;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

public class TARTab extends FragmentPagerAdapter {

    private Context myContext;
    private TasksAndReclamationsSDB data;
    private int totalTabs;

    public Tab3Fragment tab3Fragment;

    public TARTab(Context context, FragmentManager fm, int totalTabs, TasksAndReclamationsSDB dataID) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
        this.data = dataID;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Tab1Fragment(data);
            case 1:
                return new Tab2Fragment(data);
            case 3:
                return tab3Fragment = new Tab3Fragment(data);
            case 2:
                return new DetailedReportTovarsFrag(myContext, data);
            default:
                return null;
        }
    }

    public void setDataToFrag3(Integer id){
        tab3Fragment.setPhoto(id);
    }

    public void setDataToFrag3(Integer id, int tarCommentIndex){
        tab3Fragment.setPhotoTARComment(id, tarCommentIndex);
    }


    @Override
    public int getCount() {
        return totalTabs;
    }
}
