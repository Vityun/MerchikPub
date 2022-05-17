package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("reclamation")
    @Expose
    public Reclamation reclamation;
    @SerializedName("mail")
    @Expose
    public Mail mail;
    @SerializedName("work_plan")
    @Expose
    public WorkPlan workPlan;
    @SerializedName("dop_treb")
    @Expose
    public DopTreb dopTreb;
    @SerializedName("expire_tovar")
    @Expose
    public ExpireTovar expireTovar;
    @SerializedName("sotr")
    @Expose
    public Sotr sotr;
    @SerializedName("potential_clients")
    @Expose
    public PotentialClients potentialClients;
    @SerializedName("order_data")
    @Expose
    public OrderData orderData;
    @SerializedName("request_approve_data")
    @Expose
    public RequestApproveData requestApproveData;
    @SerializedName("chat")
    @Expose
    public Chat chat;
    @SerializedName("messenger")
    @Expose
    public Messenger messenger;
    @SerializedName("premium")
    @Expose
    public Premium premium;
    @SerializedName("training_content")
    @Expose
    public TrainingContent trainingContent;
    @SerializedName("options_data")
    @Expose
    public OptionsData optionsData;
    @SerializedName("images_achieve")
    @Expose
    public ImagesAchieve imagesAchieve;
}
