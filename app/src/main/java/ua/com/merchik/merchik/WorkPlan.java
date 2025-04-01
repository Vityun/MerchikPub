package ua.com.merchik.merchik;


import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionsButtons;
import ua.com.merchik.merchik.data.RealmModels.GroupTypeDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class WorkPlan {

    /**
     * ПОЛУЧИТЬ ОПЦИИ ОБЫЧНЫЕ
     */
    public LinearLayout getOptionLinearLayout(Context mContext, long otchetId) {
//        RealmResults<OptionsDB> options = RealmManager.getOptionsButtonRED(otchetId);
        List<OptionsDB> arraylist = RealmManager.getOptionsButtonRED2(otchetId);

        // Отображаю "Описанные" на стороне приложения опции
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            IntStream intStream = Arrays.stream(describedOptionsButt);
//            String[] answer = intStream.sorted().mapToObj(String::valueOf).toArray(String[]::new);
//
//            options = options.where()
//                    .in("optionId", answer)
//                    .sort("so")
//                    .findAll();
//        }

//        List<OptionsDB> arraylist = RealmManager.INSTANCE.copyFromRealm(options);
        Log.e("arraylist", "" + arraylist); // Проверяем, что объекты скопированы правильно

//        List<OptionsDB> test = new ArrayList<>();
//        if (options != null && options.size() > 0){
//            test = RealmManager.INSTANCE.copyFromRealm(options);
//        }
//        Log.e("test", "" + test);

//        Collections.sort(arraylist, (o1, o2) -> o1.getSo().compareTo(o2.getSo()));

        LinearLayout ll = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(1, 1, 1, 1);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setId((int) otchetId);
        ll.setScrollContainer(true);

        if (arraylist == null) return ll;

        for (int i = 0; i < arraylist.size(); i++) {
            long linearId = Long.parseLong(arraylist.get(i).getID());

            ImageView imgOpt = new ImageView(mContext);// Устанавливаем контекст для Опций
            imgOpt.setImageResource(R.drawable.option_signal_null); // Устанавливаем Сигнал опции
            if (arraylist.get(i).getIsSignal().equals("1")) {
                imgOpt.setImageResource(R.drawable.option_signal_bad); // Устанавливаем Сигнал опции
            } else if (arraylist.get(i).getIsSignal().equals("2")) {
                imgOpt.setImageResource(R.drawable.option_signal_ok); // Устанавливаем Сигнал опции
            }
            imgOpt.setId((int) linearId);
            imgOpt.setLayoutParams(layoutParams);

            // Расчёт размеров картинок Сигналов опций
            final float scale = mContext.getResources().getDisplayMetrics().density;
            int pixelsH = (int) (14 * scale + 0.5f);
            int pixelsW = (int) (7 * scale + 0.5f);

            // Установка размеров
            imgOpt.getLayoutParams().height = pixelsH;
            imgOpt.getLayoutParams().width = pixelsW;

            ll.addView(imgOpt);
        }
        return ll;
    }

    /**
     * ПОЛУЧИТЬ ОПЦИИ КНОПКИ
     *
     * @return
     */
    public ArrayList<OptionsButtons> getOptionButtons(long otchetId, int wpId) {
        RealmResults<OptionsDB> options = RealmManager.getOptionsButton(otchetId);
        ArrayList<OptionsButtons> arraylist = new ArrayList<>();

        for (int i = 0; i < options.size(); i++) {
            long id = Long.parseLong(options.get(i).getID());
            int optionId = Integer.parseInt(options.get(i).getOptionId());
            String optionButtonOptionTxt = options.get(i).getOptionTxt();
            String optionButtonIsSignal = options.get(i).getIsSignal();

            OptionsButtons optionsButtons = new OptionsButtons(id, optionId, wpId, optionButtonOptionTxt, optionButtonIsSignal);
            arraylist.add(optionsButtons);
        }

        return arraylist;
    }

    //
    public List<OptionsDB> getOptionButtons2(long otchetId, long wpId) {
        RealmResults<OptionsDB> options = RealmManager.getOptionsButton(otchetId);
        List<OptionsDB> arraylist;
        arraylist = RealmManager.INSTANCE.copyFromRealm(options);
        return arraylist;
    }


    public List<OptionsDB> getAllOtchetOptions(long otchetId, String codeDad2) {
        RealmResults<OptionsDB> options = RealmManager.getOptionsByOtchetId(otchetId, codeDad2);
        List<OptionsDB> arraylist = new ArrayList<>();
        arraylist = options;
        return arraylist;
    }


    /**
     * Получить данные о КПСе
     */
    public WPDataObj getKPS(long wpId) {
        WpDataDB wpRow = RealmManager.INSTANCE.copyFromRealm(RealmManager.getWorkPlanRowById(wpId));

//        long id = wpRow.getId();
        String date = Clock.getHumanTimeYYYYMMDD(wpRow.getDt().getTime() / 1000);
        String customer_id = wpRow.getClient_id();
        int address_id = wpRow.getAddr_id();
        String photo_type = "0";
        Map<Integer, String> customerTypeGrp = getCustomerGroups(customer_id);
        String doc_num = wpRow.getDoc_num();
        int theme_id = wpRow.getTheme_id();
        String photo_user_id = String.valueOf(wpRow.getUser_id());
        long dad2 = wpRow.getCode_dad2();
        String customer_txt = wpRow.getClient_txt();
        String address_txt = wpRow.getAddr_txt();

        Float lat = null, lon = null;
        if (wpRow.getAddr_location_xd() != null && !wpRow.getAddr_location_xd().equals("")) {
            lat = Float.valueOf(wpRow.getAddr_location_xd());
            lon = Float.valueOf(wpRow.getAddr_location_yd());
        }
        return new WPDataObj(wpId, date, customer_id, address_id, photo_type, customerTypeGrp, doc_num, theme_id, photo_user_id, dad2, customer_txt, address_txt, lat, lon);
    }

    public WPDataObj getKPS(TasksAndReclamationsSDB task) {
        WPDataObj result = new WPDataObj();

        result.setDate(String.valueOf(task.dt));
        result.setCustomerId(task.client);
        result.setAddressId(task.addr);
        result.setPhotoType("0");
        result.setCustomerTypeGrp(getCustomerGroups(task.client));
        result.setDocNum("");   // Не нужное, скорее всего
        result.setThemeId(task.themeId);
        result.setPhotoUserId("");  // !!!
        result.setDad2(task.codeDad2);
        result.setLatitude((float) SQL_DB.addressDao().getById(task.addr).locationXd);
        result.setLongitude((float) SQL_DB.addressDao().getById(task.addr).locationYd);

        return result;
    }

    public Map<Integer, String> getCustomerGroups(String customer_id) {
        Map<Integer, String> mapCustomerType = new HashMap<>();
        Log.e("TAG_TEST_GRP", "CUSTOMER_ID: " + customer_id);
        RealmResults<GroupTypeDB> realmResults = RealmManager.getAllGroupTypeByCustomerId(customer_id);
        Log.e("TAG_TEST_GRP", "realm result: " + realmResults);
        for (int i = 0; i < realmResults.size(); i++) {
            mapCustomerType.put(realmResults.get(i).getID(), realmResults.get(i).getNm());
        }
        return mapCustomerType;
    }


    /**
     * Возвращает id-шник отчёта. Зависит от action.
     */
    public long getWpOpchetId(WpDataDB wpDataDB) {
        try {
            int action = wpDataDB.getAction();
            if (action == 1 || action == 94) {
                return wpDataDB.getDoc_num_otchet_id();
            } else {
                return wpDataDB.getDoc_num_1c_id();
            }
        } catch (Exception e) {
            Globals.addLog();// Запись в таблицу Лога
            return 0;
        }
    }


}


