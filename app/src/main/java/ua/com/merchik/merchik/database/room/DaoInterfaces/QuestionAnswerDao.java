package ua.com.merchik.merchik.database.room.DaoInterfaces;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ua.com.merchik.merchik.data.QuestionAnswerDB;

@Dao
public abstract class QuestionAnswerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<QuestionAnswerDB> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(QuestionAnswerDB item);

    @Query("SELECT * FROM question_answers ORDER BY dt DESC")
    public abstract List<QuestionAnswerDB> getAll();

    @Query("SELECT * FROM question_answers WHERE ID = :id LIMIT 1")
    public abstract QuestionAnswerDB getById(long id);

    @Query("SELECT * FROM question_answers WHERE user_id = :userId ORDER BY dt DESC")
    public abstract List<QuestionAnswerDB> getByUserId(long userId);

    @Query("SELECT * FROM question_answers WHERE id_quest = :questId ORDER BY dt DESC")
    public abstract List<QuestionAnswerDB> getByQuestId(int questId);

    @Query("DELETE FROM question_answers")
    public abstract void clear();

    @Transaction
    public void replaceAll(List<QuestionAnswerDB> list) {
        clear();

        if (list != null && !list.isEmpty()) {
            insertAll(list);
        }
    }
}