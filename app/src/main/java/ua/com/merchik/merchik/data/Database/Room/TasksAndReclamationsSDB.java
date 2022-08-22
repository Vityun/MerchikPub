package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "tasks_and_reclamations")
public class TasksAndReclamationsSDB {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("tp")
    @Expose
    @ColumnInfo(name = "tp")
    public Integer tp;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;

    @SerializedName("dt_real_post")
    @Expose
    @ColumnInfo(name = "dt_real_post")
    public Long dtRealPost;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;

    @SerializedName("author")
    @Expose
    @ColumnInfo(name = "author")
    public Integer author;

    @SerializedName("addr")
    @Expose
    @ColumnInfo(name = "addr")
    public Integer addr;

    @SerializedName("client")
    @Expose
    @ColumnInfo(name = "client")
    public String client;

    @SerializedName("state")
    @Expose
    @ColumnInfo(name = "state")
    public Integer state;

    @SerializedName("photo")
    @Expose
    @ColumnInfo(name = "photo")
    public Integer photo;

    @SerializedName("photo_2")
    @Expose
    @ColumnInfo(name = "photo_2")
    public Integer photo2;

    @SerializedName("comment")
    @Expose
    @ColumnInfo(name = "comment")
    public String comment;

    @SerializedName("vinovnik")
    @Expose
    @ColumnInfo(name = "vinovnik")
    public Integer vinovnik;

    @SerializedName("vinovnik2")
    @Expose
    @ColumnInfo(name = "vinovnik2")
    public Integer vinovnik2;

    @SerializedName("vinovnik_read_dt")
    @Expose
    @ColumnInfo(name = "vinovnik_read_dt")
    public Long vinovnikReadDt;

    @SerializedName("zamena_user_id")
    @Expose
    @ColumnInfo(name = "zamena_user_id")
    public Integer zamenaUserId;

    @SerializedName("zamena_dt")
    @Expose
    @ColumnInfo(name = "zamena_dt")
    public Long zamenaDt;

    @SerializedName("zamena_who")
    @Expose
    @ColumnInfo(name = "zamena_who")
    public Integer zamenaWho;

    @SerializedName("contacter_id")
    @Expose
    @ColumnInfo(name = "contacter_id")
    public Integer contacterId;

    @SerializedName("super_id")
    @Expose
    @ColumnInfo(name = "super_id")
    public Integer superId;

    @SerializedName("territorial_id")
    @Expose
    @ColumnInfo(name = "territorial_id")
    public Integer territorialId;

    @SerializedName("regional_id")
    @Expose
    @ColumnInfo(name = "regional_id")
    public Integer regionalId;

    @SerializedName("nop_id")
    @Expose
    @ColumnInfo(name = "nop_id")
    public Integer nopId;

    @SerializedName("dvi")
    @Expose
    @ColumnInfo(name = "dvi")
    public Integer dvi;

    @SerializedName("zakazchik")
    @Expose
    @ColumnInfo(name = "zakazchik")
    public Integer zakazchik;

    @SerializedName("id_1c")
    @Expose
    @ColumnInfo(name = "id_1c")
    public String id1c;

    @SerializedName("doc_num_1c_id")
    @Expose
    @ColumnInfo(name = "doc_num_1c_id")
    public Long docNum1cId;

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public Long codeDad2;

    @SerializedName("code_dad2_src_doc")
    @Expose
    @ColumnInfo(name = "code_dad2_src_doc")
    public Long codeDad2SrcDoc;

    @SerializedName("tel_num")
    @Expose
    @ColumnInfo(name = "tel_num")
    public String telNum;

    @SerializedName("last_answer")
    @Expose
    @ColumnInfo(name = "last_answer")
    public String lastAnswer;

    @SerializedName("last_answer_user_id")
    @Expose
    @ColumnInfo(name = "last_answer_user_id")
    public Integer lastAnswerUserId;

    @SerializedName("last_answer_dt_change")
    @Expose
    @ColumnInfo(name = "last_answer_dt_change")
    public String lastAnswerDtChange;

    @SerializedName("respond")
    @Expose
    @ColumnInfo(name = "respond")
    public Integer respond;

    @SerializedName("report_id")
    @Expose
    @ColumnInfo(name = "report_id")
    public Long reportId;

    @SerializedName("discount")
    @Expose
    @ColumnInfo(name = "discount")
    public Integer discount;

