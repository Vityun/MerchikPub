package ua.com.merchik.merchik.data.UploadToServ;



public class WPDataAdditionalServ {

    public long element_id;

    public long dt;
    public int client_id;
    public String isp;
    public int addr_id;
    public long code_dad2;
    public int theme_id;
    public String user_decision;
    public int user_session_id;

    // Конструктор со всеми параметрами

    public WPDataAdditionalServ(){};

    public WPDataAdditionalServ(long element_id, long dt, int client_id, String isp, int addr_id,
                                long code_dad2, int theme_id, String user_decision, int user_session_id) {
        this.element_id = element_id;
        this.dt = dt;
        this.client_id = client_id;
        this.isp = isp;
        this.addr_id = addr_id;
        this.code_dad2 = code_dad2;
        this.theme_id = theme_id;
        this.user_decision = user_decision;
        this.user_session_id = user_session_id;
    }

    // Геттеры и сеттеры
    public long getElement_id() {
        return element_id;
    }

    public void setElement_id(long element_id) {
        this.element_id = element_id;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public int getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(int addr_id) {
        this.addr_id = addr_id;
    }

    public long getCode_dad2() {
        return code_dad2;
    }

    public void setCode_dad2(long code_dad2) {
        this.code_dad2 = code_dad2;
    }

    public int getTheme_id() {
        return theme_id;
    }

    public void setTheme_id(int theme_id) {
        this.theme_id = theme_id;
    }

    public String getUser_decision() {
        return user_decision;
    }

    public void setUser_decision(String user_decision) {
        this.user_decision = user_decision;
    }

    public int getUser_session_id() {
        return user_session_id;
    }

    public void setUser_session_id(int user_session_id) {
        this.user_session_id = user_session_id;
    }
}
