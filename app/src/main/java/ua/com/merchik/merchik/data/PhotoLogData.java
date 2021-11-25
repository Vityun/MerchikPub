package ua.com.merchik.merchik.data;

public class PhotoLogData {


    private int id;
    private int user_id;
    private int addr_id;
    private int cust_id;

    private String date;
    private String addr_txt;
    private String cust_txt;

    private String photo_num;
    private String photo_hash;
    private int photo_type;

    private long photo_create;
    private long photo_upload;
    private long photo_server;

    public PhotoLogData(int id, int user_id, int addr_id, String photo_num, String photo_hash, int photo_type){
        this.id = id;
        this.user_id = user_id;
        this.addr_id = addr_id;
        this.photo_num = photo_num;
        this.photo_hash = photo_hash;
        this.photo_type = photo_type;
    }

    public PhotoLogData(Integer id, Integer user_id, Integer photo_type, String date, Integer addr_id, Integer cust_id, String photo_num, Long photo_create, Long photo_upload, Long photo_server) {
        this.id = id;
        this.user_id = user_id;
        this.photo_type = photo_type;
        this.date = date;
        this.addr_id = addr_id;
        this.cust_id = cust_id;
        this.photo_num = photo_num;
        this.photo_create = photo_create;
        this.photo_upload = photo_upload;
        this.photo_server = photo_server;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(int addr_id) {
        this.addr_id = addr_id;
    }

    public String getPhoto_num() {
        return photo_num;
    }

    public void setPhoto_num(String photo_num) {
        this.photo_num = photo_num;
    }

    public String getPhoto_hash() {
        return photo_hash;
    }

    public void setPhoto_hash(String photo_hash) {
        this.photo_hash = photo_hash;
    }

    public int getPhoto_type() {
        return photo_type;
    }

    public void setPhoto_type(int photo_type) {
        this.photo_type = photo_type;
    }

    public long getPhoto_create() {
        return photo_create;
    }

    public void setPhoto_create(long photo_create) {
        this.photo_create = photo_create;
    }

    public long getPhoto_upload() {
        return photo_upload;
    }

    public void setPhoto_upload(long photo_upload) {
        this.photo_upload = photo_upload;
    }

    public long getPhoto_server() {
        return photo_server;
    }

    public void setPhoto_server(long photo_server) {
        this.photo_server = photo_server;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCust_id() {
        return cust_id;
    }

    public void setCust_id(int cust_id) {
        this.cust_id = cust_id;
    }
}
