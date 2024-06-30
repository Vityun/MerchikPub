package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;

@Entity(tableName = "settings_ui")
public class SettingsUISDB {

    @SerializedName("id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("context_tag")
    @Expose
    @ColumnInfo(name = "context_tag")
    public String contextTAG;

    @SerializedName("table_db")
    @Expose
    @ColumnInfo(name = "table_db")
    public String tableDB;

    @SerializedName("settings_json")
    @Expose
    @ColumnInfo(name = "settings_json")
    public String settingsJson;

}
