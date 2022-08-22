package ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class AdditionalMaterialsJOINAdditionalMaterialsAddressSDB {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @ColumnInfo(name = "approve")
    public String approve;

    @ColumnInfo(name = "client")
    public String client;

    @ColumnInfo(name = "user_id")
    public Integer user_id;

    @ColumnInfo(name = "dt")
    public String dt;

    @ColumnInfo(name = "expire")
    public String expire;

    @ColumnInfo(name = "file_archive")
    public String fileArchive;

    @ColumnInfo(name = "txt")
    public String txt;

    @ColumnInfo(name = "file_ext")
    public String fileExt;

    @ColumnInfo(name = "file_size")
    public String fileSize;

    @ColumnInfo(name = "score")
    public String score;

    @ColumnInfo(name = "score_cnt")
    public String scoreCnt;

    @ColumnInfo(name = "score_sum")
    public String scoreSum;

    @ColumnInfo(name = "state")
    public String state;

    @ColumnInfo(name = "idAMAddr")
    public Integer idAMAddr;

    @ColumnInfo(name = "file_id")
    public Integer fileId;

    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;
}
