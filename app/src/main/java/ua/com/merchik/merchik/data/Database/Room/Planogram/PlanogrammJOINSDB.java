package ua.com.merchik.merchik.data.Database.Room.Planogram;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.sql.Date;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

@Entity(tableName = "planogramm_join")
public class PlanogrammJOINSDB implements DataObjectUI {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;

    @ColumnInfo(name = "planogrammClientId")
    public Integer planogrammClientId;

    @ColumnInfo(name = "planogrammClientTxt")
    public String planogrammClientTxt;

    @ColumnInfo(name = "planogrammName")
    public String planogrammName;

    @ColumnInfo(name = "planogrammComment")
    public String planogrammComment;

    @ColumnInfo(name = "planogrammDtStart")
    @TypeConverters({DateConverter.class})
    public Date planogrammDtStart;

    @ColumnInfo(name = "planogrammDtEnd")
    @TypeConverters({DateConverter.class})
    public Date planogrammDtEnd;

    @ColumnInfo(name = "planogrammAddress")
    public Integer planogrammAddress;

    @ColumnInfo(name = "planogrammAddressTxt")
    public String planogrammAddressTxt;

    @ColumnInfo(name = "planogrammCityTxt")
    public String planogrammCityTxt;

    @ColumnInfo(name = "planogrammGroupId")
    public Integer planogrammGroupId;

    @ColumnInfo(name = "planogrammGroupTxt")
    public String planogrammGroupTxt;

    @ColumnInfo(name = "planogrammPhotoId")
    public Integer planogrammPhotoId;

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

    public static class DateConverter {
        @TypeConverter
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        @TypeConverter
        public static Long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }

}
