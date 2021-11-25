package ua.com.merchik.merchik.data.Database.Room.UsersSDBDat;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.Expose;

public class UsersSDB_1 {

    @Expose
    @ColumnInfo(name = "id")
    public Integer id;

    @Expose
    @ColumnInfo(name = "fio")
    public String fio;

    @Expose
    @ColumnInfo(name = "city_id")
    public Integer cityId;

    @Expose
    @ColumnInfo(name = "work_addr_id")
    public Integer workAddrId;

    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;
}
