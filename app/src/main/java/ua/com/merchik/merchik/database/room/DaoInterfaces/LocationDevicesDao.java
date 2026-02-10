package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.LocationDevices;

@Dao
public interface LocationDevicesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<LocationDevices> items);

    @Query("SELECT * FROM location_devices ORDER BY dtCreate DESC")
    List<LocationDevices> getAll();

    @Query("SELECT * FROM location_devices WHERE id = :id LIMIT 1")
    LocationDevices getById(long id);

    // Есть ли запись с таким MAC + addr_id
    @Query("SELECT EXISTS(SELECT 1 FROM location_devices WHERE mac = :mac AND addrId = :addrId LIMIT 1)")
    boolean existsByMacAndAddrId(String mac, long addrId);

    @Query("SELECT EXISTS(SELECT 1 FROM location_devices WHERE mac = :mac COLLATE NOCASE AND addrId = :addrId LIMIT 1)")
    boolean existsByMacAndAddrIdNoCase(String mac, long addrId);


    @Query("DELETE FROM location_devices")
    void clear();
}
