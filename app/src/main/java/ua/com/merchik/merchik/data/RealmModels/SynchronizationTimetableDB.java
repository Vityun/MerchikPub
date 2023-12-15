package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SynchronizationTimetableDB extends RealmObject {
    @PrimaryKey
    private int id;
    private String table_name;
    private int update_frequency;
    private long vpi_server;
    private long vpi_app;
    private long vpo_export;
    private long vpo_app;

    public String tableTxt;
    public int update;

    public SynchronizationTimetableDB() {
    }

    public SynchronizationTimetableDB(int id, String table_name, int update_frequency, long vpiServer, long vpiApp, long vpoExport, long vpoApp, String tableTxt, int update) {
        this.id = id;
        this.table_name = table_name;
        this.update_frequency = update_frequency;
        this.vpi_server = vpiServer;
        this.vpi_app = vpiApp;
        this.vpo_export = vpoExport;
        this.vpo_app = vpoApp;
        this.tableTxt = tableTxt;
        this.update = update;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public int getUpdate_frequency() {
        return update_frequency;
    }

    public void setUpdate_frequency(int update_frequency) {
        this.update_frequency = update_frequency;
    }

    public long getVpi_server() {
        return vpi_server;
    }

    public void setVpi_server(long vpi_server) {
        this.vpi_server = vpi_server;
    }

    public long getVpi_app() {
        return vpi_app;
    }

    public void setVpi_app(long vpi_app) {
        this.vpi_app = vpi_app;
    }

    public long getVpo_export() {
        return vpo_export;
    }

    public void setVpo_export(long vpo_export) {
        this.vpo_export = vpo_export;
    }

    public long getVpo_app() {
        return vpo_app;
    }

    public void setVpo_app(long vpo_app) {
        this.vpo_app = vpo_app;
    }
}


