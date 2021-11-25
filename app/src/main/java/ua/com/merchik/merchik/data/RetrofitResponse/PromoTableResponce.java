package ua.com.merchik.merchik.data.RetrofitResponse;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.PromoDB;

public class PromoTableResponce {

    private Boolean state;
    private List<PromoDB> list = null;
    private Integer serverTime;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<PromoDB> getList() {
        return list;
    }

    public void setList(List<PromoDB> list) {
        this.list = list;
    }

    public Integer getServerTime() {
        return serverTime;
    }

    public void setServerTime(Integer serverTime) {
        this.serverTime = serverTime;
    }
}
