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
import androidx.lifecycle.ViewModelProvider;

import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARHomeFrag;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

public class TARFragmentHome extends Fragment {

    private static final String LIST_TAG = "TARHomeFrag";
    private static final String DETAIL_TAG = "TARSecondFrag";
    private static final String DETAIL_BACK_STACK = "tar_detail";
    private final FragmentManager.OnBackStackChangedListener backStackListener = this::updateCurrentChild;

    private OnFragmentInteractionListener mListener;
    Fragment oldFrag;

    private FragmentManager fragmentManager;
    public TARHomeFrag homeFrag;
    public TARSecondFrag secondFrag;
    private TARViewModel viewModel;

    public TARFragmentHome() {
    }

    @Deprecated
    public TARFragmentHome(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        Log.d("test", "test");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TARViewModel.class);
    }

    public static TARFragmentHome newInstance(FragmentManager fragmentManager) {
        TARFragmentHome fragment = new TARFragmentHome();
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
        super.onViewCreated(view, savedInstanceState);
        FragmentManager children = getChildFragmentManager();
        children.addOnBackStackChangedListener(backStackListener);
        if (children.findFragmentById(R.id.frame_layout) != null) {
            updateCurrentChild();
            return;
        }

        showList();
        int initialTaskId = TARActivity.getInitialTarId(requireActivity().getIntent());
        if (initialTaskId > 0) showDetail(TARSecondFrag.newInstanceById(initialTaskId));
    }

    @Override
    public void onDestroyView() {
        getChildFragmentManager().removeOnBackStackChangedListener(backStackListener);
        super.onDestroyView();
    }

    private void updateCurrentChild() {
        Fragment current = getChildFragmentManager().findFragmentById(R.id.frame_layout);
        if (current instanceof TARSecondFrag) {
            secondFrag = (TARSecondFrag) current;
        } else if (current instanceof TARHomeFrag) {
            homeFrag = (TARHomeFrag) current;
            secondFrag = null;
            viewModel.setTasksAndReclamations(null);
            TARSecondFrag.TaRID = 0;
        }
    }

    private void showList() {
        int type = requireActivity() instanceof TARActivity
                ? ((TARActivity) requireActivity()).getSectionType() : TARType;
        homeFrag = TARHomeFrag.newInstance(type, null);
        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.frame_layout, homeFrag, LIST_TAG)
                .commit();
    }

    public void returnToList() {
        FragmentManager children = getChildFragmentManager();
        if (children.isStateSaved() || !(children.findFragmentById(R.id.frame_layout) instanceof TARSecondFrag)) return;
        if (!children.popBackStackImmediate(DETAIL_BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            // A detail restored from an older version might have no list in the back stack.
            secondFrag = null;
            viewModel.setTasksAndReclamations(null);
            TARSecondFrag.TaRID = 0;
            showList();
        }
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

    public void openTask(TasksAndReclamationsSDB data) {
        if (!isAdded() || getChildFragmentManager().isStateSaved() || secondFrag != null
                || data == null || data.id == null || data.id <= 0) return;
        showDetail(TARSecondFrag.newInstance(data));
    }

    private void showDetail(TARSecondFrag detail) {
        secondFrag = detail;
        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.frame_layout, detail, DETAIL_TAG)
                .addToBackStack(DETAIL_BACK_STACK)
                .commit();
    }
}
