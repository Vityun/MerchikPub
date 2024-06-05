package ua.com.merchik.merchik.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class CheckAndLogCompetitorAppsOnDevice {
    private Context context;

    public CheckAndLogCompetitorAppsOnDevice(Context context) {
        this.context = context;
    }

    public List<String> getCompetitorPackages() {
        List<String> competitorPackages = new ArrayList<>();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (metaData != null) {
                int resId = metaData.getInt("com.example.yourapp.COMPETITOR_PACKAGES");
                competitorPackages = Arrays.asList(context.getResources().getStringArray(resId));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return competitorPackages;
    }

    public Map<String, Boolean> checkInstalledApps(List<String> packageNames) {
        Map<String, Boolean> result = new HashMap<>();
        for (String packageName : packageNames) {
            result.put(packageName, isAppInstalled(packageName));
        }
        return result;
    }

    private boolean isAppInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void saveAppsToLog() {
        List<String> competitorPackages = getCompetitorPackages();
        Map<String, Boolean> installedApps = checkInstalledApps(competitorPackages);
        for (Map.Entry<String, Boolean> entry : installedApps.entrySet()) {
            if (entry.getValue()) {
                Long hash = Long.valueOf(entry.getKey().hashCode());
                RealmManager.setRowToLog(
                        Collections.singletonList(new LogDB(
                                RealmManager.getLastIdLogDB() + 1,
                                System.currentTimeMillis() / 1000,
                                "app: " + entry.getKey(),
                                1311,
                                null,
                                null,
                                hash,
                                Globals.userId,
                                null,
                                Globals.session,
                                null
                        ))
                );
            }
        }
    }
}
