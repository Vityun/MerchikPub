package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;

public class AddPhotoViewHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout layout;
    private TextView text;
    private ImageView photo;

    public AddPhotoViewHolder(@NonNull View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.testText);
        photo = itemView.findViewById(R.id.photo);
    }

    public void bind(ViewHolderTypeList.AddPhotoLayoutData addPhotoLayoutData) {
        text.setText(addPhotoLayoutData.dataText);
        photo.setOnClickListener(view -> {
            // Открываем журнал фото
            // И вібираем фото. Его надо как-то сохранить
            // и передать обратно, для обновления вьюхи
        });
        photo.setOnLongClickListener(view -> {
            // Открываем фотоаппарат
            // И делаем фото. Его надо как-то сохранить
            // и передать обратно, для обновления вьюхи
            return false;
        });
    }

    private void openPhotoLog(){

    }

    private void openCamera(){

    }
}
