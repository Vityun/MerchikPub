package ua.com.merchik.merchik.Filter;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.AutoTextUsersViewHolder;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDBDat.UserSDBJoin;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;

public class MyFilter {

    private Context mContext;
    private Globals.SourceAct sourceAct;

    private List<WpDataDB> wpList;
    private List<StackPhotoDB> stackPhotoList;
    private List<TovarDB> tovarList;

    public MyFilter() {
    }

    public MyFilter(Context mContext) {
        this.mContext = mContext;
    }

    public MyFilter(Context context, Globals.SourceAct source) {
        this.mContext = context;
        this.sourceAct = source;
    }


    /**
     * Получение отфильтрованных ответов
     * Их у меня должно быть на каждый случай что ли?
     * Ну для говнокода - изи, а как нормально?
     */
    // Для ПЛАНА РАБОТ
    public List<WpDataDB> getFilteredResultsWP(String constraint, List<WpDataDB> sorted, List<WpDataDB> orig) {

        constraint = constraint.toLowerCase();

        Log.e("getFilteredResultsWP", "constraint: " + constraint);
//        Log.e("getFilteredResultsWP", "sorted: " + sorted.size());
        Log.e("getFilteredResultsWP", "orig: " + orig.size());

        List<WpDataDB> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }


        for (WpDataDB item : sorted) {
            try {
                String themeId = String.valueOf(item.getTheme_id());

//                AtomicReference<ThemeDB> theme = new AtomicReference<>();
//                RealmManager.INSTANCE.executeTransactionAsync((realm) -> {
//                    theme.set(realm.copyFromRealm(ThemeRealm.getThemeById(themeId)));
//                });

                // Дата
                if (item.getDt() != null && !item.getDt().equals("") && Clock.getHumanTimeYYYYMMDD(item.getDt().getTime() / 1000).toLowerCase().contains(constraint)) {   //+TODO CHANGE DATE
                    results.add(item);
                }
                // Адрес
                else if (item.getAddr_txt() != null && !item.getAddr_txt().equals("") && item.getAddr_txt().toLowerCase().contains(constraint)) {
                    results.add(item);
                }
                // Клиент
                else if (item.getClient_txt() != null && !item.getClient_txt().equals("") && item.getClient_txt().toLowerCase().contains(constraint)) {
                    results.add(item);
                }
                // Пользователь
                else if (item.getUser_txt() != null && !item.getUser_txt().equals("") && item.getUser_txt().toLowerCase().contains(constraint)) {
                    results.add(item);
                }
                // Тема
//                else if (theme.get() != null && theme.get().getNm() != null && !theme.get().getNm().equals("") && theme.get().getNm().toLowerCase().contains(constraint)){
//                    results.add(item);
//                }
            } catch (Exception e) {
                Log.d("test", "test");
            }
        }
        return results;
    }


    public List<StackPhotoDB> getFilteredResultsSP(String constraint, List<StackPhotoDB> sorted, List<StackPhotoDB> orig) {

        Log.e("getFilteredResultsSP", "constraint: " + constraint);

        List<StackPhotoDB> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        for (StackPhotoDB item : sorted) {
            // Дата Надо подумать
//            if (item.getAddressTxt() != null && !item.getDt().equals("") && item.getDt().toLowerCase().contains(constraint)) {
//                results.add(item);
//            }

            // ID сайта
            if (item.getPhotoServerId() != null && !item.getPhotoServerId().equals("") && item.getPhotoServerId().toLowerCase().contains(constraint)) {
                results.add(item);
            }

            // Адрес
            if (item.getAddressTxt() != null && !item.getAddressTxt().equals("") && item.getAddressTxt().toLowerCase().contains(constraint)) {
                results.add(item);
            }
            // Клиент
            else if (item.getCustomerTxt() != null && !item.getCustomerTxt().equals("") && item.getCustomerTxt().toLowerCase().contains(constraint)) {
                results.add(item);
            }
            // Пользователь
            else if (item.getUserTxt() != null && !item.getUserTxt().equals("") && item.getUserTxt().toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }


        return results;
    }


    public List<TovarDB> getFilteredResultsTOV(String constraint, List<TovarDB> sorted, List<TovarDB> orig) {
        List<TovarDB> results = new ArrayList<>();

        try {
            Log.e("getFilteredResultsTOV", "constraint: " + constraint);
            try {
                Log.e("getFilteredResultsTOV", "sorted: " + sorted.size());
            } catch (Exception e) {
            }

            Log.e("getFilteredResultsTOV", "orig: " + orig.size());


            for (TovarDB item : orig) {
                ArticleSDB article = SQL_DB.articleDao().getByTovId(Integer.parseInt(item.getiD()));
                if (article != null) {
                    item.article = article.vendorCode;
                }
            }

            if (sorted == null) {
                sorted = orig;
            }

            Log.e("getFilteredResultsTOV", "orig: " + orig.size());

            for (TovarDB item : sorted) {
                //
                if (item.getNm() != null && !item.getNm().equals("") && item.getNm().toLowerCase().contains(constraint)) {
                    results.add(item);
                } else if (item.getWeight() != null && !item.getWeight().equals("") && item.getWeight().toLowerCase().contains(constraint)) {
                    results.add(item);
                } else if (item.getBarcode() != null && !item.getBarcode().equals("") && item.getBarcode().toLowerCase().contains(constraint)) {
                    results.add(item);
                } else if (item.article != null && (!String.valueOf(item.article).equals("") || !String.valueOf(item.article).equals("0")) && String.valueOf(item.article).toLowerCase().contains(constraint)) {
                    results.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getFilteredResultsTOV", "Exception e: " + e);
        }

        return results;
    }


    public List<TasksAndReclamationsSDB> getFilteredResultsTAR(String constraint, List<TasksAndReclamationsSDB> sorted, List<TasksAndReclamationsSDB> orig) {
        List<TasksAndReclamationsSDB> results = new ArrayList<>();
        try {
            if (sorted == null) {
                sorted = orig;
            }

            for (TasksAndReclamationsSDB item : sorted) {

                Log.d("test", "item: " + item);

                if (item.addrNm != null && !item.addrNm.equals("") && item.addrNm.toLowerCase().contains(constraint)) {
                    results.add(item);
                } else if (item.clientNm != null && !item.clientNm.equals("") && item.clientNm.toLowerCase().contains(constraint)) {
                    results.add(item);
                } else if (item.sortNm != null && !item.sortNm.equals("") && item.sortNm.toLowerCase().contains(constraint)) {
                    results.add(item);
                }
            }

            Log.e("getFilteredResultsTAR", "end: " + results.size());

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "getFilteredResultsTAR", "Exception e: " + e);
        }

        return results;
    }


    /***/
    public List<AddressDB> getAddressFilterable(String constraint, List<AddressDB> sorted, List<AddressDB> orig) {

        List<AddressDB> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        for (AddressDB item : sorted) {
            if (item.getNm() != null && !item.getNm().equals("") && item.getNm().toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }

        return results;
    }

    /***/
    public <T> List<T> getUserSdbFilterable(AutoTextUsersViewHolder.AutoTextUserEnum type, String constraint, List<T> sorted, List<T> orig) {

        List<T> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        switch (type) {

            case DEPARTMENT:
                for (UserSDBJoin item : (List<UserSDBJoin>) sorted) {
                    if (item.fio != null && !item.fio.equals("") && item.fio.toLowerCase().contains(constraint)) {
                        results.add((T) item);
                    } else if (item.nm != null && !item.nm.equals("") && item.nm.toLowerCase().contains(constraint)) {
                        results.add((T) item);
                    }
                }
                break;


            case DEFAULT:

            default:
                //1
                for (UsersSDB item : (List<UsersSDB>) sorted) {
                    if (item.fio != null && !item.fio.equals("") && item.fio.toLowerCase().contains(constraint)) {
                        results.add((T) item);
                    }
                }
                break;
        }


        return results;
    }


    /**
     * Попытка сделать универсальный поиск в зависимости от генерика который я передаю
     */
    public <T> List<T> getFilterableDataSDB(String constraint, List<T> sorted, List<T> orig) {
        List<T> res = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        if (sorted != null && sorted.size() > 0) {
            if (sorted.get(0) instanceof AddressSDB) {
                res = filerAddressSDB(constraint, (List<AddressSDB>) sorted);
            } else if (sorted.get(0) instanceof UsersSDB) {
                res = filerUserSDB(constraint, (List<UsersSDB>) sorted);
            } else if (sorted.get(0) instanceof CustomerSDB) {
                res = filerCustomerSDB(constraint, (List<CustomerSDB>) sorted);
            } else if (sorted.get(0) instanceof ThemeDB) {
                res = filerThemeDB(constraint, (List<ThemeDB>) sorted);
            } else {
                return sorted;
            }
        } else {
            return sorted;
        }


        return res;
    }

    private <T> List<T> filerAddressSDB(String constraint, List<AddressSDB> sorted) {
        List<T> res = new ArrayList<>();
        for (AddressSDB item : sorted) {
            if (item.nm != null && !item.nm.equals("") && item.nm.toLowerCase().contains(constraint)) {
                res.add((T) item);
            }
        }

        return res;
    }

    private <T> List<T> filerUserSDB(String constraint, List<UsersSDB> sorted) {
        List<T> res = new ArrayList<>();
        for (UsersSDB item : sorted) {
            if (item.fio != null && !item.fio.equals("") && item.fio.toLowerCase().contains(constraint)) {
                res.add((T) item);
            }
        }

        return res;
    }

    private <T> List<T> filerCustomerSDB(String constraint, List<CustomerSDB> sorted) {
        List<T> res = new ArrayList<>();
        for (CustomerSDB item : sorted) {
            if (item.nm != null && !item.nm.equals("") && item.nm.toLowerCase().contains(constraint)) {
                res.add((T) item);
            }
        }

        return res;
    }

    private <T> List<T> filerThemeDB(String constraint, List<ThemeDB> sorted) {
        List<T> res = new ArrayList<>();
        for (ThemeDB item : sorted) {
            if (item.getNm() != null && !item.getNm().equals("") && item.getNm().toLowerCase().contains(constraint)) {
                res.add((T) item);
            }
        }

        return res;
    }


    /***/
    public List<CustomerDB> getCustomerFilterable(String constraint, List<CustomerDB> sorted, List<CustomerDB> orig) {

        List<CustomerDB> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        for (CustomerDB item : sorted) {
            if (item.getNm() != null && !item.getNm().equals("") && item.getNm().toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }

        return results;
    }


    /***/
    public List<ThemeDB> getThemeFilterable(String constraint, List<ThemeDB> sorted, List<ThemeDB> orig) {

        List<ThemeDB> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        for (ThemeDB item : sorted) {
            if (item.getNm() != null && !item.getNm().equals("") && item.getNm().toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }

        return results;
    }


    /***/
    public List<TasksAndReclamationsRealm.TaRStatus> getStatusFilterable(String constraint, List<TasksAndReclamationsRealm.TaRStatus> sorted, List<TasksAndReclamationsRealm.TaRStatus> orig) {

        List<TasksAndReclamationsRealm.TaRStatus> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        for (TasksAndReclamationsRealm.TaRStatus item : sorted) {
            if (item.nm != null && !item.nm.equals("") && item.nm.toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }

        return results;
    }


    /***/
    public List<OpinionSDB> getOpinionsFilterable(String constraint, List<OpinionSDB> sorted, List<OpinionSDB> orig) {

        List<OpinionSDB> results = new ArrayList<>();
        if (sorted == null) {
            sorted = orig;
        }

        for (OpinionSDB item : sorted) {
            if (item.nm != null && !item.nm.equals("") && item.nm.toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }

        return results;
    }


    /**
     * Получение отфильтрованных ответов
     * Их у меня должно быть на каждый случай что ли?
     * Ну для говнокода - изи, а как нормально?
     */
    // Для ВИТРИН
    public List<ShowcaseSDB> getFilteredResultsShowcaseSDB(String constraint, List<ShowcaseSDB> sorted, List<ShowcaseSDB> orig) {

        constraint = constraint.toLowerCase();

        List<ShowcaseSDB> results = new ArrayList<>();
        if (sorted == null || constraint.equals("")) {
            sorted = orig;
        }

        for (ShowcaseSDB item : sorted) {
            try {
                // Название
                if (item.nm != null && !item.nm.equals("") && item.nm.toLowerCase().contains(constraint)) {
                    results.add(item);
                } else if (item.tovarGrpTxt != null && !item.tovarGrpTxt.equals("") && item.tovarGrpTxt.toLowerCase().contains(constraint)) {
                    results.add(item);
                }

            } catch (Exception e) {
                Log.e("FilterShowcase", "Exception e: " + e);
            }
        }
        return results;
    }
}
