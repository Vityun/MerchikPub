package ua.com.merchik.merchik.data.UploadToServ;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionAnswerUploadResponse {

    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<QuestionAnswerUploadResponseList> list;

    @SerializedName("error")
    @Expose
    public String error;
}

