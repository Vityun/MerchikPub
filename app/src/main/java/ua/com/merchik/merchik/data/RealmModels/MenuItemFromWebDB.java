package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MenuItemFromWebDB extends RealmObject {

    @PrimaryKey
    private Integer id;
    private String nm;
    private String url;
    private String module;
    private String internalName;
    private Integer parent;
    private RealmList<Integer> submenu;

    private String img;
    private String comment;

    public Integer getID() {
        return id;
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public RealmList<Integer> getSubmenu() {
        return submenu;
    }

    public void setSubmenu(RealmList<Integer> submenu) {
        this.submenu = submenu;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
