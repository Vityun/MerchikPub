package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LogMPDB extends RealmObject {

    @PrimaryKey
    public int id;
    public long serverId;
    public String gp;

//    public Location location;   // 25.08.23. Тут буде зберігатися інфа щодо координат

    public Integer provider;        // 27.08.23. Провайдер.. // 0=NULL; 1=GPS; 2=NET.

    public double CoordX, CoordY, CoordAltitude;
    public long CoordTime;
    public float CoordSpeed, CoordAccuracy;
    public boolean mocking;

    public long codeDad2;       // 25.08.23. Код дад2. Все як завжди
    public long vpi;            // 25.08.23. Останній час змінення (час запису в БД)

    public int address;         // 27.08.23 Адрес
    public int distance;        // 27.08.23 Расстояние между адресом и координатами.
    public int inPlace;         // 27.08.23 Признак "На месте" 1 - на месте, 0 - НЕ на месте;

    public long upload;         // 27.08.23. Время выгрузки на сервер координат. Это для того что б не забить сервер.

    public LogMPDB() {
    }

    public LogMPDB(int id, String gp) {
        this.id = id;
        this.gp = gp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGp() {
        return gp;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }
}
