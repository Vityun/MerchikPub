package ua.com.merchik.merchik.data;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class HashElements {

    @SerializedName("client_id")
    @Expose
    private Map<String, String> clientId;

    @SerializedName("addr_id")
    @Expose
    private Map<String, String> addrId;

    @SerializedName("code_dad2")
    @Expose
    private Map<String, String> codeDad2;

    public Map<String, String> getClientId() { return clientId; }
    public void setClientId(Map<String, String> clientId) { this.clientId = clientId; }

    public Map<String, String> getAddrId() { return addrId; }
    public void setAddrId(Map<String, String> addrId) { this.addrId = addrId; }

    public Map<String, String> getCodeDad2() { return codeDad2; }
    public void setCodeDad2(Map<String, String> codeDad2) { this.codeDad2 = codeDad2; }
}
