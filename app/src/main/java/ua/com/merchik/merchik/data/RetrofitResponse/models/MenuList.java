package ua.com.merchik.merchik.data.RetrofitResponse.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;

public class MenuList {

    @SerializedName("images_type_list")
    @Expose
    private List<ImagesTypeListDB> imagesTypeList = null;

    public List<ImagesTypeListDB> getImagesTypeList() {
        return imagesTypeList;
    }

    public void setImagesTypeList(List<ImagesTypeListDB> imagesTypeList) {
        this.imagesTypeList = imagesTypeList;
    }

}