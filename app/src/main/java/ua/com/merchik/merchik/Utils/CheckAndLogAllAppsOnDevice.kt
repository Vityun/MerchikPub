package ua.com.merchik.merchik.Utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import ua.com.merchik.merchik.Activities.MyApplication
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.database.realm.RealmManager

enum class AppTypeForScan{
    ONLY_SYSTEM, ONLY_INSTALLED
}

class CheckAndLogAllAppsOnDevice {

    companion object {

        private fun getApps(appsType: AppTypeForScan): Single<List<PackageInfo>> {
            return Single.create {
                val packageManager = MyApplication.getAppContext().packageManager
                val appList = packageManager.getInstalledPackages(0)

                it.onSuccess(
                    when (appsType) {
                        AppTypeForScan.ONLY_SYSTEM -> appList.filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1}
                        AppTypeForScan.ONLY_INSTALLED -> appList.filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0}
                    }
                )
            }
        }

        private fun getLabelApp(appInfo: ApplicationInfo): CharSequence {
            val packageManager = MyApplication.getAppContext().packageManager
            return packageManager.getApplicationLabel(appInfo)
        }

        fun saveAppsToLog(appsType: AppTypeForScan) {
            val disposable = CompositeDisposable()
            disposable.add(
                getApps(appsType)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { appList: List<PackageInfo> ->
                        for (appInfo in appList) {
                            val labelInfo = getLabelApp(appInfo.applicationInfo)
                            RealmManager.setRowToLog(
                                listOf(
                                    LogDB(
                                        RealmManager.getLastIdLogDB() + 1,
                                        System.currentTimeMillis() / 1000,
                                        "app: ${appInfo.packageName} | $labelInfo",
                                        1309,
                                        null,
                                        null,
                                        appInfo.packageName.hashCode().toLong(),
                                        Globals.userId,
                                        null,
                                        Globals.session,
                                        null
                                    )
                                )
                            )
                        }
                        disposable.dispose()
                    }
            )
        }
    }
}