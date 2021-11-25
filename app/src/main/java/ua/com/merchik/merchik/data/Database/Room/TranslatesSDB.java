package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


@Entity(tableName = "translates")
public class TranslatesSDB {
    @SerializedName("ID")
    @ColumnInfo(name = "id")
    @Expose
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "num_1c")
    @SerializedName("num_1c")
    @Expose
    public String num1c;

    @ColumnInfo(name = "internal_name")
    @SerializedName("internal_name")
    @Expose
    public String internalName;

    @ColumnInfo(name = "lang_id")
    @SerializedName("lang_id")
    @Expose
    public String langId;

    @ColumnInfo(name = "default_value")
    @SerializedName("default_value")
    @Expose
    public String defaultValue;

    @ColumnInfo(name = "nm")
    @SerializedName("nm")
    @Expose
    public String nm;

    @ColumnInfo(name = "title")
    @SerializedName("title")
    @Expose
    public String title;

    @ColumnInfo(name = "script_mod")
    @SerializedName("script_mod")
    @Expose
    public String scriptMod;

    @ColumnInfo(name = "script_act")
    @SerializedName("script_act")
    @Expose
    public String scriptAct;

    @ColumnInfo(name = "url")
    @SerializedName("url")
    @Expose
    public String url;

    @ColumnInfo(name = "platform_id")
    @SerializedName("platform_id")
    @Expose
    public String platformId;

    @ColumnInfo(name = "dt_update")
    @SerializedName("dt_update")
    @Expose
    public String dtUpdate;
}
