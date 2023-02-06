package ua.com.merchik.merchik.Activities.navigationMenu;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.MenuItemFromWebDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class MenuHeaderAdapter extends RecyclerView.Adapter<MenuHeaderAdapter.MenuHeaderViewHolder> {

    private List<MenuHeader> data;
    private MenuListener listener;

    public MenuHeaderAdapter(List<MenuHeader> data, MenuListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuHeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MenuHeaderViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MenuHeaderViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private ImageView icon;
        private ImageView iconShadow;
        private ImageView arrow;
        private TextView name;
        private TextView number, number2, number3, number4;
        private RecyclerView recyclerSub;
        private MenuSubAdapter adapter;

        public MenuHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.menu_item);
            icon = itemView.findViewById(R.id.icon);
            iconShadow = itemView.findViewById(R.id.iconShadow);
            arrow = itemView.findViewById(R.id.arrowIcon);
            name = itemView.findViewById(R.id.menuText);
            number = itemView.findViewById(R.id.menuNumber);
            number2 = itemView.findViewById(R.id.menuNumber2);
            number3 = itemView.findViewById(R.id.menuNumber3);
            number4 = itemView.findViewById(R.id.menuNumber4);
            recyclerSub = itemView.findViewById(R.id.recyclerSub);
            recyclerSub.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            adapter = new MenuSubAdapter();
            recyclerSub.setAdapter(adapter);
        }

        public void bind(MenuHeader header) {
            final int newColor = itemView.getResources().getColor(R.color.startColor);
            final int shadowColor = itemView.getResources().getColor(R.color.colorUnselectedTab);
            final int shadow = itemView.getResources().getColor(R.color.shadow);

            itemView.setOnClickListener(v -> {
                listener.onClick(v, header.menuItemFromWebDB);
                Log.e("NWitemClick", "ITEM_CLICK: " + header.menuItemFromWebDB.getID());
            });

            // Наполенние текстом
            name.setText(header.menuItemFromWebDB.getNm());
            name.setTypeface(null, Typeface.BOLD);
            name.setShadowLayer(3, 5, 5, shadow);

            // Заполнение числа
            ArrayList arr = getNumbers(header.menuItemFromWebDB);
            Log.e("MenuHeaderBind", "id: " + header.menuItemFromWebDB.getID());
            Log.e("MenuHeaderBind", "arr: " + arr);

            if (arr != null && arr.size() > 0) {
                try {
                    setNumberData(number, String.valueOf(arr.get(0)));
                    setNumberData(number2, String.valueOf(arr.get(1)));
                    setNumberData(number3, String.valueOf(arr.get(2)));
                    setNumberData(number4, String.valueOf(arr.get(3)));

                    number.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "Количество работ по плану за " + Clock.today, Toast.LENGTH_LONG).show());
                    number2.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "Количество выполненых работ за " + Clock.today, Toast.LENGTH_LONG).show());
                    number3.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "Процент выполненых работ за " + Clock.today, Toast.LENGTH_LONG).show());

                } catch (Exception e) {
                    // TODO Поправить/Разобраться/Доработать
                    // Это ужасно. Как будет время надо подумать как правильно сделать вывод этого
                    // всего безобразия. Сейчас вылетает Exception в элементах которые не заполнены,
                    // но я к ним обращаюсь.
                }
            }

