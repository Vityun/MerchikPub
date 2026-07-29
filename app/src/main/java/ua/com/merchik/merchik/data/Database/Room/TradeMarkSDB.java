package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trade_mark")
public class TradeMarkSDB {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id = "";

    @ColumnInfo(name = "nm")
    public String nm;

    @ColumnInfo(name = "dt_update")
    public String dtUpdate;

    @ColumnInfo(name = "sort_type")
    public String sortType;
}
