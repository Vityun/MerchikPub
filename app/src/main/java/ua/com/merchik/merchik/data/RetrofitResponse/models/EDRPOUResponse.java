package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EDRPOUResponse {
    @SerializedName("label")
    @Expose
    public String label;
    @SerializedName("company_name")
    @Expose
    public String companyName;
    @SerializedName("company_id")
    @Expose
    public String companyId;
    @SerializedName("client_id")
    @Expose
    public String clientId;
    @SerializedName("confirmation")
    @Expose
    public boolean confirmation;

    @Override
    public String toString() {
        return companyName;
    }
}
