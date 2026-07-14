package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

/**
 * Room. Таблица. Витрины.
 * Не путуть с полками и тп..
 * */

@Entity(tableName = "showcase")
public class ShowcaseSDB implements DataObjectUI {

    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "ID")
    public Integer id;

    @SerializedName("select_id")
    @Expose
    @ColumnInfo(name = "select_id")
    public String selectId;

    @SerializedName("select_name")
    @Expose
    @ColumnInfo(name = "select_name")
    public String selectName;

    @SerializedName("photo")
    @Expose
    @ColumnInfo(name = "photo")
    public String photo;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photo_id")
    public Integer photoId;

    @SerializedName("photo_big")
    @Expose
    @ColumnInfo(name = "photo_big")
    public String photoBig;

    @SerializedName("planogram_id")
    @Expose
    @ColumnInfo(name = "planogram_id")
    public Integer planogramId;

    @SerializedName("photo_planogram_id")
    @Expose
    @ColumnInfo(name = "photo_planogram_id")
    public Integer photoPlanogramId;

    @SerializedName("photo_planogram_txt")
    @Expose
    @ColumnInfo(name = "photo_planogram_txt")
    public String photoPlanogramTxt;

    @SerializedName("photo_planogram_author_id")
    @Expose
    @ColumnInfo(name = "photo_planogram_author_id")
    public String photoPlanogramAuthorId;

    @SerializedName("photo_planogram_author_txt")
    @Expose
    @ColumnInfo(name = "photo_planogram_author_txt")
    public String photoPlanogramAuthorTxt;

    @SerializedName("photo_planogram_dt_update")
    @Expose
    @ColumnInfo(name = "photo_planogram_dt_update")
    public String photoPlanogramDtUpdate;

    @SerializedName("isp_id")
    @Expose
    @ColumnInfo(name = "isp_id")
    public String ispId;

    @SerializedName("isp_txt")
    @Expose
    @ColumnInfo(name = "isp_txt")
    public String ispTxt;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("client_txt")
    @Expose
    @ColumnInfo(name = "client_txt")
    public String clientTxt;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public String addrId;

    @SerializedName("addr_addr")
    @Expose
    @ColumnInfo(name = "addr_addr")
    public String addrAddr;

    @SerializedName("line")
    @Expose
    @ColumnInfo(name = "line")
    public String line;

    @SerializedName("rack")
    @Expose
    @ColumnInfo(name = "rack")
    public String rack;

    @SerializedName("status")
    @Expose
    @ColumnInfo(name = "status")
    public Integer status;

    @SerializedName("status_author_id")
    @Expose
    @ColumnInfo(name = "status_author_id")
    public String statusAuthorId;

    @SerializedName("status_author_txt")
    @Expose
    @ColumnInfo(name = "status_author_txt")
    public String statusAuthorTxt;

    @SerializedName("status_dt_update")
    @Expose
    @ColumnInfo(name = "status_dt_update")
    public String statusDtUpdate;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public String authorId;

    @SerializedName("author_txt")
    @Expose
    @ColumnInfo(name = "author_txt")
    public String authorTxt;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public String dtUpdate;

    // Поля которых Вова мне не передал в JSON, но возможно они в будущем мне могут понадобиться:

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("tovar_grp")
    @Expose
    @ColumnInfo(name = "tovar_grp")
    public Integer tovarGrp;

    @SerializedName("isp")
    @Expose
    @ColumnInfo(name = "isp")
    public String isp;

    @SerializedName("photo_planogram_id_author")
    @Expose
    @ColumnInfo(name = "photo_planogram_id_author")
    public Integer photoPlanogramIdAuthor;

    @SerializedName("photo_planogram_id_dt_update")
    @Expose
    @ColumnInfo(name = "photo_planogram_id_dt_update")
    public Integer photoPlanogramIdDtUpdate;

    @SerializedName("status_author")
    @Expose
    @ColumnInfo(name = "status_author")
    public Integer statusAuthor;

    @SerializedName("tp_id")
    @Expose
    @ColumnInfo(name = "tp_id")
    public Integer tpId;            // Тип фото Витрині


    @ColumnInfo(name = "so")
    public Integer so;

    @Ignore
    @ColumnInfo(name = "tovarGrpTxt")
    public String tovarGrpTxt;

    @Ignore
    @ColumnInfo(name = "showcasePhoto")
    public int showcasePhoto;

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return DataObjectUI.DefaultImpls.getFieldTranslateId(this, key);
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

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getContainerModifier(this, jsonObject);
    }

    @Nullable
    @Override
    public Integer getIdResImage() {
        return DataObjectUI.DefaultImpls.getIdResImage(this);
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsImageOnUI(this);
    }

    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsForOrderOnUI(this);
    }

    @NonNull
    @Override
    public List<String> getPreferredFieldOrder() {
        return DataObjectUI.DefaultImpls.getPreferredFieldOrder(this);
    }

    @Override
    public @NotNull String getCommentsForImage() {
        return DataObjectUI.DefaultImpls.getCommentsForImage(this);
    }


    @Override
    public @Nullable String getCommentForImageValue(@NotNull String key, @NotNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getCommentForImageValue(this, key, jsonObject);
    }
}
