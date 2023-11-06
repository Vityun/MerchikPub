package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;

public class DialogMap {

    private Dialog dialog;
    private TextView systemText;
    private TextView userText;
    private Button btn;
    private GoogleMap map;
    private Float spotLat;
    private Float spotLon;

    private SupportMapFragment mapFragment;
    private AppCompatActivity context;

    public DialogMap(AppCompatActivity context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_info_gps_map);
        systemText = dialog.findViewById(R.id.dialog_info_gps_map_textview_sys);
        userText = dialog.findViewById(R.id.dialog_info_gps_map_textview_user);
        // Элементы диалога
        btn = dialog.findViewById(R.id.dialog_info_gps_map_button);
        btn.setOnClickListener(v -> {
            dismiss();
            dialog.cancel();
        });


        mapFragment = (SupportMapFragment) context.getSupportFragmentManager()
                .findFragmentById(R.id.mapView);

        Log.e("DialogMap", "SupportMapFragment: " + mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                updateMap();
            });
        }
    }

    public DialogMap(AppCompatActivity context, String test, Float locationXd, Float locationYd, String s, double coordX, double coordY, String s1) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_info_gps_map);
        systemText = dialog.findViewById(R.id.dialog_info_gps_map_textview_sys);
        userText = dialog.findViewById(R.id.dialog_info_gps_map_textview_user);
        // Элементы диалога
        btn = dialog.findViewById(R.id.dialog_info_gps_map_button);
        btn.setOnClickListener(v -> {
            dismiss();
            dialog.cancel();
        });

        mapFragment = (SupportMapFragment) context.getSupportFragmentManager()
                .findFragmentById(R.id.mapView);

        Log.e("DialogMap", "SupportMapFragment: " + mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                updateMap2(locationXd, locationYd, s, coordX, coordY, s1);
            });
        }
    }

    public void setData(String s1, String s2) {
        systemText.setText(s1);
        userText.setText(s2);
    }

    public void setSpot(Float lat, Float lon) {
        spotLat = lat;
        spotLon = lon;
    }

    public void updateMap() {
        if (map == null) return;
        if (spotLat != null && spotLon != null) {
            LatLng coord = new LatLng(spotLat, spotLon);
            map.addMarker(new MarkerOptions()
                    .position(coord)
                    .title("Торговая точка")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
        }

        LatLng coordUser = new LatLng(Globals.CoordX, Globals.CoordY);
        map.addMarker(new MarkerOptions()
                .position(coordUser)
                .title("Ваше местоположение")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        CameraPosition camPos = new CameraPosition.Builder()
                .target(coordUser)
                .zoom(14)
                .build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        map.animateCamera(camUpd3);
    }

    public void updateMap2(Float locationXd, Float locationYd, String s, double coordX, double coordY, String s1) {
        if (map == null) return;

        LatLng coord = new LatLng(locationXd, locationYd);
        map.addMarker(new MarkerOptions()
                .position(coord)
                .title(s)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        );

        LatLng coordUser = new LatLng(coordX, coordY);
        map.addMarker(new MarkerOptions()
                .position(coordUser)
                .title(s1)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        CameraPosition camPos = new CameraPosition.Builder()
                .target(coordUser)
                .zoom(14)
                .build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        map.animateCamera(camUpd3);
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            if (mapFragment != null) {
                context.getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
            }
        }
    }

}
