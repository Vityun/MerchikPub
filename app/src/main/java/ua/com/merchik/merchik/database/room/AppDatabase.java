package ua.com.merchik.merchik.database.room;

import android.annotation.SuppressLint;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsGroupsSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatGrpSDB;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatGrpTEMPSDB;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.data.Database.Room.CitySDB;
import ua.com.merchik.merchik.data.Database.Room.ContentSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.DateConverter;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.OblastSDB;
import ua.com.merchik.merchik.data.Database.Room.OborotVedSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionThemeSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammImagesSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;
import ua.com.merchik.merchik.data.Database.Room.PotentialClientSDB;
import ua.com.merchik.merchik.data.Database.Room.ReclamationPercentageSDB;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSLogSDB;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSPlanSDB;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.Database.Room.ShelfSizeSDB;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AchievementsDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AdditionalMaterialsAddressDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AdditionalMaterialsDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AdditionalMaterialsGroupsDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AddressDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ArticleDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ChatDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ChatGrpDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.CityDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ContentDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.CustomerDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.EKLDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.FragmentDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.LanguagesDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OblastDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OborotVedDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OpinionDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OpinionThemeDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.PlanogrammAddressDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.PlanogrammDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.PlanogrammGroupDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.PlanogrammImagesDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.PotentialClientDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ReclamationPercentageDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SMSLogDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SMSPlanDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SamplePhotoDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ShelfSizeDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ShowcaseDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SiteObjectsDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.StandartDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TarDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TovarGroupClientDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TovarGroupDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TranslatesDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.UsersDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.VideoViewDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.VotesDao;

@SuppressLint("RestrictedApi")
@Database(
        entities = {
                LanguagesSDB.class,     // Языки
                SiteObjectsSDB.class,   // Обьекты сайта
                TranslatesSDB.class,    // Переводы
                OpinionSDB.class,       // Мнения
                OpinionThemeSDB.class,  // Темы мнений
                OborotVedSDB.class,     // Оборотная ведомость
                AddressSDB.class,       // Адрес
                CustomerSDB.class,      // Клиенты
                UsersSDB.class,         // Сотрудники
                CitySDB.class,          // Города
                OblastSDB.class,         // Области
                EKL_SDB.class,           // ЭКЛ-ы (Электронный Контрольный Лист)
                TovarGroupSDB.class,     // Группы Товаров
                TovarGroupClientSDB.class,// Группы Товаров Клиентов
                ChatSDB.class,           // Чат
                StandartSDB.class,       // Стандарты
                ContentSDB.class,        // Контенты
                TasksAndReclamationsSDB.class,   // Задачи и Рекламации
                AdditionalMaterialsAddressSDB.class,
                AdditionalMaterialsGroupsSDB.class,
                AdditionalMaterialsSDB.class,
                PotentialClientSDB.class,       // Потенциальный клиент
                SamplePhotoSDB.class,            // Образцы Фото
                AchievementsSDB.class,   // Достижения
                VoteSDB.class,               // Оценки
                ArticleSDB.class,        // Артикула
                ChatGrpSDB.class,        // Группы чатов
                ChatGrpTEMPSDB.class,        // Временная таблица
                ReclamationPercentageSDB.class,  // Процент рекламаций
                ShelfSizeSDB.class,      // Доля полочного пространства
                FragmentSDB.class,           // Таблица Фрагментов (полей на фото)
                ViewListSDB.class,       // Знак видео просмотрено или нет
                ShowcaseSDB.class,       // Витрины, не путать с полками и тп
                PlanogrammSDB.class,      // Планограми
                PlanogrammAddressSDB.class, // Планограми Адреса
                PlanogrammGroupSDB.class,   // Планограми Группы
                PlanogrammImagesSDB.class,   // Планограми Идентификаторы
                SMSPlanSDB.class,       //  СМС А (тут типо должны быть те кто в очереди или недавно отправленные)
                SMSLogSDB.class         //  СМС Б
        },
        version = 52
)


@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LanguagesDao langListDao();

    public abstract SiteObjectsDao siteObjectsDao();

    public abstract TranslatesDao translatesDao();

    public abstract OpinionDao opinionDao();

    public abstract OpinionThemeDao opinionThemeDao();

    public abstract OborotVedDao oborotVedDao();    // Оборотная ведомость

    public abstract AddressDao addressDao();

    public abstract CustomerDao customerDao();

    public abstract UsersDao usersDao();

    public abstract CityDao cityDao();

    public abstract OblastDao oblastDao();

    public abstract EKLDao eklDao();

    public abstract TovarGroupDao tovarGroupDao();

    public abstract TovarGroupClientDao tovarGroupClientDao();

    public abstract ChatDao chatDao();

    public abstract StandartDao standartDao();

    public abstract ContentDao contentDao();

    public abstract TarDao tarDao();

    public abstract AdditionalMaterialsAddressDao additionalMaterialsAddressDao();

    public abstract AdditionalMaterialsGroupsDao additionalMaterialsGroupsDao();

    public abstract AdditionalMaterialsDao additionalMaterialsDao();

    public abstract PotentialClientDao potentialClientDao();

    public abstract SamplePhotoDao samplePhotoDao();

    public abstract AchievementsDao achievementsDao();

    public abstract VotesDao votesDao();

    public abstract ArticleDao articleDao();

    public abstract ChatGrpDao chatGrpDao();

    public abstract ReclamationPercentageDao reclamationPercentageDao();

    public abstract ShelfSizeDao shelfSizeDao();

    public abstract FragmentDao fragmentDao();

    public abstract VideoViewDao videoViewDao();

    public abstract ShowcaseDao showcaseDao();

    public abstract PlanogrammDao planogrammDao();

    public abstract PlanogrammAddressDao planogrammAddressDao();

    public abstract PlanogrammGroupDao planogrammGroupDao();

    public abstract PlanogrammImagesDao planogrammImagesDao();

    public abstract SMSPlanDao smsPlanDao();

    public abstract SMSLogDao smsLogDao();

    public class MyAutoMigration {
    }
}
