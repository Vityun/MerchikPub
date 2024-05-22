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

class CheckAndLogAllAppsOnDevice {
    companion object {
        private fun getApps(withSystemApps: Boolean): Single<List<PackageInfo>> {
            return Single.create {
                val packageManager = MyApplication.getAppContext().packageManager
                val appList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

                it.onSuccess(
                    if (withSystemApps) appList
                    else appList.filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0}
                )
            }
        }

        fun saveAppsToLog(withSystemApps: Boolean) {
            val disposable = CompositeDisposable()
            disposable.add(
                getApps(withSystemApps)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { appList: List<PackageInfo> ->
                        for (appInfo in appList) {
                            RealmManager.setRowToLog(
                                listOf(
                                    LogDB(
                                        RealmManager.getLastIdLogDB() + 1,
                                        System.currentTimeMillis() / 1000,
                                        "app: ${appInfo.packageName}",
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