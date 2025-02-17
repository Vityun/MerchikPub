package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Options.Options.ConductMode.SALARY_CUT;

import android.annotation.SuppressLint;
import android.content.Context;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import ua.com.merchik.merchik.Activities.Features.ui.ComposeFunctions;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.FabYoutube;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Recyclers.KeyValueData;
import ua.com.merchik.merchik.Recyclers.KeyValueListAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;


@SuppressLint("ValidFragment")
//@AndroidEntryPoint // Аннотация для поддержки Hilt
public class DetailedReportHomeFrag extends Fragment {

    private static AppCompatActivity mContext;
    private WpDataDB wpDataDB;

    private GoogleMap map;

    private Float spotLat;
    private Float spotLon;

    private FabYoutube fabYoutube = new FabYoutube();
    private FloatingActionButton fabYouTube;
    private TextView badgeTextView;
    public static final Integer[] DetailedReportHomeFrag_VIDEO_LESSONS = new Integer[]{819, 4456};

    // Интерфейс
    TextView activity_title;
    TextView textDRDateV, textDRAddrV, textDRCustV, textDRMercV, textTheme;
    LinearLayout option_signal_layout2;
    private RecyclerView recycler;

    private ImageView merchikImg;

    private ComposeView composeView;
    private static CommentViewModel viewModel;

    public DetailedReportHomeFrag() {
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag/1", "create");
    }

    public static DetailedReportHomeFrag newInstance(AppCompatActivity context, WpDataDB wpDataDB,
                                                     CommentViewModel commentViewModel) {
        DetailedReportHomeFrag fragment = new DetailedReportHomeFrag();
        Bundle args = new Bundle();
        args.putParcelable("wpDataDB", wpDataDB);
        mContext = context;
        fragment.setArguments(args);
        viewModel = commentViewModel;
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onCreate");

        Bundle args = getArguments();
        if (args != null) {
            wpDataDB = args.getParcelable("wpDataDB");
            Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onCreate/wpDataDB: " + wpDataDB);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag", "onDetach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag/onCreateView", "inflater: " + inflater);
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag/onCreateView", "container: " + container);
        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag/onCreateView", "create: " + savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_dr_home, container, false);

        Globals.writeToMLOG("INFO", "DetailedReportHomeFrag/onCreateView", "v: " + v);

        try {
            WorkPlan workPlan = new WorkPlan();
            LinearLayout ll = workPlan.getOptionLinearLayout(mContext, workPlan.getWpOpchetId(wpDataDB));

            merchikImg = v.findViewById(R.id.merchik);
            Drawable drawable = merchikImg.getBackground();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTint(mContext.getResources().getColor(R.color.colotSelectedTab2));
            }

            fabYouTube = v.findViewById(R.id.fab);
            badgeTextView = v.findViewById(R.id.badge_text_view_tar);

            textDRDateV = v.findViewById(R.id.textDRDateVal);
            textDRAddrV = v.findViewById(R.id.textDRAddrVal);
            textDRCustV = v.findViewById(R.id.textDRCustVal);
            textDRMercV = v.findViewById(R.id.textDRMercVal);
            option_signal_layout2 = v.findViewById(R.id.option_signal_layout2);
            option_signal_layout2.setOnClickListener(view -> openConductDialog());
            recycler = v.findViewById(R.id.recycler);
            composeView = v.findViewById(R.id.composeView);


            textDRDateV.setText(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000));
            textDRAddrV.setText(wpDataDB.getAddr_txt());
            textDRCustV.setText(wpDataDB.getClient_txt());
            textDRMercV.setText(wpDataDB.getUser_txt());

            option_signal_layout2.addView(ll);

            recycler.setAdapter(new KeyValueListAdapter(createKeyValueData(wpDataDB)));
            recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

            spotLat = Float.valueOf(wpDataDB.getAddr_location_xd());
            spotLon = Float.valueOf(wpDataDB.getAddr_location_yd());

            Log.e("DetailedReportHomeFrag", "onCreateView.spotLat: " + spotLat);
            Log.e("DetailedReportHomeFrag", "onCreateView.spotLon: " + spotLon);

            // 23.08.23 Видаляю згадування про мапу для того щоб перевірити чи не ізза неї ідуть проблеми
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapView2);

            Log.e("DetailedReportHomeFrag", "SupportMapFragment: " + mapFragment);

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    map = googleMap;
                    updateMap();
                });
            }

            ComposeFunctions.setContent(composeView, wpDataDB, viewModel);
            Log.e("ComposeFunctions!", "textDRAddrV: " + textDRAddrV.getTextColors().getDefaultColor());

            fabYoutube.setFabVideo(fabYouTube, DetailedReportHomeFrag_VIDEO_LESSONS, () -> fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, DetailedReportHomeFrag_VIDEO_LESSONS));
            fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, DetailedReportHomeFrag_VIDEO_LESSONS);

        } catch (Exception e) {
            Log.e("DetailedReportHomeFrag", "Exception e: " + e);
        }

        return v;
    }


    private void Text(String этоТекстовоеПолеВCompose) {
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
            Log.d("ADD_OPINION_FROM_DETAIL", "themeId: " + value);
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
        map.getUiSettings().setScrollGesturesEnabled(false);
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
        new Options().conduct(getContext(), wp, opt, SALARY_CUT, new Clicks.click() {
            @Override
            public <T> void click(T data) {
                OptionsDB optionsDB = (OptionsDB) data;
                OptionMassageType msgType = new OptionMassageType();
                msgType.type = OptionMassageType.Type.DIALOG;
                new Options().optControl(getContext(), wp, optionsDB, Integer.parseInt(optionsDB.getOptionControlId()), null, msgType, Options.NNKMode.CHECK, new OptionControl.UnlockCodeResultListener() {
                    @Override
                    public void onUnlockCodeSuccess() {

                    }

                    @Override
                    public void onUnlockCodeFailure() {

                    }
                });
            }
        });
    }

}
