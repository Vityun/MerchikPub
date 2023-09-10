package ua.com.merchik.merchik.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import java.util.Date;

public class Data implements Parcelable {

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

    protected Data(Parcel in) {
        id = in.readLong();
        addr = in.readString();
        cust = in.readString();
        merc = in.readString();
        date = new Date(in.readLong());
        otchetId = in.readLong();
        s = in.readString();
        images = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(addr);
        dest.writeString(cust);
        dest.writeString(merc);
        dest.writeLong(date.getTime());
        dest.writeLong(otchetId);
        dest.writeString(s);
        dest.writeInt(images);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

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