package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.TestViewHolderData;

public class ButtonViewHorder extends RecyclerView.ViewHolder {

    private Context context;
    private Button button;

    public ButtonViewHorder(@NonNull View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        button = itemView.findViewById(R.id.button);
    }

    public void bind(TestViewHolderData data, Clicks.clickListener click) {
        button.setText(data.msg);
        button.setOnClickListener(v->{
            Toast.makeText(context, "История ещё в разработке", Toast.LENGTH_SHORT).show();
        });
    }

    public void bind(SiteHintsDB data, Clicks.click click) {
        String s = data.getNm().replace("&quot;", "\"");
        button.setText(s);
        button.setOnClickListener(v->{
            click.click(data);
        });
    }
}
