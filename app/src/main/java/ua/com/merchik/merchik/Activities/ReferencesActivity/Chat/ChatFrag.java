package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.features.main.DBViewModels.ChatSDBViewModel;

public class ChatFrag extends Fragment {
    private static final String ARG_CHAT_ID = "chat_id";
    private Disposable historySubscription;
    private UpdateChat updateChat;

    private ChatGrpJoinedTemp chat;
    private List<ChatSDB> massages;

    private ImageView back;
    private TextView title, lastMassage, count;
    private RecyclerView recycler;

    private int norReadMassageCnt = 0;

    public ChatFrag() {
    }

    public static ChatFrag newInstance(int chatId) {
        ChatFrag fragment = new ChatFrag();
        Bundle args = new Bundle();
        args.putInt(ARG_CHAT_ID, chatId);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFrag(ChatGrpJoinedTemp chat, List<ChatSDB> massages, UpdateChat updateChat) {
        this.chat = chat;
        this.massages = massages;
        this.updateChat = updateChat;
        Bundle args = new Bundle();
        args.putInt(ARG_CHAT_ID, chat.chatId);
        setArguments(args);
    }

    public interface UpdateChat{
        void updateChat();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_massage, container, false);

        back = v.findViewById(R.id.back);
        title = v.findViewById(R.id.title);
        lastMassage = v.findViewById(R.id.sub_title);
        count = v.findViewById(R.id.count);
        recycler = v.findViewById(R.id.recycler);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setBack();
        if (chat != null && massages != null) {
            setData();
            return;
        }
        int chatId = requireArguments().getInt(ARG_CHAT_ID);
        lastMassage.setText(R.string.chat_loading);
        ChatSDBViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChatSDBViewModel.class);
        historySubscription = viewModel.loadConversation(chatId).subscribe(data -> {
            chat = new ChatGrpJoinedTemp();
            chat.chatId = chatId;
            chat.nm = data.getChat().nm;
            chat.lastMsg = data.getChat().lastMsg;
            massages = data.getMessages();
            setData();
        }, error -> {
            lastMassage.setText(R.string.chat_load_error);
            Toast.makeText(requireContext(), R.string.chat_load_error, Toast.LENGTH_LONG).show();
            closeChat();
        });
    }

    private void setData() {
        setBack();
        setTitle();
        setRecycler();
    }

    private void setBack() {
        back.setOnClickListener(view -> closeChat());
    }

    private void closeChat() {
        if (getParentFragmentManager().isStateSaved()) return;
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            getParentFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    private void setTitle() {
        count.setText("" + calculateNotReadMsg());
        title.setText(chat.nm);
        lastMassage.setText(chat.lastMsg);
    }

    private void setRecycler() {
        recycler.setAdapter(new ChatMassagesAdapter(massages, (ChatSDB item) -> {
            norReadMassageCnt = Math.max(0, norReadMassageCnt - 1);
            this.count.setText("" + norReadMassageCnt);
            if (updateChat != null) updateChat.updateChat();
        }));
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    private int calculateNotReadMsg(){
        norReadMassageCnt = 0;
        for (ChatSDB item : massages){
            if (item.dtRead == null || item.dtRead <= 0) norReadMassageCnt++;
        }
        return norReadMassageCnt;
    }

    @Override
    public void onDestroyView() {
        if (historySubscription != null) historySubscription.dispose();
        historySubscription = null;
        recycler.setAdapter(null);
        recycler = null;
        back = null;
        title = null;
        lastMassage = null;
        count = null;
        super.onDestroyView();
    }
}
