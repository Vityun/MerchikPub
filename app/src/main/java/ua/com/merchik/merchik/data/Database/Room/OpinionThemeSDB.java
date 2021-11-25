package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "opinions_theme")
public class OpinionThemeSDB {
    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "id")
    public String id;

    @SerializedName("mnenie_id")
    @Expose
    @ColumnInfo(name = "mnenie_id")
    public String mnenieId;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public String themeId;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public String dtChange;
}
