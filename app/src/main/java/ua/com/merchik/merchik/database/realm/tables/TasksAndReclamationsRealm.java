package ua.com.merchik.merchik.database.realm.tables;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.TasksAndReclamationsDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

/**
 * 15.03.2021
 * Класс для запросов в БД к табличке ЗИР (Задачи и Рекламации)
 * */
public class TasksAndReclamationsRealm {

    /**
     * 15.03.2021
     * Сохранение данных в таблицу ЗИР
     */
    public static void setTasksAndReclamations(List<TasksAndReclamationsDB> data) {
        try {
            if (data != null){
                INSTANCE.beginTransaction();
                INSTANCE.delete(TasksAndReclamationsDB.class);
                INSTANCE.copyToRealmOrUpdate(data);
                INSTANCE.commitTransaction();
            }else {
                // TODO Set to LOG info about error
            }
        }catch (Exception e){
            // TODO Set to LOG info about error
        }
    }

    public static void createNew(List<TasksAndReclamationsDB> data){
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealm(data);
        INSTANCE.commitTransaction();
    }


    public static List<TasksAndReclamationsDB> getTasks(){
        return INSTANCE.where(TasksAndReclamationsDB.class)
                .equalTo("tp", "1")
                .findAll();
    }


    public static List<TasksAndReclamationsDB> getReclamations(){
        return INSTANCE.where(TasksAndReclamationsDB.class)
                .equalTo("tp", "0")
                .findAll();
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
    public static String getStatusTxt(int tp, int status){
        try {
            if (tp == 0){
                switch (status){
                    case 0:
                        return "Активные";

                    case 1:
                        return "Исправленые";

                    case 2:
                        return "Отменённые";

                    case 3:
                        return "Истек срок исполнения";

                    default:
                        return "Не известный статус";
                }
            }

            if (tp == 1){   // ЗАДАЧА
                switch (status){
                    case 0:
                        return "Активные";

                    case 1:
                        return "Выполненные";

                    case 2:
                        return "Не выполненные";

                    case 3:
                        return "Отмененные";

                    default:
                        return "Не известный статус";
                }
            }
        }catch (Exception e){
            return "Не удалось определить. Возникла Ошибка. Обратитесь к Вашему администратору.";
        }
        return "Не удалось определить";
    }

    public static List<TaRStatus> getTaRStatus(int type){
        List<TaRStatus> resultTask = new ArrayList<>();
        if (type == 0){
            TaRStatus tars0 = new TaRStatus();
            tars0.id = 0;
            tars0.nm = "Активные";

            TaRStatus tars1 = new TaRStatus();
            tars1.id = 1;
            tars1.nm = "Исправленые";

            TaRStatus tars2 = new TaRStatus();
            tars2.id = 2;
            tars2.nm = "Отменённые";

            TaRStatus tars3 = new TaRStatus();
            tars3.id = 3;
            tars3.nm = "Истек срок исполнения";

            resultTask.add(tars0);
            resultTask.add(tars1);
            resultTask.add(tars2);
            resultTask.add(tars3);
        }else if (type == 1){
            TaRStatus tars0 = new TaRStatus();
            tars0.id = 0;
            tars0.nm = "Активные";

            TaRStatus tars1 = new TaRStatus();
            tars1.id = 1;
            tars1.nm = "Выполненные";

            TaRStatus tars2 = new TaRStatus();
            tars2.id = 2;
            tars2.nm = "Не выполненные";

            TaRStatus tars3 = new TaRStatus();
            tars3.id = 3;
            tars3.nm = "Отменённые";

            resultTask.add(tars0);
            resultTask.add(tars1);
            resultTask.add(tars2);
            resultTask.add(tars3);
        }

        return resultTask;
    }



    public static List<TasksAndReclamationsDB> getToUnload(){
        return INSTANCE.where(TasksAndReclamationsDB.class)
                .findAll();
    }


    public static RealmResults<TasksAndReclamationsDB> getTARByDad2(String dad2){
        return INSTANCE.where(TasksAndReclamationsDB.class)
                .equalTo("codeDad2", dad2)
                .sort("dt")
                .findAll();
    }


    public static class TaRStatus{
        public Integer id;
        public String nm;
        @Override
        public String toString() {
            return nm;
        }
    }


}
