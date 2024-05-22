package ua.com.merchik.merchik.data.Database.Room.Planogram;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.sql.Date;

@Entity(tableName = "planogramm_join")
public class PlanogrammJOINSDB {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;

    @ColumnInfo(name = "planogrammClientId")
    public Integer planogrammClientId;

    @ColumnInfo(name = "planogrammClientTxt")
    public String planogrammClientTxt;

    @ColumnInfo(name = "planogrammName")
    public String planogrammName;

    @ColumnInfo(name = "planogrammComment")
    public String planogrammComment;

    @ColumnInfo(name = "planogrammDtStart")
    @TypeConverters({DateConverter.class})
    public Date planogrammDtStart;

    @ColumnInfo(name = "planogrammDtEnd")
    @TypeConverters({DateConverter.class})
    public Date planogrammDtEnd;

    @ColumnInfo(name = "planogrammAddress")
    public Integer planogrammAddress;

    @ColumnInfo(name = "planogrammAddressTxt")
    public String planogrammAddressTxt;

    @ColumnInfo(name = "planogrammCityTxt")
    public String planogrammCityTxt;

    @ColumnInfo(name = "planogrammGroupId")
    public Integer planogrammGroupId;

    @ColumnInfo(name = "planogrammGroupTxt")
    public String planogrammGroupTxt;

    @ColumnInfo(name = "planogrammPhotoId")
    public Integer planogrammPhotoId;

    @Ignore
    @ColumnInfo(name = "planogrammPhoto")
    public int planogrammPhoto;

    public static class DateConverter {
        @TypeConverter
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        @TypeConverter
        public static Long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }

}
