package ua.com.merchik.merchik.data.RetrofitResponse.photos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 21.09.21
 * Ответ с точки входа: {"act":"list_image","mod":"images_view"}
 * В list хранятся данные о фотках
 * */
public class ImagesViewListImageResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<ImagesViewListImageList> list = null;

    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("page_total")
    @Expose
    public Integer pageTotal;
}
