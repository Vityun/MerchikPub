package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;

public class ChatFrag extends Fragment {

    private ChatGrpJoinedTemp chat;
    private List<ChatSDB> massages;

    private ImageView back;
    private TextView title, lastMassage, count;
    private RecyclerView recycler;

    private int norReadMassageCnt = 0;

    public ChatFrag(ChatGrpJoinedTemp chat, List<ChatSDB> massages) {
        this.chat = chat;
        this.massages = massages;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_massage, container, false);

        back = v.findViewById(R.id.back);
        title = v.findViewById(R.id.title);
        lastMassage = v.findViewById(R.id.sub_title);
        count = v.findViewById(R.id.count);
        recycler = v.findViewById(R.id.recycler);

        setData();

        return v;
    }

    private void setData() {
        setBack();
        setTitle();
        setRecycler();
    }

    private void setBack() {
        back.setOnClickListener(view -> {
//            getActivity().getSupportFragmentManager().popBackStackImmediate();
            getActivity().onBackPressed();

            Toast.makeText(getContext(), "Ви натиснули 'Назад'", Toast.LENGTH_SHORT).show();
        });
    }

    private void setTitle() {
        count.setText("" + calculateNotReadMsg());
        title.setText(chat.nm);
        lastMassage.setText(chat.lastMsg);
    }

    private void setRecycler() {
        recycler.setAdapter(new ChatMassagesAdapter(massages, () -> {
            norReadMassageCnt--;
            this.count.setText("" + norReadMassageCnt);
        }));
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    private int calculateNotReadMsg(){
        for (ChatSDB item : massages){
            if (item.dtRead == 0) norReadMassageCnt++;
        }
        return norReadMassageCnt;
    }
}
