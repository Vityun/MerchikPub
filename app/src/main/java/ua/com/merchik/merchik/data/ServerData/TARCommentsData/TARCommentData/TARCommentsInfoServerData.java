package ua.com.merchik.merchik.data.ServerData.TARCommentsData.TARCommentData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TARCommentsInfoServerData {
    @SerializedName("comment_id")
    @Expose
    private String commentId;
    @SerializedName("comment_date")
    @Expose
    private String commentDate;
    @SerializedName("author_fio")
    @Expose
    private String authorFio;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("img_src")
    @Expose
    private String imgSrc;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getAuthorFio() {
        return authorFio;
    }

    public void setAuthorFio(String authorFio) {
        this.authorFio = authorFio;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }
}
