package ua.com.merchik.merchik.data.Database.Room.Planogram;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "planogramm_address")
public class PlanogrammAddressSDB {

    @SerializedName("ID")
    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;


    @SerializedName("planogram_id")
    @ColumnInfo(name = "planogram_id")
    public Integer planogramId;


    @SerializedName("addr_id")
    @ColumnInfo(name = "addr_id")
    public Integer addrId;


    @SerializedName("city_txt")
    @ColumnInfo(name = "city_txt")
    public String cityTxt;


    @SerializedName("addr_txt")
    @ColumnInfo(name = "addr_txt")
    public String addrTxt;


    @SerializedName("group_txt")
    @ColumnInfo(name = "group_txt")
    public String groupTxt;


    @SerializedName("author_txt")
    @ColumnInfo(name = "author_txt")
    public String authorTxt;


    @SerializedName("author_id")
    @ColumnInfo(name = "author_id")
    public Integer authorId;


    @SerializedName("dt_update_ut")
    @ColumnInfo(name = "dt_update_ut")
    public Long dtUpdateUt;


    @SerializedName("dt_update")
    @ColumnInfo(name = "dt_update")
    public String dtUpdate;
}
