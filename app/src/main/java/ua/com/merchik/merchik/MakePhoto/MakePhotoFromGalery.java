package ua.com.merchik.merchik.MakePhoto;

import static ua.com.merchik.merchik.MakePhoto.MakePhoto.PICK_GALLERY_IMAGE_REQUEST;

import android.content.Context;
import android.content.Intent;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class MakePhotoFromGalery {

    public static WpDataDB MakePhotoFromGaleryWpDataDB;
    public static TasksAndReclamationsSDB MakePhotoFromGaleryTasksAndReclamationsSDB;
    public static String tovarId;
    public void openGalleryToPeakPhoto(Context context, WpDataDB wp){
        MakePhotoFromGaleryWpDataDB = wp;
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        ((DetailedReportActivity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_GALLERY_IMAGE_REQUEST);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        ((DetailedReportActivity) context).startActivityForResult(intent, PICK_GALLERY_IMAGE_REQUEST);
    }

}
