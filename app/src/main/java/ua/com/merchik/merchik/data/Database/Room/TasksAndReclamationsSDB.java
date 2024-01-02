package ua.com.merchik.merchik.data.Database.Room;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "tasks_and_reclamations")
public class TasksAndReclamationsSDB implements Parcelable {

    public TasksAndReclamationsSDB() {

    }

    /**
     * 17.03.2021
     * В зависимости от того Задача это или Рекламация - у них разные текстовые состояния.
     *
     * поле state:
     * рекламации: (tp = 0)
     * 0 - Активные                 (! Красная)
     * 1 - Исправленые              (Галочка зелёная )
     * 2 - Отменённые               (крестик серая)
     * 3 - Истек срок исполнения    (! серый)
     * задачи: (tp = 1)
     * 0 - Активные         (! Красная)
     * 1 - Выполненные      (Галочка зелёная )
     * 2 - Не выполненные   (! серый)
     * 3 - Отмененные       (крестик серая)
     * */

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

    @ColumnInfo(name = "photoHash")
    public String photoHash;

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
    public Long audioId;

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

    public TasksAndReclamationsSDB(Parcel in) {
        id = in.readInt();
        tp = in.readInt();
        dt = in.readLong();
        dtRealPost = in.readLong();
        dtChange = in.readLong();
        author = in.readInt();
        addr = in.readInt();
        client = in.readString();
        state = in.readInt();
        photo = in.readInt();
        photo2 = in.readInt();
        photoHash = in.readString();
        comment = in.readString();
        vinovnik = in.readInt();
        vinovnik2 = in.readInt();
        vinovnikReadDt = in.readLong();
        zamenaUserId = in.readInt();
        zamenaDt = in.readLong();
        zamenaWho = in.readInt();
        contacterId = in.readInt();
        superId = in.readInt();
        territorialId = in.readInt();
        regionalId = in.readInt();
        nopId = in.readInt();
        dvi = in.readInt();
        zakazchik = in.readInt();
        id1c = in.readString();
        docNum1cId = in.readLong();
        codeDad2 = in.readLong();
        codeDad2SrcDoc = in.readLong();
        telNum = in.readString();
        lastAnswer = in.readString();
        lastAnswerUserId = in.readInt();
        lastAnswerDtChange = in.readString();
        respond = in.readInt();
        reportId = in.readLong();
        discount = in.readInt();
        discountSmeta = in.readString();
        voteScore = in.readInt();
        voterId = in.readInt();
        vinovnikScore = in.readInt();
        vinovnikScoreUserId = in.readInt();
        vinovnikScoreComment = in.readString();
        vinovnikScoreDt = in.readLong();
        themeGrpId = in.readInt();
        themeId = in.readInt();
        sumPremiya = in.readString();
        sumPenalty = in.readString();
        duration = in.readInt();
        refId = in.readInt();
        summaZp = in.readString();
        budget = in.readString();
        complete = in.readString();
        sotrOpinionId = in.readInt();
        sotrOpinionAuthorId = in.readInt();
        sotrOpinionDt = in.readLong();
        noNeedReply = in.readInt();
        audioId = in.readLong();
        potentialClientId = in.readInt();
        dt_start_plan = in.readLong();
        dt_end_plan = in.readLong();
        dt_start_fact = in.readLong();
        dt_end_fact = in.readLong();
        uploadStatus = in.readInt();
        addrNm = in.readString();
        clientNm = in.readString();
        sortNm = in.readString();
        coordX = in.readString();
        coordY = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(tp);
        dest.writeLong(dt);
        dest.writeLong(dtRealPost);
        dest.writeLong(dtChange);
        dest.writeInt(author);
        dest.writeInt(addr);
        dest.writeString(client);
        dest.writeInt(state);
        dest.writeInt(photo);
        dest.writeInt(photo2);
        dest.writeString(photoHash);
        dest.writeString(comment);
        dest.writeInt(vinovnik);
        dest.writeInt(vinovnik2);
        dest.writeLong(vinovnikReadDt);
        dest.writeInt(zamenaUserId);
        dest.writeLong(zamenaDt);
        dest.writeInt(zamenaWho);
        dest.writeInt(contacterId);
        dest.writeInt(superId);
        dest.writeInt(territorialId);
        dest.writeInt(regionalId);
        dest.writeInt(nopId);
        dest.writeInt(dvi);
        dest.writeInt(zakazchik);
        dest.writeString(id1c);
        dest.writeLong(docNum1cId != null ? docNum1cId : 0);
        dest.writeLong(codeDad2);
        dest.writeLong(codeDad2SrcDoc);
        dest.writeString(telNum);
        dest.writeString(lastAnswer);
        dest.writeInt(lastAnswerUserId);
        dest.writeString(lastAnswerDtChange);
        dest.writeInt(respond);
        dest.writeLong(reportId);
        dest.writeInt(discount);
        dest.writeString(discountSmeta);
        dest.writeInt(voteScore);
        dest.writeInt(voterId);
        dest.writeInt(vinovnikScore);
        dest.writeInt(vinovnikScoreUserId);
        dest.writeString(vinovnikScoreComment);
        dest.writeLong(vinovnikScoreDt);
        dest.writeInt(themeGrpId);
        dest.writeInt(themeId);
        dest.writeString(sumPremiya);
        dest.writeString(sumPenalty);
        dest.writeInt(duration);
        dest.writeInt(refId);
        dest.writeString(summaZp);
        dest.writeString(budget);
        dest.writeString(complete);
        dest.writeInt(sotrOpinionId);
        dest.writeInt(sotrOpinionAuthorId);
        dest.writeLong(sotrOpinionDt);
        dest.writeInt(noNeedReply);
        dest.writeLong(audioId);
        dest.writeInt(potentialClientId);
        dest.writeLong(dt_start_plan);
        dest.writeLong(dt_end_plan);
        dest.writeLong(dt_start_fact);
        dest.writeLong(dt_end_fact);
        dest.writeInt(uploadStatus != null ? uploadStatus : 0);//dest.writeInt(uploadStatus);
        dest.writeString(addrNm);
        dest.writeString(clientNm);
        dest.writeString(sortNm);
        dest.writeString(coordX);
        dest.writeString(coordY);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TasksAndReclamationsSDB> CREATOR = new Parcelable.Creator<TasksAndReclamationsSDB>() {
        @Override
        public TasksAndReclamationsSDB createFromParcel(Parcel in) {
            return new TasksAndReclamationsSDB(in);
        }

        @Override
        public TasksAndReclamationsSDB[] newArray(int size) {
            return new TasksAndReclamationsSDB[size];
        }
    };


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TasksAndReclamationsSDB)) {
            return false;
        }
        TasksAndReclamationsSDB other = (TasksAndReclamationsSDB) obj;

        // Проверка на null
        if (this.id == null || other.id == null) {
            return false;
        }

        return this.id.equals(other.id);
    }


}
