package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class StackPhotoDB extends RealmObject {

    @PrimaryKey
    private int id;
    private String photoServerId;   // ID фотки на сервере. Должно устанавливаться после того как сервер говорит что по хэшу такое фото уже на нём есть.

    private Long dt;                    // дата

    private Integer object_id;          //
    private Integer user_id;
    private Integer addr_id;
    private String client_id;
    private Integer theme_id;
    private String time_event;          // Дата выполнения работ с Плана работ. Формат: YYYY-MM-DD
    private long vpi;                   // Время последнего изменения
    private long create_time;           // Время создания фото
    private long upload_to_server;      // Время выгрузки НА сервер
    private long get_on_server;         // Время когда фото обработано сервером
    private long code_dad2;
    private String photo_num;           // Путь к файлу и его название
    private String photo_hash;          // Хэш фотографии
    private Integer photo_type;         // Тип фото (из таблички типов фото)

    private String photo_size;         // Размер фото Full - большой, Small - thumb_

    private String photo_user_id;
    private String photo_group_id;
    private String doc_id;
    private String comment;
    private String gp;
    private long upload_time;
    private long upload_status;
    private boolean status;

    private Integer error;
    private long errorTime;
    private String errorTxt;

    private String userTxt;
    private String customerTxt;
    private String addressTxt;
    private String photo_typeTxt;

    private Integer approve;
    private Integer dvi;            // ДВИ - для внутреннего использования
    private String mark;            // Оценка данной фотки. Должно в перспективе быть в другой табличке, но пока тут. Выставлять должен руководитель.
    private String premiya;         // Премия. НА данный момент мне не дали понять что именно и когда это устанавливается.
    private String photoServerURL;  // URL этой фотки на сервере для того что б её можно было загрузить в приложение. По умолчанию будет писаться thumb_ для экономии места/трафика

    private boolean dviUpload;
    private boolean commentUpload;
    private boolean markUpload;
    private boolean premiyaUpload;

    public StackPhotoDB() {
    }


    /*для сохранения фото Сотрудников*/
    public StackPhotoDB(int id, Integer user_id, long vpi, long create_time, long upload_to_server, long get_on_server, String photo_num, Integer photo_type, String photo_size, String photo_user_id, String userTxt, String photoServerURL) {
        this.id = id;
        this.user_id = user_id;
        this.vpi = vpi;
        this.create_time = create_time;
        this.upload_to_server = upload_to_server;
        this.get_on_server = get_on_server;
        this.photo_num = photo_num;
        this.photo_type = photo_type;
        this.photo_size = photo_size;
        this.photo_user_id = photo_user_id;
        this.userTxt = userTxt;
        this.photoServerURL = photoServerURL;
    }

    public StackPhotoDB(int id, Integer user_id, Integer addr_id, String client_id, Integer theme_id, String time_event, long vpi, long create_time, long upload_to_server, long get_on_server, long code_dad2, String photo_num, String photo_hash, Integer photo_type, String photo_user_id, String photo_group_id, String doc_id, String comment, String gp, long upload_time, long upload_status, boolean status) {
        this.id = id;
        this.user_id = user_id;
        this.addr_id = addr_id;
        this.client_id = client_id;
        this.theme_id = theme_id;
        this.time_event = time_event;
        this.vpi = vpi;
        this.create_time = create_time;
        this.upload_to_server = upload_to_server;
        this.get_on_server = get_on_server;
        this.code_dad2 = code_dad2;
        this.photo_num = photo_num;
        this.photo_hash = photo_hash;
        this.photo_type = photo_type;
        this.photo_user_id = photo_user_id;
        this.photo_group_id = photo_group_id;
        this.doc_id = doc_id;
        this.comment = comment;
        this.gp = gp;
        this.upload_time = upload_time;
        this.upload_status = upload_status;
        this.status = status;
    }

    public StackPhotoDB(int id, String photoServerId, Integer object_id, Integer user_id, Integer addr_id, String client_id, Integer theme_id, String time_event, long vpi, long create_time, long upload_to_server, long get_on_server, long code_dad2, String photo_num, String photo_hash, Integer photo_type, String photo_user_id, String photo_group_id, String doc_id, String comment, String gp, long upload_time, long upload_status, boolean status, String userTxt, String customerTxt, String addressTxt) {
        this.id = id;
        this.photoServerId = photoServerId;
        this.object_id = object_id;
        this.user_id = user_id;
        this.addr_id = addr_id;
        this.client_id = client_id;
        this.theme_id = theme_id;
        this.time_event = time_event;
        this.vpi = vpi;
        this.create_time = create_time;
        this.upload_to_server = upload_to_server;
        this.get_on_server = get_on_server;
        this.code_dad2 = code_dad2;
        this.photo_num = photo_num;
        this.photo_hash = photo_hash;
        this.photo_type = photo_type;
        this.photo_user_id = photo_user_id;
        this.photo_group_id = photo_group_id;
        this.doc_id = doc_id;
        this.comment = comment;
        this.gp = gp;
        this.upload_time = upload_time;
        this.upload_status = upload_status;
        this.status = status;
        this.userTxt = userTxt;
        this.customerTxt = customerTxt;
        this.addressTxt = addressTxt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getObject_id() {
        return object_id;
    }

    public void setObject_id(Integer object_id) {
        this.object_id = object_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(Integer addr_id) {
        this.addr_id = addr_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public Integer getTheme_id() {
        return theme_id;
    }

    public void setTheme_id(Integer theme_id) {
        this.theme_id = theme_id;
    }

    public String getTime_event() {
        return time_event;
    }

    public void setTime_event(String time_event) {
        this.time_event = time_event;
    }

    public long getVpi() {
        return vpi;
    }

    public void setVpi(long vpi) {
        this.vpi = vpi;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public long getUpload_to_server() {
        return upload_to_server;
    }

    public void setUpload_to_server(long upload_to_server) {
        this.upload_to_server = upload_to_server;
    }

    public long getGet_on_server() {
        return get_on_server;
    }

    public void setGet_on_server(long get_on_server) {
        this.get_on_server = get_on_server;
    }

    public long getCode_dad2() {
        return code_dad2;
    }

    public void setCode_dad2(long code_dad2) {
        this.code_dad2 = code_dad2;
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

    public Integer getPhoto_type() {
        return photo_type;
    }

    public void setPhoto_type(Integer photo_type) {
        this.photo_type = photo_type;
    }


    public String getPhoto_size() {
        return photo_size;
    }

    public void setPhoto_size(String photo_size) {
        this.photo_size = photo_size;
    }

    public Integer getDvi() {
        return dvi;
    }

    public String getPhoto_user_id() {
        return photo_user_id;
    }

    public void setPhoto_user_id(String photo_user_id) {
        this.photo_user_id = photo_user_id;
    }

    public String getPhoto_group_id() {
        return photo_group_id;
    }

    public void setPhoto_group_id(String photo_group_id) {
        this.photo_group_id = photo_group_id;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getGp() {
        return gp;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }

    public long getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(long upload_time) {
        this.upload_time = upload_time;
    }

    public long getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(long upload_status) {
        this.upload_status = upload_status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getUserTxt() {
        return userTxt;
    }

    public void setUserTxt(String userTxt) {
        this.userTxt = userTxt;
    }

    public String getCustomerTxt() {
        return customerTxt;
    }

    public void setCustomerTxt(String customerTxt) {
        this.customerTxt = customerTxt;
    }

    public String getAddressTxt() {
        return addressTxt;
    }

    public void setAddressTxt(String addressTxt) {
        this.addressTxt = addressTxt;
    }

    public String getPhoto_typeTxt() {
        return photo_typeTxt;
    }

    public void setPhoto_typeTxt(String photo_typeTxt) {
        this.photo_typeTxt = photo_typeTxt;
    }

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public long getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(long errorTime) {
        this.errorTime = errorTime;
    }

    public String getErrorTxt() {
        return errorTxt;
    }

    public void setErrorTxt(String errorTxt) {
        this.errorTxt = errorTxt;
    }

    public Integer isDvi() {
        return dvi;
    }

    public void setDvi(Integer dvi) {
        this.dvi = dvi;
    }

    public String getPhotoServerId() {
        return photoServerId;
    }

    public void setPhotoServerId(String photoServerId) {
        this.photoServerId = photoServerId;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getPremiya() {
        return premiya;
    }

    public void setPremiya(String premiya) {
        this.premiya = premiya;
    }

    public String getPhotoServerURL() {
        return photoServerURL;
    }

    public void setPhotoServerURL(String photoServerURL) {
        this.photoServerURL = photoServerURL;
    }


    public boolean isDviUpload() {
        return dviUpload;
    }

    public void setDviUpload(boolean dviUpload) {
        this.dviUpload = dviUpload;
    }

    public boolean isCommentUpload() {
        return commentUpload;
    }

    public void setCommentUpload(boolean commentUpload) {
        this.commentUpload = commentUpload;
    }

    public boolean isMarkUpload() {
        return markUpload;
    }

    public void setMarkUpload(boolean markUpload) {
        this.markUpload = markUpload;
    }

    public boolean isPremiyaUpload() {
        return premiyaUpload;
    }

    public void setPremiyaUpload(boolean premiyaUpload) {
        this.premiyaUpload = premiyaUpload;
    }

    public Integer getApprove() {
        return approve;
    }

    public void setApprove(Integer approve) {
        this.approve = approve;
    }

    public Long getDt() {
        return dt;
    }

    public void setDt(Long dt) {
        this.dt = dt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackPhotoDB that = (StackPhotoDB) o;
        return id == that.id;
    }


}
