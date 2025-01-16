package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;
import ua.com.merchik.merchik.features.main.SamplePhotoSDBOverride;

@Entity(tableName = "sample_photo")
public class SamplePhotoSDB implements DataObjectUI {
    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photoId")
    public Integer photoId;

    @SerializedName("about")
    @Expose
    @ColumnInfo(name = "about")
    public String about;

    @SerializedName("id_1c")
    @Expose
    @ColumnInfo(name = "id1c")
    public Integer id1c;

    @SerializedName("photo_tp")
    @Expose
    @ColumnInfo(name = "photoTp")
    public Integer photoTp;

    @SerializedName("active")
    @Expose
    @ColumnInfo(name = "active")
    public Integer active;

    @SerializedName("grp_id")
    @Expose
    @ColumnInfo(name = "grpId")
    public Integer grpId;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("abbr")
    @Expose
    @ColumnInfo(name = "abbr")
    public String abbr;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dtUpdate")
    public Long dtUpdate;

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return "active, dt_update, id_1c";
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
        return SamplePhotoSDBOverride.INSTANCE.getValueModifier(key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getContainerModifier(this, jsonObject);
    }

    @Nullable
    @Override
    public Integer getIdResImage() {
        return R.drawable.merchik;
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return "photo_id";
    }

    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        return SamplePhotoSDBOverride.INSTANCE.getFieldsForOrderOnUI();
    }
}
