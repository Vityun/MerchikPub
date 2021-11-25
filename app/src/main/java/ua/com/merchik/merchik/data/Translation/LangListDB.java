package ua.com.merchik.merchik.data.Translation;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
//import androidx.room.PrimaryKey;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@Entity(tableName = "languages")
public class LangListDB extends RealmObject {
//    @SerializedName("ID")
//    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String iD;
//    @SerializedName("nm")
//    @Expose
    @ColumnInfo(name = "nm")
    public String nm;
//    @SerializedName("nm_short")
//    @Expose
    @ColumnInfo(name = "nm_short")
    public String nmShort;

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

}
