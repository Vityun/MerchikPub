package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Entity(tableName = "tovar_group")
public class TovarGroupSDB {
    @SerializedName("ID")
    @Expose
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("parent")
    @Expose
    @ColumnInfo(name = "parent")
    public Integer parent;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;

    public String getNmFromList(List<TovarGroupSDB> list) {
        StringBuilder res = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (TovarGroupSDB item : list) {
                res.append(item.nm).append(", ");
            }
            res = new StringBuilder(res.substring(0, res.length() - 2));
        } else {
            return "";
        }

        return res.toString();
    }
}
