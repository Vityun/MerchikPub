package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AppUsersDB extends RealmObject {

    @PrimaryKey
    private int userId;
    private String userTxt;
    private String login;
    private String password;
    public String user_work_plan_status;

    public AppUsersDB() {
    }

    public AppUsersDB(int userId, String userTxt, String login, String password, String user_work_plan_status) {
        this.userId = userId;
        this.userTxt = userTxt;
        this.login = login;
        this.password = password;
        this.user_work_plan_status = user_work_plan_status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserTxt() {
        return userTxt;
    }

    public void setUserTxt(String userTxt) {
        this.userTxt = userTxt;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
