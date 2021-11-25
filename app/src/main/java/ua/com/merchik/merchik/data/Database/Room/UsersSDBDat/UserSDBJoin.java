package ua.com.merchik.merchik.data.Database.Room.UsersSDBDat;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.sql.Date;

public class UserSDBJoin {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @ColumnInfo(name = "fio")
    public String fio;

    @ColumnInfo(name = "tel")
    public String tel;

    @ColumnInfo(name = "tel2")
    public String tel2;

    @ColumnInfo(name = "client_id")
    public Integer clientId;

    @ColumnInfo(name = "department")
    public Integer department;

    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;

    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @ColumnInfo(name = "city_id")
    public Integer cityId;

    @ColumnInfo(name = "work_addr_id")
    public Integer workAddrId;

    @ColumnInfo(name = "inn")
    public String inn;

    @ColumnInfo(name = "report_count")
    public Integer reportCount;

    @ColumnInfo(name = "report_date_01")
    public Date reportDate01;

    @ColumnInfo(name = "report_date_05")
    public Date reportDate05;

    @ColumnInfo(name = "report_date_20")
    public Date reportDate20;

    @ColumnInfo(name = "report_date_40")
    public Date reportDate40;

    @ColumnInfo(name = "nm")
    public String nm;
}
