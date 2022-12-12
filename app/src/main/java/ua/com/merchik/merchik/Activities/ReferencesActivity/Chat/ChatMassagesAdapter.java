package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class ChatMassagesAdapter extends RecyclerView.Adapter<ChatMassagesAdapter.DefaultViewHolder> {

    private List<ChatSDB> data;
    private ChatMassageListener listener;

    public ChatMassagesAdapter(List<ChatSDB> data, ChatMassageListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public interface ChatMassageListener{
        void seeMassage(ChatSDB item);
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
            if (item.dtRead != null && item.dtRead > 0){
                massage.setTextColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));
                info.setTextColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));
                time.setTextColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));
            }else {
                massage.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                info.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                time.setTextColor(itemView.getContext().getResources().getColor(R.color.black));

                item.dtRead = System.currentTimeMillis()/1000;
                SQL_DB.chatDao().insertData(Collections.singletonList(item))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.e("test", "OK");
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Log.e("test", "Throwable e: " + e);
                            }
                        });

                sendReadMassageStatus(item);

                listener.seeMassage(item);
            }
            info.setText("Повідомлення: " + item.id + " (" + (item.dtRead > 0 ? "Прочитано" : "Не прочитано") + ")");
            time.setText(Clock.getHumanTimeSecPattern(item.dt, "HH:mm dd-MM"));
        }
    }

    private void sendReadMassageStatus(ChatSDB item){
        StandartData.StandartDataChat dataChat = new StandartData.StandartDataChat();
        dataChat.element_id = item.id;
        dataChat.msg_id = item.id;

        Exchange.chatMarkRead(dataChat, new ExchangeInterface.ExchangeResponseInterfaceSingle() {
            @Override
            public <T> void onSuccess(T data) {
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }
}
