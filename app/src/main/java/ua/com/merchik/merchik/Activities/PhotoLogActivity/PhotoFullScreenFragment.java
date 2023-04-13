package ua.com.merchik.merchik.Activities.PhotoLogActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.ortiz.touchview.TouchImageView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public class PhotoFullScreenFragment extends Fragment {

    private StackPhotoDB photoDB;

    private ImageView back;
    private TextView title, text;
    private TouchImageView image;

    public PhotoFullScreenFragment(StackPhotoDB photoDB) {
        this.photoDB = photoDB;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_full_photo, container, false);

        back = v.findViewById(R.id.back);
        title = v.findViewById(R.id.title);
        text = v.findViewById(R.id.sub_title);
        image = v.findViewById(R.id.image);

        setData();

        return v;
    }

    private void setData() {
        setBack();
        setPhoto();
    }

    private void setPhoto() {
        image.setImageURI(Uri.parse(photoDB.getPhoto_num()));
    }

    private void setBack() {
        back.setOnClickListener(view -> {
            getFragmentManager().beginTransaction().remove(PhotoFullScreenFragment.this).commit();
        });
    }
}
