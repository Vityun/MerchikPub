package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "images_type_list")
public class ImagesTypeListSDB {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "nm")
    public String nm;
}
