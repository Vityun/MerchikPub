package ua.com.merchik.merchik.data.Database.Room;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.RetrofitResponse.BonusItemResponse;

@Entity(tableName = "bonus")
public class BonusSDB {

    @SerializedName("id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Long id;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Long authorId;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;

    @SerializedName("percent")
    @Expose
    @ColumnInfo(name = "percent")
    public String percent;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Long themeId;

    @SerializedName("option_id")
    @Expose
    @ColumnInfo(name = "option_id")
    public Long optionId;

    @SerializedName("so")
    @Expose
    @ColumnInfo(name = "so")
    public Integer so;

    public BonusSDB() {}

    public BonusSDB(BonusItemResponse from) {
        id = from.id;
        authorId = from.authorId;
        dtChange = from.dtChange;
        percent = from.percent;
        themeId = from.themeId;
        optionId = from.optionId;
        so = from.so;
    }
}
