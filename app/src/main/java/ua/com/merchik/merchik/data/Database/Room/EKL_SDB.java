package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ekl")
public class EKL_SDB {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;  // id записи в базе данных. Код ЭКЛ-а на стороне сайта.

    @ColumnInfo(name = "user_id")
    public Integer userId;

    @ColumnInfo(name = "sotr_id")
    public Integer sotrId;

    @ColumnInfo(name = "client_id")
    public Integer clientId;

    @ColumnInfo(name = "address_id")
    public Integer addressId;

    @ColumnInfo(name = "dad2")
    public Long dad2;   //

    @ColumnInfo(name = "code")
    public String code; // экл

    @ColumnInfo(name = "ekl_code")
    public String eklCode; // код ЭКЛ-а

    @ColumnInfo(name = "ekl_hash_code")
    public String eklHashCode; // код ЭКЛ-а

    @ColumnInfo(name = "state")
    public Boolean state;   // статус ответа от сервера по данному ЭКЛ-у

    @ColumnInfo(name = "vpi")
    public Long vpi;    // Время последнего изменения

    @ColumnInfo(name = "vpiSend")
    public Long vpiSend;    // Время последнего изменения нажатия на кнопку выгрузить 

    @ColumnInfo(name = "upload")
    public Boolean upload;  // Статус выгрузки данного ЭКЛ-а на сервер

    @ColumnInfo(name = "comment")
    public String comment;  // Комментарий к записи.
}
