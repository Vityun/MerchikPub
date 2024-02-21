package ua.com.merchik.merchik.Utils.UniversalAdapter;

import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;
import static ua.com.merchik.merchik.toolbar_menus.internetStatus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Collections;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class AdapterUtil extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private UniversalAdapterData data;
    private Globals.ReferencesEnum referencesEnum;

    public AdapterUtil(Context mContext, UniversalAdapterData data, Globals.ReferencesEnum referencesEnum) {
        this.mContext = mContext;
        this.data = data;
        this.referencesEnum = referencesEnum;

        Log.e("AdapterUtil", "ENTER referencesEnum: " + referencesEnum);

//        int addrSize = data.addressDBList != null ? data.addressDBList.size() : 0;
//        int custSize = data.customerDBList != null ? data.customerDBList.size() : 0;
//        int userSize = data.usersDBList != null ? data.usersDBList.size() : 0;

        int test = 0;

        int addrSize = data.address != null ? data.address.size() : 0;
        int custSize = data.customers != null ? data.customers.size() : 0;
        int userSize = data.users != null ? test : 0;

        Log.e("AdapterUtil", "ENTER data АДРЕСА SIZE: " + addrSize);
        Log.e("AdapterUtil", "ENTER data КЛИЕНТЫ SIZE: " + custSize);
        Log.e("AdapterUtil", "ENTER data СОТРУДНИКИ SIZE: " + userSize);
    }

    public void refresh(UniversalAdapterData newData) {
        data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (referencesEnum) {
            case ACHIEVEMENTS:
                return new DefaultViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_default_achievements, parent, false));
            default:
                return new DefaultViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_default, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DefaultViewHolder viewHolder = (DefaultViewHolder) holder;

        switch (referencesEnum) {
            case ADDRESS:
//                viewHolder.bind(data.addressDBList.get(position), referencesEnum);
                viewHolder.bind(data.address.get(position), referencesEnum);
                break;

            case CUSTOMER:
//                viewHolder.bind(data.customerDBList.get(position), referencesEnum);
                viewHolder.bind(data.customers.get(position), referencesEnum);
                break;

            case USERS:
//                viewHolder.bind(data.usersDBList.get(position), referencesEnum);
                viewHolder.bind(data.users.get(position), referencesEnum);
                break;

            case CHAT:
                viewHolder.bind(data.chats.get(position), referencesEnum);
                break;

            case ACHIEVEMENTS:
                viewHolder.bind(data.achievementsSDBS.get(position), referencesEnum);
                break;
        }

    }

    @Override
    public int getItemCount() {
        try {
            switch (referencesEnum) {
                case ADDRESS:
//                    return data.addressDBList.size();
                    return data.address.size();

                case CUSTOMER:
//                    return data.customerDBList.size();
                    return data.customers.size();

                case USERS:
//                    return data.usersDBList.size();
                    return data.users.size();

                case CHAT:
                    return data.chats.size();

                case ACHIEVEMENTS:
                    return data.achievementsSDBS.size();

                default:
                    return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }


    // ========== VIEW HOLDERs ==========

    public class DefaultViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout layout;
        ImageView image, image2;
        TextView title1, title2, title3, title4, title5;
        TextView text1, text2, text3, text4, text5;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout);

            image = itemView.findViewById(R.id.image);
            image2 = itemView.findViewById(R.id.image2);

            title1 = itemView.findViewById(R.id.title1);
            title2 = itemView.findViewById(R.id.title2);
            title3 = itemView.findViewById(R.id.title3);
            title4 = itemView.findViewById(R.id.title4);
            title5 = itemView.findViewById(R.id.title5);

            text1 = itemView.findViewById(R.id.text1);
            text2 = itemView.findViewById(R.id.text2);
            text3 = itemView.findViewById(R.id.text3);
            text4 = itemView.findViewById(R.id.text4);
            text5 = itemView.findViewById(R.id.text5);
        }

        public <T> void bind(T data, Globals.ReferencesEnum referencesEnum) {
            Log.e("AdapterUtil", "data: " + data);


            switch (referencesEnum) {
                case ADDRESS:
                    bindAddress((AddressSDB) data);
                    break;

                case USERS:
                    bindUsers((UsersSDB) data);
                    break;

                case CUSTOMER:
                    bindCustomers((CustomerSDB) data);
                    break;

                case CHAT:
                    bindChats((ChatSDB) data);
                    break;

                case ACHIEVEMENTS:
                    bindACHIEVEMENTS((AchievementsSDB) data);
                    break;
            }
        }

        private void bindCustomers(CustomerSDB data) {
            title1.setText("ID:");
            title2.setText("Название:");
            title3.setText("ЕДРПОУ:");
            title4.setText("Автор:");
            title5.setText("ВПИ:");

            text1.setText("" + data.id);
            text2.setText("" + data.nm);
            text3.setText("" + data.edrpou);
            text4.setText("" + "НЕТ");
            text5.setText("" + data.dtUpdate);
        }

        private void bindUsers(UsersSDB data) {
            title1.setText("ID:");
            title2.setText("Имя:");
            title3.setText("Город:");
            title4.setText("Место работы:");
            title5.setText("Автор:");

            text1.setText("" + data.id);
            text2.setText("" + data.fio);
            text3.setText("" + data.cityId);
            text4.setText("" + data.workAddrId);
            text5.setText("" + data.authorId);

            Log.e("AdapterUtilStack", "stackPhotoDBAll: " + StackPhotoRealm.getAll().size());

            // Если в Базе данных такой фотки нет - я постараюсь её загрузить
            if (data.img_personal_photo_stackId != null) {
                Log.e("AdapterUtil", "(" + data.id + ")Фото есть: " + data.img_personal_photo_stackId);

                StackPhotoDB photo = StackPhotoRealm.getById(data.img_personal_photo_stackId);

                try {
                    File file = new File(photo.getPhoto_num());
                    Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);

                    Log.e("AdapterUtil", "(" + data.id + ")Фото есть, размещаю TEST: " + b);

                    if (b != null) {
                        Log.e("AdapterUtil", "(" + data.id + ")Фото есть, размещаю: " + b.toString().length());
                        image.setImageBitmap(b);
                    }
                } catch (Exception e) {
                    Log.e("AdapterUtil", "(" + data.id + ")Фото есть, ошибка: " + e);
                    image.setImageResource(R.mipmap.merchik);
                }
            } else {
                Log.e("AdapterUtil", "(" + data.id + ") надо качать?");
                image.setImageResource(R.mipmap.merchik);
                Log.e("AdapterUtil", "(" + data.id + ")скачиваю");
                PhotoDownload.downloadPhoto(data.img_personal_photo_thumb, new ExchangeInterface.ExchangePhoto() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {

                        Log.e("AdapterUtil", "(" + data.id + ")Скачал: " + bitmap.toString().length());

                        // Отображаю фото
                        image.setImageBitmap(bitmap);

                        // Сохраняю фото в памяти и получаю место хранения
                        String path = Globals.savePhotoToPhoneMemory("/Sotr", "Sotr" + data.id, bitmap);

                        // создаю в БД запись этой фотки
                        int newId = RealmManager.stackPhotoGetLastId() + 1;

                        StackPhotoDB stack = new StackPhotoDB();
                        stack.setId(newId);
                        stack.setUser_id(data.id);
                        stack.setCreate_time(System.currentTimeMillis());
                        stack.setUpload_to_server(System.currentTimeMillis());
                        stack.setGet_on_server(System.currentTimeMillis());
                        stack.setPhoto_num(path);
                        stack.setPhoto_type(29);
                        stack.setPhoto_size("small");
                        stack.setUserTxt(data.fio);
                        stack.setPhotoServerURL(data.img_personal_photo_thumb);


                        RealmManager.stackPhotoSavePhoto(stack);

                        // Сохраняю фотку в СОТР
                        data.img_personal_photo_stackId = stack.getId();
                        SQL_DB.usersDao().insertAll(Collections.singletonList(data));

                        Log.e("AdapterUtilStack", "stackPhotoDBAll_2: " + StackPhotoRealm.getAll().size());
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("AdapterUtil", "(" + data.id + ")скачиваю, есть ошибка: " + error);

                        // Сохраняю фотку в СОТР
                        data.img_personal_photo_stackId = 0;
                        SQL_DB.usersDao().insertAll(Collections.singletonList(data));

                        image.setImageResource(R.mipmap.merchik);
                    }
                });
            }

            layout.setOnClickListener(view -> {
                openSotrInfoDialog(data);
            });


        }

        private void bindAddress(AddressSDB data) {
            title1.setText("ID:");
            title2.setText("Адрес:");
            title3.setText("Город:");
            title4.setText("Область:");
            title5.setText("Сеть:");

//            text1.setText("" + data.getAddrId());
//            text2.setText("" + data.getNm());
//            text3.setText("" + data.getCityId());
//            text4.setText("" + data.getOblId());
//            text5.setText("" + data.getTpId());

            text1.setText("" + data.id);
            text2.setText("" + data.nm);
            text3.setText("" + data.cityId);
            text4.setText("" + data.oblId);
            text5.setText("" + data.tpId);
        }


        private void bindChats(ChatSDB data) {
            title1.setText("ID:");
            title2.setText("Сообщение:");
            title3.setText("Создал:");
            title4.setText("Адресат:");
            title5.setText("Код Чата:");

            String autor = "Не определено";
            String test = SQL_DB.usersDao().getUserName(data.userId);
            String destination = SQL_DB.usersDao().getUserName(data.userIdTo);
            if (test != null && !test.equals("")) {
                autor = test;
            }

            text1.setText("" + data.id);
            text2.setText("" + data.msg);
            text3.setText("" + autor);
            text4.setText("" + destination);
            text5.setText("" + data.chatId);

            if (data.dtRead == 0) {
                image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_email));
                image.setColorFilter(mContext.getResources().getColor(R.color.black));
            } else {
                image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_email_open));
                image.setColorFilter(mContext.getResources().getColor(R.color.shadow));
            }


            StringBuilder msg = new StringBuilder();
            msg.append("Создал: ").append(autor).append("\n");
            msg.append("Адресат: ").append(destination).append("\n");
            msg.append("Код Чата: ").append(data.chatId).append("\n");
            msg.append("Сообщение: ").append(data.msg).append("\n");

            layout.setOnClickListener((v) -> {
                try {
                    DialogData dialog = new DialogData(mContext);
                    dialog.setTitle("Сообщение " + data.id);
                    dialog.setText(msg);
                    dialog.setClose(dialog::dismiss);
                    dialog.show();

                    data.dtRead = System.currentTimeMillis() / 1000;
                    SQL_DB.chatDao().insertData(Collections.singletonList(data))
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_email_open));
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_email));
                                }
                            });


                    StandartData.StandartDataChat dataChat = new StandartData.StandartDataChat();
                    dataChat.element_id = data.id;
                    dataChat.msg_id = data.id;

                    if (internetStatus != 1) {

                    } else {
                        Exchange.chatMarkRead(dataChat, new ExchangeInterface.ExchangeResponseInterfaceSingle() {
                            @Override
                            public <T> void onSuccess(T data) {
                                Toast.makeText(mContext, "Сообщение прочитано", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String error) {

                            }
                        });
                    }

                    notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(layout.getContext(), "При чтении сообщения произошла ошибка. Обратитесь к своему руководителю за помощью.", Toast.LENGTH_LONG).show();
                    Globals.writeToMLOG("ERROR", "AdapterUtil/bindChats", "Exception e: " + e);
                }

            });
        }

        private void bindACHIEVEMENTS(AchievementsSDB data) {
            title1.setText("ID:");
            title2.setText("Кліент:");
            title3.setText("Адреса:");
            title4.setText("Виконавець:");
            title5.setText("Коментар:");

            text1.setText("" + data.serverId);
            text2.setText("" + data.spiskliNm);
            text3.setText("" + data.adresaNm);
            text4.setText("" + data.sotrFio);
            text5.setText("" + data.commentTxt);

            try {
                StackPhotoDB stackPhotoBefore = StackPhotoRealm.getByServerId(data.img_before_hash);

                if (stackPhotoBefore == null){
                    stackPhotoBefore = StackPhotoRealm.getByServerId(String.valueOf(data.imgBeforeId));
                }

                StackPhotoDB stackPhotoAfter = StackPhotoRealm.getByHash(data.img_after_hash);

                if (stackPhotoAfter == null){
                    stackPhotoAfter = StackPhotoRealm.getByServerId(String.valueOf(data.imgAfterId));
                }

                image.setImageURI(Uri.parse(stackPhotoBefore.photo_num));
                image2.setImageURI(Uri.parse(stackPhotoAfter.photo_num));
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "bindACHIEVEMENTS", "Exception e: " + e);
            }


            layout.setOnClickListener(v -> {
                DialogData dialog = new DialogData(v.getContext());
                dialog.setTitle("Коментар");
                dialog.setText(data.commentTxt);
                dialog.setClose(dialog::dismiss);
                dialog.show();
            });
        }
    }

    /**
     * 27.03.23.
     * Создаю модальное окно с "Карточкой сотрудника".
     * На данном этапе это сделано колхозно и нужно только для того что б сделать перессылку на сайт.
     */
    private void openSotrInfoDialog(UsersSDB data) {
        DialogData dialog = new DialogData(mContext);
        dialog.setTitle("Картка користувача");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.setText(makeEmployeeCardText(data), () -> {
            });
        } else {
            dialog.setText("Обратитесь к Вашему руководителю.");
        }
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    /**
     * 27.03.23.
     * Колхозно собрана информация о сотруднике. Это надо будет переделывать.
     * На данный момент основная задача этого безобразия - сделать перессылку на сайт для того что б
     * мерчандайзеры могли определить координаты своего места жительства.
     * <p>
     * На стороне приложения, для этих целей есть не описанная точка входа(П.Сайт 25 Ноября 22г):
     * mod=location
     * act=save_home_location
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private SpannableStringBuilder makeEmployeeCardText(UsersSDB data) {
        SpannableStringBuilder res = new SpannableStringBuilder();
        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);

        res.append(Html.fromHtml("<b>ФИО: </b>"))
                .append(data.fio)
                .append("\n");

        // ФИРМА-------
        res.append(Html.fromHtml("<b>Фирма: </b>"));
        try {
            res.append(SQL_DB.customerDao().getById(data.clientId).nm);
        } catch (Exception e) {
            res.append("Определить фирму не получилось");
        }
        res.append("\n");
        //--------------

        res.append(Html.fromHtml("<b>Телефон: </b>"))
                .append(data.tel)
                .append("\n");

        res.append(Html.fromHtml("<b>Количество отчётов: </b>"))
                .append(String.valueOf(data.reportCount))
                .append("\n");

        if (data.id == userId) {
            res.append("\n");
            res.append(createLinkedString("Определить координаты места жительства", makeLink()));
        }

        return res;
    }

    private SpannableString createLinkedString(String msg, String link) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                textView.getContext().startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

    private String makeLink() {
        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);
        String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
        hash = Globals.getSha1Hex(hash);

        return String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=/mobile.php?mod=sotr_list**act=my_profile", userId, hash);
    }


}
