package ua.com.merchik.merchik.data.RetrofitResponse.photos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Данные о фотках для сохранения в приложении и подальшей загрузки с сервера
 * */
public class ImagesViewListImageList {
    @SerializedName("ID")
    @Expose
    public Integer id;

    @SerializedName("dt")
    @Expose
    public Long dt;

    @SerializedName("dt_upload")
    @Expose
    public Long dtUpload;

    @SerializedName("client_id")
    @Expose
    public String clientId;

    @SerializedName("addr_id")
    @Expose
    public Integer addrId;

    @SerializedName("orig_date_ut")
    @Expose
    public Long origDateUt;

    @SerializedName("dvi")
    @Expose
    public Integer dvi;

    @SerializedName("dvi_dt")
    @Expose
    public Long dviDt;

    @SerializedName("dvi_user_id")
    @Expose
    public Integer dviUserId;

    @SerializedName("comments")
    @Expose
    public String comments;

    @SerializedName("comment_who_id")
    @Expose
    public Integer commentWhoId;

    @SerializedName("merchik_id")
    @Expose
    public Integer merchikId;

    @SerializedName("hash")
    @Expose
    public String hash;

    @SerializedName("have_prem")
    @Expose
    public String havePrem;

    @SerializedName("tov_type")
    @Expose
    public String tovType;

    @SerializedName("tovar_id")
    @Expose
    public String tovarId;

    @SerializedName("photo_tp")
    @Expose
    public Integer photoTp;

    @SerializedName("approve")
    @Expose
    public Integer approve;

    @SerializedName("doc_type")
    @Expose
    public String docType;

    @SerializedName("doc_num_1c_id")
    @Expose
    public String docNum1cId;

    @SerializedName("photo_user_id")
    @Expose
    public Integer photoUserId;

    @SerializedName("location_xd")
    @Expose
    public String locationXd;

    @SerializedName("location_yd")
    @Expose
    public String locationYd;

    @SerializedName("location_accuracy")
    @Expose
    public String locationAccuracy;

    @SerializedName("location_distance_to_addr")
    @Expose
    public String locationDistanceToAddr;

    @SerializedName("platform_id")
    @Expose
    public String platformId;

    @SerializedName("photo_url")
    @Expose
    public String photoUrl;
}
