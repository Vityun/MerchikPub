package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Recyclers.KeyValueData;
import ua.com.merchik.merchik.Recyclers.KeyValueListAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

@SuppressLint("ValidFragment")
public class DetailedReportHomeFrag extends Fragment {

    private AppCompatActivity mContext;
    private ArrayList<Data> list;
    private WpDataDB wpDataDB;

    private GoogleMap map;

    private Float spotLat;
    private Float spotLon;

    // Интерфейс
    TextView activity_title;
    TextView textDRDateV, textDRAddrV, textDRCustV, textDRMercV, textTheme;
    LinearLayout option_signal_layout2;
    private RecyclerView recycler;

    private ImageView merchikImg;

    public DetailedReportHomeFrag(AppCompatActivity context, ArrayList<Data> list, WpDataDB wpDataDB) {
        // Required empty public constructor
        this.mContext = context;
        this.list = list;
        this.wpDataDB = wpDataDB;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dr_home, container, false);

        try {
            WorkPlan workPlan = new WorkPlan();
            LinearLayout ll = workPlan.getOptionLinearLayout(mContext, workPlan.getWpOpchetId(wpDataDB));

            merchikImg = v.findViewById(R.id.merchik);
            Drawable drawable = merchikImg.getBackground();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTint(mContext.getResources().getColor(R.color.colotSelectedTab2));
            }

            textDRDateV = v.findViewById(R.id.textDRDateVal);
            textDRAddrV = v.findViewById(R.id.textDRAddrVal);
            textDRCustV = v.findViewById(R.id.textDRCustVal);
            textDRMercV = v.findViewById(R.id.textDRMercVal);
            option_signal_layout2 = v.findViewById(R.id.option_signal_layout2);
            option_signal_layout2.setOnClickListener(view -> openConductDialog());
            recycler = v.findViewById(R.id.recycler);

            textDRDateV.setText(Clock.getHumanTimeYYYYMMDD(list.get(0).getDate().getTime() / 1000));
            textDRAddrV.setText(list.get(0).getAddr());
            textDRCustV.setText(list.get(0).getCust());
            textDRMercV.setText(list.get(0).getMerc());
            option_signal_layout2.addView(ll);

            recycler.setAdapter(new KeyValueListAdapter(createKeyValueData(wpDataDB)));
            recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


//            textTheme = v.findViewById(R.id.theme);

            int themeId = wpDataDB.getTheme_id();
            ThemeDB themeDB = ThemeRealm.getByID(String.valueOf(themeId));
            if (themeId == 998) {
                textTheme.append(themeDB.getNm());
            } else {
                CharSequence chsr = Html.fromHtml("<font color=red>" + themeDB.getNm() + "</font>");
                textTheme.append(chsr);
            }


            spotLat = Float.valueOf(wpDataDB.getAddr_location_xd());
            spotLon = Float.valueOf(wpDataDB.getAddr_location_yd());

            Log.e("DetailedReportHomeFrag", "onCreateView.spotLat: " + spotLat);
            Log.e("DetailedReportHomeFrag", "onCreateView.spotLon: " + spotLon);

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapView2);

            Log.e("DetailedReportHomeFrag", "SupportMapFragment: " + mapFragment);

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    map = googleMap;
                    updateMap();
                });
            }

        } catch (Exception e) {

        }

        return v;
    }

    /*Заполнение данных над картой*/
    private List<KeyValueData> createKeyValueData(WpDataDB wpDataDB) {
        List<KeyValueData> result = new ArrayList<>();

        result.add(themeData(wpDataDB));
        result.add(statusData(wpDataDB));
        result.add(new KeyValueData(Html.fromHtml("<b>Премия (план):</b>"), "" + wpDataDB.getCash_ispolnitel(), null));
        result.add(new KeyValueData(Html.fromHtml("<b>Снижение (по опциям):</b>"), Html.fromHtml("<u>" + wpDataDB.cash_penalty + "</u>"), this::openConductDialog));
        result.add(new KeyValueData(Html.fromHtml("<b>Премия (факт):</b>"), "" + wpDataDB.cash_fact, null));
        result.add(new KeyValueData(Html.fromHtml("<b>Продолж. работ (по документу):</b>"), "", null));
        result.add(new KeyValueData(Html.fromHtml("<b>Продолж. работ (средняя):</b>"), "", null));
        result.add(new KeyValueData(Html.fromHtml("<b>Стоимость часа:</b>"), "", null));

        return result;
    }

    /*Заполнение строки: Тема*/
    private KeyValueData themeData(WpDataDB wpDataDB) {
        CharSequence key;
        CharSequence value;

        key = Html.fromHtml("<b>Тема:</b>");

        int themeId = wpDataDB.getTheme_id();
        ThemeDB themeDB = ThemeRealm.getByID(String.valueOf(themeId));
        if (themeId == 998) {
            value = themeDB.getNm();
        } else {
            value = Html.fromHtml("<font color=red>" + themeDB.getNm() + "</font>");
        }

        return new KeyValueData(key, value, null);
    }

    /*Заполнение строки: Статус отчёта*/
    private KeyValueData statusData(WpDataDB wpDataDB) {
        CharSequence key = Html.fromHtml("<b>Статус отчёта:</b>");
        CharSequence value;

        if (wpDataDB.getStatus() == 1) {
            value = Html.fromHtml("<font color=green>Проведён</font>");
        } else {
            value = Html.fromHtml("<font color=red>Не проведён</font>");
        }

        return new KeyValueData(key, value, null);
    }


    private void updateMap() {
        Log.e("DetailedReportHomeFrag", "updateMap.spotLat: " + spotLat);
        Log.e("DetailedReportHomeFrag", "updateMap.spotLon: " + spotLon);

        if (map == null) return;
        if (spotLat != null && spotLon != null) {
            LatLng coord = new LatLng(spotLat, spotLon);
            map.addMarker(new MarkerOptions()
                    .position(coord)
                    .title(wpDataDB.getAddr_txt())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );

            LatLng coordUser = new LatLng(Globals.CoordX, Globals.CoordY);
            map.addMarker(new MarkerOptions()
                    .position(coordUser)
                    .title("Ваше местоположение")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            CameraPosition camPos = new CameraPosition.Builder()
                    .target(coord)
                    .zoom(14)
                    .build();
            CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
            map.animateCamera(camUpd3);
        }
    }

    private SpannableString createLinkedString(String msg) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                openConductDialog();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

    private void openConductDialog() {
        WorkPlan workPlan = new WorkPlan();
        List<OptionsDB> opt = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());
        WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(wpDataDB.getCode_dad2());
        new Options().conduct(getContext(), wp, opt, 3, new Clicks.click() {
            @Override
            public <T> void click(T data) {
                OptionsDB optionsDB = (OptionsDB) data;
                OptionMassageType msgType = new OptionMassageType();
                msgType.type = OptionMassageType.Type.DIALOG;
                new Options().optControl(getContext(), wp, optionsDB, Integer.parseInt(optionsDB.getOptionControlId()), msgType, Options.NNKMode.CHECK);
            }
        });
    }

}
