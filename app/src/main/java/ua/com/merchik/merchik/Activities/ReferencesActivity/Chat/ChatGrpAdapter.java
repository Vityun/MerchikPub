package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.ChatGrpSDB;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class ChatGrpAdapter extends RecyclerView.Adapter<ChatGrpAdapter.DefaultViewHolder> {

    private List<ChatGrpSDB> data;
    private AppCompatActivity activity;

    public ChatGrpAdapter(List<ChatGrpSDB> data, AppCompatActivity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ChatGrpAdapter.DefaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DefaultViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_group_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatGrpAdapter.DefaultViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class DefaultViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private TextView title, lastMsg, date, countMsg;
        private ImageView chatImg;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout_chat_grp);
            chatImg = itemView.findViewById(R.id.chat_img);
            title = itemView.findViewById(R.id.chat_title);
            lastMsg = itemView.findViewById(R.id.chat_last_msg);
            date = itemView.findViewById(R.id.chat_date);
            countMsg = itemView.findViewById(R.id.chat_items);
        }

        public void bind(ChatGrpSDB chatGrpItem) {


            SQL_DB.chatDao().getAllById(chatGrpItem.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        countMsg.setText("" + result.size());

                        layout.setOnClickListener(view -> {
                            FragmentManager manager = activity.getSupportFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.layout_references, new ChatFrag(chatGrpItem, result), "CHAT_MASSAGES");
                            transaction.addToBackStack(null);
                            transaction.commit();
                        });
                    });

            title.setText(chatGrpItem.nm);
            lastMsg.setText(chatGrpItem.lastMsg);
            date.setText(Clock.getHumanTimeSecPattern(chatGrpItem.dtLastUpdate, "HH:mm dd-MM"));
        }
    }
}
