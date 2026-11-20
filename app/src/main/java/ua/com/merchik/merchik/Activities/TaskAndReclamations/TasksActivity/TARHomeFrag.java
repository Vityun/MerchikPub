package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARFragmentHome;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.features.main.DBViewModels.TarHomeViewModel;
import ua.com.merchik.merchik.features.main.tar.TarHomeContent;

public class TARHomeFrag extends Fragment implements TARFragmentHome.OnFragmentInteractionListener {
    // TARActivity routes camera/gallery results to this dialog.
    public DialogCreateTAR dialog;
    private Globals.TARInterface onClickListener;
    private TarHomeViewModel viewModel;
    private int type;

    public static TARHomeFrag newInstance(int type, Globals.TARInterface listener) {
        TARHomeFrag fragment = new TARHomeFrag();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        fragment.onClickListener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ComposeView view = new ComposeView(requireContext());
        view.setId(R.id.tar_home_list);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        type = requireArguments().getInt("type");
        int tarId = getParentFragment() instanceof TARFragmentHome
                ? 0 : requireActivity().getIntent().getIntExtra("TAR_ID", 0);
        viewModel = new ViewModelProvider(requireActivity())
                .get("tar_home_" + type + "_" + tarId, TarHomeViewModel.class);
        viewModel.configure(type, tarId);
        viewModel.setContext(requireActivity());
        viewModel.setOnCreateTask(this::createTask);
        viewModel.setOnOpen(new Globals.TARInterface() {
            @Override public void onSuccess(TasksAndReclamationsSDB data) { openTask(data); }
            @Override public void onFailure(String error) { }
        });
        TarHomeContent.setContentTarHome((ComposeView) view, viewModel);
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.startObserving();
    }

    @Override
    public void onStop() {
        viewModel.stopObserving();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        viewModel.setOnOpen(null);
        viewModel.setOnCreateTask(null);
        viewModel.setContext(null);
        viewModel.setLauncher(null);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroyView();
    }

    private void openTask(TasksAndReclamationsSDB data) {
        if (!isAdded() || getParentFragmentManager().isStateSaved()) return;
        if (getParentFragment() instanceof TARFragmentHome) {
            ((TARFragmentHome) getParentFragment()).openTask(data);
        } else if (onClickListener != null) {
            onClickListener.onSuccess(data);
        }
    }

    @Override public void messageFromParentFragment(String msg) { }

    public interface OnFragmentInteractionListener {
        void messageFromChildFragment(String msg);
    }

    private void createTask() {
        if (!isAdded()) return;
        dialog = new DialogCreateTAR(requireContext());
        dialog.setClose(dialog::dismiss);
        dialog.setTarType(type);
        dialog.setRecyclerView(new Clicks.click() {
            @Override public <T> void click(T data) {
                switch ((Integer) data) {
                    case 1:
                        Intent intent = new Intent(requireContext(), PhotoLogActivity.class);
                        intent.putExtra("choise", true);
                        intent.putExtra("resultCode", 100);
                        if (dialog.address != null) intent.putExtra("address", dialog.address.getAddrId());
                        intent.putExtra("customer", dialog.customer != null ? dialog.customer.getId() : "");
                        requireActivity().startActivityForResult(intent, 100);
                        break;
                    case 2:
                        try {
                            new MakePhoto().openCamera(requireActivity(), 202);
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "TARHome/createTask/camera", e.toString());
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.clickSave(() -> {
            viewModel.updateContent();
            dialog.dismiss();
        }, 1);
        dialog.show();
    }
}
