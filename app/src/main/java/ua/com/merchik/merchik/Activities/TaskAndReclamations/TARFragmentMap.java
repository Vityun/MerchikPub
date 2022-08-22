package ua.com.merchik.merchik.Activities.TaskAndReclamations;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class TARFragmentMap extends Fragment {

    private GoogleMap map;
    private TextView description;


    public TARFragmentMap() {
        Log.d("test", "test");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tab_wp_map, container, false);

        description = v.findViewById(R.id.description);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                updateMap();
            });
        }
        return v;
    }

    private void updateMap() {
        if (map == null) return;
        LatLng coordUser = new LatLng(Globals.CoordX, Globals.CoordY);
        map.addMarker(new MarkerOptions()
                .position(coordUser)
                .title("Ваше местоположение")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Добавление в цикле маркеров - магазинов
        List<TasksAndReclamationsSDB> data = SQL_DB.tarDao().getAllJoinAddressSDB();
        for (TasksAndReclamationsSDB item : data) {
            try {
                float spotLat = Float.parseFloat(item.coordX);
                float spotLon = Float.parseFloat(item.coordY);

                LatLng coord = new LatLng(spotLat, spotLon);
                map.addMarker(new MarkerOptions()
                        .position(coord)
                        .title("" + item.addr)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                );

            } catch (Exception e) {

            }
        }

        CharSequence msg = description.getText();
        description.setText(msg + " ... и добавил целых: " + data.size() + " меток на карту..");

        CameraPosition camPos = new CameraPosition.Builder()
                .target(coordUser)
                .zoom(14)
                .build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        map.animateCamera(camUpd3);
    }
}
