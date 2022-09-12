package ua.com.merchik.merchik.database.room;

import android.annotation.SuppressLint;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.ChatSDB;
import ua.com.merchik.merchik.data.Database.Room.CitySDB;
import ua.com.merchik.merchik.data.Database.Room.ContentSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.DateConverter;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.OblastSDB;
import ua.com.merchik.merchik.data.Database.Room.OborotVedSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionThemeSDB;
import ua.com.merchik.merchik.data.Database.Room.PotentialClientSDB;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AdditionalMaterialsAddressDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AdditionalMaterialsDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.AddressDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ChatDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.CityDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ContentDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.CustomerDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.EKLDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.LanguagesDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OblastDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OborotVedDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OpinionDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.OpinionThemeDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.PotentialClientDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SamplePhotoDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SiteObjectsDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.StandartDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TarDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TovarGroupClientDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TovarGroupDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TranslatesDao;
import ua.com.merchik.merchik.database.room.DaoInterfaces.UsersDao;

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
                AdditionalMaterialsSDB.class,
                PotentialClientSDB.class,       // Потенциальный клиент
                SamplePhotoSDB.class            // Образцы Фото
        },
        version = 21
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

    public abstract AdditionalMaterialsDao additionalMaterialsDao();

    public abstract PotentialClientDao potentialClientDao();

    public abstract SamplePhotoDao samplePhotoDao();

    public class MyAutoMigration {
    }
}
