package ua.com.merchik.merchik.data;

import android.view.View;

import java.io.Serializable;
import java.util.Date;

public class Data implements Serializable{

    private long id;
    private String addr;
    private String cust;
    private String merc;
    private Date date;

    private long otchetId;

    private View options;
    private String s;

    private int images;


    public Data(long id, String addr, String cust, String merc,
                Date date, long otchetId, View options, int images) {
        this.id = id;
        this.addr = addr;
        this.cust = cust;
        this.merc = merc;
        this.date = date;
        this.otchetId = otchetId;
        this.options = options;
        this.images = images;
    }

    public Data(long id, String addr, String cust, String merc, Date date, long otchetId, String s, int images) {
        this.id = id;
        this.addr = addr;
        this.cust = cust;
        this.merc = merc;
        this.date = date;
        this.otchetId = otchetId;
        this.s = s;
        this.images = images;
    }

    public Long getId() {
        return this.id;
    }

    public String getAddr() {
        return this.addr;
    }

    public String getCust() {
        return this.cust;
    }

    public String getMerc() {
        return this.merc;
    }

    public Date getDate() {
        return this.date;
    }

    public long getOtchetId() {
        return this.otchetId;
    }

    public View getOptionsSignals() {
        return this.options;
    }

    public String getOptionsSignalsString() {
        return this.s;
    }

    public int getImages() {
        return this.images;
    }

}