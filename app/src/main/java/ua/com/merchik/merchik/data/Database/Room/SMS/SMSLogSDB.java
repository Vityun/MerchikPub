package ua.com.merchik.merchik.data.Database.Room.SMS;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "sms_log", indices = {@Index(value = {"serverId"}, unique = true)})
public class SMSLogSDB {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "serverId")
    @NonNull
    public Long serverId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;

    @SerializedName("dt_act")
    @Expose
    @ColumnInfo(name = "dt_act")
    public Long dtAct;

    @SerializedName("sender")
    @Expose
    @ColumnInfo(name = "sender")
    public Integer sender;

    @SerializedName("sender_nm")
    @Expose
    @ColumnInfo(name = "sender_nm")
    public String senderNm;

    @SerializedName("recipient_id")
    @Expose
    @ColumnInfo(name = "recipient_id")
    public Integer recipientId;

    @SerializedName("recipient_tel")
    @Expose
    @ColumnInfo(name = "recipient_tel")
    public String recipientTel;

    @SerializedName("recipient_email")
    @Expose
    @ColumnInfo(name = "recipient_email")
    public String recipientEmail;

    @SerializedName("recipient_list")
    @Expose
    @ColumnInfo(name = "recipient_list")
    public String recipientList;

    @SerializedName("chat_id")
    @Expose
    @ColumnInfo(name = "chat_id")
    public Integer chatId;

    @SerializedName("txt")
    @Expose
    @ColumnInfo(name = "txt")
    public String txt;

    @SerializedName("txt_long")
    @Expose
    @ColumnInfo(name = "txt_long")
    public String txtLong;

    @SerializedName("tp")
    @Expose
    @ColumnInfo(name = "tp")
    public Integer tp;

    @SerializedName("msg_type")
    @Expose
    @ColumnInfo(name = "msg_type")
    public Integer msgType;

    @SerializedName("msg_type_code")
    @Expose
    @ColumnInfo(name = "msg_type_code")
    public Integer msgTypeCode;

    @SerializedName("gw_type")
    @Expose
    @ColumnInfo(name = "gw_type")
    public Integer gwType;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @SerializedName("client_from")
    @Expose
    @ColumnInfo(name = "client_from")
    public String clientFrom;

    @SerializedName("client_to")
    @Expose
    @ColumnInfo(name = "client_to")
    public String clientTo;

    @SerializedName("var_id")
    @Expose
    @ColumnInfo(name = "var_id")
    public Long varId;

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public Long codeDad2;

    @SerializedName("msg_hash")
    @Expose
    @ColumnInfo(name = "msg_hash")
    public String msgHash;

    @SerializedName("priority")
    @Expose
    @ColumnInfo(name = "priority")
    public String priority;

}
