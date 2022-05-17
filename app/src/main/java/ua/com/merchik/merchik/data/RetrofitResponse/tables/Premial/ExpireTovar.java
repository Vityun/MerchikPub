package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExpireTovar {
    @SerializedName("list_expire")
    @Expose
    public List<Object> listExpire = null;
    @SerializedName("list_expire_client")
    @Expose
    public List<Object> listExpireClient = null;
    @SerializedName("list_addr_client")
    @Expose
    public List<Object> listAddrClient = null;
    @SerializedName("expire_count")
    @Expose
    public long expireCount;
}
