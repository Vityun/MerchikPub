package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARCommentMakePhoto;

import static ua.com.merchik.merchik.MakePhoto.CAMERA_REQUEST_TAR_COMMENT_PHOTO;

import android.app.Activity;

import ua.com.merchik.merchik.MakePhoto;

public class TARCommentMakePhoto {


    public void openCamera(Activity activity) {
        MakePhoto makePhoto = new MakePhoto();
        makePhoto.openCamera(activity, CAMERA_REQUEST_TAR_COMMENT_PHOTO);
    }
}
