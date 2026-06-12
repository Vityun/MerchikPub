package ua.com.merchik.merchik.data.UploadToServ;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QuestionAnswerUploadResponseList {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("mnenie_id")
    @Expose
    public String mnenieId;

    @SerializedName("element_id")
    @Expose
    public String elementId;
}