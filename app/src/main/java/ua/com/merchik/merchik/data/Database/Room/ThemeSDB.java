package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "theme_list")
public class ThemeSDB {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id = "";

    @ColumnInfo(name = "nm")
    public String nm;

    @ColumnInfo(name = "comment")
    public String comment;

    @ColumnInfo(name = "grp_id")
    public String grpId;

    @ColumnInfo(name = "tp")
    public String tp;

    @ColumnInfo(name = "need_photo")
    public Integer needPhoto;

    @ColumnInfo(name = "need_report")
    public Integer needReport;

    @ColumnInfo(name = "dt_update")
    public String dtUpdate;

    @ColumnInfo(name = "opros_theme")
    public Integer oprosTheme;

    @ColumnInfo(name = "audio_filter")
    public Integer audioFilter;
}