//            layout.setBackgroundColor(Color.RED);

            // Если это не подменю - не отображаю стрелочки(обозначающие выпадающий список).
            if (header.menuItemFromWebDB.getSubmenu().size() == 0) {
                arrow.setVisibility(View.GONE);

                layout.setOnClickListener(v -> {
                    listener.onClick(v, header.menuItemFromWebDB);
                    Log.e("NWitemClick", "ITEM_CLICK: " + header.menuItemFromWebDB.getID());
                });
            } else {
                layout.setOnClickListener(v -> {
                    listener.onClick(v, header.menuItemFromWebDB);
                    Log.e("NWitemClick", "2_ITEM_CLICK: " + header.menuItemFromWebDB.getID());
                });
            }


            if (header.menuItemFromWebDB.getID() == 131) {
                String lang = PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getString("lang", "UA");
                switch (lang) {
                    case "UA":
                        icon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ua));
                        break;

                    case "RU":
                        icon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ru));
                        break;

                    case "GB":
                        icon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.gb));
                        break;

                    case "PL":
                        icon.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.pl));
                        break;
                }
            } else {
                // Установка иконочки
                // @drawable -- иконка пункта меню
                Drawable drawable = getItemMenuIcon(itemView.getContext(), header.menuItemFromWebDB);
                icon.setImageDrawable(drawable);
                icon.setColorFilter(newColor);

                // @drawable2 -- иконка пункта меню которая выступает тенью. Если не добавлять отдельный
                // drawable, то стили подтянет с первого. (не знаю как это подправить)
                Drawable drawable2 = getItemMenuIcon(itemView.getContext(), header.menuItemFromWebDB);
                iconShadow.setImageDrawable(drawable2);
                iconShadow.setColorFilter(shadowColor);
            }

            layout.setOnClickListener(v -> {

                Log.e("NAV_VIEW_M", "Я НАЖАЛ СЮДА");

                listener.onClick(v, header.menuItemFromWebDB);
                Log.e("NWitemClick", "2_ITEM_CLICK: " + header.menuItemFromWebDB.getID());

                header.isExapanded = !header.isExapanded;
                if (header.items.isEmpty()) header.isExapanded = false;
                handleRecycler(header.isExapanded);
            });

            adapter.setData(header.items);
            handleRecycler(header.isExapanded);
        }

        private void handleRecycler(boolean isExapanded) {
            if (isExapanded) {
                recyclerSub.setVisibility(View.VISIBLE);
                arrow.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_angle_down_solid));
            } else {
                recyclerSub.setVisibility(View.GONE);
                arrow.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_angle_right_solid));
            }
        }
    }


    /**
     * 25.12.2020
     * Поиск иконки меню в телефоне
     * <p>
     * Если в строке базы данных есть строка с иконкой - ищет эту иконку в памяти телефона
     * и возрвщение её drawable
     *
     * @param context    -- контекст RecyclerView (нужен для получения доступов к ресурсам)
     * @param menuItemDB -- строка базы данных "текущего" элемента меню
     * @return
     */
    public Drawable getItemMenuIcon(Context context, MenuItemFromWebDB menuItemDB) {
        String path = menuItemDB.getImg();

        Log.e("getItemMenuIcon", "path: " + path);

        String fileName = new File(path).getName();

        Log.e("getItemMenuIcon", "0_fileName: " + fileName);

        StringTokenizer tokens = new StringTokenizer(fileName, "-");
        String first1 = "";
        String first2 = "";
        if (tokens.countTokens() > 0) {
            first1 = tokens.nextToken();
            first2 = tokens.nextToken();
        }

        Log.e("getItemMenuIcon", "2_fileName: " + first1);
        Log.e("getItemMenuIcon", "3_fileName: " + first2);

        fileName = "ic_" + first2;

        Log.e("getItemMenuIcon", "1_fileName: " + fileName);

        try {
            Resources resources = context.getResources();
            final int resourceId = resources.getIdentifier(fileName, "drawable",
                    context.getPackageName());

            return resources.getDrawable(resourceId);

//            return null;
        } catch (Exception e) {
            Log.e("getItemMenuIcon", "Exception: " + e.toString());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 29.12.2020
     * Установка в TextView "чисел" данных.
     * <p>
     * Устанавливаю в "балончик"(текстовое поле обозначающее "число") данные которые получаю ранее.
     * Делаю их видимыми ибо по умолчанию данные скрыты
     */
    public void setNumberData(TextView number, String data) {
        if (!data.isEmpty()) {
            number.setText(data);
            number.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 29.12.2020
     * Заполнение "чисел" пунктов меню
     * <p>
     * Захардкоженный функционал заполнения "чисел"("балончиков") пунктов меню в зависимости от
     * их идентификаторов
     *
     * @param menuItemDB -- строка базы данных "текущего" элемента меню
     */
    public ArrayList getNumbers(MenuItemFromWebDB menuItemDB) {
        ArrayList res = new ArrayList();

        switch (menuItemDB.getID()) {
            case 135:   // План работ/план работ
                List<WpDataDB> data135 = RealmManager.getAllWorkPlan();
                int wpDataCount135 = data.size();
                int completed135 = RealmManager.getWpData(1, Clock.today);
                double d135 = (double) completed135 / wpDataCount135;
                double percent135 = (double) d135 * 100;
                int p135 = (int) percent135;

                res.add(RealmManager.getWpDataDate(Clock.today));
                res.add(completed135);
                res.add("" + p135 + "%");
                res.add("");

                return res;
            case 129:   // План работ
                List<WpDataDB> data = RealmManager.getAllWorkPlan();
                int wpDataCount = data.size();
                int completed = RealmManager.getWpData(1, Clock.today);
                double d = (double) completed / wpDataCount;
                double percent = (double) d * 100;
                int p = (int) percent;

                res.add(RealmManager.getWpDataDate(Clock.today));
                res.add(completed);
                res.add("" + p + "%");
                res.add("");

                return res;

            case 143:   // Задачи
                try {
                    long time = Clock.getDateLong(-30).getTime() / 1000;
                    res.add(SQL_DB.tarDao().getAllByTp(Globals.userId, 1, time).size());
                }catch (Exception e){}
                return res;

            case 144:   // Рекламации
                try {
                    long time = Clock.getDateLong(-30).getTime() / 1000;
                    res.add(SQL_DB.tarDao().getAllByTp(Globals.userId, 0, time).size());
                }catch (Exception e){}
                return res;

            default:
                return res;
        }
    }


    /**
     * ПОДМЕНЮ
     */
    public class MenuSubViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon, iconShadow;
        private ImageView arrow;
        private TextView name;
        private TextView number, number2, number3, number4;

        public MenuSubViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            iconShadow = itemView.findViewById(R.id.iconShadow);
            arrow = itemView.findViewById(R.id.arrowIcon);
            name = itemView.findViewById(R.id.menuText);
            number = itemView.findViewById(R.id.subMenuNumber);
            number2 = itemView.findViewById(R.id.subMenuNumber1);
            number3 = itemView.findViewById(R.id.subMenuNumber2);
            number4 = itemView.findViewById(R.id.subMenuNumber3);
        }

        public void bind(MenuItemFromWebDB item) {
            final int newColor = itemView.getResources().getColor(R.color.startColor);
            final int shadowColor = itemView.getResources().getColor(R.color.colorUnselectedTab);
            final int shadow = itemView.getResources().getColor(R.color.shadow);


            itemView.setOnClickListener(v -> {
                listener.onClick(v, item);
                Log.e("NWitemClick", "SUB_ITEM_CLICK: " + item.getID());
            });

            name.setText(item.getNm());
            name.setTypeface(null, Typeface.BOLD);
            name.setShadowLayer(3, 5, 5, shadow);


            // Заполнение числа
            ArrayList arr = getNumbers(item);
            Log.e("MenuHeaderBind", "id: " + item.getID());
            Log.e("MenuHeaderBind", "arr: " + arr);

            if (arr != null && arr.size() > 0) {
                try {
                    setNumberData(number, String.valueOf(arr.get(0)));
                    setNumberData(number2, String.valueOf(arr.get(1)));
                    setNumberData(number3, String.valueOf(arr.get(2)));
                    setNumberData(number4, String.valueOf(arr.get(3)));

                    number.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "Количество работ по плану за " + Clock.today, Toast.LENGTH_LONG).show());
                    number2.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "Количество выполненых работ за " + Clock.today, Toast.LENGTH_LONG).show());
                    number3.setOnClickListener(v -> Toast.makeText(itemView.getContext(), "Процент выполненых работ за " + Clock.today, Toast.LENGTH_LONG).show());

                } catch (Exception e) {
                    // TODO Поправить/Разобраться/Доработать
                    // Это ужасно. Как будет время надо подумать как правильно сделать вывод этого
                    // всего безобразия. Сейчас вылетает Exception в элементах которые не заполнены,
                    // но я к ним обращаюсь.
                }
            }


            // delete arrow
            if (item.getSubmenu().size() == 0) {
                arrow.setVisibility(View.GONE);
            }

            Drawable drawable = getItemMenuIcon(itemView.getContext(), item);
            icon.setImageDrawable(drawable);
            icon.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);

            Drawable drawable2 = getItemMenuIcon(itemView.getContext(), item);
            iconShadow.setImageDrawable(drawable2);
            iconShadow.setColorFilter(shadowColor, PorterDuff.Mode.SRC_ATOP);
            //...
        }
    }

    public class MenuSubAdapter extends RecyclerView.Adapter<MenuSubViewHolder> {

        private List<MenuItemFromWebDB> data;

        public void setData(List<MenuItemFromWebDB> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MenuSubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MenuSubViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_sub, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MenuSubViewHolder holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    public interface MenuListener {
        void onClick(View view, MenuItemFromWebDB item);
    }
}
