package ua.com.merchik.merchik.data.URLData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Params {
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("client_id")
    @Expose
    private String clientId;
    @SerializedName("addr_id")
    @Expose
    private String addrId;
    @SerializedName("img_type_id")
    @Expose
    private String imgTypeId;
    @SerializedName("photo_user_id")
    @Expose
    private String photoUserId;
    @SerializedName("client_tovar_group")
    @Expose
    private String clientTovarGroup;
    @SerializedName("doc_num")
    @Expose
    private Integer docNum;
    @SerializedName("theme_id")
    @Expose
    private Integer themeId;
    @SerializedName("tovar_id")
    @Expose
    private String tovarId;
    @SerializedName("code_dad2")
    @Expose
    private String codeDad2;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAddrId() {
        return addrId;
    }

    public void setAddrId(String addrId) {
        this.addrId = addrId;
    }

    public String getImgTypeId() {
        return imgTypeId;
    }

    public void setImgTypeId(String imgTypeId) {
        this.imgTypeId = imgTypeId;
    }

    public String getPhotoUserId() {
        return photoUserId;
    }

    public void setPhotoUserId(String photoUserId) {
        this.photoUserId = photoUserId;
    }

    public String getClientTovarGroup() {
        return clientTovarGroup;
    }

    public void setClientTovarGroup(String clientTovarGroup) {
        this.clientTovarGroup = clientTovarGroup;
    }

    public Integer getDocNum() {
        return docNum;
    }

    public void setDocNum(Integer docNum) {
        this.docNum = docNum;
    }

    public Integer getThemeId() {
        return themeId;
    }

    public void setThemeId(Integer themeId) {
        this.themeId = themeId;
    }

    public String getTovarId() {
        return tovarId;
    }

    public void setTovarId(String tovarId) {
        this.tovarId = tovarId;
    }

    public String getCodeDad2() {
        return codeDad2;
    }

    public void setCodeDad2(String codeDad2) {
        this.codeDad2 = codeDad2;
    }
}
