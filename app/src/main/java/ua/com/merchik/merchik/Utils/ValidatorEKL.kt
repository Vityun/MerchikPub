package ua.com.merchik.merchik.Utils

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.EKL.EKLDataHolder
import java.util.function.Predicate


data class ControlEKLData(
    var result: Boolean,
    var message: String
)

object ValidatorEKL {

    //    кешируем запросы БД, расчитывется в первую очередь на вызов из цикла, что бы минимизировать запросы в БД
    private val wpDataCache = mutableMapOf<String, WpDataDB?>()
    private val addressCache = mutableMapOf<Int, AddressSDB?>()
    private val userCache = mutableMapOf<Int, UsersSDB?>()
    private val userDocumentCache = mutableMapOf<Int, UsersSDB?>()
    private val tovarGroupCache = mutableMapOf<String, List<TovarGroupClientSDB>?>()

//    fun controlEKL(addr_id: Int, user_id, ): ControlEKLData {
//
//        val control = ControlEKLData(false, "")
//        val TG = TovarGroupSDB()
//
//        // Кэширование данных
////        val wpDataDB = wpDataCache.getOrPut(wpId) {
////                WpDataRealm.getWpDataRowById(id)
////        }
//        val addressSDB = addressCache.getOrPut(addr_id) {
//            RoomManager.SQL_DB.addressDao().getById(addr_id)
//        }
//
//        val usersSDBPTT = userCache.getOrPut(ptt_user_id) {
//            RoomManager.SQL_DB.usersDao().getById(wpDataDB.ptt_user_id)
//        }
//        val documentUser = userCache.getOrPut(wpDataDB.user_id) {
//            RoomManager.SQL_DB.usersDao().getUserById(wpDataDB.user_id)
//        }
//
//        val tovarGroupClientSDB = tovarGroupCache.getOrPut(wpDataDB.client_id)
//        {
//            RoomManager.SQL_DB.tovarGroupClientDao().getAllBy(
//                wpDataDB.client_id,
//                addressSDB.tpId
//            )
//        }
//
//        Log.e("ValidatorEKL", "usersSDBPTT otdelId: ${usersSDBPTT.otdelId}")
//        Log.e("ValidatorEKL", "documentUser otdelId: ${documentUser.otdelId}")
//
//        val optionMsg = StringBuilder()
//
//        val ids: MutableList<Int> = ArrayList()
//        for (item in tovarGroupClientSDB) {
//            ids.add(item.tovarGrpId)
//        }
//        Log.e("ValidatorEKL", "tovarGroupClientSDB id: $ids")
//
//        val tovarGroupSDB = RoomManager.SQL_DB.tovarGroupDao().getAllByIds(ids)
//
//        val test: TovarGroupSDB = tovarGroupSDB.stream()
//            .filter(Predicate<TovarGroupSDB> { item: TovarGroupSDB -> item.id == usersSDBPTT.otdelId })
//            .findFirst().orElse(null)
//        Log.e("test", "test: $test")
//
//        if (tovarGroupSDB.stream()
//                .filter(Predicate<TovarGroupSDB> { item: TovarGroupSDB -> item.id == usersSDBPTT.otdelId })
//                .findFirst()
//                .orElse(null) == null && (addressSDB.kolKass > 5 || addressSDB.kolKass == 0)
//        ) {
//            if (documentUser.reportDate20 != null && documentUser.reportDate20.time >= wpDataDB.dt
//                    .time
//            ) {
//                control.result = false
//                optionMsg.append(", но ").append("ПТТ работает в отделе ")
//                    .append(RoomManager.SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm)
//                    .append(" и не может подписывать ЭКЛ для: ")
//                    .append(TG.getNmFromList(tovarGroupSDB))
//                    .append(" (но исполнитель не провел свой 20-й отчет и эту блокировку пропускаем)")
//            } else {
//                control.result = true
//                optionMsg.append(", но ").append("ПТТ работает в отделе ")
//                    .append(RoomManager.SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm)
//                    .append(" и не может подписывать ЭКЛ для: ")
//                    .append(TG.getNmFromList(tovarGroupSDB))
//                    .append(" (для магазина в котором более 5 касс и исполнитель провел 20-й отчет)")
//            }
//        } else if (tovarGroupSDB.stream()
//                .filter(Predicate<TovarGroupSDB> { item: TovarGroupSDB -> item.id == usersSDBPTT.otdelId })
//                .findFirst()
//                .orElse(null) == null && (addressSDB.kolKass in 1..5)
//        ) {
//            if (documentUser.reportDate40 != null && documentUser.reportDate40.time >= wpDataDB.dt
//                    .time
//            ) {
//                control.result = false
//                optionMsg.append(", но ").append("ПТТ работает в отделе ")
//                    .append(RoomManager.SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm)
//                    .append(" и не может подписывать ЭКЛ для: ")
//                    .append(TG.getNmFromList(tovarGroupSDB))
//                    .append(" (но исполнитель не провел свой 40-й отчет и эту блокировку пропускаем)")
//            } else {
//                control.result = false
//                optionMsg.append(", но ").append("ПТТ работает в отделе ")
//                    .append(RoomManager.SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm)
//                    .append(" и не может подписывать ЭКЛ для: ")
//                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (но в данном магазине ")
//                    .append(addressSDB.kolKass).append(" касс и это допустимо)")
//            }
//        }
//        control.message = optionMsg.toString()
//        return control
//    }

