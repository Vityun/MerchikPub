package ua.com.merchik.merchik.Activities.TaskAndReclamations;

import static ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity.TARType;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARHomeFrag;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

public class TARFragmentHome extends Fragment {

    private OnFragmentInteractionListener mListener;
    Fragment oldFrag;

    public static int secondFragId;
    public static String secondFragTAG;

    private FragmentManager fragmentManager;
    public TARHomeFrag homeFrag;
    public TARSecondFrag secondFrag;

    public TARFragmentHome(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        Log.d("test", "test");
    }

    public static TARFragmentHome newInstance(FragmentManager fragmentManager) {
        TARFragmentHome fragment = new TARFragmentHome(fragmentManager);
        fragment.setFragmentManager(fragmentManager);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Globals.writeToMLOG("INFO", "TARFragmentHome/onSaveInstanceState", "outState: " + outState);
        try {

        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "TARFragmentHome/onSaveInstanceState", "Exception e: " + e);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Globals.writeToMLOG("INFO", "TARFragmentHome/onViewStateRestored", "savedInstanceState: " + savedInstanceState);
        if (savedInstanceState != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_tar_home, container, false);
        return v;
    }//------------------------------- /ON CREATE --------------------------------------------------

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Fragment childFragment = setHomeFrag();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, childFragment).commit();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void messageFromParentFragment(String msg);
    }

    private Fragment setHomeFrag() {

        homeFrag = new TARHomeFrag().newInstance(TARType, new Globals.TARInterface() {
            @Override
            public void onSuccess(TasksAndReclamationsSDB data) {
                secondFrag = new TARSecondFrag(fragmentManager, data);

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, secondFrag, "TARSecondFrag").commit();

                secondFragId = secondFrag.getId();
                secondFragTAG = secondFrag.getTag();
            }

            @Override
            public void onFailure(String error) {
            }
        });

        return homeFrag;
    }
}
