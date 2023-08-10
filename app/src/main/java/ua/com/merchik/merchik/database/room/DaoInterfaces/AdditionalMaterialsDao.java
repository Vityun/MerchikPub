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
            "LEFT JOIN additional_materials_address AS ama ON ad.id = ama.file_id " +
            "LEFT JOIN additional_materials_groups on additional_materials_groups.file_id=ad.id " +
            "WHERE client = :clientId AND amaAddrId = :addrId AND state = :state " +
            "GROUP BY ad.id " +
            "HAVING  (count(amaAddrId)>0)")
    List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> getAllForOptionTEST(String clientId, int addrId, String state);

    // TEST2
    @Query("SELECT ad.id AS id, ad.client AS client, ad.expire AS expire, ad.state AS state, " +
            "ad.approve AS approve, ad.user_id AS user_id, ad.dt AS dt, ad.file_archive AS file_archive, " +
            "ad.file_ext AS file_ext, ad.file_size AS file_size, ad.score AS score, ad.score_cnt AS score_cnt, " +
            "ad.score_sum AS score_sum, ad.txt AS txt, ama.id AS amaId, ama.file_id AS amaFileId, " +
            "ama.addr_id AS amaAddrId, ama.author_id AS amaAuthorId, ama.dt_update AS amaDtUpdate FROM additional_materials AS ad " +
            "LEFT JOIN additional_materials_address AS ama ON ad.id = ama.file_id " +
            "LEFT JOIN additional_materials_groups on additional_materials_groups.file_id=ad.id " +
            "LEFT JOIN address as adresa_addr on adresa_addr.ID=ama.addr_id " +
            "WHERE client = :clientId AND approve = 1 AND state = :state " +
            "GROUP BY ad.id ")
    List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> getAllForOptionTEST2(String clientId, String state);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AdditionalMaterialsSDB> data);


/*    select add_materials.ID as numberRow, add_materials.ID as select_id, add_materials.ID as select_name,
add_materials.ID as ID, add_materials.client as client, spiskli.nm as client_txt, add_materials.user_id as user_id,
add_materials.txt as upload_file, add_materials.file_size as file_size, add_materials.file_ext as file_ext,
add_materials.score_cnt as score_cnt, add_materials.score_sum as score_sum, adresa_addr.nomer_tt as nomer_tt,
count(distinct add_materials_assoc_grp.grp_id) as group_count, count(distinct if(add_materials_assoc_addr.addr_id>0,add_materials_assoc_addr.addr_id,null))
as addr_count, count(add_materials_assoc_addr.addr_id) as addr_count_total, add_materials.txt as txt,
add_materials.approve as approve, add_materials.approve as approve__data, add_materials.txt as load_file,
add_materials.dt as dt, add_materials.expire as expire, add_materials.state as file_archive, add_materials.state as file_archive__data,
add_materials.state as state, add_materials.score as score
    from add_materials
    inner join spiskli on spiskli.uid=add_materials.client
    left join add_materials_assoc_grp on add_materials_assoc_grp.file_id=add_materials.ID
    left join add_materials_assoc_addr on add_materials_assoc_addr.file_id=add_materials.ID
    left join adresa as adresa_addr on adresa_addr.ID=add_materials_assoc_addr.addr_id
    left join adresa as adresa_grp on adresa_grp.tp_id=add_materials_assoc_grp.grp_id
    where add_materials.client in ('32246') and
(add_materials_assoc_addr.addr_id = 0 or add_materials_assoc_grp.grp_id in (383) or adresa_addr.tp_id in (383)) and
    add_materials.state in (0)
    GROUP BY add_materials.ID
    having (count(add_materials_assoc_addr.addr_id)>0 or count(distinct add_materials_assoc_grp.grp_id)>0)

    limit 0,25*/
}