    fun controlEKL(): ControlEKLData {

        val control = ControlEKLData(false, "")
        val TG = TovarGroupSDB()
        val client_id = EKLDataHolder.instance().usersPTTWPClientId
        val addr_id = EKLDataHolder.instance().usersPTTWorkAddressId
        val ptt_user_id = EKLDataHolder.instance().usersPTTWPPttUserId
        val user_id = EKLDataHolder.instance().usersPTTWPDataUserId
        val time = EKLDataHolder.instance().usersPTTWPDataTime

        if (client_id != null && addr_id != null && ptt_user_id != null
            && user_id != null && time != null
        ) {

            val addressSDB =
                addressCache.getOrPut(addr_id) {
                    RoomManager.SQL_DB.addressDao().getById(addr_id)
                }

            Log.e("ValidatorEKL", "ptt_user_id: $ptt_user_id")
            Log.e("ValidatorEKL", "client_id: $client_id")
            Log.e("ValidatorEKL", "tpId: ${addressSDB?.tpId}")

//            if (ptt_user_id == 0){
//                control.result = true
//                return control
//            }


            val usersSDBPTT =
                userCache.getOrPut(ptt_user_id) {
                    RoomManager.SQL_DB.usersDao().getById(ptt_user_id)
                }

            Log.e("ValidatorEKL", "usersSDBPTT id: ${usersSDBPTT?.id}")

            val documentUser: UsersSDB? =
                userDocumentCache.getOrPut(user_id) {
                    RoomManager.SQL_DB.usersDao().getUserById(user_id)
                }

            val key = client_id

            var list = tovarGroupCache[key]
            if (list == null) {
                list = RoomManager.SQL_DB.tovarGroupClientDao().getAllBy(
                    key,
                    addressSDB?.tpId ?: 0
                )
                tovarGroupCache[key] = list
            }

            if (list.isNullOrEmpty()) {
                val fallback = RoomManager.SQL_DB.tovarGroupClientDao().getAllBy(key, 0)
                list = fallback
                tovarGroupCache[key] = fallback   // важно: перезаписать кэш
            }

            val tovarGroupClientSDB = list


            Log.e("ValidatorEKL", "tovarGroupClientSDB size: ${tovarGroupClientSDB?.size}")


            Log.e("ValidatorEKL", "addressSDB cityId: ${addressSDB?.cityId}")

            Log.e("ValidatorEKL", "usersSDBPTT otdelId: ${usersSDBPTT?.otdelId}")
            Log.e("ValidatorEKL", "usersSDBPTT fio: ${usersSDBPTT?.fio}")
            Log.e("ValidatorEKL", "documentUser otdelId: ${documentUser?.otdelId}")

            val optionMsg = StringBuilder()

            val ids: MutableList<Int> = ArrayList()
            if (tovarGroupClientSDB != null) {
                for (item in tovarGroupClientSDB) {
                    ids.add(item.tovarGrpId)
                }
            }

            if (ids.isEmpty()) {
                ids.add(usersSDBPTT?.otdelId ?: 0)
            } else {
                usersSDBPTT?.otdelId?.takeIf { it != 0 }?.let {
                    ids.add(it)
                }
            }

            EKLDataHolder.instance().usersPTTtovarIdList = ids

            Log.e("ValidatorEKL", "tovarGroupClientSDB id: $ids")

            val tovarGroupSDB = RoomManager.SQL_DB.tovarGroupDao().getAllByIds(ids)

            val test: TovarGroupSDB? = tovarGroupSDB.stream()
                .filter(Predicate<TovarGroupSDB> { item: TovarGroupSDB ->
                    item.id == (usersSDBPTT?.otdelId ?: 0)
                })
                .findFirst().orElse(null)
            Log.e("test", "test: $test")

            if (addressSDB != null) {
                if (test == null && (addressSDB.kolKass > 5 || addressSDB.kolKass == 0)
                ) {
                    if (documentUser != null) {
                        if (documentUser.reportDate20 != null && documentUser.reportDate20.time >= time
                        ) {
                            control.result = false
                            if (usersSDBPTT != null) {
                                optionMsg.append(", но ").append("ПТТ работает в отделе ")
                                    .append(
                                        RoomManager.SQL_DB.tovarGroupDao()
                                            .getById(usersSDBPTT.otdelId).nm
                                    )
                                    .append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB))
                                    .append(" (но исполнитель не провел свой 20-й отчет и эту блокировку пропускаем)")
                            }
                        } else {
                            control.result = true
                            if (usersSDBPTT != null) {
                                optionMsg.append(", но ").append("ПТТ работает в отделе ")
                                    .append(
                                        RoomManager.SQL_DB.tovarGroupDao()
                                            .getById(usersSDBPTT.otdelId).nm
                                    )
                                    .append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB))
                                    .append(" (для магазина в котором более 5 касс и исполнитель провел 20-й отчет)")
                            }
                        }
                    }
                } else if (test == null && (addressSDB.kolKass in 1..5)
                ) {
                    if (documentUser?.reportDate40 != null && documentUser.reportDate40?.time!! >= time
                    ) {
                        control.result = false
                        if (usersSDBPTT != null) {
                            optionMsg.append(", но ").append("ПТТ работает в отделе ")
                                .append(
                                    RoomManager.SQL_DB.tovarGroupDao()
                                        .getById(usersSDBPTT.otdelId).nm
                                )
                                .append(" и не может подписывать ЭКЛ для: ")
                                .append(TG.getNmFromList(tovarGroupSDB))
                                .append(" (но исполнитель не провел свой 40-й отчет и эту блокировку пропускаем)")
                        }
                    } else {
                        control.result = false
                        if (usersSDBPTT != null) {
                            if (addressSDB != null) {
                                optionMsg.append(", но ").append("ПТТ работает в отделе ")
                                    .append(
                                        RoomManager.SQL_DB.tovarGroupDao()
                                            .getById(usersSDBPTT.otdelId).nm
                                    )
                                    .append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB))
                                    .append(" (но в данном магазине ")
                                    .append(addressSDB.kolKass).append(" касс и это допустимо)")
                            }
                        }
                    }
                }
            }

            //                    добавил 24.01.25 пропускаем если сотр. провел более 2000 отчетов
