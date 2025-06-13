package ua.com.merchik.merchik.data.RetrofitResponse.tables.update.wpdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WpDataUpdateResponseListData {
    @SerializedName("client_end_dt")
    @Expose
    public boolean clientEndDt;

    @SerializedName("client_start_dt")
    @Expose
    public boolean clientStartDt;

    @SerializedName("visit_end_dt")
    @Expose
    public boolean visitEndDt;

    @SerializedName("visit_start_dt")
    @Expose
    public boolean visitStartDt;

    @SerializedName("dt_update")
    @Expose
    public boolean dtUpdate;

    @SerializedName("status_set")
    @Expose
    public boolean statusSet;

    @SerializedName("user_comment")
    @Expose
    public boolean userComment;

    @SerializedName("user_comment_dt_update")
    @Expose
    public boolean userCommentDtUpdate;

    @SerializedName("user_opinion_id")
    @Expose
    public boolean user_opinion_id;

    @SerializedName("client_work_duration")
    @Expose
    public boolean client_work_duration;

}
