package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;

@Dao
public interface AdditionalMaterialsDao {
    @Query("SELECT * FROM additional_materials")
    List<AdditionalMaterialsSDB> getAll();

    @Query("SELECT * FROM additional_materials WHERE id = :id")
    AdditionalMaterialsSDB getById(int id);

    @Query("SELECT * FROM additional_materials WHERE client = :clientId")
    List<AdditionalMaterialsSDB> getAllByClientId(String clientId);

    @Query("SELECT * FROM additional_materials WHERE client = :clientId AND approve = :approve AND state = :state AND expire >= :expire")
    List<AdditionalMaterialsSDB> getAllForOption(String clientId, String approve, String state, String expire);
    // approve, state, expire,

    // TEST
    @Query("SELECT ad.id AS id, ad.client AS client, ad.expire AS expire, ad.state AS state, " +
            "ad.approve AS approve, ad.user_id AS user_id, ad.dt AS dt, ad.file_archive AS file_archive, " +
            "ad.file_ext AS file_ext, ad.file_size AS file_size, ad.score AS score, ad.score_cnt AS score_cnt, " +
            "ad.score_sum AS score_sum, ad.txt AS txt, ama.id AS amaId, ama.file_id AS amaFileId, " +
            "ama.addr_id AS amaAddrId, ama.author_id AS amaAuthorId, ama.dt_update AS amaDtUpdate FROM additional_materials AS ad " +
            "INNER JOIN additional_materials_address AS ama ON ad.id = ama.file_id " +
            "WHERE client = :clientId AND approve = :approve AND state = :state")
    List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> getAllForOptionTEST(String clientId, String approve, String state);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AdditionalMaterialsSDB> data);
}
