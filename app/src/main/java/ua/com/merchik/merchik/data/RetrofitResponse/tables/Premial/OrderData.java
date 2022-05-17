package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderData {
    @SerializedName("total_visit_one_time")
    @Expose
    public long totalVisitOneTime;
    @SerializedName("total_visit_regular")
    @Expose
    public long totalVisitRegular;
    @SerializedName("total_cash_one_time")
    @Expose
    public long totalCashOneTime;
    @SerializedName("total_cash_regular")
    @Expose
    public double totalCashRegular;
    @SerializedName("decision_set")
    @Expose
    public long decisionSet;
    @SerializedName("decision_need")
    @Expose
    public long decisionNeed;
    @SerializedName("total_duration_per_month")
    @Expose
    public double totalDurationPerMonth;
}