    @SerializedName("discount_smeta")
    @Expose
    @ColumnInfo(name = "discount_smeta")
    public String discountSmeta;

    @SerializedName("vote_score")
    @Expose
    @ColumnInfo(name = "vote_score")
    public Integer voteScore;

    @SerializedName("voter_id")
    @Expose
    @ColumnInfo(name = "voter_id")
    public Integer voterId;

    @SerializedName("vinovnik_score")
    @Expose
    @ColumnInfo(name = "vinovnik_score")
    public Integer vinovnikScore;

    @SerializedName("vinovnik_score_user_id")
    @Expose
    @ColumnInfo(name = "vinovnik_score_user_id")
    public Integer vinovnikScoreUserId;

    @SerializedName("vinovnik_score_comment")
    @Expose
    @ColumnInfo(name = "vinovnik_score_comment")
    public String vinovnikScoreComment;

    @SerializedName("vinovnik_score_dt")
    @Expose
    @ColumnInfo(name = "vinovnik_score_dt")
    public Long vinovnikScoreDt;

    @SerializedName("theme_grp_id")
    @Expose
    @ColumnInfo(name = "theme_grp_id")
    public Integer themeGrpId;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Integer themeId;

    @SerializedName("sum_premiya")
    @Expose
    @ColumnInfo(name = "sum_premiya")
    public String sumPremiya;

    @SerializedName("sum_penalty")
    @Expose
    @ColumnInfo(name = "sum_penalty")
    public String sumPenalty;

    @SerializedName("duration")
    @Expose
    @ColumnInfo(name = "duration")
    public Integer duration;

    @SerializedName("ref_id")
    @Expose
    @ColumnInfo(name = "ref_id")
    public Integer refId;

    @SerializedName("summa_zp")
    @Expose
    @ColumnInfo(name = "summa_zp")
    public String summaZp;

    @SerializedName("budget")
    @Expose
    @ColumnInfo(name = "budget")
    public String budget;

    @SerializedName("complete")
    @Expose
    @ColumnInfo(name = "complete")
    public String complete;

    @SerializedName("sotr_opinion_id")
    @Expose
    @ColumnInfo(name = "sotr_opinion_id")
    public Integer sotrOpinionId;

    @SerializedName("sotr_opinion_author_id")
    @Expose
    @ColumnInfo(name = "sotr_opinion_author_id")
    public Integer sotrOpinionAuthorId;

    @SerializedName("sotr_opinion_dt")
    @Expose
    @ColumnInfo(name = "sotr_opinion_dt")
    public Long sotrOpinionDt;

    @SerializedName("no_need_reply")
    @Expose
    @ColumnInfo(name = "no_need_reply")
    public Integer noNeedReply;

    @SerializedName("audio_id")
    @Expose
    @ColumnInfo(name = "audio_id")
    public Integer audioId;

    @SerializedName("potential_client_id")
    @Expose
    @ColumnInfo(name = "potential_client_id")
    public Integer potentialClientId;


    @SerializedName("dt_start_plan")
    @Expose
    @ColumnInfo(name = "dt_start_plan")
    public Long dt_start_plan;//    dt_start_plan - плановое время начала (unixtime)

    @SerializedName("dt_end_plan")
    @Expose
    @ColumnInfo(name = "dt_end_plan")
    public Long dt_end_plan;//    dt_end_plan - плановое время окончания (unixtime)

    @SerializedName("dt_start_fact")
    @Expose
    @ColumnInfo(name = "dt_start_fact")
    public Long dt_start_fact;//    dt_start_fact - фактическое время начала (unixtime)

    @SerializedName("dt_end_fact")
    @Expose
    @ColumnInfo(name = "dt_end_fact")
    public Long dt_end_fact;//    dt_end_fact - фактическое время окончания (unixtime)

    /**
     * 0 - выгружен
     * 1 - надо выгрузить
     * NULL - получено с сервера
     * */
    @SerializedName("uploadStatus")
    @Expose
    @ColumnInfo(name = "uploadStatus")
    public Integer uploadStatus;

    // Доп поля для JOINов
//    @Ignore
    @ColumnInfo(name = "addr_nm")
    public String addrNm;

//    @Ignore
    @ColumnInfo(name = "client_nm")
    public String clientNm;

//    @Ignore
    @ColumnInfo(name = "sotr_nm")
    public String sortNm;

    @ColumnInfo(name = "coord_X")
    public String coordX;

    @ColumnInfo(name = "coord_Y")
    public String coordY;
}
