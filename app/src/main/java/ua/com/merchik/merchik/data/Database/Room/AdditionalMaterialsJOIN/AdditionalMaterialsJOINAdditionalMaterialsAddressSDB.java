package ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class AdditionalMaterialsJOINAdditionalMaterialsAddressSDB {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @ColumnInfo(name = "client")
    public String client;

    @ColumnInfo(name = "expire")
    public String expire;

    @ColumnInfo(name = "state")
    public String state;

    @ColumnInfo(name = "approve")
    public String approve;

    @ColumnInfo(name = "user_id")
    public Integer user_id;

    @ColumnInfo(name = "dt")
    public String dt;

    @ColumnInfo(name = "file_archive")
    public String fileArchive;

    @ColumnInfo(name = "file_ext")
    public String fileExt;

    @ColumnInfo(name = "file_size")
    public String fileSize;

    @ColumnInfo(name = "txt")
    public String txt;

    @ColumnInfo(name = "score")
    public String score;

    @ColumnInfo(name = "score_cnt")
    public String scoreCnt;

    @ColumnInfo(name = "score_sum")
    public String scoreSum;

    // ama
    @ColumnInfo(name = "amaId")
    @NonNull
    public Integer amaId;

    @ColumnInfo(name = "amaFileId")
    public Integer amaFileId;

    @ColumnInfo(name = "amaAddrId")
    public Integer amaAddrId;

    @ColumnInfo(name = "amaAuthorId")
    public Integer amaAuthorId;

    @ColumnInfo(name = "amaDtUpdate")
    public Long amaDtUpdate;

}
