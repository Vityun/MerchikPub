package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.ChatSDB;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class ChatMassagesAdapter extends RecyclerView.Adapter<ChatMassagesAdapter.DefaultViewHolder> {

    private List<ChatSDB> data;
    private ChatMassageListener listener;

    public ChatMassagesAdapter(List<ChatSDB> data, ChatMassageListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DefaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatMassagesAdapter.DefaultViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DefaultViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class DefaultViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private TextView massage, info, time;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.chat_item);
            massage = itemView.findViewById(R.id.massage);
            info = itemView.findViewById(R.id.info);
            time = itemView.findViewById(R.id.time);
        }

        public void bind(ChatSDB item) {
            massage.setText(item.msg);
            if (item.dtRead > 0){
                massage.setTextColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));
                info.setTextColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));
                time.setTextColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));
            }else {
                massage.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                info.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                time.setTextColor(itemView.getContext().getResources().getColor(R.color.black));

                item.dtRead = System.currentTimeMillis()/1000;
                SQL_DB.chatDao().insertData(Collections.singletonList(item));
                listener.seeMassage();
            }
            info.setText("Повідомлення: " + item.id + " (" + (item.dtRead > 0 ? "Прочитано" : "Не прочитано") + ")");
            time.setText(Clock.getHumanTimeSecPattern(item.dt, "HH:mm dd-MM"));
        }
    }

    public interface ChatMassageListener{
        void seeMassage();
    }
}
