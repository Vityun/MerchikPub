package ua.com.merchik.merchik.MakePhoto;

import static ua.com.merchik.merchik.MakePhoto.MakePhoto.PICK_GALLERY_IMAGE_REQUEST;

import android.content.Context;
import android.content.Intent;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Utils.PhotoPickerUtils;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class MakePhotoFromGalery {

    public static WpDataDB MakePhotoFromGaleryWpDataDB;
    public static TasksAndReclamationsSDB MakePhotoFromGaleryTasksAndReclamationsSDB;
    public static String tovarId;
    public static Integer photoType;
    public void openGalleryToPeakPhoto(Context context, WpDataDB wp){
        MakePhotoFromGaleryWpDataDB = wp;
        Intent intent = PhotoPickerUtils.createSingleImageChooser();
        ((DetailedReportActivity) context).startActivityForResult(intent, PICK_GALLERY_IMAGE_REQUEST);
    }

}
