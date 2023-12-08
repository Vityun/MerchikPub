package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.TEST_DATA;
import ua.com.merchik.merchik.data.TestViewHolderData;

public class PhotoAndInfoViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    public static StackPhotoDB stackPhotoDB;

    private ConstraintLayout layout;
    private TextView testText;
    public ImageView photo;
    public ImageButton galleryPick;


    public PhotoAndInfoViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();

        layout = itemView.findViewById(R.id.layout);
        testText = itemView.findViewById(R.id.testText);
        photo = itemView.findViewById(R.id.photo);
        galleryPick = itemView.findViewById(R.id.galleryPick);
        galleryPick.setVisibility(View.VISIBLE);
    }

    public void bind(TestViewHolderData data, Clicks.clickListener click) {
        testText.setText(data.msg);
        testText.setTextColor(context.getResources().getColor(R.color.hintColorDefault));

        try{
            if (data.photo!=null){
                photo.setImageURI(Uri.parse(data.photo.getPhoto_num()));
            }
        }catch (Exception e){

        }

        photo.setOnClickListener(v -> {
            Log.e("DOUBLE_CLICK", "setOnClickListener");
            TEST_DATA test = new TEST_DATA();
            test.type = 2;
            click.click(test);
        });

        photo.setOnLongClickListener(view -> {
            Log.e("DOUBLE_CLICK", "setOnLongClickListener");
            TEST_DATA test = new TEST_DATA();
            test.type = 1;
            click.click(test);
            return false;
        });

        galleryPick.setOnClickListener(view -> {
            Log.e("DOUBLE_CLICK", "setOnLongClickListener");
            TEST_DATA test = new TEST_DATA();
            test.type = 3;
            click.click(test);
        });
    }
}
