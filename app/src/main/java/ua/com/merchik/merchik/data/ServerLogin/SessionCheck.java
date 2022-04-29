package ua.com.merchik.merchik.data.ServerLogin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.WebSocketData.WebsocketParam;

public class SessionCheck {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("stats")
    @Expose
    private Stats stats;
    @SerializedName("auth")
    @Expose
    private Boolean auth;
    @SerializedName("user_info")
    @Expose
    private UserInfo userInfo;
    @SerializedName("predp_info")
    @Expose
    private PredpInfo predpInfo;
    @SerializedName("session_id")
    @Expose
    private String sessionId;

    @SerializedName("websocket_param")
    @Expose
    public WebsocketParam websocketParam;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Boolean getAuth() {
        return auth;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public PredpInfo getPredpInfo() {
        return predpInfo;
    }

    public void setPredpInfo(PredpInfo predpInfo) {
        this.predpInfo = predpInfo;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
