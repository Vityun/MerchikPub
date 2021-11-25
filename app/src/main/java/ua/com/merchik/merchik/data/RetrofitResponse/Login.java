package ua.com.merchik.merchik.data.RetrofitResponse;

public class Login {

    private Boolean state;
    private String error;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