//            if (control.result && documentUser!!.reportCount >= 2000) {
//                control.result = false
//                optionMsg.append(", но сотрудник провел более 2000 отчетов и эту блокировку пропускаем до 01.03.2025.")
//            }
            control.message = optionMsg.toString()
        }
        return control
    }


    fun controlEKL(wpId: String): ControlEKLData {

        val control = ControlEKLData(false, "")
        val TG = TovarGroupSDB()

        Log.e("ValidatorEKL", "ID: $wpId")
        val id = wpId.toLong()
        val wp = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowById(id) as WpDataDB)
        Log.e("ValidatorEKL", "wp: ${wp.action_txt} | ${wp.code_dad2}")
        Log.e("ValidatorEKL", "wpDataDB.ptt_user_id: ${wp.ptt_user_id}")

        val wpDataDB = wpDataCache.getOrPut(wpId) { RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowById(id) as WpDataDB) }

        val addressSDB = wpDataDB?.let {
            addressCache.getOrPut(it.addr_id) {
                wpDataDB.let { RoomManager.SQL_DB.addressDao().getById(it.addr_id) }
            }
        }
        Log.e("ValidatorEKL", "wpDataDB.user_id: ${wpDataDB?.user_id}")

        val usersSDBPTT = wpDataDB?.let {
            userCache.getOrPut(it.ptt_user_id) {
                RoomManager.SQL_DB.usersDao().getById(wpDataDB.ptt_user_id)
            }
        }
        val documentUser: UsersSDB? = wpDataDB?.let {
            userDocumentCache.getOrPut(it.user_id) {
                RoomManager.SQL_DB.usersDao().getUserById(wpDataDB.user_id)
            }
        }

        val tovarGroupClientSDB = wpDataDB?.let {
            tovarGroupCache.getOrPut(it.client_id)
            {
                RoomManager.SQL_DB.tovarGroupClientDao().getAllBy(
                    wpDataDB.client_id,
                    addressSDB?.tpId ?: 0
                )
            }
        }

        Log.e("ValidatorEKL", "usersSDBPTT otdelId: ${usersSDBPTT?.otdelId}")
        Log.e("ValidatorEKL", "documentUser otdelId: ${documentUser?.otdelId}")

        val optionMsg = StringBuilder()

        val ids: MutableList<Int> = ArrayList()
        if (tovarGroupClientSDB != null) {
            for (item in tovarGroupClientSDB) {
                ids.add(item.tovarGrpId)
            }
        }
        Log.e("ValidatorEKL", "tovarGroupClientSDB id: $ids")

        val tovarGroupSDB = RoomManager.SQL_DB.tovarGroupDao().getAllByIds(ids)

        val test: TovarGroupSDB? = tovarGroupSDB.stream()
            .filter(Predicate<TovarGroupSDB> { item: TovarGroupSDB ->
                item.id == (usersSDBPTT?.otdelId ?: 0)
            })
            .findFirst().orElse(null)
        Log.e("test", "test: $test")

        if (addressSDB != null) {
            if (test == null && (addressSDB.kolKass > 5 || addressSDB.kolKass == 0)
            ) {
                if (documentUser != null) {
                    if (documentUser.reportDate20 != null && documentUser.reportDate20.time >= wpDataDB.dt
                            .time
                    ) {
                        control.result = false
                        if (usersSDBPTT != null) {
                            optionMsg.append(", но ").append("ПТТ работает в отделе ")
                                .append(
                                    RoomManager.SQL_DB.tovarGroupDao()
                                        .getById(usersSDBPTT.otdelId).nm
                                )
                                .append(" и не может подписывать ЭКЛ для: ")
                                .append(TG.getNmFromList(tovarGroupSDB))
                                .append(" (но исполнитель не провел свой 20-й отчет и эту блокировку пропускаем)")
                        }
                    } else {
                        control.result = true
                        if (usersSDBPTT != null) {
                            optionMsg.append(", но ").append("ПТТ работает в отделе ")
                                .append(
                                    RoomManager.SQL_DB.tovarGroupDao()
                                        .getById(usersSDBPTT.otdelId).nm
                                )
                                .append(" и не может подписывать ЭКЛ для: ")
                                .append(TG.getNmFromList(tovarGroupSDB))
                                .append(" (для магазина в котором более 5 касс и исполнитель провел 20-й отчет)")
                        }
                    }
                }
            } else if (test == null && (addressSDB.kolKass in 1..5)
            ) {
                if (documentUser?.reportDate40 != null && documentUser.reportDate40?.time!! >= wpDataDB.dt
                        .time
                ) {
                    control.result = false
                    if (usersSDBPTT != null) {
                        optionMsg.append(", но ").append("ПТТ работает в отделе ")
                            .append(
                                RoomManager.SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm
                            )
                            .append(" и не может подписывать ЭКЛ для: ")
                            .append(TG.getNmFromList(tovarGroupSDB))
                            .append(" (но исполнитель не провел свой 40-й отчет и эту блокировку пропускаем)")
                    }
                } else {
                    control.result = false
                    if (usersSDBPTT != null) {
                        if (addressSDB != null) {
                            optionMsg.append(", но ").append("ПТТ работает в отделе ")
                                .append(
                                    RoomManager.SQL_DB.tovarGroupDao()
                                        .getById(usersSDBPTT.otdelId).nm
                                )
                                .append(" и не может подписывать ЭКЛ для: ")
                                .append(TG.getNmFromList(tovarGroupSDB))
                                .append(" (но в данном магазине ")
                                .append(addressSDB.kolKass).append(" касс и это допустимо)")
                        }
                    }
                }
            }
        }
        control.message = optionMsg.toString()
        return control
    }


}