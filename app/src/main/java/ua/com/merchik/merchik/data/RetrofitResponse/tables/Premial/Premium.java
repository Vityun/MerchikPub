package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Premium {
    @SerializedName("avg_hour_rate")
    @Expose
    public String avgHourRate;
    @SerializedName("previous_period")
    @Expose
    public long previousPeriod;
    @SerializedName("current_period")
    @Expose
    public long currentPeriod;
}
