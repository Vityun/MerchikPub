package ua.com.merchik.merchik.data.Database.Room.Planogram;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.sql.Date;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

@Entity(tableName = "planogramm")
public class PlanogrammSDB implements DataObjectUI {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    @SerializedName("ID")
    public Integer id;

    @ColumnInfo(name = "isp_id")
    @SerializedName("isp_id")
    public String ispId;

    @ColumnInfo(name = "isp_txt")
    @SerializedName("isp_txt")
    public String ispTxt;

    @ColumnInfo(name = "client_id")
    @SerializedName("client_id")
    public String clientId;

    @ColumnInfo(name = "client_txt")
    @SerializedName("client_txt")
    public String clientTxt;

    @ColumnInfo(name = "img_id")
    @SerializedName("img_id")
    public Integer imgId;

    @ColumnInfo(name = "photo")
    @SerializedName("photo")
    public String photo;

    @ColumnInfo(name = "photo_id")
    @SerializedName("photo_id")
    public Long photoId;

    @ColumnInfo(name = "photo_big")
    @SerializedName("photo_big")
    public String photoBig;

    @ColumnInfo(name = "nm")
    @SerializedName("nm")
    public String nm;

    @ColumnInfo(name = "comments")
    @SerializedName("comments")
    public String comments;

    @ColumnInfo(name = "dt_start")
    @SerializedName("dt_start")
    public Date dtStart;

    @ColumnInfo(name = "dt_end")
    @SerializedName("dt_end")
    public Date dtEnd;

    @ColumnInfo(name = "author_id")
    @SerializedName("author_id")
    public String authorId;

    @ColumnInfo(name = "authorTxt")
    @SerializedName("authorTxt")
    public String authorTxt;

    @ColumnInfo(name = "dtUpdate")
    @SerializedName("dtUpdate")
    public Date dtUpdate;

    @Ignore
    @ColumnInfo(name = "planogrammPhoto")
    public int planogrammPhoto;

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getTranslateId(@NonNull String key) {
        return DataObjectUI.DefaultImpls.getTranslateId(this, key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return DataObjectUI.DefaultImpls.getValueUI(this, key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getFieldModifier(this, key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getValueModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getValueModifier(this, key, jsonObject);
    }
}
