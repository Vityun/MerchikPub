package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "ekl")
public class EKL_SDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;  // id записи в базе данных. Код ЭКЛ-а на стороне сайта.

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;

    @SerializedName("dt_verify")
    @Expose
    @ColumnInfo(name = "dt_verify")
    public Long dtVerify;

    @SerializedName("user_id")
    @Expose
    @ColumnInfo(name = "user_id")
    public Integer userId;  // Кто отправляет ЭКЛ

    @SerializedName("user_id_verify")
    @Expose
    @ColumnInfo(name = "sotr_id")
    public Integer sotrId;  // ПТТ

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "address_id")
    public Integer addressId;

    @SerializedName("department")
    @Expose
    @ColumnInfo(name = "department")
    public Integer department;  //ГруппуТоваров у ЭКЛ

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "dad2")
    public Long dad2;   //

    @ColumnInfo(name = "code")
    public String code; // экл

    @SerializedName("ekl_code")
    @Expose
    @ColumnInfo(name = "ekl_code")
    public String eklCode; // код ЭКЛ-а

    @SerializedName("code_check")
    @Expose
    @ColumnInfo(name = "ekl_hash_code")
    public String eklHashCode; // код ЭКЛ-а

    @ColumnInfo(name = "state")
    public Boolean state;   // статус ответа от сервера по данному ЭКЛ-у

    @SerializedName("doc_type")
    @Expose
    @ColumnInfo(name = "doc_type")
    public Integer docType;

    @SerializedName("doc_num")
    @Expose
    @ColumnInfo(name = "doc_num")
    public Long docNum;

    @SerializedName("doc_num_1c")
    @Expose
    @ColumnInfo(name = "doc_num_1c")
    public String docNum1c;

    @ColumnInfo(name = "vpi")
    public Long vpi;    // Время последнего изменения

    @ColumnInfo(name = "vpiSend")
    public Long vpiSend;    // Время последнего изменения нажатия на кнопку выгрузить 

    @ColumnInfo(name = "upload")
    public Boolean upload;  // Статус выгрузки данного ЭКЛ-а на сервер

    @ColumnInfo(name = "comment")
    public String comment;  // Комментарий к записи.

    @SerializedName("code_verify")
    @Expose
    @ColumnInfo(name = "code_verify")
    public Integer codeVerify;
}

/*
{"ID":"620533",=
"dt":"1663752526",=
"dt_verify":"0",=
"client_id":"10349",=
"addr_id":"28847",=
"user_id":"176053",=
"user_id_verify":"232361",=
"doc_type":"1",=
"doc_num":"9401952729",=
"doc_num_1c":"АОи-01952729",=
"code_dad2":"1210922028847052363",=
"department":"148",=
"code_check":"73b676f3924651a68732c6c69528344566628495",
"code_verify":"0"},*/
