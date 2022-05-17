package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Premial {
    @SerializedName("state")
    @Expose
    public boolean state;
    @SerializedName("data")
    @Expose
    public Data data;
}

/*
Передаю Вове
* сотрудника, период(начало/конец)



* */
