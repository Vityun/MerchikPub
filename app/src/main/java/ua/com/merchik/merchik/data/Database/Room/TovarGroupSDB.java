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
        if (list == null || list.isEmpty()) return "";

        int size = list.size();

        // <= 4: выводим всё
        if (size <= 4) {
            StringBuilder res = new StringBuilder();
            for (TovarGroupSDB item : list) {
                if (item == null || item.nm == null || item.nm.trim().isEmpty()) continue;
                if (res.length() > 0) res.append(", ");
                res.append(item.nm.trim());
            }
            return res.toString();
        }

        // > 4: первые 3 + "и еще N отделов"
        StringBuilder res = new StringBuilder();
        int shown = 0;

        for (TovarGroupSDB item : list) {
            if (item == null || item.nm == null || item.nm.trim().isEmpty()) continue;

            if (shown > 0) res.append(", ");
            res.append(item.nm.trim());
            shown++;

            if (shown == 3) break;
        }

        int remaining = size - 3; // по ТЗ считаем от исходного размера
        if (res.length() > 0) {
            res.append(" и еще ").append(remaining).append(" отделов.");
        } else {
            // на случай если первые 3 были пустые по nm
            return "и еще " + size + " отделов.";
        }

        return res.toString();
    }


    public String getIdFromList(List<TovarGroupSDB> list) {
        StringBuilder res = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            for (TovarGroupSDB item : list) {
                res.append(item.id).append(", ");
            }
            res = new StringBuilder(res.substring(0, res.length() - 2));
        } else {
            return "";
        }

        return res.toString();
    }
}
